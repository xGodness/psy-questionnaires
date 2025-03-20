package ru.xgodness.questionnaire.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KeyRange {
    private int min;
    private int max;
    private String key;
}
