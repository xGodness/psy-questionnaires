package ru.xgodness.endpoint.questionnaire.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.xgodness.endpoint.questionnaire.model.QuestionnaireCompletionState;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClientHistory {
    private String clientUsername;
    private Map<String, List<QuestionnaireCompletionState>> history;
}
