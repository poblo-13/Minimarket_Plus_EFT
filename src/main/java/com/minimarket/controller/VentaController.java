package com.minimarket.controller;

import com.minimarket.api.dto.VentaRequest;
import com.minimarket.api.dto.VentaResponse;
import com.minimarket.api.mapper.ResourceMapper;
import com.minimarket.entity.Venta;
import com.minimarket.repository.UsuarioRepository;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@Tag(name = "Ventas")
@SecurityRequirement(name = "bearerAuth")
public class VentaController {
    private final VentaService ventaService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    @Operation(summary = "Listar ventas")
    @ApiResponses(@ApiResponse(responseCode = "401", description = "Autenticación Basic requerida; error RFC 9457.",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))))
    public CollectionModel<EntityModel<VentaResponse>> listarVentas() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<Venta> ventas = isCliente(authentication)
                ? ventaService.findByUsuarioId(currentUserId(authentication))
                : ventaService.findAll();
        List<EntityModel<VentaResponse>> resources = ventas.stream().map(this::resource).toList();
        return CollectionModel.of(resources, linkTo(methodOn(VentaController.class).listarVentas()).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una venta")
    @ApiResponses({@ApiResponse(responseCode = "401", description = "Autenticación Basic requerida; error RFC 9457.",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada")})
    public ResponseEntity<EntityModel<VentaResponse>> obtenerVentaPorId(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Venta venta = ventaService.findById(id);
        if (venta != null && isCliente(authentication) && !venta.getUsuario().getUsername().equals(authentication.getName())) {
            throw new AuthorizationDeniedException("No puede acceder a ventas de otro usuario");
        }
        return venta == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(resource(venta));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('" + SecurityRoles.CAJERO + "', '" + SecurityRoles.ADMIN + "')")
    @Operation(summary = "Registrar una venta con sus líneas", description = "El servidor asigna fecha y precios; líneas duplicadas se consolidan.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Venta creada"),
             @ApiResponse(responseCode = "400", description = "Solicitud inválida; error RFC 9457.", content = @Content(
                     mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                     schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
             @ApiResponse(responseCode = "401", description = "Autenticación Basic requerida; error RFC 9457.", content = @Content(
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

    private boolean isCliente(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> (SecurityRoles.AUTHORITY_PREFIX + SecurityRoles.CLIENTE).equals(authority.getAuthority()));
    }

    private Long currentUserId(Authentication authentication) {
        return usuarioRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new AuthorizationDeniedException("Usuario autenticado no encontrado"))
                .getId();
    }
}
