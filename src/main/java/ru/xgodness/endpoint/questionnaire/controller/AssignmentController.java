package ru.xgodness.endpoint.questionnaire.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.xgodness.endpoint.questionnaire.dto.AssignmentRequest;
import ru.xgodness.endpoint.questionnaire.dto.QuestionnaireIdentifierList;
import ru.xgodness.endpoint.questionnaire.service.AssignmentService;

@RestController
@RequestMapping("/assignment")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PreAuthorize("hasAuthority('SPECIALIST')")
    @PostMapping("/create")
    public ResponseEntity<Void> createAssignment(@RequestBody AssignmentRequest request) {
        assignmentService.createAssignment(request);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('SPECIALIST')")
    @PostMapping("/delete")
    public ResponseEntity<Void> deleteAssignment(@RequestBody AssignmentRequest request) {
        assignmentService.deleteAssignment(request);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('CLIENT')")
    @GetMapping("/list")
    public ResponseEntity<QuestionnaireIdentifierList> getAssignedToClient() {
        return ResponseEntity.ok(assignmentService.findAllAssignedToClient());
    }

    @PreAuthorize("hasAuthority('SPECIALIST')")
    @GetMapping("/list/{client-username}")
    public ResponseEntity<QuestionnaireIdentifierList> getAssignedToClientBySpecialist(@PathVariable(name = "client-username") String clientUsername) {
        return ResponseEntity.ok(assignmentService.findAllAssignedToClient(clientUsername));
    }
}
