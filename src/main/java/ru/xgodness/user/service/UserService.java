package ru.xgodness.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.xgodness.user.model.User;
import ru.xgodness.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getByUsername(String username) {
        return userRepository.findUserByUsername(username);
    }

}
