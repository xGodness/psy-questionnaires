package ru.xgodness.endpoint.questionnaire.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.xgodness.endpoint.questionnaire.dto.QuestionnaireIdentifierList;
import ru.xgodness.endpoint.questionnaire.repository.QuestionnaireIdentifierRepository;

@Service
@RequiredArgsConstructor
public class QuestionnaireIdentifierService {

    private final QuestionnaireIdentifierRepository identifierRepository;

    public QuestionnaireIdentifierList findAllIdentifiers() {
        return new QuestionnaireIdentifierList(identifierRepository.findAll());
    }

    public QuestionnaireIdentifierList findAllIdentifiersWithDisplayNameLike(String pattern) {
        return new QuestionnaireIdentifierList(identifierRepository.findAllWithDisplayNameLike(pattern));
    }

}
