package com.minimarket.controller;

import com.minimarket.entity.Categoria;
import com.minimarket.service.CategoriaService;
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
public class CategoriaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CategoriaService categoriaService;

    @InjectMocks
    private CategoriaController categoriaController;

    private Categoria categoriaMock;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        // Configuramos nuestra cancha de pruebas web
        mockMvc = MockMvcBuilders.standaloneSetup(categoriaController).build();
        objectMapper = new ObjectMapper();

        categoriaMock = new Categoria();
        categoriaMock.setId(1L);
        categoriaMock.setNombre("Bebidas");
    }

    @Test
    public void testListarCategorias() throws Exception {
        when(categoriaService.findAll()).thenReturn(Arrays.asList(categoriaMock));

        // Simulamos GET a /api/categorias
        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.links[0].rel").value("self"));
    }

    @Test
    public void testObtenerCategoriaPorId_Encontrado() throws Exception {
        when(categoriaService.findById(1L)).thenReturn(categoriaMock);

        // Simulamos GET a /api/categorias/1
        mockMvc.perform(get("/api/categorias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Bebidas"));
    }

    @Test
    public void testObtenerCategoriaPorId_NoEncontrado() throws Exception {
        when(categoriaService.findById(99L)).thenReturn(null);

        // Simulamos GET a ID inexistente
        mockMvc.perform(get("/api/categorias/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGuardarCategoria() throws Exception {
        when(categoriaService.save(any(Categoria.class))).thenReturn(categoriaMock);

        // Simulamos POST
        mockMvc.perform(post("/api/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Bebidas\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/categorias/1"))
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    public void testActualizarCategoria_Encontrado() throws Exception {
        when(categoriaService.findById(1L)).thenReturn(categoriaMock);
        when(categoriaService.save(any(Categoria.class))).thenReturn(categoriaMock);

        // Simulamos PUT
        mockMvc.perform(put("/api/categorias/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Bebidas\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    public void testActualizarCategoria_NoEncontrado() throws Exception {
        when(categoriaService.findById(99L)).thenReturn(null);

        // Simulamos PUT a ID inexistente
        mockMvc.perform(put("/api/categorias/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Bebidas\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testEliminarCategoria_Encontrado() throws Exception {
        when(categoriaService.findById(1L)).thenReturn(categoriaMock);

        // Simulamos DELETE
        mockMvc.perform(delete("/api/categorias/1"))
                .andExpect(status().isNoContent());

        verify(categoriaService, times(1)).deleteById(1L);
    }

    @Test
    public void testEliminarCategoria_NoEncontrado() throws Exception {
        when(categoriaService.findById(99L)).thenReturn(null);

        // Simulamos DELETE a ID inexistente
        mockMvc.perform(delete("/api/categorias/99"))
                .andExpect(status().isNotFound());
    }
}
