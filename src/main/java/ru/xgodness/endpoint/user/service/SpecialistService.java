package ru.xgodness.endpoint.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import ru.xgodness.endpoint.user.dto.BindList;
import ru.xgodness.endpoint.user.dto.ClientUsernameList;
import ru.xgodness.endpoint.user.repository.ClientSpecialistBindRepository;
import ru.xgodness.endpoint.user.repository.UserRepository;
import ru.xgodness.exception.BindRequestNotFoundException;
import ru.xgodness.exception.ValidationException;
import ru.xgodness.security.util.SecurityContextUtils;

@Log
@Service
@RequiredArgsConstructor
public class SpecialistService {

    private final ClientSpecialistBindRepository clientSpecialistBindRepository;
    private final UserRepository userRepository;

    public ClientUsernameList getPendingBindsClientUsernames() {
        String specialistUsername = SecurityContextUtils.getAuthenticatedUsername();
        return new ClientUsernameList(
                clientSpecialistBindRepository.findAllPendingBySpecialistUsername(specialistUsername)
        );
    }

    public ClientUsernameList getApprovedBindsClientUsernames() {
        String specialistUsername = SecurityContextUtils.getAuthenticatedUsername();
        return new ClientUsernameList(
                clientSpecialistBindRepository.findAllApprovedBySpecialistUsername(specialistUsername)
        );
    }

    public BindList getAllBindsClientUsernames() {
        String specialistUsername = SecurityContextUtils.getAuthenticatedUsername();
        return clientSpecialistBindRepository.findAllBySpecialistUsername(specialistUsername);
    }

    public void approvePendingBind(String clientUsername) {
        if (clientUsername == null)
            throw new ValidationException("Не указано имя пользователя клиента");

        String specialistUsername = SecurityContextUtils.getAuthenticatedUsername();

        if (!clientSpecialistBindRepository.approveBindRequest(clientUsername, specialistUsername))
            throw new BindRequestNotFoundException(clientUsername);
    }

    public void discardPendingBind(String clientUsername) {
        if (clientUsername == null)
            throw new ValidationException("Не указано имя пользователя клиента");

        String specialistUsername = SecurityContextUtils.getAuthenticatedUsername();

        if (!clientSpecialistBindRepository.discardBindRequest(clientUsername, specialistUsername))
            throw new BindRequestNotFoundException(clientUsername);
    }
}
