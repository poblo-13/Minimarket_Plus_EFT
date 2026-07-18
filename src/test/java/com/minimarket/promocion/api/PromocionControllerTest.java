package com.minimarket.promocion.api;

import com.minimarket.promocion.PromocionService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PromocionControllerTest {
    @Mock PromocionService service; @InjectMocks PromocionController controller; private MockMvc mvc;
    @BeforeEach void setup() { mvc = MockMvcBuilders.standaloneSetup(controller).build(); }
    @Test @WithMockUser(roles = "ADMIN") void listaDtosSinExponerEntidad() throws Exception {
        when(service.listar()).thenReturn(List.of(new PromocionResponse(1L, 2L, new BigDecimal("10"), null, LocalDate.now(), LocalDate.now(), true)));
        mvc.perform(get("/api/promociones")).andExpect(status().isOk()).andExpect(jsonPath("$[0].productoId").value(2)).andExpect(jsonPath("$[0].producto").doesNotExist());
    }
}
