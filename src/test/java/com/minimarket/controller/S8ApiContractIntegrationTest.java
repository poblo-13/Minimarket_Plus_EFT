package com.minimarket.controller;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.SecurityRoles;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = "app.seed.enabled=false")
@ActiveProfiles("test")
@AutoConfigureMockMvc
class S8ApiContractIntegrationTest {
    private static final String ADMIN_USERNAME = "s8-admin";
    private static final String ADMIN_PASSWORD = "S8AdminPass123";

    @Autowired MockMvc mockMvc;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired RolRepository rolRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void createAdminFixture() {
        Rol adminRole = rolRepository.findByNombre(SecurityRoles.ADMIN)
                .orElseGet(() -> rolRepository.save(new Rol(SecurityRoles.ADMIN)));
        Usuario admin = usuarioRepository.findByUsername(ADMIN_USERNAME).orElseGet(Usuario::new);
        admin.setUsername(ADMIN_USERNAME);
        admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setRoles(Set.of(adminRole));
        usuarioRepository.save(admin);
    }

    @Test
    void unauthenticatedRequestIsRfc9457WithBearerChallenge() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer realm=\"minimarket\""))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void validationMalformedAndInvalidIdUseProblemDetail() throws Exception {
        mockMvc.perform(post("/api/categorias").with(adminBearer())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.path").value("/api/categorias"))
                .andExpect(jsonPath("$.errors[0].field").value("nombre"));
        mockMvc.perform(post("/api/categorias").with(adminBearer())
                        .contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("MALFORMED_JSON"))
                .andExpect(jsonPath("$.path").value("/api/categorias"));
        mockMvc.perform(get("/api/productos/0").with(adminBearer()))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.path").value("/api/productos/0"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
    void categoryValidationRejectsBlankAndOverlongNames(String nombre) throws Exception {
        mockMvc.perform(post("/api/categorias").with(adminBearer())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"" + nombre + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[0].field").value("nombre"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "not-a-number"})
    void productIdentifiersRejectNonPositiveAndMalformedValues(String id) throws Exception {
        mockMvc.perform(get("/api/productos/" + id).with(adminBearer()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.path").value("/api/productos/" + id));
    }

    @Test
    void h2FlowCreatesCategoryAndHalProductWithLocation() throws Exception {
        String category = "S8-" + System.nanoTime();
        String categoryBody = mockMvc.perform(post("/api/categorias").with(adminBearer())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"" + category + "\"}"))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andReturn().getResponse().getContentAsString();
        long id = ((Number) com.jayway.jsonpath.JsonPath.read(categoryBody, "$.id")).longValue();
        mockMvc.perform(post("/api/productos").with(adminBearer())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"S8 producto\",\"precio\":1.0,\"stock\":0,\"categoriaId\":" + id + "}"))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void productPriceUsesNumericPrecisionWithinDoubleTolerance() throws Exception {
        String category = "S8-precision-" + System.nanoTime();
        String categoryBody = mockMvc.perform(post("/api/categorias").with(adminBearer())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"" + category + "\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long categoryId = ((Number) com.jayway.jsonpath.JsonPath.read(categoryBody, "$.id")).longValue();
        String productBody = mockMvc.perform(post("/api/productos").with(adminBearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Precio preciso\",\"precio\":10.10,\"stock\":0,\"categoriaId\":" + categoryId + "}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        double precio = ((Number) com.jayway.jsonpath.JsonPath.read(productBody, "$.precio")).doubleValue();
        assertEquals(10.10d, precio, 0.000001d);
    }

    @Test
    void openApiDocumentsPathsBearerAuthAndProblemSchema() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/productos']").exists())
                .andExpect(jsonPath("$.paths['/api/productos'].get.responses['200'].content['application/hal+json'].schema.$ref")
                        .value("#/components/schemas/ProductoResponse"))
                .andExpect(jsonPath("$.paths['/api/productos'].post.requestBody.content['application/json'].schema.$ref")
                        .value("#/components/schemas/ProductoRequest"))
                .andExpect(jsonPath("$.paths['/api/categorias'].post.responses['201'].content['application/hal+json'].schema.$ref")
                        .value("#/components/schemas/CategoriaResponse"))
                .andExpect(jsonPath("$.paths['/api/categorias'].post.responses['400'].content['application/problem+json'].schema.$ref")
                        .value("#/components/schemas/ProblemDetail"))
                .andExpect(jsonPath("$.paths['/api/categorias'].get.responses['401'].content['application/problem+json'].schema.$ref")
                        .value("#/components/schemas/ProblemDetail"))
                .andExpect(jsonPath("$.paths['/api/ventas'].post.responses['403'].content['application/problem+json'].schema.$ref")
                        .value("#/components/schemas/ProblemDetail"))
                .andExpect(jsonPath("$.paths['/api/ventas'].post.responses['404'].content['application/problem+json'].schema.$ref")
                        .value("#/components/schemas/ProblemDetail"))
                .andExpect(jsonPath("$.paths['/api/inventario'].post.responses['409'].content['application/problem+json'].schema.$ref")
                        .value("#/components/schemas/ProblemDetail"))
                .andExpect(jsonPath("$.paths['/api/categorias'].post.responses['403'].content['application/problem+json'].schema.$ref")
                        .value("#/components/schemas/ProblemDetail"))
                .andExpect(jsonPath("$.paths['/api/usuarios/{id}'].put.responses['403'].content['application/problem+json'].schema.$ref")
                        .value("#/components/schemas/ProblemDetail"))
                .andExpect(jsonPath("$.paths['/api/carrito/{id}'].delete.responses['403'].content['application/problem+json'].schema.$ref")
                        .value("#/components/schemas/ProblemDetail"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type")
                        .value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme")
                        .value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat")
                        .value("JWT"));
    }

    @Test
    void representativeResponsesUseTheirDocumentedContentTypes() throws Exception {
        mockMvc.perform(get("/api/productos").with(adminBearer()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/hal+json"));

        mockMvc.perform(get("/api/productos/0").with(adminBearer()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + ADMIN_USERNAME + "\",\"password\":\"" + ADMIN_PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    private RequestPostProcessor adminBearer() throws Exception {
        String body = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + ADMIN_USERNAME + "\",\"password\":\"" + ADMIN_PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = com.jayway.jsonpath.JsonPath.read(body, "$.token");
        return request -> {
            request.addHeader("Authorization", "Bearer " + token);
            return request;
        };
    }
}
