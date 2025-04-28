package ru.xgodness.security.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.xgodness.endpoint.user.exception.AuthException;
import ru.xgodness.endpoint.user.model.Role;
import ru.xgodness.endpoint.user.model.User;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = JwtProvider.class
)
public class JwtProviderTest {

    @Autowired
    private JwtProvider jwtProvider;

    private final User user = new User("user", "password", "salt", Role.CLIENT);
    private final String invalidToken = "eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJydS54Z29kbmVzcyIsInN1YiI6Imtlbm55c3NzcyIsInJvbGUiOiJDTElFTlQiLCJleHAiOjE3NDUzMzcwMDZ9.UvukdDSx2_FI7OspfdTGRmbTi_aedoSabSkZPy6ibHEM-DG0fsR6A2g3n1TZBCgspLAVvCjfcNjsQKLRAOixiQ";

    @Test
    public void generateAccessToken_validate_shouldNotThrow() {
        String accessToken = jwtProvider.generateAccessToken(user);
        assertDoesNotThrow(() -> jwtProvider.validateAccessToken(accessToken));
    }

    @Test
    public void generateRefreshToken_validate_shouldNotThrow() {
        String refreshToken = jwtProvider.generateRefreshToken(user);
        assertDoesNotThrow(() -> jwtProvider.validateRefreshToken(refreshToken));
    }

    @Test
    public void validateInvalidToken_shouldThrow() {
        assertThrows(AuthException.class, () -> jwtProvider.validateAccessToken(invalidToken));
        assertThrows(AuthException.class, () -> jwtProvider.validateRefreshToken(invalidToken));
    }
}
