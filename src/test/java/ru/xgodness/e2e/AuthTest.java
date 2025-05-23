package ru.xgodness.e2e;

import lombok.extern.java.Log;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.xgodness.exception.dto.ErrorMessages;
import ru.xgodness.persistence.DatabaseManager;

import java.util.List;

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
public class AuthTest {

    @Autowired
    private DatabaseManager databaseManager;

    @Autowired
    private TestRestTemplate restTemplate;

    private static String accessToken = null;
    private static String refreshToken = null;

    private final String username = "user";
    private final String password = "password__123**!";
    private final String role = "client";
    private final String invalidToken = "eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJydS54Z29kbmVzcyIsInN1YiI6Imtlbm55c3NzcyIsInJvbGUiOiJDTElFTlQiLCJleHAiOjE3NDUzMzcwMDZ9.UvukdDSx2_FI7OspfdTGRmbTi_aedoSabSkZPy6ibHEM-DG0fsR6A2g3n1TZBCgspLAVvCjfcNjsQKLRAOixiQ";


    @BeforeEach
    public void setUp() {
        restTemplate.getRestTemplate().setInterceptors(
                List.of(
                        (request, body, execution) -> {
                            request.getHeaders()
                                    .add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

                            if (accessToken != null)
                                request.getHeaders()
                                        .add("Authorization", "Bearer " + accessToken);

                            return execution.execute(request, body);
                        }
                )
        );
    }

    @AfterAll
    public void truncate() {
        databaseManager.executeQuery(TRUNCATE_QUERY);
    }


    @Order(1)
    @Test
    public void testRegistration_emptyBody_403() throws Exception {
        ResponseEntity<String> entity = restTemplate.postForEntity(REGISTER_PATH, null, String.class);

        log.info(entity.toString());

        assertEquals(BAD_REQUEST, entity.getStatusCode());

        ErrorMessages errors = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Request body is not readable"), errors.getMessages());
    }

