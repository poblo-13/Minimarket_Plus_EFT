package com.minimarket.controller;

import com.minimarket.api.dto.DetalleVentaRequest;
import com.minimarket.api.dto.DetalleVentaResponse;
import com.minimarket.api.mapper.ResourceMapper;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.DetalleVentaService;
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
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/detalle-ventas")
@RequiredArgsConstructor
@Tag(name = "Detalles de venta")
@SecurityRequirement(name = "basicAuth")
public class DetalleVentaController {
    private final DetalleVentaService detalleVentaService;
    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;

    @GetMapping
    @Operation(summary = "Listar detalles de venta")
    @ApiResponses(@ApiResponse(responseCode = "401", description = "Autenticación Basic requerida"))
    public CollectionModel<EntityModel<DetalleVentaResponse>> listarDetalleVentas() {
        List<EntityModel<DetalleVentaResponse>> resources = detalleVentaService.findAll().stream().map(this::resource).toList();
        return CollectionModel.of(resources, linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas()).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un detalle de venta")
    @ApiResponses({@ApiResponse(responseCode = "401", description = "Autenticación Basic requerida"),
            @ApiResponse(responseCode = "404", description = "Detalle de venta no encontrado")})
    public ResponseEntity<EntityModel<DetalleVentaResponse>> obtenerDetalleVentaPorId(@PathVariable Long id) {
        DetalleVenta detalle = detalleVentaService.findById(id);
        return detalle == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(resource(detalle));
    }

    @PostMapping
    @Operation(summary = "Crear un detalle de venta")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Detalle de venta creado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "Autenticación Basic requerida"),
            @ApiResponse(responseCode = "404", description = "Venta o producto relacionado no encontrado")})
    public ResponseEntity<EntityModel<DetalleVentaResponse>> guardarDetalleVenta(@Valid @RequestBody DetalleVentaRequest request) {
        DetalleVenta detalle = new DetalleVenta();
        apply(request, detalle);
        DetalleVenta saved = detalleVentaService.save(detalle);
        EntityModel<DetalleVentaResponse> resource = resource(saved);
        return ResponseEntity.created(URI.create(resource.getRequiredLink("self").getHref())).body(resource);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un detalle de venta")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Detalle de venta actualizado"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "Autenticación Basic requerida"),
            @ApiResponse(responseCode = "404", description = "Detalle, venta o producto relacionado no encontrado")})
    public ResponseEntity<EntityModel<DetalleVentaResponse>> actualizarDetalleVenta(@PathVariable Long id, @Valid @RequestBody DetalleVentaRequest request) {
        DetalleVenta detalle = detalleVentaService.findById(id);
        if (detalle == null) return ResponseEntity.notFound().build();
        apply(request, detalle);
        return ResponseEntity.ok(resource(detalleVentaService.save(detalle)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un detalle de venta")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Detalle de venta eliminado"),
            @ApiResponse(responseCode = "401", description = "Autenticación Basic requerida"),
            @ApiResponse(responseCode = "404", description = "Detalle de venta no encontrado")})
    public ResponseEntity<Void> eliminarDetalleVenta(@PathVariable Long id) {
        if (detalleVentaService.findById(id) == null) return ResponseEntity.notFound().build();
        detalleVentaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void apply(DetalleVentaRequest request, DetalleVenta detalle) {
        detalle.setVenta(ventaRepository.findById(request.ventaId()).orElseThrow(NoSuchElementException::new));
        detalle.setProducto(productoRepository.findById(request.productoId()).orElseThrow(NoSuchElementException::new));
        detalle.setCantidad(request.cantidad());
        detalle.setPrecio(request.precio().doubleValue());
    }

    private EntityModel<DetalleVentaResponse> resource(DetalleVenta detalle) {
        EntityModel<DetalleVentaResponse> resource = EntityModel.of(ResourceMapper.toResponse(detalle),
                linkTo(methodOn(DetalleVentaController.class).obtenerDetalleVentaPorId(detalle.getId())).withSelfRel(),
                linkTo(methodOn(DetalleVentaController.class).listarDetalleVentas()).withRel("detalle-ventas"));
        if (detalle.getVenta() != null && detalle.getVenta().getId() != null) {
            resource.add(linkTo(methodOn(VentaController.class).obtenerVentaPorId(detalle.getVenta().getId())).withRel("venta"));
        }
        if (detalle.getProducto() != null && detalle.getProducto().getId() != null) {
            resource.add(linkTo(methodOn(ProductoController.class).obtenerProductoPorId(detalle.getProducto().getId())).withRel("producto"));
        }
        return resource;
    }
}
