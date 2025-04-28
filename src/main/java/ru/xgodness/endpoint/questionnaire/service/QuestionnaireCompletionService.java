package ru.xgodness.endpoint.questionnaire.service;

import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import ru.xgodness.endpoint.questionnaire.dto.ClientHistory;
import ru.xgodness.endpoint.questionnaire.dto.QuestionnaireAnswersState;
import ru.xgodness.endpoint.questionnaire.dto.QuestionnaireForm;
import ru.xgodness.endpoint.questionnaire.exception.AccessToQuestionnaireDeniedException;
import ru.xgodness.endpoint.questionnaire.exception.CompletionNotStartedException;
import ru.xgodness.endpoint.questionnaire.exception.MissingAnswerException;
import ru.xgodness.endpoint.questionnaire.exception.QuestionnaireNotFoundException;
import ru.xgodness.endpoint.questionnaire.model.Questionnaire;
import ru.xgodness.endpoint.questionnaire.model.QuestionnaireCompletionState;
import ru.xgodness.endpoint.questionnaire.repository.AssignmentRepository;
import ru.xgodness.endpoint.questionnaire.repository.QuestionnaireCompletionRepository;
import ru.xgodness.endpoint.user.exception.BindNotFoundException;
import ru.xgodness.endpoint.user.model.Role;
import ru.xgodness.endpoint.user.repository.BindRepository;
import ru.xgodness.exception.ApplicationException;
import ru.xgodness.persistence.DatabaseManager;
import ru.xgodness.persistence.QuestionnaireRegistry;
import ru.xgodness.security.util.SecurityContextUtils;
import ru.xgodness.util.ValidationUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ru.xgodness.endpoint.user.model.Role.CLIENT;

@Log
@Service
public class QuestionnaireCompletionService {

    private final AssignmentRepository assignmentRepository;
    private final BindRepository bindRepository;
    private final QuestionnaireCompletionRepository completionRepository;
    private final QuestionnaireRegistry registry;
    private final Connection transactionConnection;

    public QuestionnaireCompletionService(
            AssignmentRepository assignmentRepository,
            BindRepository bindRepository,
            QuestionnaireCompletionRepository completionRepository,
            QuestionnaireRegistry registry,
            DatabaseManager databaseManager) throws SQLException {
        this.assignmentRepository = assignmentRepository;
        this.bindRepository = bindRepository;
        this.completionRepository = completionRepository;
        this.registry = registry;
        this.transactionConnection = databaseManager.initializeConnection();
        this.transactionConnection.setAutoCommit(false);
    }

    public QuestionnaireForm getForm(long questionnaireId) {
        String username = SecurityContextUtils.getAuthenticatedUsername();
        Role role = SecurityContextUtils.getAuthenticatedRole();

        Questionnaire questionnaire = getQuestionnaireOrThrow(questionnaireId);
        String questionnaireName = questionnaire.getName();

        if (role == CLIENT) {
            if (!completionRepository.existsNotCompletedByClientUsername(username, questionnaireName)
                    && !assignmentRepository.existsByClientUsernameAndQuestionnaireId(username, questionnaireId))
                throw new AccessToQuestionnaireDeniedException(questionnaireId);
        }

        return registry.getQuestionnaireForm(questionnaireId);
    }

