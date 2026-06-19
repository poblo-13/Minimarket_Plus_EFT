package com.minimarket.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

public class InventarioTest {

    private Inventario inventario;
    private Producto productoMock;
    private Date fechaPrueba;

    @BeforeEach
    public void setUp() {
        // Inicializamos el objeto principal
        inventario = new Inventario();
        
        // Creamos un producto simulado
        productoMock = new Producto();
        productoMock.setId(100L);
        productoMock.setNombre("Bebida Cola");

        // Fijamos una fecha exacta para la prueba
        fechaPrueba = new Date();
    }

    @Test
    public void testGettersYSetters() {
        // Act: Asignamos todos los valores al inventario
        inventario.setId(15L);
        inventario.setProducto(productoMock);
        inventario.setCantidad(50);
        inventario.setTipoMovimiento("Entrada");
        inventario.setFechaMovimiento(fechaPrueba);

        // Assert: Validamos los campos básicos
        assertEquals(15L, inventario.getId(), "El ID debe ser 15L");
        assertEquals(50, inventario.getCantidad(), "La cantidad debe ser 50");
        assertEquals("Entrada", inventario.getTipoMovimiento(), "El tipo de movimiento debe ser 'Entrada'");
        assertEquals(fechaPrueba, inventario.getFechaMovimiento(), "La fecha de movimiento debe coincidir exactamente");
        
        // Assert: Validamos la relación con Producto
        assertNotNull(inventario.getProducto(), "El producto no debe ser nulo");
        assertEquals(100L, inventario.getProducto().getId(), "El ID del producto asociado debe coincidir");
    }
}