package ru.xgodness.endpoint.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import ru.xgodness.endpoint.user.model.Role;
import ru.xgodness.endpoint.user.repository.ClientSpecialistBindRepository;
import ru.xgodness.endpoint.user.repository.UserRepository;
import ru.xgodness.exception.BindRequestAlreadyExistsException;
import ru.xgodness.exception.UserNotFoundException;
import ru.xgodness.security.util.SecurityContextUtils;

@Log
@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientSpecialistBindRepository clientSpecialistBindRepository;
    private final UserRepository userRepository;

    public void requestSpecialistBind(String specialistUsername) {
        if (!userRepository.existsByUsernameAndRole(specialistUsername, Role.SPECIALIST))
            throw new UserNotFoundException(specialistUsername);

        String clientUsername = SecurityContextUtils.getAuthenticatedUsername();

        if (clientSpecialistBindRepository.existsByClientUsernameAndSpecialistUsername(clientUsername, specialistUsername))
            throw new BindRequestAlreadyExistsException(specialistUsername);

        clientSpecialistBindRepository.createBindRequest(clientUsername, specialistUsername);
    }

}
