package ru.xgodness.endpoint.questionnaire.dto;

import lombok.Getter;

@Getter
public class AssignmentRequest {
    private String clientUsername;
    private long questionnaireId;
}
