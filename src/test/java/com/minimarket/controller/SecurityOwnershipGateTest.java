package com.minimarket.controller;

import com.minimarket.pedido.api.PedidoController;
import com.minimarket.pedido.domain.EstadoPedido;
import com.minimarket.pedido.domain.Pedido;
import com.minimarket.pedido.repository.PedidoRepository;
import com.minimarket.pedido.service.PedidoService;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.security.CurrentActorService;
import com.minimarket.security.config.SecurityConfig;
import com.minimarket.security.filter.JwtAuthenticationFilter;
import com.minimarket.security.handler.ProblemAccessDeniedHandler;
import com.minimarket.security.handler.ProblemAuthenticationEntryPoint;
import com.minimarket.security.service.CustomUserDetailsService;
import com.minimarket.security.util.JwtUtil;
import com.minimarket.service.CarritoService;
import com.minimarket.service.ProductoService;
import com.minimarket.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Exercises authorization through the application's SecurityFilterChain, not standalone MockMvc. */
@WebMvcTest({CarritoController.class, PedidoController.class})
@Import({SecurityConfig.class, ProblemAuthenticationEntryPoint.class, ProblemAccessDeniedHandler.class,
        JwtAuthenticationFilter.class})
class SecurityOwnershipGateTest {
    @Autowired MockMvc mockMvc;

    @MockBean CarritoService carritoService;
    @MockBean ProductoService productoService;
    @MockBean UsuarioService usuarioService;
    @MockBean CarritoRepository carritoRepository;
    @MockBean CurrentActorService currentActor;
    @MockBean PedidoService pedidoService;
    @MockBean PedidoRepository pedidoRepository;
    @MockBean CustomUserDetailsService customUserDetailsService;
    @MockBean JwtUtil jwtUtil;

    @Test
    void carritoSinAutenticarDevuelve401() throws Exception {
        mockMvc.perform(get("/api/carrito")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    void cajeroNoPuedeConsultarCarritosPrivados() throws Exception {
        mockMvc.perform(get("/api/carrito")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteRecibe404ParaCarritoAjeno() throws Exception {
        when(currentActor.userId()).thenReturn(1L);
        when(carritoRepository.findByIdAndUsuarioId(9L, 1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/carrito/9")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteNoPuedeEjecutarTransicionesDePedido() throws Exception {
        mockMvc.perform(patch("/api/pedidos/1/estado")
                        .contentType("application/json").content("{\"estado\":\"CONFIRMADO\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clientePuedeLeerPromocionesPeroNoMutarlasNiVerReportes() throws Exception {
        mockMvc.perform(get("/api/promociones")).andExpect(status().isNotFound());
        mockMvc.perform(post("/api/promociones").contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/reportes/rotacion")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminPuedeAtravesarGatesDePromocionesYReportes() throws Exception {
        mockMvc.perform(post("/api/promociones").contentType("application/json").content("{}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/reportes/rotacion")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    void cajeroPuedeListarPedidosYRecibeAccionDeTransicion() throws Exception {
        Pedido pedido = pedido(1L, EstadoPedido.PENDIENTE);
        when(pedidoRepository.findAll()).thenReturn(List.of(pedido));
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        mockMvc.perform(get("/api/pedidos/mis-pedidos").accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/pedidos/1").accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.transicionar-confirmado.href").exists())
                .andExpect(jsonPath("$._links.cancelar").doesNotExist());
    }

    @Test
    @WithMockUser(username = "cliente", roles = "CLIENTE")
    void clienteNoRecibeAccionDeTransicion() throws Exception {
        when(pedidoService.obtenerParaCliente(1L, "cliente"))
                .thenReturn(pedido(1L, EstadoPedido.PENDIENTE));

        mockMvc.perform(get("/api/pedidos/1").accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.cancelar.href").exists())
                .andExpect(jsonPath("$._links.transicionar-confirmado").doesNotExist());
    }

    private Pedido pedido(Long id, EstadoPedido estado) {
        Pedido pedido = new Pedido();
        pedido.setId(id);
        pedido.setEstado(estado);
        return pedido;
    }
}
