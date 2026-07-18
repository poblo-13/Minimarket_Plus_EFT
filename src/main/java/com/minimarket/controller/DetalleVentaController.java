package com.minimarket.controller;

import com.minimarket.api.dto.DetalleVentaResponse;
import com.minimarket.api.mapper.ResourceMapper;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.security.SecurityRoles;
import com.minimarket.service.DetalleVentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/** Los detalles se crean exclusivamente mediante el agregado de venta y no pueden modificarse de forma independiente. */
@RestController
@RequestMapping("/api/detalle-ventas")
@RequiredArgsConstructor
@Tag(name = "Detalles de venta")
@SecurityRequirement(name = "bearerAuth")
public class DetalleVentaController {
    private final DetalleVentaService detalleVentaService;

    @GetMapping
    @Operation(summary = "Listar detalles de venta")
    @ApiResponses(@ApiResponse(responseCode = "401", description = "Autenticación Bearer JWT requerida"))
    public CollectionModel<EntityModel<DetalleVentaResponse>> listarDetalleVentas() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<DetalleVenta> detalles = isCliente(authentication)
                ? detalleVentaService.findAll().stream().filter(detalle -> isOwner(detalle, authentication)).toList()
                : detalleVentaService.findAll();
        return CollectionModel.of(detalles.stream().map(this::resource).toList(),
                linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas()).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un detalle de venta")
    @ApiResponses({@ApiResponse(responseCode = "401", description = "Autenticación Bearer JWT requerida"),
            @ApiResponse(responseCode = "404", description = "Detalle de venta no encontrado"),
            @ApiResponse(responseCode = "403", description = "Un cliente solo puede consultar sus propios detalles")})
    public ResponseEntity<EntityModel<DetalleVentaResponse>> obtenerDetalleVentaPorId(@PathVariable Long id) {
        DetalleVenta detalle = detalleVentaService.findById(id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (detalle != null && isCliente(authentication) && !isOwner(detalle, authentication)) {
            throw new AuthorizationDeniedException("No puede acceder a detalles de otro usuario");
        }
        return detalle == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(resource(detalle));
    }

    private boolean isCliente(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> (SecurityRoles.AUTHORITY_PREFIX + SecurityRoles.CLIENTE).equals(a.getAuthority()));
    }

    private boolean isOwner(DetalleVenta detalle, Authentication authentication) {
        return detalle.getVenta() != null && detalle.getVenta().getUsuario() != null
                && detalle.getVenta().getUsuario().getUsername().equals(authentication.getName());
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
}
