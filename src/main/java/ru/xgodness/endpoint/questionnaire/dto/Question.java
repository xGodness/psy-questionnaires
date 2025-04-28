package ru.xgodness.endpoint.questionnaire.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class Question {
    private String questionText;
    private List<String> answerOptions;
}
