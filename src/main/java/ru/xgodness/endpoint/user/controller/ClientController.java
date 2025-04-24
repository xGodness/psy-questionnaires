package ru.xgodness.endpoint.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.xgodness.endpoint.user.service.ClientService;

@RestController
@RequestMapping("/client")
@PreAuthorize("hasAuthority('CLIENT')")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping("/bind/request/{specialist-username}")
    public ResponseEntity<Void> requestSpecialistBind(@PathVariable("specialist-username") String specialistUsername) {
        clientService.requestSpecialistBind(specialistUsername);
        return ResponseEntity.noContent().build();
    }

}
