package com.minimarket.controller;

import com.minimarket.entity.Producto;
import com.minimarket.service.ProductoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ProductoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductoService productoService;

    @InjectMocks
    private ProductoController productoController;

    private Producto productoMock;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productoController).build();
        objectMapper = new ObjectMapper();

        productoMock = new Producto();
        productoMock.setId(1L);
        productoMock.setNombre("Galletas");
        productoMock.setPrecio(1200.0);
        productoMock.setStock(20);
    }

    @Test
    public void testListarProductos() throws Exception {
        when(productoService.findAll()).thenReturn(Arrays.asList(productoMock));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].nombre").value("Galletas"));
    }

    @Test
    public void testObtenerProductoPorId_Encontrado() throws Exception {
        when(productoService.findById(1L)).thenReturn(productoMock);

        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Galletas"));
    }

    @Test
    public void testObtenerProductoPorId_NoEncontrado() throws Exception {
        when(productoService.findById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/productos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGuardarProducto() throws Exception {
        when(productoService.save(any(Producto.class))).thenReturn(productoMock);

        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productoMock)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    public void testActualizarProducto_Encontrado() throws Exception {
        when(productoService.findById(1L)).thenReturn(productoMock);
        when(productoService.save(any(Producto.class))).thenReturn(productoMock);

        mockMvc.perform(put("/api/productos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productoMock)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    public void testActualizarProducto_NoEncontrado() throws Exception {
        when(productoService.findById(99L)).thenReturn(null);

        mockMvc.perform(put("/api/productos/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productoMock)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testEliminarProducto_Encontrado() throws Exception {
        when(productoService.findById(1L)).thenReturn(productoMock);

        mockMvc.perform(delete("/api/productos/1"))
                .andExpect(status().isNoContent());

        verify(productoService, times(1)).deleteById(1L);
    }

    @Test
    public void testEliminarProducto_NoEncontrado() throws Exception {
        when(productoService.findById(99L)).thenReturn(null);

        mockMvc.perform(delete("/api/productos/99"))
                .andExpect(status().isNotFound());
    }
}