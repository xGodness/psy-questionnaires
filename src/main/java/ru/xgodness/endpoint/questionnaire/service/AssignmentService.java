package ru.xgodness.endpoint.questionnaire.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.xgodness.endpoint.questionnaire.dto.AssignmentRequest;
import ru.xgodness.endpoint.questionnaire.dto.QuestionnaireIdentifierList;
import ru.xgodness.endpoint.questionnaire.exception.AssignmentAlreadyExistsException;
import ru.xgodness.endpoint.questionnaire.exception.AssignmentNotFoundException;
import ru.xgodness.endpoint.questionnaire.exception.QuestionnaireNotFoundException;
import ru.xgodness.endpoint.questionnaire.repository.AssignmentRepository;
import ru.xgodness.endpoint.questionnaire.repository.QuestionnaireIdentifierRepository;
import ru.xgodness.endpoint.user.exception.BindNotFoundException;
import ru.xgodness.endpoint.user.repository.BindRepository;
import ru.xgodness.security.util.SecurityContextUtils;
import ru.xgodness.util.ValidationUtils;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final BindRepository bindRepository;
    private final QuestionnaireIdentifierRepository identifierRepository;

    public void createAssignment(AssignmentRequest request) {
        String specialistUsername = SecurityContextUtils.getAuthenticatedUsername();
        String clientUsername = request.getClientUsername();
        long questionnaireId = request.getQuestionnaireId();

        ValidationUtils.fieldValidator()
                .clientUsernameNotEmpty(clientUsername)
                .validate();

        if (!bindRepository.existsApprovedByClientUsernameAndSpecialistUsername(clientUsername, specialistUsername))
            throw new BindNotFoundException(clientUsername);

        if (assignmentRepository.existsByClientUsernameAndQuestionnaireId(clientUsername, questionnaireId))
            throw new AssignmentAlreadyExistsException(clientUsername);

        if (!identifierRepository.existsById(questionnaireId))
            throw new QuestionnaireNotFoundException(questionnaireId);

        assignmentRepository.save(clientUsername, questionnaireId);
    }

    public void deleteAssignment(AssignmentRequest request) {
        String specialistUsername = SecurityContextUtils.getAuthenticatedUsername();
        String clientUsername = request.getClientUsername();
        long questionnaireId = request.getQuestionnaireId();

        ValidationUtils.fieldValidator()
                .clientUsernameNotEmpty(clientUsername)
                .validate();

        if (!bindRepository.existsApprovedByClientUsernameAndSpecialistUsername(clientUsername, specialistUsername))
            throw new BindNotFoundException(clientUsername);

        if (!assignmentRepository.existsByClientUsernameAndQuestionnaireId(clientUsername, questionnaireId))
            throw new AssignmentNotFoundException(clientUsername);

        assignmentRepository.delete(clientUsername, questionnaireId);
    }

    public QuestionnaireIdentifierList findAllAssignedToClient() {
        String clientUsername = SecurityContextUtils.getAuthenticatedUsername();
        return new QuestionnaireIdentifierList(identifierRepository.findAllAssignedTo(clientUsername));
    }

    public QuestionnaireIdentifierList findAllAssignedToClient(String clientUsername) {
        String specialistUsername = SecurityContextUtils.getAuthenticatedUsername();

        ValidationUtils.fieldValidator()
                .clientUsernameNotEmpty(clientUsername)
                .validate();

        if (!bindRepository.existsApprovedByClientUsernameAndSpecialistUsername(clientUsername, specialistUsername))
            throw new BindNotFoundException(clientUsername);

        return new QuestionnaireIdentifierList(identifierRepository.findAllAssignedTo(clientUsername));
    }

}
