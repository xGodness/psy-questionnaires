package ru.xgodness.security.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.xgodness.endpoint.user.model.Role;
import ru.xgodness.endpoint.user.model.User;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = JwtProvider.class
)
public class JwtProviderTest {

    @Autowired
    private JwtProvider jwtProvider;

    private final User user = new User("user", "password", "salt", Role.CLIENT);

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
}
