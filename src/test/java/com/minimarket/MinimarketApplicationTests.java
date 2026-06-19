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
        // Ejecuta el método main para cubrir esa línea específica en JaCoCo
        MinimarketApplication.main(new String[]{});
    }

}