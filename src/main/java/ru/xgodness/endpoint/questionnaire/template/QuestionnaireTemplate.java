package ru.xgodness.endpoint.questionnaire.template;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import ru.xgodness.endpoint.questionnaire.model.InterpretationKeyRange;
import ru.xgodness.endpoint.questionnaire.model.QuestionnaireType;

import java.util.List;

@Getter
@SuperBuilder
public abstract class QuestionnaireTemplate {
    private String name;
    private QuestionnaireType type;
    private String displayName;
    private String description;
    private int questionCount;
    private int answerCount;
    private List<Integer> answerWeights;
    private List<InterpretationKeyRange> interpretationKeyRanges;
}
