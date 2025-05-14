package ru.xgodness.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import ru.xgodness.exception.dto.ErrorMessages;
import ru.xgodness.persistence.DatabaseManager;
import ru.xgodness.persistence.DynamicMigrationDDLBuilder;
import ru.xgodness.persistence.QuestionnaireRegistry;
import ru.xgodness.persistence.StaticMigrationDDLProvider;

import java.util.List;
import java.util.Map;

@Log
@Component
public class E2ETestUtils {
    protected static final String REGISTER_PATH = "/auth/register";
    protected static final String LOGIN_PATH = "/auth/login";
    protected static final String TOKEN_ACCESS_PATH = "/auth/token/access";
    protected static final String TOKEN_REFRESH_PATH = "/auth/token/refresh";
    protected static final String REQUEST_BIND_PATH = "/bind/%s/request";
    protected static final String APPROVE_BIND_PATH = "/bind/%s/approve";
    protected static final String DISCARD_BIND_PATH = "/bind/%s/discard";
    protected static final String GET_PENDING_BINDS_PATH = "/bind/get/pending";
    protected static final String GET_APPROVED_BINDS_PATH = "/bind/get/approved";
    protected static final String GET_ALL_BINDS_PATH = "/bind/get/all";
    protected static final String CREATE_ASSIGNMENT_PATH = "/assignment/create";
    protected static final String DELETE_ASSIGNMENT_PATH = "/assignment/delete";
    protected static final String LIST_ASSIGNMENTS_PATH = "/assignment/list";
    protected static final String GET_QUESTIONNAIRE_FORM_PATH = "/questionnaire";
    protected static final String LIST_QUESTIONNAIRES_PATH = "/questionnaire/list";
    protected static final String GET_QUESTIONNAIRE_HISTORY_PATH = "questionnaire/history/%d";
    protected static final String GET_QUESTIONNAIRE_COMPLETION_STATE_PATH = "questionnaire/%d/state";
    protected static final String UPDATE_QUESTIONNAIRE_COMPLETION_STATE_PATH = "questionnaire/%d/update";
    protected static final String COMPLETE_QUESTIONNAIRE_PATH = "questionnaire/%d/complete";

    protected static final String TRUNCATE_QUERY = "TRUNCATE TABLE app_user, assignment, client_specialist RESTART IDENTITY CASCADE;";

    protected static final String password = "password123";
    protected static final String nonexistentUsername = "nonexistent";
    protected static final long nonexistentQuestionnaireId = 9999999L;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final QuestionnaireRegistry registry;
    private final DatabaseManager databaseManager;
    private final StaticMigrationDDLProvider staticMigrationDDLProvider;
    private final DynamicMigrationDDLBuilder dynamicMigrationDDLBuilder;

    public E2ETestUtils(QuestionnaireRegistry registry,
                        DatabaseManager databaseManager,
                        StaticMigrationDDLProvider staticMigrationDDLProvider,
                        DynamicMigrationDDLBuilder dynamicMigrationDDLBuilder) {
        this.registry = registry;
        this.databaseManager = databaseManager;
        this.staticMigrationDDLProvider = staticMigrationDDLProvider;
        this.dynamicMigrationDDLBuilder = dynamicMigrationDDLBuilder;
    }

    @PreDestroy
    void dropAll() {
        databaseManager.executeQuery(staticMigrationDDLProvider.getDropAllQuery());
        for (var questionnaire : registry.getAllQuestionnaires()) {
            databaseManager.executeQuery(dynamicMigrationDDLBuilder.buildDropTableForQuestionnaire(questionnaire));
        }
    }

    protected static ErrorMessages mapStringToErrorMessages(String string) throws Exception {
        return objectMapper.readValue(string, ErrorMessages.class);
    }

    protected static JwtResponse mapStringToJwtResponse(String string) throws Exception {
        return objectMapper.readValue(string, JwtResponse.class);
    }

    protected static ClientUsernameList mapStringToClientUsernameList(String string) throws Exception {
        return objectMapper.readValue(string, ClientUsernameList.class);
    }

    protected static BindList mapStringToBindList(String string) throws Exception {
        return objectMapper.readValue(string, BindList.class);
    }

    protected static QuestionnaireIdentifierList mapStringToQuestionnaireIdentifierList(String string) throws Exception {
        return objectMapper.readValue(string, QuestionnaireIdentifierList.class);
    }

    protected static void setHeaders(TestContext context, String username) {
        context.restTemplate.getRestTemplate().setInterceptors(
                List.of(
                        (request, body, execution) -> {
                            request.getHeaders()
                                    .add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

                            if (context.accessTokenMap.get(username) != null)
                                request.getHeaders()
                                        .add("Authorization", "Bearer " + context.accessTokenMap.get(username));

                            return execution.execute(request, body);
                        }
                )
        );
    }

    protected static void registerUser(TestContext context, String username) {
        ResponseEntity<String> entity = context.restTemplate.postForEntity(
                REGISTER_PATH,
                new RegisterRequest(
                        username,
                        password,
                        username.startsWith("specialist") ? "SPECIALIST" : "CLIENT"
                ),
                String.class
        );

        log.info(entity.toString());

        assert entity.getStatusCode().is2xxSuccessful();
    }

