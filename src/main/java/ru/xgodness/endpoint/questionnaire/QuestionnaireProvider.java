package ru.xgodness.endpoint.questionnaire;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.xgodness.endpoint.questionnaire.model.*;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

@Log
public class QuestionnaireProvider {
    private static final String QUESTIONNAIRE_FILE_SUFFIX = ".questionnaire.json";
    private static final String QUESTIONNAIRES_DIR_PATH = "src/main/resources/questionnaires";
    @Getter
    private static final Map<String, Questionnaire> questionnaireMap = new HashMap<>();

    static {
        try {
            initializeQuestionnaires();
        } catch (Exception ex) {
            log.severe(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    @SneakyThrows
    private static void initializeQuestionnaires() {
        try (Stream<Path> paths = Files.walk(Paths.get(QUESTIONNAIRES_DIR_PATH))) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(QUESTIONNAIRE_FILE_SUFFIX))
                    .forEach(QuestionnaireProvider::mapQuestionnaire);
        }
    }

    @SneakyThrows(IOException.class)
    private static void mapQuestionnaire(Path questionnaireFilePath) {
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
        List<KeyRange> keyRanges = new ArrayList<>();
        JSONArray keyRangeJson;
        for (int i = 0; i < keyRangesJson.length(); i++) {
            keyRangeJson = keyRangesJson.getJSONArray(i);
            keyRanges.add(new KeyRange(
                    keyRangeJson.getInt(0),
                    keyRangeJson.getInt(1),
                    keyRangeJson.getString(2)
            ));
        }

        Questionnaire.QuestionnaireBuilder<?, ?> builder;
        switch (requireNonNull(type)) {
            case SELECTION -> builder = mapSelectionQuestionnaire(rootObj);
            case EVALUATION -> builder = mapEvaluationQuestionnaire(rootObj);
            default -> throw new InvalidObjectException("Invalid questionnaire type");
        }

        questionnaireMap.put(
                // TODO: display_name or name as key?
                name,
                builder
                        .name(name)
                        .type(type)
                        .displayName(displayName)
                        .description(description)
                        .questionCount(questionCount)
                        .answerCount(answerCount)
                        .answerWeights(answerWeights)
                        .keyRanges(keyRanges)
                        .build()
        );
    }

    private static SelectionQuestionnaire.SelectionQuestionnaireBuilder<?, ?> mapSelectionQuestionnaire(JSONObject rootObj) {
        List<List<String>> answerOptions = new ArrayList<>();
        JSONArray jsonArray = rootObj.getJSONArray("answer_options");
        for (var i = 0; i < jsonArray.length(); i++)
            answerOptions.add(jsonArray.getJSONArray(i).toList().stream().map(Object::toString).toList());

        return SelectionQuestionnaire.builder().answerOptions(answerOptions);
    }

    private static EvaluationQuestionnaire.EvaluationQuestionnaireBuilder<?, ?> mapEvaluationQuestionnaire(JSONObject rootObj) {
        return EvaluationQuestionnaire.builder()
                .answerOptions(
                        rootObj.getJSONArray("answer_options").toList().stream().map(Object::toString).toList())
                .questions(
                        rootObj.getJSONArray("questions").toList().stream().map(Object::toString).toList());
    }
}
