package ru.xgodness.endpoint.questionnaire.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Collection;
import java.util.List;

@Getter
@SuperBuilder
@AllArgsConstructor
public abstract class Questionnaire {
    private long id;
    private String name;
    private QuestionnaireType type;
    private String displayName;
    private String description;
    private int questionCount;
    private int answerCount;
    private List<Integer> answerWeights;
    private List<InterpretationKeyRange> interpretationKeyRanges;

    public int calculateResultSum(Collection<Integer> answers) {
        int resultSum = 0;
        for (var value : answers)
            resultSum += answerWeights.get(value - 1);
        return resultSum;
    }

    public String interpreterResult(int resultSum) {
        for (var keyRange : interpretationKeyRanges) {
            if (keyRange.getMin() <= resultSum && resultSum <= keyRange.getMax())
                return keyRange.getInterpretation();
        }

        throw new IllegalStateException("Result sum does not fit in questionnaire key range");
    }
}
