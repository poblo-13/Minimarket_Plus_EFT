package com.minimarket.controller;

import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.SecurityRoles;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.seed.enabled=true")
@ActiveProfiles("dev")
@AutoConfigureMockMvc
class AutenticacionUsuarioTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void loginRealPermiteAccederConBearer() throws Exception {
        String token = login("admin", "admin123");

        mockMvc.perform(get("/api/productos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void basicAuthenticationIsRejected() throws Exception {
        mockMvc.perform(get("/api/productos").with(httpBasic("admin", "admin123")))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer realm=\"minimarket\""));
    }

    @Test
    void invalidAndManipulatedBearerTokensReturn401() throws Exception {
        mockMvc.perform(get("/api/productos").header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized());

        String token = login("admin", "admin123");
        String manipulated = token.substring(0, token.length() - 1) + "x";
        mockMvc.perform(get("/api/productos").header("Authorization", "Bearer " + manipulated))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deletedTokenUserReturns401() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setUsername("jwt-deleted-user");
        usuario.setPassword(passwordEncoder.encode("password123"));
        usuario.setRoles(usuarioRepository.findByUsername("admin").orElseThrow().getRoles());
        usuarioRepository.saveAndFlush(usuario);
        String token = login("jwt-deleted-user", "password123");
        usuarioRepository.deleteById(usuario.getId());
        usuarioRepository.flush();

        mockMvc.perform(get("/api/productos").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginRequestValidationReturns400() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void authEndpointsArePublicOnlyForTheirPostMethods() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/auth/register"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerCreatesOnlyClienteAndDoesNotExposeSecrets() throws Exception {
        String username = "registered-client";

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"Password123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.token").doesNotExist());

        Usuario registered = usuarioRepository.findByUsername(username).orElseThrow();
        org.junit.jupiter.api.Assertions.assertTrue(passwordEncoder.matches("Password123", registered.getPassword()));
        org.junit.jupiter.api.Assertions.assertEquals(
                java.util.Set.of(SecurityRoles.CLIENTE),
                registered.getRoles().stream().map(rol -> rol.getNombre()).collect(java.util.stream.Collectors.toSet())
        );

        String token = login(username, "Password123");
        mockMvc.perform(get("/api/productos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/inventario").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerRejectsDuplicateUsernameAndIgnoresRoleInput() throws Exception {
        String username = "duplicate-client";
        String body = "{\"username\":\"" + username + "\",\"password\":\"Password123\"}";

        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("USERNAME_ALREADY_EXISTS"));
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"role-input-client\",\"password\":\"Password123\",\"roles\":[\"ADMIN\"]}"))
                .andExpect(status().isCreated());

        Usuario roleInputUser = usuarioRepository.findByUsername("role-input-client").orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(
                java.util.Set.of(SecurityRoles.CLIENTE),
                roleInputUser.getRoles().stream().map(rol -> rol.getNombre()).collect(java.util.stream.Collectors.toSet())
        );
    }

    private String login(String username, String password) throws Exception {
        return mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()
                .replaceFirst(".*\"token\":\"([^\"]+)\".*", "$1");
    }
}
