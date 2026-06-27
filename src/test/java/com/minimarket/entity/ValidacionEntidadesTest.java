package com.minimarket.entity;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class ValidacionEntidadesTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void productoInvalidoDebeTenerViolaciones() {
        Producto producto = new Producto();
        producto.setNombre("");
        producto.setPrecio(0.0);
        producto.setStock(-1);

        assertFalse(validator.validate(producto).isEmpty());
    }

    @Test
    void ventaSinUsuarioFechaNiDetallesDebeTenerViolaciones() {
        Venta venta = new Venta();

        assertFalse(validator.validate(venta).isEmpty());
    }

    @Test
    void detalleVentaInvalidoDebeTenerViolaciones() {
        DetalleVenta detalle = new DetalleVenta();
        detalle.setCantidad(0);
        detalle.setPrecio(0.0);

        assertFalse(validator.validate(detalle).isEmpty());
    }

    @Test
    void ventaConDetalleInvalidoPropagaValidacion() {
        Usuario usuario = new Usuario();
        usuario.setUsername("cliente");
        usuario.setPassword("demo");
        Venta venta = new Venta();
        venta.setUsuario(usuario);
        venta.setFecha(LocalDateTime.now());
        venta.setDetalles(List.of(new DetalleVenta()));

        assertFalse(validator.validate(venta).isEmpty());
    }
}
