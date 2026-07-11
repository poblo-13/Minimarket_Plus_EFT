package com.minimarket.controller;

import com.minimarket.api.dto.InventarioRequest;
import com.minimarket.api.dto.InventarioResponse;
import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.security.SecurityRoles;
import com.minimarket.service.InventarioService;
import com.minimarket.service.ProductoService;
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
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
@Validated
@Tag(name = "Inventario", description = "Movimientos de inventario")
@SecurityRequirement(name = "basicAuth")
public class InventarioController {

    private final InventarioService inventarioService;
    private final ProductoService productoService;

    @GetMapping
    @Operation(summary = "Listar movimientos de inventario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movimientos encontrados"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public CollectionModel<EntityModel<InventarioResponse>> listarMovimientosDeInventario() {
        List<EntityModel<InventarioResponse>> movements = inventarioService.findAll().stream()
                .map(this::toModel)
                .toList();
        return CollectionModel.of(movements,
                linkTo(methodOn(InventarioController.class).listarMovimientosDeInventario()).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un movimiento de inventario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movimiento encontrado"),
            @ApiResponse(responseCode = "400", description = "Identificador inválido", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Movimiento no encontrado", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public EntityModel<InventarioResponse> obtenerMovimientoPorId(@PathVariable @Positive Long id) {
        return toModel(requireMovement(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('" + SecurityRoles.ADMIN + "')")
    @Operation(summary = "Registrar un movimiento de inventario")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Movimiento creado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Se requiere rol ADMIN", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<EntityModel<InventarioResponse>> registrarMovimiento(@Valid @RequestBody InventarioRequest request) {
        Inventario saved = inventarioService.save(fromRequest(request, new Inventario()));
        EntityModel<InventarioResponse> model = toModel(saved);
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('" + SecurityRoles.ADMIN + "')")
    @Operation(summary = "Actualizar un movimiento de inventario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movimiento actualizado"),
            @ApiResponse(responseCode = "400", description = "Solicitud o identificador inválido", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Se requiere rol ADMIN", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Movimiento o producto no encontrado", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public EntityModel<InventarioResponse> actualizarMovimiento(@PathVariable @Positive Long id,
                                                                  @Valid @RequestBody InventarioRequest request) {
        Inventario updated = inventarioService.save(fromRequest(request, requireMovement(id)));
        return toModel(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('" + SecurityRoles.ADMIN + "')")
    @Operation(summary = "Eliminar un movimiento de inventario")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Movimiento eliminado"),
            @ApiResponse(responseCode = "400", description = "Identificador inválido", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Se requiere rol ADMIN", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Movimiento no encontrado", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<Void> eliminarMovimiento(@PathVariable @Positive Long id) {
        inventarioService.deleteById(requireMovement(id).getId());
        return ResponseEntity.noContent().build();
    }

    private Inventario requireMovement(Long id) {
        Inventario movement = inventarioService.findById(id);
        if (movement == null) {
            throw new NoSuchElementException("Inventario no encontrado");
        }
        return movement;
    }

    private Inventario fromRequest(InventarioRequest request, Inventario movement) {
        Producto product = productoService.findById(request.productoId());
        if (product == null) {
            throw new NoSuchElementException("Producto no encontrado");
        }
        movement.setProducto(product);
        movement.setCantidad(request.cantidad());
        movement.setTipoMovimiento(request.tipoMovimiento());
        movement.setFechaMovimiento(request.fechaMovimiento());
        return movement;
    }

    private EntityModel<InventarioResponse> toModel(Inventario movement) {
        InventarioResponse response = new InventarioResponse(movement.getId(), movement.getProducto().getId(),
                movement.getCantidad(), movement.getTipoMovimiento(), movement.getFechaMovimiento());
        return EntityModel.of(response,
                linkTo(methodOn(InventarioController.class).obtenerMovimientoPorId(movement.getId())).withSelfRel(),
                linkTo(methodOn(ProductoController.class).obtenerProductoPorId(movement.getProducto().getId())).withRel("producto"));
    }
}
