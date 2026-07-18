package com.minimarket.reporte.api;

import com.minimarket.reporte.RotacionService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReporteRotacionControllerTest {
    @Mock RotacionService service; @InjectMocks ReporteRotacionController controller; private MockMvc mvc;
    @BeforeEach void setup() { mvc = MockMvcBuilders.standaloneSetup(controller).build(); }
    @Test @WithMockUser(roles = "ADMIN") void entregaRotacionAgregada() throws Exception {
        when(service.consultar(any(LocalDate.class), any(LocalDate.class), any())).thenReturn(List.of(new RotacionProductoResponse(1, 1L, "Arroz", 4L, new BigDecimal("4000.00"))));
        mvc.perform(get("/api/reportes/rotacion").param("desde", "2026-01-01").param("hasta", "2026-01-31"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].posicionRotacion").value(1)).andExpect(jsonPath("$[0].cantidadVendida").value(4)).andExpect(jsonPath("$[0].importeVendido").value(4000));
    }
}
