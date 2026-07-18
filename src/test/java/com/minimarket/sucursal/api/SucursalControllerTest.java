package com.minimarket.sucursal.api;

import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.exception.ApiExceptionHandler;
import com.minimarket.sucursal.StockSucursal;
import com.minimarket.sucursal.StockSucursalService;
import com.minimarket.sucursal.Sucursal;
import com.minimarket.sucursal.SucursalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;

import com.minimarket.sucursal.api.dto.SucursalResponse;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SucursalControllerTest {
    @Mock private SucursalRepository sucursalRepository;
    @Mock private StockSucursalService stockSucursalService;
    @Mock private SucursalResponseAssembler sucursalAssembler;
    @Mock private DisponibilidadResponseAssembler disponibilidadAssembler;
    @InjectMocks private SucursalController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listaSucursalesComoColeccionHal() throws Exception {
        Sucursal sucursal = sucursal(1L);
        when(sucursalRepository.findAll()).thenReturn(List.of(sucursal));
        when(sucursalAssembler.toModel(sucursal)).thenAnswer(invocation ->
                new SucursalResponseAssembler().toModel(invocation.getArgument(0)));

        CollectionModel<EntityModel<SucursalResponse>> hal = controller.listarSucursales();
        assertEquals("/api/sucursales", hal.getRequiredLink("self").getHref());

        mockMvc.perform(get("/api/sucursales").accept("application/hal+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void exponeSoloCantidadYMinimoParaDisponibilidad() throws Exception {
        StockSucursal stock = stock(1L, 10L, 24, 5);
        when(stockSucursalService.consultarStock(1L, 10L)).thenReturn(stock);
        when(disponibilidadAssembler.toModel(stock)).thenAnswer(invocation ->
                new DisponibilidadResponseAssembler().toModel(invocation.getArgument(0)));

        mockMvc.perform(get("/api/sucursales/1/productos/10/disponibilidad").accept("application/hal+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(24))
                .andExpect(jsonPath("$.minimo").value(5))
                .andExpect(jsonPath("$.sucursal").doesNotExist())
                .andExpect(jsonPath("$.producto").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void disponibilidadConIdsInexistentesDevuelveProblemDetail404() throws Exception {
        when(stockSucursalService.consultarStock(99L, 10L))
                .thenThrow(new EntityNotFoundException("Sucursal no encontrada: 99"));

        mockMvc.perform(get("/api/sucursales/99/productos/10/disponibilidad"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private Sucursal sucursal(Long id) {
        Sucursal value = new Sucursal();
        value.setId(id);
        value.setNombre("Centro");
        return value;
    }

    private StockSucursal stock(Long sucursalId, Long productoId, int cantidad, int minimo) {
        Producto producto = new Producto();
        producto.setId(productoId);
        producto.setNombre("Arroz");
        producto.setPrecio(100D);
        producto.setStock(99);
        Categoria categoria = new Categoria();
        categoria.setId(3L);
        categoria.setNombre("Abarrotes");
        producto.setCategoria(categoria);
        StockSucursal value = new StockSucursal();
        value.setSucursal(sucursal(sucursalId));
        value.setProducto(producto);
        value.setDisponible(cantidad);
        value.setStockMinimo(minimo);
        return value;
    }
}
