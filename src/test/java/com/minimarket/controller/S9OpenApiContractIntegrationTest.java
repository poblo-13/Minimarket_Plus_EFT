package com.minimarket.controller;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Manifest contract for S9 operations. Keep this list in sync with controller mappings. */
@SpringBootTest(properties = "app.seed.enabled=false")
@ActiveProfiles("test")
@AutoConfigureMockMvc
class S9OpenApiContractIntegrationTest {
    @Autowired MockMvc mockMvc;

    @ParameterizedTest(name = "{0} {1} -> {2} documents {3} as {4}")
    @MethodSource("operations")
    void documentsEachS9Operation(String path, String method, String status, String mediaType, String schema) throws Exception {
        DocumentContext api = apiDocs();
        String base = "$.paths['" + path + "']." + method + ".responses['" + status + "']";
        assertNotNull(api.read(base), path + " " + method + " must document " + status);
        if (mediaType == null) {
            return;
        }
        assertNotNull(api.read(base + ".content['" + mediaType + "']"));
        if (schema.startsWith("array:")) {
            assertEquals("array", api.read(base + ".content['" + mediaType + "'].schema.type"));
            assertEquals("#/components/schemas/" + schema.substring(6), api.read(base + ".content['" + mediaType + "'].schema.items.$ref"));
        } else {
            assertEquals("#/components/schemas/" + schema, api.read(base + ".content['" + mediaType + "'].schema.$ref"));
        }
    }

    @ParameterizedTest(name = "{0} {1} has request DTO {2}")
    @MethodSource("requests")
    void documentsRequestDtos(String path, String method, String requestSchema) throws Exception {
        assertEquals("#/components/schemas/" + requestSchema,
                apiDocs().read("$.paths['" + path + "']." + method + ".requestBody.content['application/json'].schema.$ref"));
    }

    @Test
    void runtimeContentTypeMatchesTheDocumentedPublicResponse() throws Exception {
        mockMvc.perform(get("/public/hola"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/plain"));
        assertNotNull(apiDocs().read("$.paths['/public/hola'].get.responses['200'].content['text/plain']"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void documentsProblemDetailForEveryApiOperationFailure() throws Exception {
        Map<String, Object> paths = apiDocs().read("$.paths");
        for (Map.Entry<String, Object> path : paths.entrySet()) {
            if (!path.getKey().startsWith("/api/")) {
                continue;
            }
            Map<String, Object> pathItem = (Map<String, Object>) path.getValue();
            for (String method : new String[]{"get", "post", "put", "patch", "delete"}) {
                if (!(pathItem.get(method) instanceof Map<?, ?> operation)) {
                    continue;
                }
                Map<String, Object> responses = (Map<String, Object>) operation.get("responses");
                for (String code : new String[]{"400", "401", "403", "404", "409", "500"}) {
                    Map<String, Object> response = (Map<String, Object>) responses.get(code);
                    assertNotNull(response, path.getKey() + " " + method + " missing " + code);
                    Map<String, Object> content = (Map<String, Object>) response.get("content");
                    assertNotNull(content, path.getKey() + " " + method + " " + code + " lacks problem content");
                    Map<String, Object> problem = (Map<String, Object>) content.get("application/problem+json");
                    assertNotNull(problem, path.getKey() + " " + method + " " + code + " media type");
                    Map<String, Object> schema = (Map<String, Object>) problem.get("schema");
                    assertEquals("#/components/schemas/ProblemDetail", schema.get("$ref"));
                }
            }
        }
    }

    private DocumentContext apiDocs() throws Exception {
        return JsonPath.parse(mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
    }

    private static Stream<Arguments> operations() {
        return Stream.of(
                op("/auth/login", "post", "200", "application/json", "JwtResponse"),
                op("/auth/register", "post", "201", "application/json", "RegisterResponse"),
                op("/api/pedidos", "post", "201", "application/hal+json", "PedidoResponse"),
                op("/api/pedidos/mis-pedidos", "get", "200", "application/hal+json", "array:PedidoResponse"),
                op("/api/pedidos/{id}", "get", "200", "application/hal+json", "PedidoResponse"),
                op("/api/pedidos/{id}", "delete", "204", null, null),
                op("/api/pedidos/{id}/estado", "patch", "200", "application/hal+json", "PedidoResponse"),
                op("/api/sucursales", "get", "200", "application/hal+json", "SucursalResponse"),
                op("/api/sucursales/{sucursalId}/productos/{productoId}/disponibilidad", "get", "200", "application/hal+json", "DisponibilidadResponse"),
                op("/api/admin/proveedores", "post", "201", "application/json", "ProveedorResponse"),
                op("/api/admin/productos/{productoId}/proveedor-reposicion", "put", "200", null, null),
                op("/api/admin/sucursales", "post", "201", "application/hal+json", "SucursalResponse"),
                op("/api/admin/sucursales/{id}", "get", "200", "application/hal+json", "SucursalResponse"),
                op("/api/admin/sucursales/{id}/stock", "put", "200", "application/hal+json", "StockSucursalResponse"),
                op("/api/admin/sucursales/{id}/entradas", "post", "200", "application/hal+json", "StockSucursalResponse"),
                op("/api/admin/sucursales/{id}/salidas", "post", "200", "application/hal+json", "StockSucursalResponse"),
                op("/api/ordenes-compra", "get", "200", "application/hal+json", "OrdenCompraResponse"),
                op("/api/promociones", "get", "200", "application/json", "array:PromocionResponse"),
                op("/api/promociones/{id}", "get", "200", "application/json", "PromocionResponse"),
                op("/api/promociones", "post", "201", "application/json", "PromocionResponse"),
                op("/api/promociones/{id}", "put", "200", "application/json", "PromocionResponse"),
                op("/api/promociones/{id}", "delete", "204", null, null),
                op("/api/reportes/rotacion", "get", "200", "application/json", "array:RotacionProductoResponse"));
    }

    private static Stream<Arguments> requests() {
        return Stream.of(
                Arguments.of("/auth/login", "post", "LoginRequest"), Arguments.of("/auth/register", "post", "RegisterRequest"),
                Arguments.of("/api/pedidos", "post", "CrearPedidoRequest"), Arguments.of("/api/pedidos/{id}/estado", "patch", "CambiarEstadoPedidoRequest"),
                Arguments.of("/api/admin/proveedores", "post", "CrearProveedorRequest"), Arguments.of("/api/admin/productos/{productoId}/proveedor-reposicion", "put", "ConfigurarProveedorProductoRequest"),
                Arguments.of("/api/admin/sucursales", "post", "CrearSucursalRequest"), Arguments.of("/api/admin/sucursales/{id}/stock", "put", "ConfigurarStockRequest"),
                Arguments.of("/api/admin/sucursales/{id}/entradas", "post", "MovimientoStockRequest"), Arguments.of("/api/admin/sucursales/{id}/salidas", "post", "MovimientoStockRequest"),
                Arguments.of("/api/promociones", "post", "PromocionRequest"), Arguments.of("/api/promociones/{id}", "put", "PromocionRequest"));
    }

    private static Arguments op(String path, String method, String status, String mediaType, String schema) {
        return Arguments.of(path, method, status, mediaType, schema);
    }
}
