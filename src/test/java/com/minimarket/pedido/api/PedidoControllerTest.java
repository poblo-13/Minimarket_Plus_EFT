package com.minimarket.pedido.api;

import com.minimarket.exception.ApiExceptionHandler;
import com.minimarket.pedido.domain.EstadoPedido;
import com.minimarket.pedido.domain.Pedido;
import com.minimarket.pedido.domain.TipoEntrega;
import com.minimarket.pedido.service.PedidoService;
import com.minimarket.pedido.repository.PedidoRepository;
import com.minimarket.entity.Venta;
import com.minimarket.security.SecurityRoles;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PedidoControllerTest {
    private PedidoService pedidoService;
    private PedidoRepository pedidoRepository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        pedidoService = mock(PedidoService.class);
        pedidoRepository = mock(PedidoRepository.class);
        HandlerMethodArgumentResolver authenticationResolver = new HandlerMethodArgumentResolver() {
            @Override public boolean supportsParameter(MethodParameter parameter) {
                return Authentication.class.isAssignableFrom(parameter.getParameterType());
            }
            @Override public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer container,
                                                    NativeWebRequest request, WebDataBinderFactory binderFactory) {
                return SecurityContextHolder.getContext().getAuthentication();
            }
        };
        mockMvc = MockMvcBuilders.standaloneSetup(new PedidoController(pedidoService, pedidoRepository))
                .setControllerAdvice(new ApiExceptionHandler(), new PedidoApiExceptionHandler())
                .setCustomArgumentResolvers(authenticationResolver)
                .build();
    }

    @Test
    void creaPedidoParaElUsuarioAutenticadoSinAceptarDatosDelServidor() throws Exception {
        when(pedidoService.crear(eq("cliente-a"), org.mockito.ArgumentMatchers.any(CrearPedidoRequest.class)))
                .thenReturn(pedido(7L, EstadoPedido.PENDIENTE));

        mockMvc.perform(post("/api/pedidos").with(authenticatedAs("cliente-a"))
                        .contentType("application/json")
                        .content("""
                                {"tipoEntrega":"RETIRO_TIENDA","sucursalId":2,
                                 "detalles":[{"productoId":3,"cantidad":2}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/pedidos/7"))
                .andExpect(jsonPath("$.total").value(10.00));

        verify(pedidoService).crear(eq("cliente-a"), org.mockito.ArgumentMatchers.any(CrearPedidoRequest.class));

        mockMvc.perform(post("/api/pedidos").with(authenticatedAs("cliente-a"))
                        .contentType("application/json")
                        .content("""
                                {"clienteId":99,"tipoEntrega":"RETIRO_TIENDA","sucursalId":2,
                                 "detalles":[{"productoId":3,"cantidad":2,"precioUnitario":0.01}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_JSON"));
    }

    @Test
    void pedidoAjenoEsIndistinguibleDeUnoInexistenteParaSegundoCliente() throws Exception {
        when(pedidoService.obtenerParaCliente(7L, "cliente-b"))
                .thenThrow(new NoSuchElementException("Pedido no encontrado"));

        mockMvc.perform(get("/api/pedidos/7").with(authenticatedAs("cliente-b")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void cancelacionPendienteDelDuenoDevuelveNoContent() throws Exception {
        mockMvc.perform(delete("/api/pedidos/7").with(authenticatedAs("cliente-a")))
                .andExpect(status().isNoContent());
        verify(pedidoService).cancelar(7L, "cliente-a");
    }

    @Test
    void transicionesCorrectasEInvalidasUsanEstadosHttpEsperados() throws Exception {
        when(pedidoService.cambiarEstado(7L, EstadoPedido.CONFIRMADO))
                .thenReturn(pedido(7L, EstadoPedido.CONFIRMADO));
        when(pedidoService.cambiarEstado(7L, EstadoPedido.LISTO))
                .thenThrow(new IllegalStateException("Transición de estado no válida"));

        mockMvc.perform(patch("/api/pedidos/7/estado").with(authenticatedAs("cajero"))
                        .contentType("application/json").content("{\"estado\":\"CONFIRMADO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"));
        mockMvc.perform(patch("/api/pedidos/7/estado").with(authenticatedAs("cajero"))
                        .contentType("application/json").content("{\"estado\":\"LISTO\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void pedidoDeStaffExponeSoloEnlacesConsultablesDeVentaYSucursal() {
        Pedido pedido = pedido(7L, EstadoPedido.PENDIENTE);
        Venta venta = new Venta();
        venta.setId(9L);
        pedido.setVenta(venta);
        Authentication staff = new UsernamePasswordAuthenticationToken("admin", "test",
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + SecurityRoles.ADMIN)));

        when(pedidoRepository.findById(7L)).thenReturn(Optional.of(pedido));
        var resource = new PedidoController(pedidoService, pedidoRepository).obtener(7L, staff);
        assertEquals("/api/pedidos/7", resource.getRequiredLink("self").getHref());
        assertEquals("/api/ventas/9", resource.getRequiredLink("venta").getHref());
        assertEquals("/api/admin/sucursales/2", resource.getRequiredLink("sucursal").getHref());
    }

    private Pedido pedido(Long id, EstadoPedido estado) {
        Pedido pedido = new Pedido();
        pedido.setId(id);
        pedido.setEstado(estado);
        pedido.setTipoEntrega(TipoEntrega.RETIRO_TIENDA);
        pedido.setSucursalId(2L);
        pedido.setTotal(new BigDecimal("10.00"));
        return pedido;
    }

    private RequestPostProcessor authenticatedAs(String username) {
        return request -> {
            Authentication authentication = new UsernamePasswordAuthenticationToken(username, "test");
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setUserPrincipal(authentication);
            return request;
        };
    }
}
