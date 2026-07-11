package com.minimarket.controller;

import com.minimarket.entity.Venta;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.service.VentaService;
import com.minimarket.repository.UsuarioRepository;
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
import java.util.List;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class VentaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VentaService ventaService;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks
    private VentaController ventaController;

    private Venta ventaMock;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ventaController).build();
        objectMapper = new ObjectMapper().findAndRegisterModules();

        ventaMock = new Venta();
        ventaMock.setId(100L);
        ventaMock.setFecha(LocalDateTime.now());
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cliente");
        usuario.setPassword("demo");
        ventaMock.setUsuario(usuario);
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Galletas");
        producto.setPrecio(1500.0);
        producto.setStock(20);
        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(producto);
        detalle.setCantidad(2);
        detalle.setPrecio(1500.0);
        ventaMock.setDetalles(List.of(detalle));
    }

    @Test
    public void testListarVentas() throws Exception {
        when(ventaService.findAll()).thenReturn(Arrays.asList(ventaMock));

        mockMvc.perform(get("/api/ventas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.links[0].rel").value("self"));
    }

    @Test
    public void testObtenerVentaPorId_Encontrado() throws Exception {
        when(ventaService.findById(100L)).thenReturn(ventaMock);

        mockMvc.perform(get("/api/ventas/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    public void testObtenerVentaPorId_NoEncontrado() throws Exception {
        when(ventaService.findById(99L)).thenReturn(null);

        mockMvc.perform(get("/api/ventas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGuardarVenta() throws Exception {
        when(usuarioRepository.findById(1L)).thenReturn(java.util.Optional.of(ventaMock.getUsuario()));
        when(ventaService.save(any(Venta.class))).thenReturn(ventaMock);

        mockMvc.perform(post("/api/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuarioId\":1,\"fecha\":\"2025-01-01T10:00:00\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/ventas/100"))
                .andExpect(jsonPath("$.id").value(100L));
    }
}
