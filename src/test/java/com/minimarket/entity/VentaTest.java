package com.minimarket.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class VentaTest {

    private Venta venta;
    private Usuario usuarioMock;
    private Date fechaPrueba;
    private List<DetalleVenta> detallesMock;

    @BeforeEach
    public void setUp() {
        // Inicializamos el objeto principal
        venta = new Venta();
        
        // Creamos un usuario simulado
        usuarioMock = new Usuario();
        usuarioMock.setId(1L);
        usuarioMock.setUsername("cliente_estrella");

        // Fijamos una fecha exacta
        fechaPrueba = new Date();

        // Creamos una lista simulada con un detalle de venta
        DetalleVenta detalle = new DetalleVenta();
        detalle.setId(5L);
        detallesMock = new ArrayList<>();
        detallesMock.add(detalle);
    }

    @Test
    public void testGettersYSetters() {
        // Act: Asignamos todos los valores a la venta
        venta.setId(100L);
        venta.setUsuario(usuarioMock);
        venta.setFecha(fechaPrueba);
        venta.setDetalles(detallesMock);

        // Assert: Validamos los campos básicos
        assertEquals(100L, venta.getId(), "El ID debe ser 100L");
        assertEquals(fechaPrueba, venta.getFecha(), "La fecha debe coincidir exactamente");
        
        // Assert: Validamos la relación con Usuario
        assertNotNull(venta.getUsuario(), "El usuario no debe ser nulo");
        assertEquals(1L, venta.getUsuario().getId(), "El ID del usuario asociado debe ser 1L");

        // Assert: Validamos la relación con los Detalles de Venta
        assertNotNull(venta.getDetalles(), "La lista de detalles no debe ser nula");
        assertEquals(1, venta.getDetalles().size(), "La lista debe contener 1 detalle de venta");
        assertEquals(5L, venta.getDetalles().get(0).getId(), "El ID del detalle en la lista debe ser 5L");
    }
}