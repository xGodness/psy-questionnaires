package ru.xgodness.endpoint.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.xgodness.endpoint.user.dto.BindList;
import ru.xgodness.endpoint.user.dto.ClientUsernameList;
import ru.xgodness.endpoint.user.service.SpecialistService;

@RestController
@RequestMapping("/specialist")
@PreAuthorize("hasAuthority('SPECIALIST')")
@RequiredArgsConstructor
public class SpecialistController {

    private final SpecialistService specialistService;

    @GetMapping("/bind/get/pending")
    public ResponseEntity<ClientUsernameList> getPendingBinds() {
        return new ResponseEntity<>(specialistService.getPendingBindsClientUsernames(), HttpStatus.OK);
    }

    @GetMapping("/bind/get/approved")
    public ResponseEntity<ClientUsernameList> getApprovedBinds() {
        return new ResponseEntity<>(specialistService.getApprovedBindsClientUsernames(), HttpStatus.OK);
    }

    @GetMapping("/bind/get/all")
    public ResponseEntity<BindList> getAllBinds() {
        return new ResponseEntity<>(specialistService.getAllBindsClientUsernames(), HttpStatus.OK);
    }

    @PostMapping("/bind/approve/{client-username}")
    public ResponseEntity<Void> approvePendingBind(@PathVariable("client-username") String clientUsername) {
        specialistService.approvePendingBind(clientUsername);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bind/discard/{client-username}")
    public ResponseEntity<Void> discardPendingBind(@PathVariable("client-username") String clientUsername) {
        specialistService.discardPendingBind(clientUsername);
        return ResponseEntity.ok().build();
    }

}
