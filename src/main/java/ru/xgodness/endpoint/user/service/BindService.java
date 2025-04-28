package ru.xgodness.endpoint.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import ru.xgodness.endpoint.user.dto.BindList;
import ru.xgodness.endpoint.user.dto.ClientUsernameList;
import ru.xgodness.endpoint.user.exception.BindRequestAlreadyExistsException;
import ru.xgodness.endpoint.user.exception.BindRequestNotFoundException;
import ru.xgodness.endpoint.user.exception.UserNotFoundException;
import ru.xgodness.endpoint.user.model.Role;
import ru.xgodness.endpoint.user.repository.BindRepository;
import ru.xgodness.endpoint.user.repository.UserRepository;
import ru.xgodness.security.util.SecurityContextUtils;
import ru.xgodness.util.ValidationUtils;

@Log
@Service
@RequiredArgsConstructor
public class BindService {

    private final BindRepository bindRepository;
    private final UserRepository userRepository;

    public void requestSpecialistBind(String specialistUsername) {
        if (!userRepository.existsByUsernameAndRole(specialistUsername, Role.SPECIALIST))
            throw new UserNotFoundException(specialistUsername);

        String clientUsername = SecurityContextUtils.getAuthenticatedUsername();

        if (bindRepository.existsByClientUsernameAndSpecialistUsername(clientUsername, specialistUsername))
            throw new BindRequestAlreadyExistsException(specialistUsername);

        bindRepository.createBindRequest(clientUsername, specialistUsername);
    }

    public ClientUsernameList getPendingBindsClientUsernames() {
        String specialistUsername = SecurityContextUtils.getAuthenticatedUsername();
        return new ClientUsernameList(
                bindRepository.findAllPendingBySpecialistUsername(specialistUsername)
        );
    }

    public ClientUsernameList getApprovedBindsClientUsernames() {
        String specialistUsername = SecurityContextUtils.getAuthenticatedUsername();
        return new ClientUsernameList(
                bindRepository.findAllApprovedBySpecialistUsername(specialistUsername)
        );
    }

    public BindList getAllBindsClientUsernames() {
        String specialistUsername = SecurityContextUtils.getAuthenticatedUsername();
        return bindRepository.findAllBySpecialistUsername(specialistUsername);
    }

    public void approvePendingBind(String clientUsername) {
        ValidationUtils.fieldValidator()
                .clientUsernameNotEmpty(clientUsername)
                .validate();

        String specialistUsername = SecurityContextUtils.getAuthenticatedUsername();

        if (!bindRepository.approveBindRequest(clientUsername, specialistUsername))
            throw new BindRequestNotFoundException(clientUsername);
    }

    public void discardPendingBind(String clientUsername) {
        ValidationUtils.fieldValidator()
                .clientUsernameNotEmpty(clientUsername)
                .validate();

        String specialistUsername = SecurityContextUtils.getAuthenticatedUsername();

        if (!bindRepository.discardBindRequest(clientUsername, specialistUsername))
            throw new BindRequestNotFoundException(clientUsername);
    }

}