    protected static void loginUser(TestContext context, String username) throws Exception {
        ResponseEntity<String> entity = context.restTemplate.postForEntity(
                LOGIN_PATH,
                new LoginRequest(
                        username,
                        password
                ),
                String.class
        );

        assert entity.getStatusCode().is2xxSuccessful();

        JwtResponse jwtResponse = mapStringToJwtResponse(entity.getBody());
        String accessToken = jwtResponse.getAccessToken();

        assert accessToken != null;

        context.accessTokenMap.put(username, accessToken);
    }

    protected static ResponseEntity<String> requestBind(TestContext context, String clientUsername, String specialistUsername) {
        setHeaders(context, clientUsername);
        var result = context.restTemplate.postForEntity(
                REQUEST_BIND_PATH.formatted(specialistUsername),
                null,
                String.class
        );

        log.info(result.toString());
        return result;
    }

    protected static ResponseEntity<String> approveBindRequest(TestContext context, String specialistUsername, String clientUsername) {
        setHeaders(context, specialistUsername);
        var result = context.restTemplate.postForEntity(
                APPROVE_BIND_PATH.formatted(clientUsername),
                null,
                String.class
        );

        log.info(result.toString());
        return result;
    }

    protected static ResponseEntity<String> discardBindRequest(TestContext context, String specialistUsername, String clientUsername) {
        setHeaders(context, specialistUsername);
        var result = context.restTemplate.postForEntity(
                DISCARD_BIND_PATH.formatted(clientUsername),
                null,
                String.class
        );

        log.info(result.toString());
        return result;
    }

    protected static ResponseEntity<String> createAssignment(TestContext context, String specialistUsername, String clientUsername, long questionnaireId) {
        setHeaders(context, specialistUsername);
        var result = context.restTemplate.postForEntity(
                CREATE_ASSIGNMENT_PATH,
                new AssignmentRequest(
                        clientUsername,
                        questionnaireId
                ),
                String.class
        );

        log.info(result.toString());
        return result;
    }

    protected static ResponseEntity<String> getPendingBinds(TestContext context, String specialistUsername) {
        setHeaders(context, specialistUsername);
        var result = context.restTemplate.getForEntity(
                GET_PENDING_BINDS_PATH,
                String.class
        );

        log.info(result.toString());
        return result;
    }

    protected static ResponseEntity<String> getApprovedBinds(TestContext context, String specialistUsername) {
        setHeaders(context, specialistUsername);
        var result = context.restTemplate.getForEntity(
                GET_APPROVED_BINDS_PATH,
                String.class
        );

        log.info(result.toString());
        return result;
    }

    protected static ResponseEntity<String> getAllBinds(TestContext context, String specialistUsername) {
        setHeaders(context, specialistUsername);
        var result = context.restTemplate.getForEntity(
                GET_ALL_BINDS_PATH,
                String.class
        );

        log.info(result.toString());
        return result;
    }

    protected static ResponseEntity<String> deleteAssignment(TestContext context, String specialistUsername, String clientUsername, long questionnaireId) {
        setHeaders(context, specialistUsername);
        var result = context.restTemplate.postForEntity(
                DELETE_ASSIGNMENT_PATH,
                new AssignmentRequest(
                        clientUsername,
                        questionnaireId
                ),
                String.class
        );

        log.info(result.toString());
        return result;
    }

    protected static ResponseEntity<String> listAssignmentsClient(TestContext context, String clientUsername) {
        setHeaders(context, clientUsername);
        var result = context.restTemplate.getForEntity(
                LIST_ASSIGNMENTS_PATH,
                String.class
        );

        log.info(result.toString());
        return result;
    }

    protected static ResponseEntity<String> listAssignmentsSpecialist(TestContext context, String specialistUsername, String clientUsername) {
        setHeaders(context, specialistUsername);
        var result = context.restTemplate.getForEntity(
                LIST_ASSIGNMENTS_PATH + "/" + clientUsername,
                String.class
        );

        log.info(result.toString());
        return result;
    }

    @AllArgsConstructor
    protected static class TestContext {
        private final Map<String, String> accessTokenMap;
        private final TestRestTemplate restTemplate;
    }

    @AllArgsConstructor
    @Getter
    protected static class RegisterRequest {
        private String username;
        private String password;
        private String role;
    }

    @AllArgsConstructor
    @Getter
    protected static class LoginRequest {
        private String username;
        private String password;
    }

    @AllArgsConstructor
    @Getter
    protected static class RefreshJwtRequest {
        private String refreshToken;
    }

    @Setter
    @Getter
    protected static class JwtResponse {
        private String type;
        private String accessToken;
        private String refreshToken;
    }

    @Getter
    @Setter
    protected static class ClientUsernameList {
        private List<String> clients;
    }

    @Getter
    @Setter
    protected static class BindList {
        private ClientUsernameList pending;
        private ClientUsernameList approved;
    }

    @AllArgsConstructor
    @Getter
    protected static class AssignmentRequest {
        private String clientUsername;
        private long questionnaireId;
    }

    @Getter
    @Setter
    protected static class QuestionnaireIdentifier {
        private long id;
        private String name;
        private String displayName;
    }

    @Getter
    @Setter
    protected static class QuestionnaireIdentifierList {
        private List<QuestionnaireIdentifier> questionnaires;
    }

}
