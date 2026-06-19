package com.minimarket.controller;

import com.minimarket.entity.Usuario;
import com.minimarket.service.UsuarioService;
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
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    private Usuario usuarioMock;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController).build();
        objectMapper = new ObjectMapper();

        usuarioMock = new Usuario();
        usuarioMock.setId(1L);
        usuarioMock.setUsername("goleador99");
        usuarioMock.setPassword("claveSegura123");
    }

    @Test
    public void testListarUsuarios() throws Exception {
        when(usuarioService.findAll()).thenReturn(Arrays.asList(usuarioMock));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].username").value("goleador99"));
    }

    @Test
    public void testObtenerUsuarioPorId_Encontrado() throws Exception {
        // Aquí usamos Optional.of() porque el servicio devuelve un Optional
        when(usuarioService.findById(1L)).thenReturn(Optional.of(usuarioMock));

        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("goleador99"));
    }

    @Test
    public void testObtenerUsuarioPorId_NoEncontrado() throws Exception {
        // Simulamos que no se encontró el usuario
        when(usuarioService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/usuarios/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGuardarUsuario() throws Exception {
        when(usuarioService.save(any(Usuario.class))).thenReturn(usuarioMock);

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuarioMock)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    public void testActualizarUsuario_Encontrado() throws Exception {
        when(usuarioService.findById(1L)).thenReturn(Optional.of(usuarioMock));
        when(usuarioService.save(any(Usuario.class))).thenReturn(usuarioMock);

        mockMvc.perform(put("/api/usuarios/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuarioMock)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    public void testActualizarUsuario_NoEncontrado() throws Exception {
        when(usuarioService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/usuarios/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuarioMock)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testEliminarUsuario_Encontrado() throws Exception {
        when(usuarioService.findById(1L)).thenReturn(Optional.of(usuarioMock));

        mockMvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isNoContent());

        verify(usuarioService, times(1)).deleteById(1L);
    }

    @Test
    public void testEliminarUsuario_NoEncontrado() throws Exception {
        when(usuarioService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/usuarios/99"))
                .andExpect(status().isNotFound());
    }
}