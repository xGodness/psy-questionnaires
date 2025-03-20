package ru.xgodness.questionnaire.model;

public enum QuestionnaireType {
    SELECTION("selection"),
    EVALUATION("evaluation");

    final String label;

    QuestionnaireType(String label) {
        this.label = label.toLowerCase();
    }

    public static QuestionnaireType fromLabel(String label) {
        return QuestionnaireType.valueOf(label);
    }
}
