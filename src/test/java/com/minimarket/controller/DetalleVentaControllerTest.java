package com.minimarket.controller;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.service.DetalleVentaService;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.VentaRepository;
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
public class DetalleVentaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DetalleVentaService detalleVentaService;
    @Mock private ProductoRepository productoRepository;
    @Mock private VentaRepository ventaRepository;

    @InjectMocks
    private DetalleVentaController detalleVentaController;

    private DetalleVenta detalleVentaMock;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        // Configuramos el simulador de peticiones web
        mockMvc = MockMvcBuilders.standaloneSetup(detalleVentaController).build();
        objectMapper = new ObjectMapper();

        detalleVentaMock = new DetalleVenta();
        detalleVentaMock.setId(5L);
        detalleVentaMock.setCantidad(3);
        detalleVentaMock.setPrecio(1500.0);
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Galletas");
        producto.setPrecio(1500.0);
        producto.setStock(20);
        detalleVentaMock.setProducto(producto);
    }

    @Test
    public void testListarDetalleVentas() throws Exception {
        when(detalleVentaService.findAll()).thenReturn(Arrays.asList(detalleVentaMock));

        // Simulamos GET
        mockMvc.perform(get("/api/detalle-ventas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.links[0].rel").value("self"));
    }

    @Test
    public void testObtenerDetalleVentaPorId_Encontrado() throws Exception {
        when(detalleVentaService.findById(5L)).thenReturn(detalleVentaMock);

        // Simulamos GET con ID válido
        mockMvc.perform(get("/api/detalle-ventas/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.cantidad").value(3));
    }

    @Test
    public void testObtenerDetalleVentaPorId_NoEncontrado() throws Exception {
        when(detalleVentaService.findById(99L)).thenReturn(null);

        // Simulamos GET con ID inexistente (esperamos un 404)
        mockMvc.perform(get("/api/detalle-ventas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGuardarDetalleVenta() throws Exception {
        com.minimarket.entity.Venta venta = new com.minimarket.entity.Venta(); venta.setId(1L);
        detalleVentaMock.setVenta(venta);
        when(ventaRepository.findById(1L)).thenReturn(java.util.Optional.of(venta));
        when(productoRepository.findById(1L)).thenReturn(java.util.Optional.of(detalleVentaMock.getProducto()));
        when(detalleVentaService.save(any(DetalleVenta.class))).thenReturn(detalleVentaMock);

        // Simulamos POST
        mockMvc.perform(post("/api/detalle-ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ventaId\":1,\"productoId\":1,\"cantidad\":3,\"precio\":1500.0}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/detalle-ventas/5"))
                .andExpect(jsonPath("$.id").value(5L));
    }

    @Test
    public void testActualizarDetalleVenta_Encontrado() throws Exception {
        when(detalleVentaService.findById(5L)).thenReturn(detalleVentaMock);
        com.minimarket.entity.Venta venta = new com.minimarket.entity.Venta(); venta.setId(1L);
        detalleVentaMock.setVenta(venta);
        when(ventaRepository.findById(1L)).thenReturn(java.util.Optional.of(venta));
        when(productoRepository.findById(1L)).thenReturn(java.util.Optional.of(detalleVentaMock.getProducto()));
        when(detalleVentaService.save(any(DetalleVenta.class))).thenReturn(detalleVentaMock);

        // Simulamos PUT
        mockMvc.perform(put("/api/detalle-ventas/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ventaId\":1,\"productoId\":1,\"cantidad\":3,\"precio\":1500.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L));
    }

    @Test
    public void testActualizarDetalleVenta_NoEncontrado() throws Exception {
        when(detalleVentaService.findById(99L)).thenReturn(null);

        // Simulamos PUT a ID inexistente
        mockMvc.perform(put("/api/detalle-ventas/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ventaId\":1,\"productoId\":1,\"cantidad\":3,\"precio\":1500.0}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testEliminarDetalleVenta_Encontrado() throws Exception {
        when(detalleVentaService.findById(5L)).thenReturn(detalleVentaMock);

        // Simulamos DELETE
        mockMvc.perform(delete("/api/detalle-ventas/5"))
                .andExpect(status().isNoContent());

        verify(detalleVentaService, times(1)).deleteById(5L);
    }

    @Test
    public void testEliminarDetalleVenta_NoEncontrado() throws Exception {
        when(detalleVentaService.findById(99L)).thenReturn(null);

        // Simulamos DELETE a ID inexistente
        mockMvc.perform(delete("/api/detalle-ventas/99"))
                .andExpect(status().isNotFound());
    }
}
