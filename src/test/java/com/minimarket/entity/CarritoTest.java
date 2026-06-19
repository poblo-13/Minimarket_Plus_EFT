package com.minimarket.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CarritoTest {

    private Carrito carrito;
    private Usuario usuarioMock;
    private Producto productoMock;

    @BeforeEach
    public void setUp() {
        // Inicializamos el objeto principal
        carrito = new Carrito();
        
        // Creamos un usuario simulado
        usuarioMock = new Usuario();
        usuarioMock.setId(1L);
        usuarioMock.setUsername("cliente1");

        // Creamos un producto simulado
        productoMock = new Producto();
        productoMock.setId(10L);
        productoMock.setNombre("Galletas");
    }

    @Test
    public void testGettersYSetters() {
        // Act: Asignamos todos los valores al carrito
        carrito.setId(5L);
        carrito.setUsuario(usuarioMock);
        carrito.setProducto(productoMock);
        carrito.setCantidad(3);

        // Assert: Validamos los campos básicos
        assertEquals(5L, carrito.getId(), "El ID debe ser 5L");
        assertEquals(3, carrito.getCantidad(), "La cantidad debe ser 3");
        
        // Assert: Validamos la relación con Usuario
        assertNotNull(carrito.getUsuario(), "El usuario no debe ser nulo");
        assertEquals(1L, carrito.getUsuario().getId(), "El ID del usuario debe coincidir");
        
        // Assert: Validamos la relación con Producto
        assertNotNull(carrito.getProducto(), "El producto no debe ser nulo");
        assertEquals(10L, carrito.getProducto().getId(), "El ID del producto debe coincidir");
    }
}