package ru.xgodness.endpoint.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.xgodness.endpoint.user.dto.BindList;
import ru.xgodness.endpoint.user.dto.ClientUsernameList;
import ru.xgodness.endpoint.user.service.BindService;

@RestController
@RequestMapping("/bind")
@RequiredArgsConstructor
public class BindController {

    private final BindService bindService;

    @PreAuthorize("hasAuthority('CLIENT')")
    @PostMapping("/{specialist-username}/request")
    public ResponseEntity<Void> requestSpecialistBind(@PathVariable("specialist-username") String specialistUsername) {
        bindService.requestSpecialistBind(specialistUsername);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('SPECIALIST')")
    @GetMapping("/get/pending")
    public ResponseEntity<ClientUsernameList> getPendingBinds() {
        return new ResponseEntity<>(bindService.getPendingBindsClientUsernames(), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('SPECIALIST')")
    @GetMapping("/get/approved")
    public ResponseEntity<ClientUsernameList> getApprovedBinds() {
        return new ResponseEntity<>(bindService.getApprovedBindsClientUsernames(), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('SPECIALIST')")
    @GetMapping("/get/all")
    public ResponseEntity<BindList> getAllBinds() {
        return new ResponseEntity<>(bindService.getAllBindsClientUsernames(), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('SPECIALIST')")
    @PostMapping("/{client-username}/approve")
    public ResponseEntity<Void> approvePendingBind(@PathVariable("client-username") String clientUsername) {
        bindService.approvePendingBind(clientUsername);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('SPECIALIST')")
    @PostMapping("/{client-username}/discard")
    public ResponseEntity<Void> discardPendingBind(@PathVariable("client-username") String clientUsername) {
        bindService.discardPendingBind(clientUsername);
        return ResponseEntity.noContent().build();
    }

}
