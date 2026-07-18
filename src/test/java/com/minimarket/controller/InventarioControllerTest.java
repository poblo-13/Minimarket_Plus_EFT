package com.minimarket.controller;

import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.exception.InsufficientStockException;
import com.minimarket.service.InventarioService;
import com.minimarket.service.ProductoService;
import com.minimarket.sucursal.SucursalRepository;
import com.minimarket.sucursal.Sucursal;
import com.minimarket.exception.ApiExceptionHandler;
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
import java.time.LocalDateTime;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class InventarioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InventarioService inventarioService;
    @Mock private ProductoService productoService;
    @Mock private SucursalRepository sucursalRepository;

    @InjectMocks
    private InventarioController inventarioController;

    private Inventario inventarioMock;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(inventarioController).setControllerAdvice(new ApiExceptionHandler()).build();
        objectMapper = new ObjectMapper().findAndRegisterModules();

        inventarioMock = new Inventario();
        inventarioMock.setId(15L);
        inventarioMock.setCantidad(50);
        inventarioMock.setTipoMovimiento("Entrada");
        inventarioMock.setFechaMovimiento(LocalDateTime.now());
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Galletas");
        producto.setPrecio(1200.0);
        producto.setStock(20);
        inventarioMock.setProducto(producto);
        Sucursal sucursal = new Sucursal(); sucursal.setId(2L); sucursal.setNombre("Centro"); inventarioMock.setSucursal(sucursal);
    }

    @Test
    public void testListarMovimientosDeInventario() throws Exception {
        when(inventarioService.findAll()).thenReturn(Arrays.asList(inventarioMock));

        mockMvc.perform(get("/api/inventario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.links[0].rel").value("self"));
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
        when(productoService.findById(1L)).thenReturn(inventarioMock.getProducto());
        when(sucursalRepository.findById(2L)).thenReturn(java.util.Optional.of(inventarioMock.getSucursal()));
        when(inventarioService.save(any(Inventario.class))).thenReturn(inventarioMock);

        mockMvc.perform(post("/api/inventario")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sucursalId\":2,\"productoId\":1,\"cantidad\":50,\"tipoMovimiento\":\"Entrada\",\"fechaMovimiento\":\"2025-01-01T10:00:00\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/inventario/15"))
                .andExpect(jsonPath("$.id").value(15L));

        ArgumentCaptor<Inventario> movement = ArgumentCaptor.forClass(Inventario.class);
        verify(inventarioService).save(movement.capture());
        assertNotEquals(LocalDateTime.of(2025, 1, 1, 10, 0), movement.getValue().getFechaMovimiento());
    }

    @Test
    public void testActualizarMovimiento_IsNotExposed() throws Exception {
        mockMvc.perform(put("/api/inventario/15")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productoId\":1,\"cantidad\":50,\"tipoMovimiento\":\"Entrada\",\"fechaMovimiento\":\"2025-01-01T10:00:00\"}"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testEliminarMovimiento_IsNotExposed() throws Exception {
        mockMvc.perform(delete("/api/inventario/15"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testRegistrarSalidaInsuficiente_ReturnsConflictProblem() throws Exception {
        when(productoService.findById(1L)).thenReturn(inventarioMock.getProducto());
        when(sucursalRepository.findById(2L)).thenReturn(java.util.Optional.of(inventarioMock.getSucursal()));
        when(inventarioService.save(any(Inventario.class))).thenThrow(new InsufficientStockException());

        mockMvc.perform(post("/api/inventario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sucursalId\":2,\"productoId\":1,\"cantidad\":50,\"tipoMovimiento\":\"Salida\",\"fechaMovimiento\":\"2025-01-01T10:00:00\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));
    }
}
