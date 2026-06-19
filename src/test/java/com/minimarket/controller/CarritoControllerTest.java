package com.minimarket.controller;

import com.minimarket.entity.Carrito;
import com.minimarket.service.CarritoService;
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
public class CarritoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CarritoService carritoService;

    @InjectMocks
    private CarritoController carritoController;

    private Carrito carritoMock;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        // Configuramos el simulador de peticiones web
        mockMvc = MockMvcBuilders.standaloneSetup(carritoController).build();
        objectMapper = new ObjectMapper(); // Para convertir objetos a JSON

        carritoMock = new Carrito();
        carritoMock.setId(10L);
        carritoMock.setCantidad(2);
    }

    @Test
    public void testListarCarrito() throws Exception {
        when(carritoService.findAll()).thenReturn(Arrays.asList(carritoMock));

        // Simulamos un GET a /api/carrito
        mockMvc.perform(get("/api/carrito"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L));
    }

    @Test
    public void testObtenerCarritoPorId_Encontrado() throws Exception {
        when(carritoService.findById(10L)).thenReturn(carritoMock);

        // Simulamos un GET a /api/carrito/10
        mockMvc.perform(get("/api/carrito/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    public void testObtenerCarritoPorId_NoEncontrado() throws Exception {
        when(carritoService.findById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/carrito/99"))
                .andExpect(status().isNotFound()); // Esperamos un 404
    }

    @Test
    public void testAgregarProductoAlCarrito() throws Exception {
        when(carritoService.save(any(Carrito.class))).thenReturn(carritoMock);

        // Simulamos un POST con un JSON en el cuerpo (Body)
        mockMvc.perform(post("/api/carrito")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(carritoMock)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    public void testActualizarCarrito_Encontrado() throws Exception {
        when(carritoService.findById(10L)).thenReturn(carritoMock);
        when(carritoService.save(any(Carrito.class))).thenReturn(carritoMock);

        // Simulamos un PUT
        mockMvc.perform(put("/api/carrito/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(carritoMock)))
                .andExpect(status().isOk());
    }

    @Test
    public void testActualizarCarrito_NoEncontrado() throws Exception {
        when(carritoService.findById(99L)).thenReturn(null);

        mockMvc.perform(put("/api/carrito/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(carritoMock)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testEliminarProductoDelCarrito_Encontrado() throws Exception {
        when(carritoService.findById(10L)).thenReturn(carritoMock);

        // Simulamos un DELETE
        mockMvc.perform(delete("/api/carrito/10"))
                .andExpect(status().isNoContent()); // Esperamos un 204 No Content

        verify(carritoService, times(1)).deleteById(10L);
    }

    @Test
    public void testEliminarProductoDelCarrito_NoEncontrado() throws Exception {
        when(carritoService.findById(99L)).thenReturn(null);

        mockMvc.perform(delete("/api/carrito/99"))
                .andExpect(status().isNotFound());
    }
}