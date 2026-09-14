package cl.duoc.barriodigital.bff.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// Endpoint publico, no requiere token (ver SecurityConfig).
@RestController
public class TestController {

    @GetMapping("/api/public")
    public String publicEndpoint() {
        return "API funcionando correctamente";
    }
}
