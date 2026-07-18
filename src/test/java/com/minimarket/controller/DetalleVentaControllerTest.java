package com.minimarket.controller;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.service.DetalleVentaService;
import com.minimarket.repository.DetalleVentaRepository;
import com.minimarket.security.CurrentActorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DetalleVentaControllerTest {
    @Mock DetalleVentaService detalleVentaService;
    @Mock DetalleVentaRepository detalleVentaRepository;
    @Mock CurrentActorService currentActor;
    @InjectMocks DetalleVentaController controller;
    MockMvc mockMvc;

    @BeforeEach void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test void readsAreAvailable() throws Exception {
        when(currentActor.isStaff()).thenReturn(true);
        when(detalleVentaRepository.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/api/detalle-ventas")).andExpect(status().isOk());
    }

    @Test void mutationsAreMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/api/detalle-ventas").content("{}")).andExpect(status().isMethodNotAllowed());
        mockMvc.perform(put("/api/detalle-ventas/1").content("{}")).andExpect(status().isMethodNotAllowed());
        mockMvc.perform(delete("/api/detalle-ventas/1")).andExpect(status().isMethodNotAllowed());
    }
}