    public QuestionnaireAnswersState getState(long questionnaireId) {
        String clientUsername = SecurityContextUtils.getAuthenticatedUsername();

        Questionnaire questionnaire = getQuestionnaireOrThrow(questionnaireId);
        String questionnaireName = questionnaire.getName();
        int questionCount = questionnaire.getQuestionCount();

        var stateOptional = completionRepository.findNotCompletedByClientUsername(clientUsername, questionnaireName, questionCount);
        if (stateOptional.isPresent())
            return new QuestionnaireAnswersState(stateOptional.get().getAnswers());

        if (!assignmentRepository.existsByClientUsernameAndQuestionnaireId(clientUsername, questionnaireId))
            throw new AccessToQuestionnaireDeniedException(questionnaireId);

        try {
            boolean deleted = assignmentRepository.deleteTransactional(transactionConnection, clientUsername, questionnaireId);
            QuestionnaireAnswersState answersState = completionRepository.saveTransactional(transactionConnection, clientUsername, questionnaireName, questionCount);
            transactionConnection.commit();

            if (deleted && !answersState.getAnswers().isEmpty())
                return answersState;

            transactionConnection.rollback();
        } catch (SQLException ex) {
            try {
                log.severe(ex.getMessage());
                transactionConnection.rollback();
            } catch (SQLException rollbackEx) {
                log.severe("Could not rollback on failed transaction. SQL state: " + rollbackEx.getSQLState() + " Exception: " + rollbackEx.getMessage());
                throw new RuntimeException(rollbackEx);
            }
        }

        log.severe("UNEXPECTED BEHAVIOR: No exception was thrown in transaction but some updates were not made");
        throw new ApplicationException();
    }

    public QuestionnaireAnswersState updateState(long questionnaireId, QuestionnaireAnswersState request) {
        String clientUsername = SecurityContextUtils.getAuthenticatedUsername();

        Questionnaire questionnaire = getQuestionnaireOrThrow(questionnaireId);
        String questionnaireName = questionnaire.getName();
        int questionCount = questionnaire.getQuestionCount();

        if (!completionRepository.existsNotCompletedByClientUsername(clientUsername, questionnaireName))
            throw new CompletionNotStartedException(questionnaireId);

        int answerCount = questionnaire.getAnswerCount();
        Map<Integer, Integer> answerUpdates = request.getAnswers();
        ValidationUtils.fieldValidator()
                .answerUpdates(answerUpdates, questionCount, answerCount)
                .validate();

        return completionRepository.update(clientUsername, questionnaireName, questionCount, answerUpdates);
    }

    public void complete(long questionnaireId) {
        String clientUsername = SecurityContextUtils.getAuthenticatedUsername();

        Questionnaire questionnaire = getQuestionnaireOrThrow(questionnaireId);
        String questionnaireName = questionnaire.getName();
        int questionCount = questionnaire.getQuestionCount();

        Optional<QuestionnaireCompletionState> completionStateOptional = completionRepository.findNotCompletedByClientUsername(clientUsername, questionnaireName, questionCount);
        QuestionnaireCompletionState completionState = completionStateOptional.orElseThrow(
                () -> new CompletionNotStartedException(questionnaireId));

        for (var value : completionState.getAnswers().values())
            if (value == null || value == 0) throw new MissingAnswerException();

        int resultSum = questionnaire.calculateResultSum(completionState.getAnswers().values());
        String resultInterpretation = questionnaire.interpreterResult(resultSum);
        completionRepository.complete(clientUsername, questionnaireName, resultSum, resultInterpretation);
    }

    public ClientHistory getClientHistory(String clientUsername) {
        String specialistUsername = SecurityContextUtils.getAuthenticatedUsername();

        if (!bindRepository.existsApprovedByClientUsernameAndSpecialistUsername(clientUsername, specialistUsername))
            throw new BindNotFoundException(clientUsername);

        Map<String, List<QuestionnaireCompletionState>> history = new HashMap<>();
        String questionnaireName;
        int questionCount;
        List<QuestionnaireCompletionState> stateList;

        for (var questionnaire : registry.getAllQuestionnaires()) {
            questionnaireName = questionnaire.getName();
            questionCount = questionnaire.getQuestionCount();
            stateList = completionRepository.findAllCompletedByClientUsername(clientUsername, questionnaireName, questionCount);

            if (!stateList.isEmpty())
                history.put(
                        questionnaireName,
                        stateList
                );
        }

        return new ClientHistory(clientUsername, history);
    }

    private Questionnaire getQuestionnaireOrThrow(long questionnaireId) {
        Questionnaire questionnaire = registry.getQuestionnaire(questionnaireId);
        if (questionnaire == null)
            throw new QuestionnaireNotFoundException(questionnaireId);
        return questionnaire;
    }

}
