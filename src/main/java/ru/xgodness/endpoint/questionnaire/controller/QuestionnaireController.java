package ru.xgodness.endpoint.questionnaire.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.xgodness.endpoint.questionnaire.dto.ClientHistory;
import ru.xgodness.endpoint.questionnaire.dto.QuestionnaireAnswersState;
import ru.xgodness.endpoint.questionnaire.dto.QuestionnaireForm;
import ru.xgodness.endpoint.questionnaire.dto.QuestionnaireIdentifierList;
import ru.xgodness.endpoint.questionnaire.service.QuestionnaireCompletionService;
import ru.xgodness.endpoint.questionnaire.service.QuestionnaireIdentifierService;

@RestController
@RequestMapping("/questionnaire")
@RequiredArgsConstructor
public class QuestionnaireController {

    private final QuestionnaireIdentifierService questionnaireIdentifierService;
    private final QuestionnaireCompletionService completionService;

    @PreAuthorize("hasAuthority('SPECIALIST')")
    @GetMapping("/list")
    public ResponseEntity<QuestionnaireIdentifierList> getAllIdentifiers(@RequestParam(name = "filter", required = false) String pattern) {
        QuestionnaireIdentifierList result;

        if (pattern == null)
            result = questionnaireIdentifierService.findAllIdentifiers();
        else
            result = questionnaireIdentifierService.findAllIdentifiersWithDisplayNameLike(pattern);

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('SPECIALIST') || hasAuthority('CLIENT')")
    @GetMapping("/{questionnaire-id}")
    public ResponseEntity<QuestionnaireForm> getForm(@PathVariable(name = "questionnaire-id") long questionnaireId) {
        return ResponseEntity.ok(completionService.getForm(questionnaireId));
    }

    @PreAuthorize("hasAuthority('CLIENT')")
    @GetMapping("/{questionnaire-id}/state")
    public ResponseEntity<QuestionnaireAnswersState> getState(@PathVariable(name = "questionnaire-id") long questionnaireId) {
        return ResponseEntity.ok(completionService.getState(questionnaireId));
    }

    @PreAuthorize("hasAuthority('CLIENT')")
    @PutMapping("/{questionnaire-id}/update")
    public ResponseEntity<QuestionnaireAnswersState> updateState(
            @PathVariable(name = "questionnaire-id") long questionnaireId,
            @RequestBody QuestionnaireAnswersState request) {
        return ResponseEntity.ok(completionService.updateState(questionnaireId, request));
    }

    @PreAuthorize("hasAuthority('CLIENT')")
    @PostMapping("/{questionnaire-id}/complete")
    public ResponseEntity<Void> complete(@PathVariable(name = "questionnaire-id") long questionnaireId) {
        completionService.complete(questionnaireId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('SPECIALIST')")
    @GetMapping("/history/{client-username}")
    public ResponseEntity<ClientHistory> getClientHistory(@PathVariable(name = "client-username") String clientUsername) {
        return ResponseEntity.ok(completionService.getClientHistory(clientUsername));
    }

}
