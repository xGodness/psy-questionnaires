package ru.xgodness.endpoint.questionnaire.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
public class EvaluationQuestionnaireTemplate extends QuestionnaireTemplate {
    private List<String> questions;
    private List<String> answerOptions;
}
