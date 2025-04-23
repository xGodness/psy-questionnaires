package ru.xgodness.endpoint.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.xgodness.endpoint.user.model.User;
import ru.xgodness.endpoint.user.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Optional<User> getByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }

}
