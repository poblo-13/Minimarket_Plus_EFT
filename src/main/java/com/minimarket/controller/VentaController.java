package com.minimarket.controller;

import com.minimarket.api.dto.VentaRequest;
import com.minimarket.api.dto.VentaResponse;
import com.minimarket.api.mapper.ResourceMapper;
import com.minimarket.entity.Venta;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@Tag(name = "Ventas")
@SecurityRequirement(name = "basicAuth")
public class VentaController {
    private final VentaService ventaService;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    @Operation(summary = "Listar ventas")
    @ApiResponses(@ApiResponse(responseCode = "401", description = "Autenticación Basic requerida"))
    public CollectionModel<EntityModel<VentaResponse>> listarVentas() {
        List<EntityModel<VentaResponse>> resources = ventaService.findAll().stream().map(this::resource).toList();
        return CollectionModel.of(resources, linkTo(methodOn(VentaController.class).listarVentas()).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una venta")
    @ApiResponses({@ApiResponse(responseCode = "401", description = "Autenticación Basic requerida"),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada")})
    public ResponseEntity<EntityModel<VentaResponse>> obtenerVentaPorId(@PathVariable Long id) {
        Venta venta = ventaService.findById(id);
        return venta == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(resource(venta));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CAJERO', 'ADMIN')")
    @Operation(summary = "Crear una venta")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Venta creada"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "Autenticación Basic requerida"),
            @ApiResponse(responseCode = "403", description = "Se requiere rol CAJERO o ADMIN"),
            @ApiResponse(responseCode = "404", description = "Usuario relacionado no encontrado")})
    public ResponseEntity<EntityModel<VentaResponse>> guardarVenta(@Valid @RequestBody VentaRequest request) {
        Venta venta = new Venta();
        venta.setUsuario(usuarioRepository.findById(request.usuarioId()).orElseThrow(NoSuchElementException::new));
        venta.setFecha(request.fecha());
        Venta saved = ventaService.save(venta);
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
}
