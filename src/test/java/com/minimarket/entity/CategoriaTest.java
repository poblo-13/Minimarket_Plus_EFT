package com.minimarket.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class CategoriaTest {

    private Categoria categoria;
    private List<Producto> productosMock;

    @BeforeEach
    public void setUp() {
        // Inicializamos el objeto principal
        categoria = new Categoria();
        
        // Creamos una lista simulada de productos para probar la relación
        Producto producto1 = new Producto();
        producto1.setId(10L);
        producto1.setNombre("Queso");
        
        productosMock = new ArrayList<>();
        productosMock.add(producto1);
    }

    @Test
    public void testGettersYSetters() {
        // Act: Asignamos todos los valores a la categoría
        categoria.setId(1L);
        categoria.setNombre("Lácteos");
        categoria.setProductos(productosMock);

        // Assert: Validamos los campos básicos
        assertEquals(1L, categoria.getId(), "El ID debe ser 1L");
        assertEquals("Lácteos", categoria.getNombre(), "El nombre debe coincidir con 'Lácteos'");
        
        // Assert: Validamos la relación con la lista de Productos
        assertNotNull(categoria.getProductos(), "La lista de productos no debe ser nula");
        assertEquals(1, categoria.getProductos().size(), "La lista debe contener exactamente 1 producto");
        assertEquals(10L, categoria.getProductos().get(0).getId(), "El ID del producto en la lista debe ser 10L");
    }
}