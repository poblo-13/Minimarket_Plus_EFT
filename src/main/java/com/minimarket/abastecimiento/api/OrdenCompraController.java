package com.minimarket.abastecimiento.api;

import com.minimarket.abastecimiento.OrdenCompraConsultaService;
import com.minimarket.abastecimiento.OrdenCompraRepository;
import com.minimarket.abastecimiento.api.dto.OrdenCompraResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/ordenes-compra", produces = MediaTypes.HAL_JSON_VALUE)
@Tag(name = "Órdenes de compra", description = "Consulta administrativa de reposición en HAL.")
@SecurityRequirement(name = "bearerAuth")
public class OrdenCompraController {
    private final OrdenCompraConsultaService ordenCompraConsultaService;
    private final OrdenCompraRepository ordenCompraRepository;
    private final OrdenCompraResponseAssembler assembler;

    @GetMapping
    @Operation(summary = "Lista órdenes de compra", description = "Consulta administrativa autenticada; no permite mutaciones.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Colección HAL de órdenes de compra.", content = @Content(mediaType = MediaTypes.HAL_JSON_VALUE, schema = @Schema(implementation = OrdenCompraResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado.", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))})
    public CollectionModel<EntityModel<OrdenCompraResponse>> listarOrdenesCompra() {
        return CollectionModel.of(ordenCompraConsultaService.listar().stream().map(assembler::toModel).toList(),
                linkTo(methodOn(OrdenCompraController.class).listarOrdenesCompra()).withSelfRel());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtiene una orden de compra", description = "Consulta administrativa de una orden en formato HAL.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orden HAL", content = @Content(mediaType = MediaTypes.HAL_JSON_VALUE, schema = @Schema(implementation = OrdenCompraResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Se requiere ADMIN", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Orden inexistente", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))})
    public EntityModel<OrdenCompraResponse> obtenerOrdenCompra(@PathVariable Long id) {
        return assembler.toModel(ordenCompraRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Orden de compra no encontrada")));
    }
}
