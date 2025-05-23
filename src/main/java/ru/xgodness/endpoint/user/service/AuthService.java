package ru.xgodness.endpoint.user.service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import ru.xgodness.endpoint.user.dto.JwtResponse;
import ru.xgodness.endpoint.user.dto.LoginRequest;
import ru.xgodness.endpoint.user.dto.RegisterRequest;
import ru.xgodness.endpoint.user.exception.AuthException;
import ru.xgodness.endpoint.user.exception.UsernameAlreadyTakenException;
import ru.xgodness.endpoint.user.model.Role;
import ru.xgodness.endpoint.user.model.User;
import ru.xgodness.endpoint.user.repository.UserRepository;
import ru.xgodness.security.util.JwtProvider;
import ru.xgodness.util.ValidationUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Log
@Service
@RequiredArgsConstructor
public class AuthService {
    private static final AuthException authException = new AuthException("Неправильный логин или пароль");

    private final UserRepository userRepository;
    private final Map<String, String> refreshStorage = new HashMap<>();
    private final JwtProvider jwtProvider;

    public void register(RegisterRequest registerRequest) {

        String username = registerRequest.getUsername();
        String password = registerRequest.getPassword();
        String roleString = registerRequest.getRole();

        ValidationUtils.fieldValidator()
                .username(username)
                .password(password)
                .role(roleString)
                .validate();

        if (userRepository.existsByUsername(username))
            throw new UsernameAlreadyTakenException(username);

        String salt = BCrypt.gensalt();
        String hashpass = BCrypt.hashpw(password, salt);

        User user = new User(
                username,
                hashpass,
                salt,
                Role.valueOfIgnoreCase(roleString)
        );

        userRepository.save(user);
    }

    public JwtResponse login(LoginRequest loginRequest) {

        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        ValidationUtils.fieldValidator()
                .username(username)
                .password(password)
                .validate();

        Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsername());
        User user = userOpt.orElseThrow(() -> authException);

        String passhash = BCrypt.hashpw(loginRequest.getPassword(), user.getSalt());

        if (!passhash.equals(user.getPasshash()))
            throw authException;

        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);
        refreshStorage.put(user.getUsername(), refreshToken);
        return new JwtResponse(accessToken, refreshToken);
    }

    public JwtResponse refresh(String refreshToken, boolean regenerateRefreshToken) {
        jwtProvider.validateRefreshToken(refreshToken);

        Claims claims = jwtProvider.getRefreshClaims(refreshToken);
        String username = claims.getSubject();
        String savedRefreshToken = refreshStorage.get(username);

        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken))
            throw new AuthException("Invalid JWT");

        Optional<User> userOpt = userRepository.findByUsername(username);
        User user = userOpt.orElseThrow(() -> authException);

        String accessToken = jwtProvider.generateAccessToken(user);
        String newRefreshToken = null;

        if (regenerateRefreshToken) {
            newRefreshToken = jwtProvider.generateRefreshToken(user);
            refreshStorage.put(username, newRefreshToken);
        }

        return new JwtResponse(accessToken, newRefreshToken);
    }

}