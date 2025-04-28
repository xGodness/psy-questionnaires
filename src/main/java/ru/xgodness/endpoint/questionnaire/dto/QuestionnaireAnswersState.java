package ru.xgodness.endpoint.questionnaire.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionnaireAnswersState {
    private Map<Integer, Integer> answers;
}
