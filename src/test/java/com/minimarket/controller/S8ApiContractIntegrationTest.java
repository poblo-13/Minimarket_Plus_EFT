package com.minimarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(post("/api/categorias").with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("MALFORMED_JSON"));
        mockMvc.perform(get("/api/productos/0").with(httpBasic("admin", "admin123")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
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
    void openApiDocumentsPathsBasicAuthAndProblemSchema() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/productos']").exists())
                .andExpect(jsonPath("$.components.securitySchemes.basicAuth.type").value("http"));
    }
}
