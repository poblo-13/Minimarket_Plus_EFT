package com.minimarket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.entity.Categoria;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.security.config.SecurityConfig;
import com.minimarket.security.service.CustomUserDetailsService;
import com.minimarket.service.InventarioService;
import com.minimarket.service.ProductoService;
import com.minimarket.service.VentaService;
import com.minimarket.service.CategoriaService;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.security.CurrentActorService;
import com.minimarket.security.handler.ProblemAccessDeniedHandler;
import com.minimarket.security.handler.ProblemAuthenticationEntryPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import com.minimarket.exception.ApiExceptionHandler;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.minimarket.security.filter.JwtAuthenticationFilter;
import com.minimarket.security.util.JwtUtil;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = {ProductoController.class, InventarioController.class, VentaController.class},
        excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ApiExceptionHandler.class))
@Import({
        SecurityConfig.class,
        ProblemAuthenticationEntryPoint.class,
        ProblemAccessDeniedHandler.class,
        JwtAuthenticationFilter.class
})
public class SeguridadAccesosTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductoService productoService;

    @MockBean
    private InventarioService inventarioService;

    @MockBean
    private VentaService ventaService;
    @MockBean private CategoriaService categoriaService;
    @MockBean private UsuarioRepository usuarioRepository;
    @MockBean private VentaRepository ventaRepository;
    @MockBean private CurrentActorService currentActorService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void relatedResourcesExist() {
        Categoria categoria = new Categoria(); categoria.setId(1L); categoria.setNombre("Abarrotes");
        Producto producto = productoValido(); producto.setId(1L);
        Usuario usuario = usuarioValido();
        when(categoriaService.findById(1L)).thenReturn(categoria);
        when(productoService.findById(1L)).thenReturn(producto);
        when(usuarioRepository.findById(1L)).thenReturn(java.util.Optional.of(usuario));
    }

    @Test
    public void testSeguridad_SinCredencialesGetProductos_DebeDar401() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer realm=\"minimarket\""))
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.path").value("/api/productos"));
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    public void testSeguridad_ClientePuedeConsultarProductos() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENTE") // Intruso
    public void testSeguridad_ClienteIntentaCrearProducto_DebeSerBloqueado() throws Exception {
        Producto producto = productoValido();
        when(productoService.save(any(Producto.class))).thenReturn(producto);

        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productoRequestJson()))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.path").value("/api/productos"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testSeguridad_AdminPuedeCrearProducto() throws Exception {
        Producto producto = productoValido();
        when(productoService.save(any(Producto.class))).thenReturn(producto);

        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productoRequestJson()))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "CLIENTE") // Intruso
    public void testSeguridad_ClienteIntentaMoverInventario_DebeSerBloqueado() throws Exception {
        Inventario inventario = new Inventario();
        inventario.setTipoMovimiento("Entrada");
        inventario.setCantidad(10);
        inventario.setFechaMovimiento(LocalDateTime.now());
        inventario.setProducto(productoValido());
        when(inventarioService.save(any(Inventario.class))).thenReturn(inventario);

        mockMvc.perform(post("/api/inventario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(inventarioRequestJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testSeguridad_AdminPuedeMoverInventario() throws Exception {
        Inventario inventario = new Inventario();
        inventario.setTipoMovimiento("Entrada");
        inventario.setCantidad(10);
        inventario.setFechaMovimiento(LocalDateTime.now());
        inventario.setProducto(productoValido());
        when(inventarioService.save(any(Inventario.class))).thenReturn(inventario);

        mockMvc.perform(post("/api/inventario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(inventarioRequestJson()))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "CLIENTE") // Intruso solo CAJERO deberia poder
    public void testSeguridad_ClienteIntentaGenerarVenta_DebeSerBloqueado() throws Exception {
        Venta venta = new Venta();
        venta.setFecha(LocalDateTime.now());
        venta.setUsuario(usuarioValido());
        venta.setDetalles(List.of(detalleValido()));
        when(ventaService.registrar(any(com.minimarket.api.dto.VentaRequest.class))).thenReturn(venta);

        mockMvc.perform(post("/api/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ventaRequestJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    public void testSeguridad_CajeroPuedeGenerarVenta() throws Exception {
        Venta venta = ventaValida();
        when(ventaService.registrar(any(com.minimarket.api.dto.VentaRequest.class))).thenReturn(venta);

        mockMvc.perform(post("/api/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ventaRequestJson()))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testSeguridad_AdminPuedeGenerarVenta() throws Exception {
        Venta venta = ventaValida();
        when(ventaService.registrar(any(com.minimarket.api.dto.VentaRequest.class))).thenReturn(venta);

        mockMvc.perform(post("/api/ventas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ventaRequestJson()))
                .andExpect(status().isCreated());
    }

    private Producto productoValido() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Abarrotes");
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Galletas");
        producto.setPrecio(990.0);
        producto.setStock(20);
        producto.setCategoria(categoria);
        return producto;
    }

    private Usuario usuarioValido() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cliente");
        usuario.setPassword("demo");
        return usuario;
    }

    private DetalleVenta detalleValido() {
        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(productoValido());
        detalle.setCantidad(2);
        detalle.setPrecio(990.0);
        return detalle;
    }

    private Venta ventaValida() {
        Venta venta = new Venta();
        venta.setId(100L);
        venta.setFecha(LocalDateTime.now());
        venta.setUsuario(usuarioValido());
        venta.setDetalles(List.of(detalleValido()));
        return venta;
    }

    private String productoRequestJson() { return "{\"nombre\":\"Galletas\",\"precio\":990.0,\"stock\":20,\"categoriaId\":1}"; }
    private String inventarioRequestJson() { return "{\"productoId\":1,\"cantidad\":10,\"tipoMovimiento\":\"Entrada\",\"fechaMovimiento\":\"2025-01-01T10:00:00\"}"; }
    private String ventaRequestJson() { return "{\"usuarioId\":1,\"lineas\":[{\"productoId\":1,\"cantidad\":2}]}"; }
}
