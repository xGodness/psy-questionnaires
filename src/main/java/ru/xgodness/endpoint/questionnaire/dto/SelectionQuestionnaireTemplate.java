package ru.xgodness.endpoint.questionnaire.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
public class SelectionQuestionnaireTemplate extends QuestionnaireTemplate {
    private List<List<String>> answerOptions;
}
