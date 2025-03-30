import org.junit.jupiter.api.Test;
import ru.xgodness.security.util.JwtProvider;
import ru.xgodness.user.model.Role;
import ru.xgodness.user.model.User;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class JwtProviderTest {

    private final String jwtAccessSecret = "K9r16VUxuXjQ7MFFlMrDgalgT2yIJx8LnNMZAsLHkxBni2sk7o+JRkGLcpnXLhvbtiX43uX/N4bb2t5GX/PvZqAHdEqB9Ro3CK876AwrcSu1yj2CfnPrxjgdaM9MywM44v609HxpKYI57hlCUBDGl7KT6R6wjSdNQptcHacjot9J0VfkNl+FkKioSqhDhSyREY7SNVHrKgNFQzCthL7l1bMS/pOs/yZn8ejf/vd5xbwIsTv67oqcCPZMJrBRu957g1Ql8Pk1dnBvxuJGFZ4Uc3bFo1p1IMwgVv2Ncr9+bV0vp6Zq6YirsZneY+GXJ1/nrx7BScEdkSUwxFGj+n6sNQ==";
    private final String jwtRefreshSecret = "wcyEHzpqExY4wvFUEX+iWSHhvXrcMar9OZ/2ldV/9GvntDa/nGH72N7fCThMczc6ytWgWRpKDBpTJ28ThlI2WjGwvoSRC2kycZGktuzYNm4ML4y5J68OLIMk45y56zsqS1TSvawNg6sYM5VFu2Fs8Fqu85R1wPPV0yoFDJxZ+BL0wm8BaL4BXURau7t/wj2bqj9JeJumn2ZaMBg530YFBl4JwAKPhJWWQzbxPlRPjAqu3kYo7RtbzwaLZ7w5p4d8ss2v6E/YwbMK/HAF3lFmKI7l+lpuL5xrWe7LaBc34qB/k4XnYgcErlraSPTZVClILar91KZbMxq1uNn6l5HnIA==";

    private final JwtProvider jwtProvider = new JwtProvider(jwtAccessSecret, jwtRefreshSecret);

    private final User user = new User("user", "password", "salt", Role.CLIENT);

    @Test
    void generateAccessToken_validate_shouldNotThrow() {
        String accessToken = jwtProvider.generateAccessToken(user);
        assertDoesNotThrow(() -> jwtProvider.validateAccessToken(accessToken));
    }

    @Test
    void generateRefreshToken_validate_shouldNotThrow() {
        String refreshToken = jwtProvider.generateRefreshToken(user);
        assertDoesNotThrow(() -> jwtProvider.validateRefreshToken(refreshToken));
    }
}
