package ru.xgodness.endpoint.questionnaire.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import ru.xgodness.endpoint.questionnaire.dto.SelectionQuestionnaireTemplate;

import java.util.List;

@Getter
@SuperBuilder
public class SelectionQuestionnaire extends Questionnaire {
    private List<List<String>> answerOptions;

    public SelectionQuestionnaire(long id, SelectionQuestionnaireTemplate template) {
        super(
                id,
                template.getName(),
                template.getType(),
                template.getDisplayName(),
                template.getDescription(),
                template.getQuestionCount(),
                template.getAnswerCount(),
                template.getAnswerWeights(),
                template.getKeyRanges()
        );
        this.answerOptions = template.getAnswerOptions();
    }

}
