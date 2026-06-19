package com.minimarket.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DetalleVentaTest {

    private DetalleVenta detalleVenta;
    private Venta ventaMock;
    private Producto productoMock;

    @BeforeEach
    public void setUp() {
        // Inicializamos el objeto principal
        detalleVenta = new DetalleVenta();
        
        // Creamos una venta simulada
        ventaMock = new Venta();
        ventaMock.setId(50L);

        // Creamos un producto simulado
        productoMock = new Producto();
        productoMock.setId(10L);
        productoMock.setNombre("Aceite");
    }

    @Test
    public void testGettersYSetters() {
        // Act: Asignamos todos los valores al detalle de la venta
        detalleVenta.setId(5L);
        detalleVenta.setVenta(ventaMock);
        detalleVenta.setProducto(productoMock);
        detalleVenta.setCantidad(2);
        detalleVenta.setPrecio(2500.0);

        // Assert: Validamos los campos numéricos básicos
        assertEquals(5L, detalleVenta.getId(), "El ID debe ser 5L");
        assertEquals(2, detalleVenta.getCantidad(), "La cantidad debe ser 2");
        assertEquals(2500.0, detalleVenta.getPrecio(), "El precio debe ser 2500.0");
        
        // Assert: Validamos la relación con Venta
        assertNotNull(detalleVenta.getVenta(), "La venta no debe ser nula");
        assertEquals(50L, detalleVenta.getVenta().getId(), "El ID de la venta asociada debe coincidir");

        // Assert: Validamos la relación con Producto
        assertNotNull(detalleVenta.getProducto(), "El producto no debe ser nulo");
        assertEquals(10L, detalleVenta.getProducto().getId(), "El ID del producto asociado debe coincidir");
    }
}