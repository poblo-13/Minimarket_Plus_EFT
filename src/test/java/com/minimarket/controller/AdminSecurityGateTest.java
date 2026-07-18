package com.minimarket.controller;

import com.minimarket.abastecimiento.OrdenCompraConsultaService;
import com.minimarket.abastecimiento.OrdenCompraRepository;
import com.minimarket.abastecimiento.api.AdministracionAbastecimientoController;
import com.minimarket.abastecimiento.api.OrdenCompraController;
import com.minimarket.abastecimiento.api.OrdenCompraResponseAssembler;
import com.minimarket.promocion.PromocionService;
import com.minimarket.promocion.api.PromocionController;
import com.minimarket.reporte.RotacionService;
import com.minimarket.reporte.api.ReporteRotacionController;
import com.minimarket.security.config.SecurityConfig;
import com.minimarket.security.filter.JwtAuthenticationFilter;
import com.minimarket.security.handler.ProblemAccessDeniedHandler;
import com.minimarket.security.handler.ProblemAuthenticationEntryPoint;
import com.minimarket.security.service.CustomUserDetailsService;
import com.minimarket.security.util.JwtUtil;
import com.minimarket.sucursal.AdministracionStockService;
import com.minimarket.sucursal.StockSucursalService;
import com.minimarket.sucursal.SucursalRepository;
import com.minimarket.sucursal.api.AdministracionSucursalController;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifies route gates through the application SecurityFilterChain. */
@WebMvcTest({AdministracionSucursalController.class, AdministracionAbastecimientoController.class,
        PromocionController.class, ReporteRotacionController.class, OrdenCompraController.class})
@Import({SecurityConfig.class, ProblemAuthenticationEntryPoint.class, ProblemAccessDeniedHandler.class,
        JwtAuthenticationFilter.class})
class AdminSecurityGateTest {
    @Autowired MockMvc mockMvc;

    @MockBean AdministracionStockService administracionStockService;
    @MockBean SucursalRepository sucursalRepository;
    @MockBean StockSucursalService stockSucursalService;
    @MockBean PromocionService promocionService;
    @MockBean RotacionService rotacionService;
    @MockBean OrdenCompraConsultaService ordenCompraConsultaService;
    @MockBean OrdenCompraRepository ordenCompraRepository;
    @MockBean OrdenCompraResponseAssembler ordenCompraResponseAssembler;
    @MockBean CustomUserDetailsService customUserDetailsService;
    @MockBean JwtUtil jwtUtil;

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminAlcanzaRutasAdministrativasDeSucursalYProveedor() throws Exception {
        mockMvc.perform(get("/api/admin/sucursales/1")).andExpect(status().isNotFound());
        mockMvc.perform(post("/api/admin/proveedores").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteNoPuedeAccederRutasAdministrativas() throws Exception {
        mockMvc.perform(get("/api/admin/sucursales/1")).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/admin/proveedores").contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CAJERO")
    void cajeroNoPuedeAccederRutasAdministrativasNiReportes() throws Exception {
        mockMvc.perform(get("/api/admin/sucursales/1")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/reportes/rotacion")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/ordenes-compra")).andExpect(status().isForbidden());
    }

    @Test
    void anonimoRecibe401EnRutasAdministrativasYOrdenes() throws Exception {
        mockMvc.perform(get("/api/admin/sucursales/1")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/admin/proveedores").contentType("application/json").content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/ordenes-compra")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void clienteLeePromocionesPeroNoLasMuta() throws Exception {
        when(promocionService.listar()).thenReturn(List.of());
        mockMvc.perform(get("/api/promociones")).andExpect(status().isOk());
        mockMvc.perform(post("/api/promociones").contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/reportes/rotacion")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/ordenes-compra")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminNoEsBloqueadoEnPromocionesReportesYOrdenes() throws Exception {
        mockMvc.perform(post("/api/promociones").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
        when(rotacionService.consultar(java.time.LocalDate.of(2026, 1, 1), java.time.LocalDate.of(2026, 1, 31), null))
                .thenReturn(List.of());
        mockMvc.perform(get("/api/reportes/rotacion")
                        .param("desde", "2026-01-01").param("hasta", "2026-01-31"))
                .andExpect(status().isOk());
        when(ordenCompraConsultaService.listar()).thenReturn(List.of());
        mockMvc.perform(get("/api/ordenes-compra").accept(MediaTypes.HAL_JSON)).andExpect(status().isOk());
    }
}
