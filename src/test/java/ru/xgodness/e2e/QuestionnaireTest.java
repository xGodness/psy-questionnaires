package ru.xgodness.e2e;

import lombok.extern.java.Log;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.TestPropertySource;
import ru.xgodness.persistence.DatabaseManager;
import ru.xgodness.persistence.QuestionnaireRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.xgodness.e2e.E2ETestUtils.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = "application.questionnaire.dir-path=src/test/resources/questionnaires")
@Log
public class QuestionnaireTest {

    @Autowired
    private DatabaseManager databaseManager;

    @Autowired
    private QuestionnaireRegistry registry;

    @Autowired
    private TestRestTemplate restTemplate;
    private final Map<String, String> accessTokenMap = new HashMap<>();
    private TestContext context;

    private static final String specialist = "specialist";
    private static final String client = "client";


    @BeforeAll
    public void setUp() throws Exception {
        context = new TestContext(accessTokenMap, restTemplate);

        for (var username : List.of(specialist, client)) {
            registerUser(context, username);
            loginUser(context, username);
        }

        requestBind(context, client, specialist);
        approveBindRequest(context, specialist, client);
    }

}
