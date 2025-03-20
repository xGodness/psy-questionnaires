package ru.xgodness.endpoint.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.xgodness.persistence.ConnectionManager;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public String ping() {
        ConnectionManager.stub();
        return "OK";
    }
}
