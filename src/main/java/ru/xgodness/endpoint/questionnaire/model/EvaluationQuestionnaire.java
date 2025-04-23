package ru.xgodness.endpoint.questionnaire.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
public class EvaluationQuestionnaire extends Questionnaire {
    private List<String> questions;
    private List<String> answerOptions;
}