    @Order(2)
    @Test
    public void testRegistration_passwordTooShort_403() throws Exception {
        ResponseEntity<String> entity = restTemplate.postForEntity(
                REGISTER_PATH,
                new RegisterRequest(
                        username,
                        "123",
                        role
                ),
                String.class
        );

        log.info(entity.toString());

        assertEquals(UNPROCESSABLE_ENTITY, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Пароль не может быть короче 8 символов"), errorMessages.getMessages());
    }

    @Order(3)
    @Test
    public void testRegistration_usernameTooLong_403() throws Exception {
        ResponseEntity<String> entity = restTemplate.postForEntity(
                REGISTER_PATH,
                new RegisterRequest(
                        username.repeat(20),
                        password,
                        role
                ),
                String.class
        );

        log.info(entity.toString());

        assertEquals(UNPROCESSABLE_ENTITY, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Имя пользователя не может быть длиннее 32 символов"), errorMessages.getMessages());
    }


    @Order(4)
    @Test
    public void testRegistration_requiredFieldsMissing_403() throws Exception {
        ResponseEntity<String> entity = restTemplate.postForEntity(
                REGISTER_PATH,
                new RegisterRequest(
                        username,
                        null,
                        null
                ),
                String.class
        );

        log.info(entity.toString());

        assertEquals(UNPROCESSABLE_ENTITY, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Пароль не может быть пустым", "Роль пользователя не может быть пустой"), errorMessages.getMessages());
    }

    @Order(5)
    @Test
    public void testRegistration_validData_200() {
        ResponseEntity<String> entity = restTemplate.postForEntity(
                REGISTER_PATH,
                new RegisterRequest(
                        username,
                        password,
                        role
                ),
                String.class
        );

        log.info(entity.toString());

        assertEquals(NO_CONTENT, entity.getStatusCode());
        assertNull(entity.getBody());
    }

    @Order(6)
    @Test
    public void testRegistration_userAlreadyExists_409() throws Exception {
        ResponseEntity<String> entity = restTemplate.postForEntity(
                REGISTER_PATH,
                new RegisterRequest(
                        username,
                        password,
                        role
                ),
                String.class
        );

        log.info(entity.toString());

        assertEquals(CONFLICT, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Пользователь с именем %s уже существует".formatted(username)), errorMessages.getMessages());
    }

    @Order(7)
    @Test
    public void testLogin_notRegisteredUser_401() throws Exception {
        ResponseEntity<String> entity = restTemplate.postForEntity(
                LOGIN_PATH,
                new LoginRequest(
                        username + "1",
                        password
                ),
                String.class
        );

        log.info(entity.toString());

        assertEquals(UNAUTHORIZED, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Неправильный логин или пароль"), errorMessages.getMessages());
    }

    @Order(8)
    @Test
    public void testLogin_invalidCredentials_401() throws Exception {
        ResponseEntity<String> entity = restTemplate.postForEntity(
                LOGIN_PATH,
                new LoginRequest(
                        username,
                        password + "1"
                ),
                String.class
        );

        log.info(entity.toString());

        assertEquals(UNAUTHORIZED, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Неправильный логин или пароль"), errorMessages.getMessages());
    }

    @Order(9)
    @Test
    public void testLogin_validCredentials_200() throws Exception {
        ResponseEntity<String> entity = restTemplate.postForEntity(
                LOGIN_PATH,
                new LoginRequest(
                        username,
                        password
                ),
                String.class
        );

        log.info(entity.toString());

        assertEquals(OK, entity.getStatusCode());

        JwtResponse jwtResponse = mapStringToJwtResponse(entity.getBody());
        String accessToken = jwtResponse.getAccessToken();
        String refreshToken = jwtResponse.getRefreshToken();

        assertNotNull(accessToken);
        assertNotNull(refreshToken);

        AuthTest.accessToken = accessToken;
        AuthTest.refreshToken = refreshToken;
    }

    @Order(10)
    @Test
    public void testRenewAccessToken_invalidRefreshToken_401() throws Exception {
        ResponseEntity<String> entity = restTemplate.postForEntity(
                TOKEN_ACCESS_PATH,
                new RefreshJwtRequest(
                        invalidToken
                ),
                String.class
        );

        log.info(entity.toString());

        assertEquals(UNAUTHORIZED, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Invalid JWT"), errorMessages.getMessages());
    }

    @Order(11)
    @Test
    public void testRenewAccessToken_noRefreshToken_401() throws Exception {
        ResponseEntity<String> entity = restTemplate.postForEntity(
                TOKEN_ACCESS_PATH,
                new RefreshJwtRequest(
                        null
                ),
                String.class
        );

        log.info(entity.toString());

        assertEquals(UNAUTHORIZED, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("JWT must not be null"), errorMessages.getMessages());
    }

    @Order(12)
    @Test
    public void testRenewAccessToken_validRefreshToken_200() throws Exception {
        ResponseEntity<String> entity = restTemplate.postForEntity(
                TOKEN_ACCESS_PATH,
                new RefreshJwtRequest(
                        refreshToken
                ),
                String.class
        );

        log.info(entity.toString());

        assertEquals(OK, entity.getStatusCode());

        JwtResponse jwtResponse = mapStringToJwtResponse(entity.getBody());
        String accessToken = jwtResponse.getAccessToken();
        String refreshToken = jwtResponse.getRefreshToken();

        assertNotNull(accessToken);
        assertNull(refreshToken);

        AuthTest.accessToken = accessToken;
    }

    @Order(13)
    @Test
    public void testRenewRefreshToken_invalidAccessToken_401() throws Exception {
        restTemplate.getRestTemplate().setInterceptors(
                List.of(
                        (request, body, execution) -> {
                            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
                            headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
                            headers.add("Authorization", "Bearer " + invalidToken);

                            request.getHeaders().addAll(headers);

                            return execution.execute(request, body);
                        }
                )
        );

        ResponseEntity<String> entity = restTemplate.postForEntity(
                TOKEN_REFRESH_PATH,
                new RefreshJwtRequest(
                        refreshToken
                ),
                String.class
        );

        log.info(entity.toString());

        assertEquals(UNAUTHORIZED, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Invalid JWT"), errorMessages.getMessages());
    }

    @Order(14)
    @Test
    public void testRenewRefreshToken_noAccessToken_401() throws Exception {
        restTemplate.getRestTemplate().setInterceptors(
                List.of(
                        (request, body, execution) -> {
                            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
                            headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
                            headers.add("Authorization", "Bearer ");

                            request.getHeaders().addAll(headers);

                            return execution.execute(request, body);
                        }
                )
        );

        ResponseEntity<String> entity = restTemplate.postForEntity(
                TOKEN_REFRESH_PATH,
                new RefreshJwtRequest(
                        refreshToken
                ),
                String.class
        );

        log.info(entity.toString());

        assertEquals(UNAUTHORIZED, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("JWT must not be null"), errorMessages.getMessages());
    }

    @Order(15)
    @Test
    public void testRenewRefreshToken_invalidRefreshToken_401() throws Exception {
        ResponseEntity<String> entity = restTemplate.postForEntity(
                TOKEN_REFRESH_PATH,
                new RefreshJwtRequest(
                        invalidToken
                ),
                String.class
        );

        log.info(entity.toString());

        assertEquals(UNAUTHORIZED, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("Invalid JWT"), errorMessages.getMessages());
    }

    @Order(16)
    @Test
    public void testRenewRefreshToken_noRefreshToken_401() throws Exception {
        ResponseEntity<String> entity = restTemplate.postForEntity(
                TOKEN_REFRESH_PATH,
                new RefreshJwtRequest(
                        null
                ),
                String.class
        );

        log.info(entity.toString());

        assertEquals(UNAUTHORIZED, entity.getStatusCode());

        ErrorMessages errorMessages = mapStringToErrorMessages(entity.getBody());
        assertEquals(List.of("JWT must not be null"), errorMessages.getMessages());
    }

    @Order(17)
    @Test
    public void testRenewRefreshToken_validRefreshAndAccessToken_200() throws Exception {
        ResponseEntity<String> entity = restTemplate.postForEntity(
                TOKEN_REFRESH_PATH,
                new RefreshJwtRequest(
                        refreshToken
                ),
                String.class
        );

        log.info(entity.toString());

        assertEquals(OK, entity.getStatusCode());

        JwtResponse jwtResponse = mapStringToJwtResponse(entity.getBody());
        String accessToken = jwtResponse.getAccessToken();
        String refreshToken = jwtResponse.getRefreshToken();

        assertNotNull(accessToken);
        assertNotNull(refreshToken);

        AuthTest.accessToken = accessToken;
        AuthTest.refreshToken = refreshToken;
    }

}
