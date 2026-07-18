package com.minimarket.controller;

import com.minimarket.entity.Carrito;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.service.CarritoService;
import com.minimarket.service.ProductoService;
import com.minimarket.service.UsuarioService;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.security.CurrentActorService;
import com.minimarket.exception.ApiExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CarritoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CarritoService carritoService;
    @Mock private ProductoService productoService;
    @Mock private UsuarioService usuarioService;
    @Mock private CarritoRepository carritoRepository;
    @Mock private CurrentActorService currentActor;

    @InjectMocks
    private CarritoController carritoController;

    private Carrito carritoMock;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        // Configuramos el simulador de peticiones web
        FilterChainProxy securityFilter = new FilterChainProxy(new DefaultSecurityFilterChain(
                AnyRequestMatcher.INSTANCE,
                new SecurityContextHolderFilter(new RequestAttributeSecurityContextRepository())));
        mockMvc = MockMvcBuilders.standaloneSetup(carritoController)
                .setControllerAdvice(new ApiExceptionHandler())
                .apply(springSecurity(securityFilter))
                .defaultRequest(get("/").with(user("cliente")))
                .build();
        objectMapper = new ObjectMapper(); // Para convertir objetos a JSON

        carritoMock = new Carrito();
        carritoMock.setId(10L);
        carritoMock.setCantidad(2);
        Producto producto = new Producto(); producto.setId(1L); carritoMock.setProducto(producto);
        Usuario usuario = new Usuario(); usuario.setId(1L); carritoMock.setUsuario(usuario);
        lenient().when(usuarioService.findByUsername("cliente")).thenReturn(java.util.Optional.of(usuario));
        lenient().when(currentActor.isStaff()).thenReturn(false);
        lenient().when(currentActor.userId()).thenReturn(1L);
    }

    @Test
    public void testListarCarrito() throws Exception {
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Arrays.asList(carritoMock));

        // Simulamos un GET a /api/carrito
        mockMvc.perform(get("/api/carrito"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.links[0].rel").value("self"));
    }

    @Test
    public void testObtenerCarritoPorId_Encontrado() throws Exception {
        when(carritoRepository.findByIdAndUsuarioId(10L, 1L)).thenReturn(java.util.Optional.of(carritoMock));

        // Simulamos un GET a /api/carrito/10
        mockMvc.perform(get("/api/carrito/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    public void testObtenerCarritoPorId_NoEncontrado() throws Exception {
        when(carritoRepository.findByIdAndUsuarioId(99L, 1L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/carrito/99"))
                .andExpect(status().isNotFound()); // Esperamos un 404
    }

    @Test
    public void testAgregarProductoAlCarrito() throws Exception {
        when(productoService.findById(1L)).thenReturn(carritoMock.getProducto());
        when(carritoService.save(any(Carrito.class))).thenReturn(carritoMock);

        // Simulamos un POST con un JSON en el cuerpo (Body)
        mockMvc.perform(post("/api/carrito")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuarioId\":1,\"productoId\":1,\"cantidad\":2}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/carrito/10"))
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    public void testActualizarCarrito_Encontrado() throws Exception {
        when(carritoRepository.findByIdAndUsuarioId(10L, 1L)).thenReturn(java.util.Optional.of(carritoMock));
        when(productoService.findById(1L)).thenReturn(carritoMock.getProducto());
        when(carritoService.save(any(Carrito.class))).thenReturn(carritoMock);

        // Simulamos un PUT
        mockMvc.perform(put("/api/carrito/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuarioId\":1,\"productoId\":1,\"cantidad\":2}"))
                .andExpect(status().isOk());
    }

    @Test
    public void testActualizarCarrito_NoEncontrado() throws Exception {
        when(carritoRepository.findByIdAndUsuarioId(99L, 1L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(put("/api/carrito/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"usuarioId\":1,\"productoId\":1,\"cantidad\":2}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testEliminarProductoDelCarrito_Encontrado() throws Exception {
        when(carritoRepository.findByIdAndUsuarioId(10L, 1L)).thenReturn(java.util.Optional.of(carritoMock));

        // Simulamos un DELETE
        mockMvc.perform(delete("/api/carrito/10"))
                .andExpect(status().isNoContent()); // Esperamos un 204 No Content

        verify(carritoService, times(1)).deleteById(10L);
    }

    @Test
    public void testEliminarProductoDelCarrito_NoEncontrado() throws Exception {
        when(carritoRepository.findByIdAndUsuarioId(99L, 1L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(delete("/api/carrito/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testObtenerCarritoDeOtroUsuario_DebeSerBloqueado() throws Exception {
        when(carritoRepository.findByIdAndUsuarioId(10L, 1L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/carrito/10"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void segundoClienteSoloConsultaSuPropioCarritoEnRepositorio() throws Exception {
        when(currentActor.userId()).thenReturn(2L);
        when(carritoRepository.findByUsuarioId(2L)).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/carrito"))
                .andExpect(status().isOk());

        verify(carritoRepository).findByUsuarioId(2L);
        verify(carritoRepository, never()).findAll();
    }

    @Test
    public void testAgregarProductoIgnoraUsuarioIdControladoPorCliente() throws Exception {
        when(productoService.findById(1L)).thenReturn(carritoMock.getProducto());
        when(carritoService.save(any(Carrito.class))).thenAnswer(invocation -> {
            Carrito saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        mockMvc.perform(post("/api/carrito")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuarioId\":2,\"productoId\":1,\"cantidad\":2}"))
                .andExpect(status().isCreated());

        org.mockito.ArgumentCaptor<Carrito> captor = org.mockito.ArgumentCaptor.forClass(Carrito.class);
        verify(carritoService).save(captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(1L, captor.getValue().getUsuario().getId());
    }
}
