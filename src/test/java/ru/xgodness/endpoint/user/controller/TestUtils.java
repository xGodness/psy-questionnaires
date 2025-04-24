package ru.xgodness.endpoint.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.xgodness.exception.dto.ErrorMessages;

import java.util.List;

public class TestUtils {
    protected static final String REGISTER_PATH = "/auth/register";
    protected static final String LOGIN_PATH = "/auth/login";
    protected static final String TOKEN_ACCESS_PATH = "/auth/token/access";
    protected static final String TOKEN_REFRESH_PATH = "/auth/token/refresh";
    protected static final String CLIENT_REQUEST_BIND_PATH = "/client/bind/request";
    protected static final String SPECIALIST_GET_PENDING_BINDS_PATH = "/specialist/bind/get/pending";
    protected static final String SPECIALIST_GET_APPROVED_BINDS_PATH = "/specialist/bind/get/approved";
    protected static final String SPECIALIST_GET_ALL_BINDS_PATH = "/specialist/bind/get/all";
    protected static final String SPECIALIST_APPROVE_BIND_PATH = "/specialist/bind/approve";
    protected static final String SPECIALIST_DISCARD_BIND_PATH = "/specialist/bind/discard";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    protected static ErrorMessages mapStringToErrorMessages(String string) throws Exception {
        return objectMapper.readValue(string, ErrorMessages.class);
    }

    protected static JwtResponse mapStringToJwtResponse(String string) throws Exception {
        return objectMapper.readValue(string, JwtResponse.class);
    }

    protected static ClientUsernameList mapStringToClientUsernameList(String string) throws Exception {
        return objectMapper.readValue(string, ClientUsernameList.class);
    }

    protected static BindList mapStringToBindList(String string) throws Exception {
        return objectMapper.readValue(string, BindList.class);
    }

    @AllArgsConstructor
    @Getter
    protected static class RegisterRequest {
        private String username;
        private String password;
        private String role;
    }

    @AllArgsConstructor
    @Getter
    protected static class LoginRequest {
        private String username;
        private String password;
    }

    @AllArgsConstructor
    @Getter
    protected static class RefreshJwtRequest {
        private String refreshToken;
    }

    @Setter
    @Getter
    protected static class JwtResponse {
        private String type;
        private String accessToken;
        private String refreshToken;
    }

    @Getter
    @Setter
    protected static class ClientUsernameList {
        private List<String> clients;
    }

    @Getter
    @Setter
    protected static class BindList {
        private ClientUsernameList pending;
        private ClientUsernameList approved;
    }

}
