package ru.xgodness.e2e;

import lombok.extern.java.Log;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import ru.xgodness.exception.dto.ErrorMessages;
import ru.xgodness.persistence.DatabaseManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.*;
import static ru.xgodness.e2e.E2ETestUtils.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = "application.questionnaire.dir-path=src/test/resources/questionnaires")
@Log
public class BindRequestTest {

    @Autowired
    private DatabaseManager databaseManager;

    @Autowired
    private TestRestTemplate restTemplate;
    private static final Map<String, String> accessTokenMap = new HashMap<>();
    private TestContext context;

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

    @BeforeAll
    public void setUp() throws Exception {
        context = new TestContext(accessTokenMap, restTemplate);

        for (var username : usernames) {
            registerUser(context, username, password);
            loginUser(context, username, password);
        }
    }

    @AfterAll
    public void truncate() {
        databaseManager.executeQuery(TRUNCATE_QUERY);
    }


    @Order(1)
    @Test
    public void testCreateBindRequest_nonexistentSpecialist_404() throws Exception {
        ResponseEntity<String> entity = requestBind(context, clientA, nonexistentUsername);
        assertEquals(NOT_FOUND, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Пользователь с именем %s не найден".formatted(nonexistentUsername)), errorMessages.getMessages());
    }

    @Order(2)
    @Test
    public void testCreateBindRequests_validSpecialistName_2xx() {
        ResponseEntity<String> entity;

        for (var client : List.of(clientA, clientB)) {
            entity = requestBind(context, client, specialistA);
            assertEquals(NO_CONTENT, entity.getStatusCode());
            assertNull(entity.getBody());
        }
    }

    @Order(3)
    @Test
    public void testCreateBindRequests_duplicateRequest_409() throws Exception {
        ResponseEntity<String> entity = requestBind(context, clientA, specialistA);
        assertEquals(CONFLICT, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Вы уже отправляли заявку, чтобы стать клиентом специалиста %s".formatted(specialistA)), errorMessages.getMessages());
    }

    @Order(4)
    @Test
    public void testGetPendingAndApprovedBinds_200() throws Exception {
        ResponseEntity<String> entity = getPendingBinds(context, specialistA);
        assertEquals(OK, entity.getStatusCode());

        ClientUsernameList pending = mapStringToClientUsernameList(entity.getBody());
        assertEquals(List.of(clientA, clientB), pending.getClients());

        entity = getApprovedBinds(context, specialistA);


        assertEquals(OK, entity.getStatusCode());

        ClientUsernameList approved = mapStringToClientUsernameList(entity.getBody());
        assertTrue(approved.getClients().isEmpty());
    }

    @Order(5)
    @Test
    public void testApprovePendingBind_nonexistentClient_404() throws Exception {
        ResponseEntity<String> entity = approveBindRequest(context, specialistA, nonexistentUsername);
        assertEquals(NOT_FOUND, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Пользователь %s не оставлял заявку, чтобы стать вашим клиентом".formatted(nonexistentUsername)), errorMessages.getMessages());
    }

    @Order(6)
    @Test
    public void testApprovePendingBind_validClient_204() {
        ResponseEntity<String> entity = approveBindRequest(context, specialistA, clientA);
        assertEquals(OK, entity.getStatusCode());

        assertNull(entity.getBody());
    }

    @Order(7)
    @Test
    public void testGetAllBinds_200() throws Exception {
        ResponseEntity<String> entity = getAllBinds(context, specialistA);
        assertEquals(OK, entity.getStatusCode());

        BindList bindList = mapStringToBindList(entity.getBody());
        assertEquals(List.of(clientA), bindList.getApproved().getClients());
        assertEquals(List.of(clientB), bindList.getPending().getClients());
    }

    @Order(8)
    @Test
    public void testDiscardPendingBind_nonexistentClient_404() throws Exception {
        ResponseEntity<String> entity = discardBindRequest(context, specialistA, nonexistentUsername);
        assertEquals(NOT_FOUND, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Пользователь %s не оставлял заявку, чтобы стать вашим клиентом".formatted(nonexistentUsername)), errorMessages.getMessages());
    }

    @Order(9)
    @Test
    public void testDiscardPendingBind_validClient_204() {
        ResponseEntity<String> entity = discardBindRequest(context, specialistA, clientB);
        assertEquals(OK, entity.getStatusCode());
        assertNull(entity.getBody());
    }

    @Order(10)
    @Test
    public void testBindsState_200() throws Exception {
        ResponseEntity<String> entity = getPendingBinds(context, specialistA);
        assertEquals(OK, entity.getStatusCode());

        ClientUsernameList pending = mapStringToClientUsernameList(entity.getBody());
        assertTrue(pending.getClients().isEmpty());

        entity = getApprovedBinds(context, specialistA);
        assertEquals(OK, entity.getStatusCode());

        ClientUsernameList approved = mapStringToClientUsernameList(entity.getBody());
        assertEquals(List.of(clientA), approved.getClients());
    }

}

