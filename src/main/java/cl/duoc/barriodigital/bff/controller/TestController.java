package cl.duoc.barriodigital.bff.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// Controller de prueba, solo sirve para verificar rapido que la aplicacion
// esta corriendo. Este endpoint es publico, no pide token (ver SecurityConfig,
// donde "/api/public" esta en la lista de rutas permitidas sin login).
@RestController
public class TestController {

    // Responde con un texto simple, sin logica de negocio detras.
    @GetMapping("/api/public")
    public String publicEndpoint() {
        return "API funcionando correctamente";
    }
}
