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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;
import static ru.xgodness.e2e.TestUtils.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Log
public class BindRequestsTest {

    @Autowired
    private DatabaseManager databaseManager;

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String nonexistentUsername = "nonexistent";
    private static final String password = "password123";

    private static final String specialistA = "specialistA";
    private static final String clientA = "clientA";
    private static final String clientB = "clientB";

    private static final List<String> usernames = List.of(
            specialistA,
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
    }

    @AfterAll
    public void truncate() {
        databaseManager.executeQuery("""
                TRUNCATE TABLE app_user, assignment, client_specialist RESTART IDENTITY CASCADE;
                """);
    }


    @Order(1)
    @Test
    public void testCreateBindRequest_nonexistentSpecialist_404() throws Exception {
        ResponseEntity<String> entity = requestBind(clientA, nonexistentUsername);
        log.info(entity.toString());

        assertEquals(NOT_FOUND, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Пользователь с именем %s не найден".formatted(nonexistentUsername)), errorMessages.getMessages());
    }

    @Order(2)
    @Test
    public void testCreateBindRequests_validSpecialistName_2xx() {
        ResponseEntity<String> entity;

        for (var client : List.of(clientA, clientB)) {
            entity = requestBind(client, specialistA);

            log.info(entity.toString());

            assertEquals(NO_CONTENT, entity.getStatusCode());
            assertNull(entity.getBody());
        }
    }

    @Order(3)
    @Test
    public void testCreateBindRequests_duplicateRequest_409() throws Exception {
        ResponseEntity<String> entity = requestBind(clientA, specialistA);

        log.info(entity.toString());

        assertEquals(CONFLICT, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Вы уже отправляли заявку, чтобы стать клиентом специалиста %s".formatted(specialistA)), errorMessages.getMessages());
    }

    @Order(4)
    @Test
    public void testGetPendingAndApprovedBinds_200() throws Exception {
        ResponseEntity<String> entity = getPendingBinds(specialistA);
        log.info(entity.toString());

        assertEquals(OK, entity.getStatusCode());

        ClientUsernameList pending = mapStringToClientUsernameList(entity.getBody());
        assertEquals(List.of(clientA, clientB), pending.getClients());

        entity = getApprovedBinds(specialistA);
        log.info(entity.toString());

        assertEquals(OK, entity.getStatusCode());

        ClientUsernameList approved = mapStringToClientUsernameList(entity.getBody());
        assertTrue(approved.getClients().isEmpty());
    }

    @Order(5)
    @Test
    public void testApprovePendingBind_nonexistentClient_404() throws Exception {
        ResponseEntity<String> entity = approveBindRequest(specialistA, nonexistentUsername);
        log.info(entity.toString());

        assertEquals(NOT_FOUND, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Пользователь %s не оставлял заявку, чтобы стать вашим клиентом".formatted(nonexistentUsername)), errorMessages.getMessages());
    }

    @Order(6)
    @Test
    public void testApprovePendingBind_validClient_204() {
        ResponseEntity<String> entity = approveBindRequest(specialistA, clientA);
        log.info(entity.toString());

        assertEquals(OK, entity.getStatusCode());

        assertNull(entity.getBody());
    }

    @Order(7)
    @Test
    public void testGetAllBinds_200() throws Exception {
        ResponseEntity<String> entity = getAllBinds(specialistA);
        log.info(entity.toString());

        assertEquals(OK, entity.getStatusCode());

        BindList bindList = mapStringToBindList(entity.getBody());
        assertEquals(List.of(clientA), bindList.getApproved().getClients());
        assertEquals(List.of(clientB), bindList.getPending().getClients());
    }

    @Order(8)
    @Test
    public void testDiscardPendingBind_nonexistentClient_404() throws Exception {
        ResponseEntity<String> entity = discardBindRequest(specialistA, nonexistentUsername);
        log.info(entity.toString());

        assertEquals(NOT_FOUND, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Пользователь %s не оставлял заявку, чтобы стать вашим клиентом".formatted(nonexistentUsername)), errorMessages.getMessages());
    }

    @Order(9)
    @Test
    public void testDiscardPendingBind_validClient_204() {
        ResponseEntity<String> entity = discardBindRequest(specialistA, clientB);
        log.info(entity.toString());

        assertEquals(OK, entity.getStatusCode());
        assertNull(entity.getBody());
    }

    @Order(10)
    @Test
    public void testBindsState_200() throws Exception {
        ResponseEntity<String> entity = getPendingBinds(specialistA);
        log.info(entity.toString());

        assertEquals(OK, entity.getStatusCode());

        ClientUsernameList pending = mapStringToClientUsernameList(entity.getBody());
        assertTrue(pending.getClients().isEmpty());

        entity = getApprovedBinds(specialistA);
        log.info(entity.toString());

        assertEquals(OK, entity.getStatusCode());

        ClientUsernameList approved = mapStringToClientUsernameList(entity.getBody());
        assertEquals(List.of(clientA), approved.getClients());
    }

    private void registerUser(String username) {
        ResponseEntity<String> entity = restTemplate.postForEntity(
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

    private void loginUser(String username) throws Exception {
        ResponseEntity<String> entity = restTemplate.postForEntity(
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

    private ResponseEntity<String> requestBind(String client, String specialist) {
        setHeaders(client);
        return restTemplate.postForEntity(
                REQUEST_BIND_PATH + "/" + specialist,
                null,
                String.class
        );
    }

    private ResponseEntity<String> approveBindRequest(String specialist, String client) {
        setHeaders(specialist);
        return restTemplate.postForEntity(
                APPROVE_BIND_PATH + "/" + client,
                null,
                String.class
        );
    }

    private ResponseEntity<String> discardBindRequest(String specialist, String client) {
        setHeaders(specialist);
        return restTemplate.postForEntity(
                DISCARD_BIND_PATH + "/" + client,
                null,
                String.class
        );
    }

    private ResponseEntity<String> getPendingBinds(String specialist) {
        setHeaders(specialist);
        return restTemplate.getForEntity(
                GET_PENDING_BINDS_PATH,
                String.class
        );
    }

    private ResponseEntity<String> getApprovedBinds(String specialist) {
        setHeaders(specialist);
        return restTemplate.getForEntity(
                GET_APPROVED_BINDS_PATH,
                String.class
        );
    }

    private ResponseEntity<String> getAllBinds(String specialist) {
        setHeaders(specialist);
        return restTemplate.getForEntity(
                GET_ALL_BINDS_PATH,
                String.class
        );
    }

}

