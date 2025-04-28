package ru.xgodness.e2e;

import lombok.extern.java.Log;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import ru.xgodness.exception.dto.ErrorMessages;
import ru.xgodness.persistence.DatabaseManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.http.HttpStatus.*;
import static ru.xgodness.e2e.TestUtils.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Log
public class AssignmentTest {

    @Autowired
    private DatabaseManager databaseManager;

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String nonexistentUsername = "nonexistent";
    private static final long nonexistentQuestionnaireId = 9999999L;
    private static final String password = "password123";

    private static final String specialistA = "specialistA";
    private static final String specialistB = "specialistB";
    private static final String clientA = "clientA";
    private static final String clientB = "clientB";

    private static final List<String> usernames = List.of(
            specialistA,
            specialistB,
            clientA,
            clientB
    );

    private static final Map<String, String> accessTokenMap = new HashMap<>();

    @BeforeAll
    public void setUp() throws Exception {
        for (var username : usernames) {
            registerUser(username);
            loginUser(username);
        }

        requestBind(clientA, specialistA);
        requestBind(clientB, specialistB);

        approveBindRequest(specialistA, clientA);
        approveBindRequest(specialistB, clientB);
    }

    @AfterAll
    public void truncate() {
        databaseManager.executeQuery("""
                TRUNCATE TABLE app_user, assignment, client_specialist RESTART IDENTITY CASCADE;
                """);
    }


