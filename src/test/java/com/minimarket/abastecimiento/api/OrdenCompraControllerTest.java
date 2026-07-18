package com.minimarket.abastecimiento.api;

import com.minimarket.abastecimiento.EstadoOrdenCompra;
import com.minimarket.abastecimiento.OrdenCompra;
import com.minimarket.abastecimiento.OrdenCompraConsultaService;
import com.minimarket.abastecimiento.OrdenCompraRepository;
import com.minimarket.abastecimiento.Proveedor;
import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.sucursal.Sucursal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;

import com.minimarket.abastecimiento.api.dto.OrdenCompraResponse;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrdenCompraControllerTest {
    @Mock private OrdenCompraConsultaService ordenCompraConsultaService;
    @Mock private OrdenCompraRepository ordenCompraRepository;
    @Mock private OrdenCompraResponseAssembler assembler;
    @InjectMocks private OrdenCompraController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listaOrdenesComoColeccionHalSinGrafos() throws Exception {
        OrdenCompra orden = orden();
        when(ordenCompraConsultaService.listar()).thenReturn(List.of(orden));
        when(assembler.toModel(orden)).thenAnswer(invocation ->
                new OrdenCompraResponseAssembler().toModel(invocation.getArgument(0)));

        CollectionModel<EntityModel<OrdenCompraResponse>> hal = controller.listarOrdenesCompra();
        assertEquals("/api/ordenes-compra", hal.getRequiredLink("self").getHref());
        assertEquals("/api/ordenes-compra/1", hal.getContent().iterator().next().getRequiredLink("self").getHref());

        mockMvc.perform(get("/api/ordenes-compra").accept("application/hal+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].sucursalId").value(2))
                .andExpect(jsonPath("$.content[0].producto").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void obtieneOrdenPorRutaIndividualConSelfReal() {
        OrdenCompra orden = orden();
        when(ordenCompraRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(assembler.toModel(orden)).thenAnswer(invocation ->
                new OrdenCompraResponseAssembler().toModel(invocation.getArgument(0)));

        EntityModel<OrdenCompraResponse> resource = controller.obtenerOrdenCompra(1L);

        assertEquals("/api/ordenes-compra/1", resource.getRequiredLink("self").getHref());
        assertEquals("/api/ordenes-compra", resource.getRequiredLink("collection").getHref());
    }

    private OrdenCompra orden() {
        Sucursal sucursal = new Sucursal(); sucursal.setId(2L); sucursal.setNombre("Norte");
        Categoria categoria = new Categoria(); categoria.setId(4L); categoria.setNombre("Despensa");
        Producto producto = new Producto(); producto.setId(10L); producto.setNombre("Arroz"); producto.setPrecio(100D); producto.setStock(1); producto.setCategoria(categoria);
        Proveedor proveedor = new Proveedor(); proveedor.setId(3L); proveedor.setNombre("Proveedor");
        OrdenCompra orden = new OrdenCompra();
        orden.setId(1L); orden.setSucursal(sucursal); orden.setProducto(producto); orden.setProveedor(proveedor);
        orden.setCantidadSolicitada(12); orden.setEstado(EstadoOrdenCompra.ABIERTA);
        return orden;
    }
}
