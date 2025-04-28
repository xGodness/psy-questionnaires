package ru.xgodness.endpoint.questionnaire.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QuestionnaireIdentifier {
    private long id;
    private String name;
    private String displayName;
}
