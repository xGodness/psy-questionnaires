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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.http.HttpStatus.*;
import static ru.xgodness.e2e.E2ETestUtils.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = "application.questionnaire.dir-path=src/test/resources/questionnaires")
@Log
public class AssignmentTest {

    @Autowired
    private DatabaseManager databaseManager;

    @Autowired
    private TestRestTemplate restTemplate;
    private final Map<String, String> accessTokenMap = new HashMap<>();
    private TestContext context;

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

    @BeforeAll
    public void setUp() throws Exception {
        context = new TestContext(accessTokenMap, restTemplate);

        for (var username : usernames) {
            registerUser(context, username);
            loginUser(context, username);
        }

        requestBind(context, clientA, specialistA);
        requestBind(context, clientB, specialistB);

        approveBindRequest(context, specialistA, clientA);
        approveBindRequest(context, specialistB, clientB);
    }

    @AfterAll
    public void truncate() {
        databaseManager.executeQuery(TRUNCATE_QUERY);
    }


    @Order(1)
    @Test
    public void testCreateAssignment_noClientUsername_422() throws Exception {
        ResponseEntity<String> entity = createAssignment(context, specialistA, null, 1);
        assertEquals(UNPROCESSABLE_ENTITY, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Не указано имя пользователя клиента"), errorMessages.getMessages());
    }

    @Order(2)
    @Test
    public void testCreateAssignment_nonexistentClient_404() throws Exception {
        ResponseEntity<String> entity = createAssignment(context, specialistA, nonexistentUsername, 1);
        assertEquals(NOT_FOUND, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Пользователь %s не является вашим клиентом, либо вы еще не одобрили его запрос".formatted(nonexistentUsername)), errorMessages.getMessages());
    }

    @Order(3)
    @Test
    public void testCreateAssignment_nonexistentQuestionnaire_404() throws Exception {
        ResponseEntity<String> entity = createAssignment(context, specialistA, clientA, nonexistentQuestionnaireId);
        assertEquals(NOT_FOUND, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Опросник с идентификатором %d не найден".formatted(nonexistentQuestionnaireId)), errorMessages.getMessages());
    }

    @Order(4)
    @Test
    public void testCreateAssignment_validData_204() {
        ResponseEntity<String> entity = createAssignment(context, specialistA, clientA, 1);
        assertEquals(NO_CONTENT, entity.getStatusCode());
        assertNull(entity.getBody());

        entity = createAssignment(context, specialistB, clientB, 1);
        assertEquals(NO_CONTENT, entity.getStatusCode());
        assertNull(entity.getBody());

        entity = createAssignment(context, specialistB, clientB, 2);
        assertEquals(NO_CONTENT, entity.getStatusCode());
        assertNull(entity.getBody());
    }

    @Order(5)
    @Test
    public void testClientListAssignments_200() throws Exception {
        ResponseEntity<String> entity = listAssignmentsClient(context, clientA);
        assertEquals(OK, entity.getStatusCode());

        QuestionnaireIdentifierList questionnaireIdentifierList = mapStringToQuestionnaireIdentifierList(entity.getBody());
        assertEquals(1, questionnaireIdentifierList.getQuestionnaires().size());

        entity = listAssignmentsClient(context, clientB);
        assertEquals(OK, entity.getStatusCode());

        questionnaireIdentifierList = mapStringToQuestionnaireIdentifierList(entity.getBody());
        assertEquals(2, questionnaireIdentifierList.getQuestionnaires().size());
    }

    @Order(6)
    @Test
    public void testDeleteAssignment_noClientUsername_422() throws Exception {
        ResponseEntity<String> entity = createAssignment(context, specialistB, null, 2);
        assertEquals(UNPROCESSABLE_ENTITY, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Не указано имя пользователя клиента"), errorMessages.getMessages());
    }

    @Order(7)
    @Test
    public void testDeleteAssignment_nonexistentClient_404() throws Exception {
        ResponseEntity<String> entity = createAssignment(context, specialistB, nonexistentUsername, 2);
        assertEquals(NOT_FOUND, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Пользователь %s не является вашим клиентом, либо вы еще не одобрили его запрос".formatted(nonexistentUsername)), errorMessages.getMessages());
    }

    @Order(8)
    @Test
    public void testDeleteAssignment_nonexistentQuestionnaire_404() throws Exception {
        ResponseEntity<String> entity = deleteAssignment(context, specialistB, clientB, nonexistentQuestionnaireId);
        assertEquals(NOT_FOUND, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Клиенту %s не было назначено заполнение этого опросника".formatted(clientB)), errorMessages.getMessages());
    }

    @Order(9)
    @Test
    public void testDeleteAssignment_validData_204() {
        ResponseEntity<String> entity = deleteAssignment(context, specialistB, clientB, 2);
        assertEquals(NO_CONTENT, entity.getStatusCode());
        assertNull(entity.getBody());
    }

    @Order(10)
    @Test
    public void testSpecialistListAssignments_clientWithNoBind_404() throws Exception {
        ResponseEntity<String> entity = listAssignmentsSpecialist(context, specialistA, clientB);
        assertEquals(NOT_FOUND, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Пользователь %s не является вашим клиентом, либо вы еще не одобрили его запрос".formatted(clientB)), errorMessages.getMessages());
    }

    @Order(11)
    @Test
    public void testSpecialistListAssignments_validData_200() throws Exception {
        ResponseEntity<String> entity = listAssignmentsSpecialist(context, specialistA, clientA);
        assertEquals(OK, entity.getStatusCode());

        QuestionnaireIdentifierList questionnaireIdentifierList = mapStringToQuestionnaireIdentifierList(entity.getBody());
        assertEquals(1, questionnaireIdentifierList.getQuestionnaires().size());

        entity = listAssignmentsSpecialist(context, specialistB, clientB);
        assertEquals(OK, entity.getStatusCode());

        questionnaireIdentifierList = mapStringToQuestionnaireIdentifierList(entity.getBody());
        assertEquals(1, questionnaireIdentifierList.getQuestionnaires().size());
    }

}
