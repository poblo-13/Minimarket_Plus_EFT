package com.minimarket;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MinimarketApplicationTests {

    @Test
    void contextLoads() {
        // Valida que el contexto de Spring Boot levanta correctamente sin errores
    }

    @Test
    void main() {
        // Ejecuta main sin servidor web: no depende del puerto 8080 del entorno.
        MinimarketApplication.main(new String[]{"--spring.main.web-application-type=none", "--app.seed.enabled=false"});
    }

}
