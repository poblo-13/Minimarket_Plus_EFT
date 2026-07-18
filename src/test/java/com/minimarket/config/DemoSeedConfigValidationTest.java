package com.minimarket.config;

import com.minimarket.abastecimiento.ProveedorRepository;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.sucursal.StockSucursalRepository;
import com.minimarket.sucursal.SucursalRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class DemoSeedConfigValidationTest {

    @Test
    void seedHabilitadoSinVariablesRequeridasFallaConMensajeClaro() {
        CommandLineRunner seed = new DemoSeedConfig().seedDatosDemo(
                mock(RolRepository.class), mock(UsuarioRepository.class), mock(CategoriaRepository.class),
                mock(ProductoRepository.class), mock(SucursalRepository.class), mock(ProveedorRepository.class),
                mock(StockSucursalRepository.class), new BCryptPasswordEncoder(), true,
                "", "", "", "", "", "");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> seed.run());

        assertTrue(exception.getMessage().contains("DEMO_ADMIN_USERNAME"));
    }
}
