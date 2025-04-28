package ru.xgodness.endpoint.questionnaire.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InterpretationKeyRange {
    private int min;
    private int max;
    private String interpretation;
}
