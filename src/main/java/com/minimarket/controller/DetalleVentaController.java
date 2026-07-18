package com.minimarket.controller;

import com.minimarket.api.dto.DetalleVentaResponse;
import com.minimarket.api.mapper.ResourceMapper;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.repository.DetalleVentaRepository;
import com.minimarket.security.CurrentActorService;
import com.minimarket.security.SecurityRoles;
import com.minimarket.service.DetalleVentaService;
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
import org.springframework.http.ProblemDetail;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/** Los detalles se crean exclusivamente mediante el agregado de venta y no pueden modificarse de forma independiente. */
@RestController
@RequestMapping(value = "/api/detalle-ventas", produces = MediaTypes.HAL_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Detalles de venta")
@SecurityRequirement(name = "bearerAuth")
public class DetalleVentaController {
    private final DetalleVentaService detalleVentaService;
    private final DetalleVentaRepository detalleVentaRepository;
    private final CurrentActorService currentActor;

    @GetMapping
    @Operation(summary = "Listar detalles de venta")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Colección HAL de detalles", content = @Content(mediaType = MediaTypes.HAL_JSON_VALUE, schema = @Schema(implementation = CollectionModel.class))), @ApiResponse(responseCode = "401", description = "Autenticación Bearer JWT requerida", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))})
    public CollectionModel<EntityModel<DetalleVentaResponse>> listarDetalleVentas() {
        List<DetalleVenta> detalles = currentActor.isStaff() ? detalleVentaRepository.findAll()
                : detalleVentaRepository.findByVentaUsuarioId(currentActor.userId());
        return CollectionModel.of(detalles.stream().map(this::resource).toList(),
                linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas()).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un detalle de venta")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Detalle HAL", content = @Content(mediaType = MediaTypes.HAL_JSON_VALUE, schema = @Schema(implementation = DetalleVentaResponse.class))), @ApiResponse(responseCode = "401", description = "Autenticación Bearer JWT requerida", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Detalle de venta no encontrado", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Un cliente solo puede consultar sus propios detalles", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))})
    public ResponseEntity<EntityModel<DetalleVentaResponse>> obtenerDetalleVentaPorId(@PathVariable Long id) {
        DetalleVenta detalle = (currentActor.isStaff() ? detalleVentaRepository.findById(id)
                : detalleVentaRepository.findByIdAndVentaUsuarioId(id, currentActor.userId())).orElse(null);
        return detalle == null ? notFound("Detalle de venta no encontrado") : ResponseEntity.ok(resource(detalle));
    }

    private EntityModel<DetalleVentaResponse> resource(DetalleVenta detalle) {
        EntityModel<DetalleVentaResponse> resource = EntityModel.of(ResourceMapper.toResponse(detalle),
                linkTo(methodOn(DetalleVentaController.class).obtenerDetalleVentaPorId(detalle.getId())).withSelfRel(),
                linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas()).withRel("detalle-ventas"));
        if (detalle.getVenta() != null && detalle.getVenta().getId() != null) {
            resource.add(linkTo(methodOn(VentaController.class).obtenerVentaPorId(detalle.getVenta().getId())).withRel("venta"));
        }
        return resource;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private ResponseEntity<EntityModel<DetalleVentaResponse>> notFound(String detail) {
        return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, detail));
    }
}
