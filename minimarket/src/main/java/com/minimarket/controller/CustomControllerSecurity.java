package com.minimarket.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app")
public class CustomControllerSecurity {

    @GetMapping("/index_normal")
    public String indexNormal() {
        return "¡Bienvenido a la Zona Libre! Este endpoint es público y no requiere inicio de sesión.";
    }

    @GetMapping("/index_protegido")
    public String indexProtegido() {
        return "¡Bienvenido a la Zona Blindada! Si lees esto, es porque pasaste la autenticación de Spring Security.";
    }
}