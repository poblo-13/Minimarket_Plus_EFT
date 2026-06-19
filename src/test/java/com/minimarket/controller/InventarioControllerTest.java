package com.minimarket.controller;

import com.minimarket.entity.Inventario;
import com.minimarket.service.InventarioService;
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
public class InventarioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InventarioService inventarioService;

    @InjectMocks
    private InventarioController inventarioController;

    private Inventario inventarioMock;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(inventarioController).build();
        objectMapper = new ObjectMapper();

        inventarioMock = new Inventario();
        inventarioMock.setId(15L);
        inventarioMock.setCantidad(50);
        inventarioMock.setTipoMovimiento("Entrada");
    }

    @Test
    public void testListarMovimientosDeInventario() throws Exception {
        when(inventarioService.findAll()).thenReturn(Arrays.asList(inventarioMock));

        mockMvc.perform(get("/api/inventario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(15L))
                .andExpect(jsonPath("$[0].tipoMovimiento").value("Entrada"));
    }

    @Test
    public void testObtenerMovimientoPorId_Encontrado() throws Exception {
        when(inventarioService.findById(15L)).thenReturn(inventarioMock);

        mockMvc.perform(get("/api/inventario/15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(15L))
                .andExpect(jsonPath("$.cantidad").value(50));
    }

    @Test
    public void testObtenerMovimientoPorId_NoEncontrado() throws Exception {
        when(inventarioService.findById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/inventario/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testRegistrarMovimiento() throws Exception {
        when(inventarioService.save(any(Inventario.class))).thenReturn(inventarioMock);

        mockMvc.perform(post("/api/inventario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inventarioMock)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(15L));
    }

    @Test
    public void testActualizarMovimiento_Encontrado() throws Exception {
        when(inventarioService.findById(15L)).thenReturn(inventarioMock);
        when(inventarioService.save(any(Inventario.class))).thenReturn(inventarioMock);

        mockMvc.perform(put("/api/inventario/15")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inventarioMock)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(15L));
    }

    @Test
    public void testActualizarMovimiento_NoEncontrado() throws Exception {
        when(inventarioService.findById(99L)).thenReturn(null);

        mockMvc.perform(put("/api/inventario/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inventarioMock)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testEliminarMovimiento_Encontrado() throws Exception {
        when(inventarioService.findById(15L)).thenReturn(inventarioMock);

        mockMvc.perform(delete("/api/inventario/15"))
                .andExpect(status().isNoContent());

        verify(inventarioService, times(1)).deleteById(15L);
    }

    @Test
    public void testEliminarMovimiento_NoEncontrado() throws Exception {
        when(inventarioService.findById(99L)).thenReturn(null);

        mockMvc.perform(delete("/api/inventario/99"))
                .andExpect(status().isNotFound());
    }
}