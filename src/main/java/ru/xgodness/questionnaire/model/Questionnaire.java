package ru.xgodness.questionnaire.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
public abstract class Questionnaire {
    private String name;
    private QuestionnaireType type;
    private String displayName;
    private String description;
    private int questionCount;
    private int answerCount;
    private List<Integer> answerWeights;
    private List<KeyRange> keyRanges;
}
