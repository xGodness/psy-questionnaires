package ru.xgodness.endpoint.questionnaire.template;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.xgodness.endpoint.questionnaire.model.InterpretationKeyRange;
import ru.xgodness.endpoint.questionnaire.model.QuestionnaireType;
import ru.xgodness.exception.ApplicationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

@Log
public class QuestionnaireTemplateBuilder {
    private static final String QUESTIONNAIRE_FILE_SUFFIX = ".questionnaire.json";
    private static final String QUESTIONNAIRES_DIR_PATH = "src/main/resources/questionnaires";

    @Getter
    private static final Set<QuestionnaireTemplate> templates = new HashSet<>();

    static {
        try {
            buildTemplates();
        } catch (Exception ex) {
            log.severe(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @SneakyThrows
    private static void buildTemplates() {
        try (Stream<Path> paths = Files.walk(Paths.get(QUESTIONNAIRES_DIR_PATH))) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(QUESTIONNAIRE_FILE_SUFFIX))
                    .forEach(QuestionnaireTemplateBuilder::parseTemplate);
        }
    }

    @SneakyThrows(IOException.class)
    private static void parseTemplate(Path questionnaireFilePath) {
        String jsonString = Files.readString(questionnaireFilePath);
        JSONObject rootObj = new JSONObject(jsonString);

        var name = rootObj.getString("name");
        var type = QuestionnaireType.fromLabel(rootObj.getString("type").toUpperCase());
        var displayName = rootObj.getString("display_name");
        var description = rootObj.getString("description");
        var questionCount = rootObj.getInt("question_count");
        var answerCount = rootObj.getInt("answer_count");
        var answerWeights = rootObj.getJSONArray("answer_weights").toList()
                .stream().map(entry -> Integer.valueOf(entry.toString())).toList();

        var keyRangesJson = rootObj.getJSONArray("keys");
        List<InterpretationKeyRange> interpretationKeyRanges = new ArrayList<>();
        JSONArray keyRangeJson;
        for (int i = 0; i < keyRangesJson.length(); i++) {
            keyRangeJson = keyRangesJson.getJSONArray(i);
            interpretationKeyRanges.add(new InterpretationKeyRange(
                    keyRangeJson.getInt(0),
                    keyRangeJson.getInt(1),
                    keyRangeJson.getString(2)
            ));
        }

        QuestionnaireTemplate.QuestionnaireTemplateBuilder<?, ?> builder;
        switch (requireNonNull(type)) {
            case SELECTION -> builder = buildSelectionQuestionnaire(rootObj);
            case EVALUATION -> builder = buildEvaluationQuestionnaire(rootObj);
            default -> throw new ApplicationException("Unknown questionnaire type");
        }

        templates.add(
                builder
                        .name(name)
                        .type(type)
                        .displayName(displayName)
                        .description(description)
                        .questionCount(questionCount)
                        .answerCount(answerCount)
                        .answerWeights(answerWeights)
                        .interpretationKeyRanges(interpretationKeyRanges)
                        .build()
        );
    }

    private static SelectionQuestionnaireTemplate.SelectionQuestionnaireTemplateBuilder<?, ?> buildSelectionQuestionnaire(JSONObject rootObj) {
        List<List<String>> answerOptions = new ArrayList<>();
        JSONArray jsonArray = rootObj.getJSONArray("answer_options");
        for (var i = 0; i < jsonArray.length(); i++)
            answerOptions.add(jsonArray.getJSONArray(i).toList().stream().map(Object::toString).toList());

        return SelectionQuestionnaireTemplate.builder().answerOptions(answerOptions);
    }

    private static EvaluationQuestionnaireTemplate.EvaluationQuestionnaireTemplateBuilder<?, ?> buildEvaluationQuestionnaire(JSONObject rootObj) {
        return EvaluationQuestionnaireTemplate.builder()
                .answerOptions(
                        rootObj.getJSONArray("answer_options").toList().stream().map(Object::toString).toList())
                .questions(
                        rootObj.getJSONArray("questions").toList().stream().map(Object::toString).toList());
    }
}
