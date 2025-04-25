package ru.xgodness.endpoint.questionnaire.model;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import ru.xgodness.endpoint.questionnaire.dto.EvaluationQuestionnaireTemplate;

import java.util.List;

@Getter
@SuperBuilder
public class EvaluationQuestionnaire extends Questionnaire {
    private List<String> questions;
    private List<String> answerOptions;

    public EvaluationQuestionnaire(long id, EvaluationQuestionnaireTemplate template) {
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
        this.questions = template.getQuestions();
        this.answerOptions = template.getAnswerOptions();
    }

}
