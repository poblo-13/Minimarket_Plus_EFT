package com.minimarket.controller;

import com.minimarket.api.dto.VentaRequest;
import com.minimarket.api.dto.VentaResponse;
import com.minimarket.api.mapper.ResourceMapper;
import com.minimarket.entity.Venta;
import com.minimarket.repository.VentaRepository;
import com.minimarket.security.CurrentActorService;
import com.minimarket.security.SecurityRoles;
import com.minimarket.service.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ProblemDetail;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import org.springframework.security.core.Authentication;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/api/ventas", produces = MediaTypes.HAL_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Ventas")
@SecurityRequirement(name = "bearerAuth")
public class VentaController {
    private final VentaService ventaService;
    private final VentaRepository ventaRepository;
    private final CurrentActorService currentActor;

    @GetMapping
    @Operation(summary = "Listar ventas")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Colección HAL de ventas", content = @Content(mediaType = MediaTypes.HAL_JSON_VALUE, schema = @Schema(implementation = CollectionModel.class))), @ApiResponse(responseCode = "401", description = "Autenticación Bearer JWT requerida; error RFC 9457.",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))})
    public CollectionModel<EntityModel<VentaResponse>> listarVentas() {
        List<Venta> ventas = currentActor.isStaff() ? ventaRepository.findAll()
                : ventaRepository.findByUsuarioId(currentActor.userId());
        List<EntityModel<VentaResponse>> resources = ventas.stream().map(this::resource).toList();
        return CollectionModel.of(resources, linkTo(methodOn(VentaController.class).listarVentas()).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una venta")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Venta HAL", content = @Content(mediaType = MediaTypes.HAL_JSON_VALUE, schema = @Schema(implementation = VentaResponse.class))), @ApiResponse(responseCode = "401", description = "Autenticación Bearer JWT requerida; error RFC 9457.",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada")})
    public ResponseEntity<EntityModel<VentaResponse>> obtenerVentaPorId(@PathVariable Long id) {
        Venta venta = (currentActor.isStaff() ? ventaRepository.findById(id)
                : ventaRepository.findByIdAndUsuarioId(id, currentActor.userId())).orElse(null);
        return venta == null ? notFound("Venta no encontrada") : ResponseEntity.ok(resource(venta));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('" + SecurityRoles.CAJERO + "', '" + SecurityRoles.ADMIN + "')")
    @Operation(summary = "Registrar una venta con sus líneas", description = "El servidor asigna fecha y precios; líneas duplicadas se consolidan.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Venta creada", content = @Content(mediaType = MediaTypes.HAL_JSON_VALUE, schema = @Schema(implementation = VentaResponse.class))),
             @ApiResponse(responseCode = "400", description = "Solicitud inválida; error RFC 9457.", content = @Content(
                     mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                     schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
             @ApiResponse(responseCode = "401", description = "Autenticación Bearer JWT requerida; error RFC 9457.", content = @Content(
                     mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                     schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
             @ApiResponse(responseCode = "403", description = "Se requiere rol CAJERO o ADMIN; error RFC 9457.", content = @Content(
                     mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                     schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
             @ApiResponse(responseCode = "404", description = "Usuario o producto relacionado no encontrado; error RFC 9457.", content = @Content(
                     mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                     schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
             @ApiResponse(responseCode = "409", description = "Stock insuficiente; error INSUFFICIENT_STOCK RFC 9457.", content = @Content(
                     mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                     schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))})
    public ResponseEntity<EntityModel<VentaResponse>> guardarVenta(@Valid @RequestBody VentaRequest request) {
        Venta saved = ventaService.registrar(request);
        EntityModel<VentaResponse> resource = resource(saved);
        return ResponseEntity.created(URI.create(resource.getRequiredLink("self").getHref())).body(resource);
    }

    private EntityModel<VentaResponse> resource(Venta venta) {
        EntityModel<VentaResponse> resource = EntityModel.of(ResourceMapper.toResponse(venta),
                linkTo(methodOn(VentaController.class).obtenerVentaPorId(venta.getId())).withSelfRel(),
                linkTo(methodOn(VentaController.class).listarVentas()).withRel("ventas"),
                linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas()).withRel("detalles"));
        if (venta.getUsuario() != null && venta.getUsuario().getId() != null) {
            resource.add(linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(venta.getUsuario().getId())).withRel("usuario"));
        }
        return resource;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private ResponseEntity<EntityModel<VentaResponse>> notFound(String detail) {
        return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, detail));
    }

}
