package ru.xgodness.endpoint.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.xgodness.endpoint.user.dto.JwtRequest;
import ru.xgodness.endpoint.user.dto.JwtResponse;
import ru.xgodness.endpoint.user.dto.RefreshJwtRequest;
import ru.xgodness.endpoint.user.dto.RegisterRequest;
import ru.xgodness.user.service.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequest registerRequest) {
        authService.register(
                registerRequest.getUsername(),
                registerRequest.getPassword(),
                registerRequest.getRole()
        );
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest authRequest) {
        JwtResponse jwtResponse = authService.login(authRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/token/access")
    public ResponseEntity<JwtResponse> newAccessToken(@RequestBody RefreshJwtRequest refreshJwtRequest) {
        JwtResponse jwtResponse = authService.refresh(refreshJwtRequest.getRefreshToken(), false);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<JwtResponse> newAccessAndRefreshTokens(@RequestBody RefreshJwtRequest refreshJwtRequest) {
        JwtResponse jwtResponse = authService.refresh(refreshJwtRequest.getRefreshToken(), true);
        return ResponseEntity.ok(jwtResponse);
    }
}
