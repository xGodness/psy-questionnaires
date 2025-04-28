package ru.xgodness.endpoint.questionnaire.dto;

import lombok.Getter;
import ru.xgodness.endpoint.questionnaire.model.EvaluationQuestionnaire;
import ru.xgodness.endpoint.questionnaire.model.Questionnaire;
import ru.xgodness.endpoint.questionnaire.model.SelectionQuestionnaire;
import ru.xgodness.exception.ApplicationException;

import java.util.ArrayList;
import java.util.List;

@Getter
public class QuestionnaireForm {
    private final String displayName;
    private final String description;
    private final List<Question> questions;

    public QuestionnaireForm(Questionnaire questionnaire) {
        this.displayName = questionnaire.getDisplayName();
        this.description = questionnaire.getDescription();

        switch (questionnaire.getType()) {

            case SELECTION -> {
                SelectionQuestionnaire selectionQuestionnaire = (SelectionQuestionnaire) questionnaire;
                this.questions = selectionQuestionnaire.getAnswerOptions()
                        .stream()
                        .map(options -> new Question(null, options))
                        .toList();
            }

            case EVALUATION -> {
                EvaluationQuestionnaire evaluationQuestionnaire = (EvaluationQuestionnaire) questionnaire;

                List<Question> questions = new ArrayList<>();
                var answerOptions = evaluationQuestionnaire.getAnswerOptions();
                var questionTexts = evaluationQuestionnaire.getQuestions();

                for (var i = 0; i < evaluationQuestionnaire.getQuestionCount(); i++) {
                    questions.add(new Question(
                            questionTexts.get(i),
                            answerOptions
                    ));
                }

                this.questions = questions;
            }

            default -> throw new ApplicationException("Unknown questionnaire type");
        }
    }
}
