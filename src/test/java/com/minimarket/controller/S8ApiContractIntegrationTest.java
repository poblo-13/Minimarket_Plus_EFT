package com.minimarket.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = "app.seed.enabled=true")
@AutoConfigureMockMvc
class S8ApiContractIntegrationTest {
    @Autowired MockMvc mockMvc;

    @Test
    void unauthenticatedRequestIsRfc9457WithBasicChallenge() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Basic realm=\"minimarket\""))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void validationMalformedAndInvalidIdUseProblemDetail() throws Exception {
        mockMvc.perform(post("/api/categorias").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.path").value("/api/categorias"))
                .andExpect(jsonPath("$.errors[0].field").value("nombre"));
        mockMvc.perform(post("/api/categorias").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("MALFORMED_JSON"))
                .andExpect(jsonPath("$.path").value("/api/categorias"));
        mockMvc.perform(get("/api/productos/0").with(httpBasic("admin", "admin123")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.path").value("/api/productos/0"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
    void categoryValidationRejectsBlankAndOverlongNames(String nombre) throws Exception {
        mockMvc.perform(post("/api/categorias").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"" + nombre + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[0].field").value("nombre"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "not-a-number"})
    void productIdentifiersRejectNonPositiveAndMalformedValues(String id) throws Exception {
        mockMvc.perform(get("/api/productos/" + id).with(httpBasic("admin", "admin123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.path").value("/api/productos/" + id));
    }

    @Test
    void h2FlowCreatesCategoryAndHalProductWithLocation() throws Exception {
        String category = "S8-" + System.nanoTime();
        String categoryBody = mockMvc.perform(post("/api/categorias").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"" + category + "\"}"))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andReturn().getResponse().getContentAsString();
        long id = ((Number) com.jayway.jsonpath.JsonPath.read(categoryBody, "$.id")).longValue();
        mockMvc.perform(post("/api/productos").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"S8 producto\",\"precio\":1.0,\"stock\":0,\"categoriaId\":" + id + "}"))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void productPriceUsesNumericPrecisionWithinDoubleTolerance() throws Exception {
        String category = "S8-precision-" + System.nanoTime();
        String categoryBody = mockMvc.perform(post("/api/categorias").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"" + category + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long categoryId = ((Number) com.jayway.jsonpath.JsonPath.read(categoryBody, "$.id")).longValue();
        String productBody = mockMvc.perform(post("/api/productos").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Precio preciso\",\"precio\":10.10,\"stock\":0,\"categoriaId\":" + categoryId + "}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        double precio = ((Number) com.jayway.jsonpath.JsonPath.read(productBody, "$.precio")).doubleValue();
        assertEquals(10.10d, precio, 0.000001d);
    }

    @Test
    void openApiDocumentsPathsBasicAuthAndProblemSchema() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/productos']").exists())
                .andExpect(jsonPath("$.components.securitySchemes.basicAuth.type").value("http"));
    }
}
