package ru.xgodness.endpoint.questionnaire.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.xgodness.endpoint.questionnaire.model.QuestionnaireIdentifier;

import java.util.List;

@Getter
@AllArgsConstructor
public class QuestionnaireIdentifierList {
    private List<QuestionnaireIdentifier> questionnaires;
}
