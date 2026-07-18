package com.minimarket.controller;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.repository.DetalleVentaRepository;
import com.minimarket.security.CurrentActorService;
import com.minimarket.security.config.SecurityConfig;
import com.minimarket.security.handler.ProblemAccessDeniedHandler;
import com.minimarket.security.handler.ProblemAuthenticationEntryPoint;
import com.minimarket.security.service.CustomUserDetailsService;
import com.minimarket.service.DetalleVentaService;
import com.minimarket.service.CategoriaService;
import com.minimarket.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.minimarket.security.filter.JwtAuthenticationFilter;
import com.minimarket.security.util.JwtUtil;
import com.minimarket.sucursal.StockSucursalService;
import com.minimarket.sucursal.SucursalRepository;
import com.minimarket.sucursal.api.SucursalController;
import com.minimarket.sucursal.api.SucursalResponseAssembler;
import com.minimarket.sucursal.api.DisponibilidadResponseAssembler;
import com.minimarket.abastecimiento.OrdenCompraConsultaService;
import com.minimarket.abastecimiento.api.OrdenCompraController;
import com.minimarket.abastecimiento.api.OrdenCompraResponseAssembler;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({UsuarioController.class, DetalleVentaController.class, CategoriaController.class,
        SucursalController.class, OrdenCompraController.class})
@Import({
        SecurityConfig.class,
        ProblemAuthenticationEntryPoint.class,
        ProblemAccessDeniedHandler.class,
        JwtAuthenticationFilter.class
})
class SecurityMutationAccessTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private UsuarioService usuarioService;
    @MockBean private RolRepository rolRepository;
    @MockBean private PasswordEncoder passwordEncoder;
    @MockBean private DetalleVentaService detalleVentaService;
    @MockBean private DetalleVentaRepository detalleVentaRepository;
    @MockBean private CurrentActorService currentActorService;
    @MockBean private VentaRepository ventaRepository;
    @MockBean private ProductoRepository productoRepository;
    @MockBean private CategoriaService categoriaService;
    @MockBean private CustomUserDetailsService customUserDetailsService;
    @MockBean private JwtUtil jwtUtil;
    @MockBean private SucursalRepository sucursalRepository;
    @MockBean private StockSucursalService stockSucursalService;
    @MockBean private SucursalResponseAssembler sucursalResponseAssembler;
    @MockBean private DisponibilidadResponseAssembler disponibilidadResponseAssembler;
    @MockBean private OrdenCompraConsultaService ordenCompraConsultaService;
    @MockBean private OrdenCompraResponseAssembler ordenCompraResponseAssembler;

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteNoPuedeCrearUsuariosNiAsignarseRoles() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nuevoAdmin\",\"password\":\"ClaveSegura1\",\"rolIds\":[99]}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteNoPuedeActualizarNiEliminarUsuarios() throws Exception {
        mockMvc.perform(put("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nuevoAdmin\",\"password\":null,\"rolIds\":[99]}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/usuarios/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteNoPuedeCrearDetallesDeVentasAjenas() throws Exception {
        mockMvc.perform(post("/api/detalle-ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(detalleRequest()))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void mutacionesDeDetalleVentaNoExistenParaCliente() throws Exception {
        mockMvc.perform(put("/api/detalle-ventas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(detalleRequest()))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(delete("/api/detalle-ventas/1"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteNoPuedeMutarCategorias() throws Exception {
        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoriaRequest()))
                .andExpect(status().isForbidden());
        mockMvc.perform(put("/api/categorias/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoriaRequest()))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/categorias/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    void cajeroNoPuedeMutarCategorias() throws Exception {
        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoriaRequest()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminPuedeCrearCategorias() throws Exception {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Lacteos");
        when(categoriaService.save(any(Categoria.class))).thenReturn(categoria);

        mockMvc.perform(post("/api/categorias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(categoriaRequest()))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void rutaNoCatalogadaSeDeniega() throws Exception {
        mockMvc.perform(post("/api/no-existe"))
                .andExpect(status().isForbidden());
    }

    @Test
    void rutaNoCatalogadaSinAutenticarDevuelve401() throws Exception {
        mockMvc.perform(post("/api/no-existe"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sucursalesRequiereAutenticacion() throws Exception {
        mockMvc.perform(get("/api/sucursales"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteNoPuedeConsultarOrdenesCompra() throws Exception {
        mockMvc.perform(get("/api/ordenes-compra"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminPuedeConsultarOrdenesCompra() throws Exception {
        when(ordenCompraConsultaService.listar()).thenReturn(java.util.List.of());
        mockMvc.perform(get("/api/ordenes-compra"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    void cajeroNoPuedeCrearDetallesDeVenta() throws Exception {
        Venta venta = new Venta();
        venta.setId(1L);
        Producto producto = new Producto();
        producto.setId(1L);
        DetalleVenta detalle = new DetalleVenta();
        detalle.setId(5L);
        detalle.setVenta(venta);
        detalle.setProducto(producto);
        detalle.setCantidad(1);
        detalle.setPrecio(1000D);
        mockMvc.perform(post("/api/detalle-ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(detalleRequest()))
                .andExpect(status().isMethodNotAllowed());
    }

    private String detalleRequest() {
        return "{\"ventaId\":1,\"productoId\":1,\"cantidad\":1,\"precio\":1000.0}";
    }

    private String categoriaRequest() {
        return "{\"nombre\":\"Lacteos\"}";
    }
}
