package ru.xgodness.user.service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import ru.xgodness.endpoint.user.dto.JwtRequest;
import ru.xgodness.endpoint.user.dto.JwtResponse;
import ru.xgodness.exception.AuthException;
import ru.xgodness.exception.UsernameAlreadyTakenException;
import ru.xgodness.security.JwtAuthentication;
import ru.xgodness.security.util.JwtProvider;
import ru.xgodness.user.model.Role;
import ru.xgodness.user.model.User;
import ru.xgodness.user.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

// TODO: check if arguments null

@Log
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final Map<String, String> refreshStorage = new HashMap<>();
    private final JwtProvider jwtProvider;

    // TODO: what if right after thread A checked that there's no user with such name thread B saves such user?
    public void register(String username, String password, Role role) {

        // TODO: validate input

        if (userRepository.existsByUsername(username))
            throw new UsernameAlreadyTakenException(username);

        String salt = BCrypt.gensalt();
        String hashpass = BCrypt.hashpw(password, salt);

        User user = new User(
                username,
                hashpass,
                salt,
                role
        );

        userRepository.saveUser(user);
    }

    public JwtResponse login(JwtRequest loginRequest) {
        User user = userRepository.findUserByUsername(loginRequest.getUsername());
        String passhash = BCrypt.hashpw(loginRequest.getPassword(), user.getSalt());

        if (!passhash.equals(user.getPasshash()))
            throw new AuthException("Неправильный пароль");

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
            throw new AuthException("JWT недействителен");

        User user = userRepository.findUserByUsername(username);
        String accessToken = jwtProvider.generateAccessToken(user);
        String newRefreshToken = null;

        if (regenerateRefreshToken) {
            newRefreshToken = jwtProvider.generateRefreshToken(user);
            refreshStorage.put(username, newRefreshToken);
        }

        return new JwtResponse(accessToken, newRefreshToken);
    }

    public JwtAuthentication getAuthInfo() {
        return (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();
    }
}