    @Order(1)
    @Test
    public void testCreateAssignment_noClientUsername_422() throws Exception {
        ResponseEntity<String> entity = createAssignment(specialistA, null, 1);
        log.info(entity.toString());

        assertEquals(UNPROCESSABLE_ENTITY, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Не указано имя пользователя клиента"), errorMessages.getMessages());
    }

    @Order(2)
    @Test
    public void testCreateAssignment_nonexistentClient_404() throws Exception {
        ResponseEntity<String> entity = createAssignment(specialistA, nonexistentUsername, 1);
        log.info(entity.toString());

        assertEquals(NOT_FOUND, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Пользователь %s не является вашим клиентом, либо вы еще не одобрили его запрос".formatted(nonexistentUsername)), errorMessages.getMessages());
    }

    @Order(3)
    @Test
    public void testCreateAssignment_nonexistentQuestionnaire_404() throws Exception {
        ResponseEntity<String> entity = createAssignment(specialistA, clientA, nonexistentQuestionnaireId);
        log.info(entity.toString());

        assertEquals(NOT_FOUND, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Опросник с идентификатором %d не найден".formatted(nonexistentQuestionnaireId)), errorMessages.getMessages());
    }

    @Order(4)
    @Test
    public void testCreateAssignment_validData_204() {
        ResponseEntity<String> entity = createAssignment(specialistA, clientA, 1);
        log.info(entity.toString());

        assertEquals(NO_CONTENT, entity.getStatusCode());
        assertNull(entity.getBody());

        entity = createAssignment(specialistB, clientB, 1);
        log.info(entity.toString());

        assertEquals(NO_CONTENT, entity.getStatusCode());
        assertNull(entity.getBody());

        entity = createAssignment(specialistB, clientB, 2);
        log.info(entity.toString());

        assertEquals(NO_CONTENT, entity.getStatusCode());
        assertNull(entity.getBody());
    }

    @Order(5)
    @Test
    public void testClientListAssignments_200() throws Exception {
        ResponseEntity<String> entity = listAssignmentsClient(clientA);
        log.info(entity.toString());

        assertEquals(OK, entity.getStatusCode());

        QuestionnaireIdentifierList questionnaireIdentifierList = mapStringToQuestionnaireIdentifierList(entity.getBody());
        assertEquals(1, questionnaireIdentifierList.getQuestionnaires().size());

        entity = listAssignmentsClient(clientB);
        log.info(entity.toString());

        assertEquals(OK, entity.getStatusCode());

        questionnaireIdentifierList = mapStringToQuestionnaireIdentifierList(entity.getBody());
        assertEquals(2, questionnaireIdentifierList.getQuestionnaires().size());
    }

    @Order(6)
    @Test
    public void testDeleteAssignment_noClientUsername_422() throws Exception {
        ResponseEntity<String> entity = createAssignment(specialistB, null, 2);
        log.info(entity.toString());

        assertEquals(UNPROCESSABLE_ENTITY, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Не указано имя пользователя клиента"), errorMessages.getMessages());
    }

    @Order(7)
    @Test
    public void testDeleteAssignment_nonexistentClient_404() throws Exception {
        ResponseEntity<String> entity = createAssignment(specialistB, nonexistentUsername, 2);
        log.info(entity.toString());

        assertEquals(NOT_FOUND, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Пользователь %s не является вашим клиентом, либо вы еще не одобрили его запрос".formatted(nonexistentUsername)), errorMessages.getMessages());
    }

    @Order(8)
    @Test
    public void testDeleteAssignment_nonexistentQuestionnaire_404() throws Exception {
        ResponseEntity<String> entity = deleteAssignment(specialistB, clientB, nonexistentQuestionnaireId);
        log.info(entity.toString());

        assertEquals(NOT_FOUND, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Вы не назначали клиенту %s прохождение этого опросника".formatted(clientB)), errorMessages.getMessages());
    }

    @Order(9)
    @Test
    public void testDeleteAssignment_validData_204() {
        ResponseEntity<String> entity = deleteAssignment(specialistB, clientB, 2);
        log.info(entity.toString());

        assertEquals(NO_CONTENT, entity.getStatusCode());
        assertNull(entity.getBody());
    }

    @Order(10)
    @Test
    public void testSpecialistListAssignments_clientWithNoBind_404() throws Exception {
        ResponseEntity<String> entity = listAssignmentsSpecialist(specialistA, clientB);
        log.info(entity.toString());

        assertEquals(NOT_FOUND, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Пользователь %s не является вашим клиентом, либо вы еще не одобрили его запрос".formatted(clientB)), errorMessages.getMessages());
    }

    @Order(11)
    @Test
    public void testSpecialistListAssignments_validData_200() throws Exception {
        ResponseEntity<String> entity = listAssignmentsSpecialist(specialistA, clientA);
        log.info(entity.toString());

        assertEquals(OK, entity.getStatusCode());

        QuestionnaireIdentifierList questionnaireIdentifierList = mapStringToQuestionnaireIdentifierList(entity.getBody());
        assertEquals(1, questionnaireIdentifierList.getQuestionnaires().size());

        entity = listAssignmentsSpecialist(specialistB, clientB);
        log.info(entity.toString());

        assertEquals(OK, entity.getStatusCode());

        questionnaireIdentifierList = mapStringToQuestionnaireIdentifierList(entity.getBody());
        assertEquals(1, questionnaireIdentifierList.getQuestionnaires().size());
    }


    private void registerUser(String username) {
        ResponseEntity<String> entity = restTemplate.postForEntity(
                REGISTER_PATH,
                new TestUtils.RegisterRequest(
                        username,
                        password,
                        username.startsWith("specialist") ? "SPECIALIST" : "CLIENT"
                ),
                String.class
        );

        log.info(entity.toString());

        assert entity.getStatusCode().is2xxSuccessful();
    }

    private void loginUser(String username) throws Exception {
        ResponseEntity<String> entity = restTemplate.postForEntity(
                LOGIN_PATH,
                new TestUtils.LoginRequest(
                        username,
                        password
                ),
                String.class
        );

        assert entity.getStatusCode().is2xxSuccessful();

        TestUtils.JwtResponse jwtResponse = mapStringToJwtResponse(entity.getBody());
        String accessToken = jwtResponse.getAccessToken();

        assert accessToken != null;

        accessTokenMap.put(username, accessToken);
    }

    private void setHeaders(String username) {
        restTemplate.getRestTemplate().setInterceptors(
                List.of(
                        (request, body, execution) -> {
                            request.getHeaders()
                                    .add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
                            request.getHeaders()
                                    .add("Authorization", "Bearer " + accessTokenMap.get(username));

                            return execution.execute(request, body);
                        }
                )
        );
    }

    private void requestBind(String client, String specialist) {
        setHeaders(client);
        var result = restTemplate.postForEntity(
                REQUEST_BIND_PATH + "/" + specialist,
                null,
                String.class
        );

        log.info(result.toString());
    }

    private void approveBindRequest(String specialist, String client) {
        setHeaders(specialist);
        var result = restTemplate.postForEntity(
                APPROVE_BIND_PATH + "/" + client,
                null,
                String.class
        );

        log.info(result.toString());
    }

    private ResponseEntity<String> createAssignment(String specialistUsername, String clientUsername, long questionnaireId) {
        setHeaders(specialistUsername);
        return restTemplate.postForEntity(
                CREATE_ASSIGNMENT_PATH,
                new AssignmentRequest(
                        clientUsername,
                        questionnaireId
                ),
                String.class
        );
    }

    private ResponseEntity<String> deleteAssignment(String specialistUsername, String clientUsername, long questionnaireId) {
        setHeaders(specialistUsername);
        return restTemplate.postForEntity(
                DELETE_ASSIGNMENT_PATH,
                new AssignmentRequest(
                        clientUsername,
                        questionnaireId
                ),
                String.class
        );
    }

    private ResponseEntity<String> listAssignmentsClient(String clientUsername) {
        setHeaders(clientUsername);
        return restTemplate.getForEntity(
                LIST_ASSIGNMENTS_PATH,
                String.class
        );
    }

    private ResponseEntity<String> listAssignmentsSpecialist(String specialistUsername, String clientUsername) {
        setHeaders(specialistUsername);
        return restTemplate.getForEntity(
                LIST_ASSIGNMENTS_PATH + "/" + clientUsername,
                String.class
        );
    }

}
