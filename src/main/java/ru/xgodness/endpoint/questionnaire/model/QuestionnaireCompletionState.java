package ru.xgodness.endpoint.questionnaire.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.Map;

@Getter
@AllArgsConstructor
public class QuestionnaireCompletionState {
    private Timestamp completionDate;
    private Integer resultSum;
    private String resultInterpretation;
    private Map<Integer, Integer> answers;
}
