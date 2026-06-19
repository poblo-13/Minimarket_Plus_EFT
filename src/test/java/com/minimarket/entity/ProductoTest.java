package com.minimarket.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ProductoTest {

    private Producto producto;
    private Categoria categoriaMock;

    @BeforeEach
    public void setUp() {
        // Inicializamos los objetos antes de cada prueba
        producto = new Producto();
        
        // Creamos una categoría simulada para asignarla al producto
        categoriaMock = new Categoria();
        categoriaMock.setId(1L);
    }

    @Test
    public void testGettersYSetters() {
        // Act: Asignamos valores a todos los atributos del producto
        producto.setId(10L);
        producto.setNombre("Leche Entera");
        producto.setPrecio(1500.0);
        producto.setStock(50);
        producto.setCategoria(categoriaMock);

        // Assert: Verificamos que los valores devueltos sean exactamente los que ingresamos
        assertEquals(10L, producto.getId(), "El ID debe ser 10L");
        assertEquals("Leche Entera", producto.getNombre(), "El nombre debe coincidir");
        assertEquals(1500.0, producto.getPrecio(), "El precio debe ser 1500.0");
        assertEquals(50, producto.getStock(), "El stock debe ser 50");
        
        // Assert: Validamos la relación con la categoría
        assertNotNull(producto.getCategoria(), "La categoría no debe ser nula");
        assertEquals(1L, producto.getCategoria().getId(), "El ID de la categoría asociada debe ser 1L");
    }
}