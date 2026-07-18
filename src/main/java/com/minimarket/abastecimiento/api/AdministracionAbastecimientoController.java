package com.minimarket.abastecimiento.api;

import com.minimarket.abastecimiento.api.dto.*;
import com.minimarket.sucursal.AdministracionStockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administración de abastecimiento")
@SecurityRequirement(name = "bearerAuth")
public class AdministracionAbastecimientoController {
    private final AdministracionStockService administracion;
    @PostMapping("/proveedores") @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear proveedor", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CrearProveedorRequest.class))))
    @ApiResponses({@ApiResponse(responseCode = "201", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProveedorResponse.class))), @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))), @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))), @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))})
    public ProveedorResponse crearProveedor(@Valid @RequestBody CrearProveedorRequest request) {
        var proveedor = administracion.crearProveedor(request.nombre());
        return new ProveedorResponse(proveedor.getId(), proveedor.getNombre());
    }
    @PutMapping("/productos/{productoId}/proveedor-reposicion")
    @Operation(summary = "Configurar proveedor de reposición", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ConfigurarProveedorProductoRequest.class))))
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Configuración aplicada"), @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))), @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))), @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))), @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))})
    public void configurarProveedor(@PathVariable @Positive Long productoId, @Valid @RequestBody ConfigurarProveedorProductoRequest request) {
        administracion.configurarProveedor(productoId, request.proveedorId());
    }
}
