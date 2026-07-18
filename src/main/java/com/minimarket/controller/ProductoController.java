package com.minimarket.controller;

import com.minimarket.api.dto.ProductoRequest;
import com.minimarket.api.dto.ProductoResponse;
import com.minimarket.api.mapper.ResourceMapper;
import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.security.SecurityRoles;
import com.minimarket.service.CategoriaService;
import com.minimarket.service.ProductoService;
import com.minimarket.promocion.PromocionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@Validated
@RequestMapping(value = "/api/productos", produces = MediaTypes.HAL_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Catálogo de productos en formato HAL.")
@SecurityRequirement(name = "bearerAuth")
public class ProductoController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;
    private final PromocionService promocionService;

    @GetMapping
    @Operation(summary = "Lista productos", description = "Requiere autenticación Bearer JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Colección HAL de productos.", content = @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE, schema = @Schema(implementation = ProductoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token Bearer JWT ausente o inválido.",
                    content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                            schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public CollectionModel<EntityModel<ProductoResponse>> listarProductos() {
        List<EntityModel<ProductoResponse>> productos = productoService.findAll().stream()
                .map(this::toModel)
                .toList();
        return CollectionModel.of(productos,
                linkTo(methodOn(ProductoController.class).listarProductos()).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un producto", description = "Requiere autenticación Bearer JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recurso HAL del producto.", content = @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE, schema = @Schema(implementation = ProductoResponse.class))),
            @ApiResponse(responseCode = "400", description = "ID inválido; error RFC 9457.", content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Token Bearer JWT ausente o inválido.", content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Producto inexistente; error RFC 9457.", content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public EntityModel<ProductoResponse> obtenerProductoPorId(
            @Parameter(in = ParameterIn.PATH, required = true) @PathVariable @Positive Long id) {
        return toModel(requireProduct(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('" + SecurityRoles.ADMIN + "')")
    @Operation(summary = "Crea un producto", description = "Requiere Bearer JWT con rol ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Producto creado; Location apunta al enlace self.", content = @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE, schema = @Schema(implementation = ProductoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Cuerpo inválido; error RFC 9457.", content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Token Bearer JWT ausente o inválido.", content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no tiene rol ADMIN; error RFC 9457.", content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Categoría inexistente; error RFC 9457.", content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<EntityModel<ProductoResponse>> guardarProducto(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProductoRequest.class)))
            @Valid @org.springframework.web.bind.annotation.RequestBody ProductoRequest request) {
        Producto saved = productoService.save(newProduct(request, requireCategory(request.categoriaId())));
        EntityModel<ProductoResponse> model = toModel(saved);
        URI location = model.getRequiredLink("self").toUri();
        return ResponseEntity.created(location).body(model);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('" + SecurityRoles.ADMIN + "')")
    @Operation(summary = "Actualiza un producto", description = "Requiere Bearer JWT con rol ADMIN.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProductoRequest.class))))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto actualizado en formato HAL.", content = @Content(
                    mediaType = MediaTypes.HAL_JSON_VALUE, schema = @Schema(implementation = ProductoResponse.class))),
            @ApiResponse(responseCode = "400", description = "ID o cuerpo inválido; error RFC 9457.", content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Token Bearer JWT ausente o inválido.", content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no tiene rol ADMIN; error RFC 9457.", content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Producto o categoría inexistente; error RFC 9457.", content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public EntityModel<ProductoResponse> actualizarProducto(@PathVariable @Positive Long id,
                                                              @Valid @RequestBody ProductoRequest request) {
        Producto existing = requireProduct(id);
        apply(request, existing, requireCategory(request.categoriaId()));
        return toModel(productoService.save(existing));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('" + SecurityRoles.ADMIN + "')")
    @Operation(summary = "Elimina un producto", description = "Requiere Bearer JWT con rol ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Producto eliminado."),
            @ApiResponse(responseCode = "400", description = "ID inválido; error RFC 9457.", content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Token Bearer JWT ausente o inválido.", content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "El usuario autenticado no tiene rol ADMIN; error RFC 9457.", content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Producto inexistente; error RFC 9457.", content = @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<Void> eliminarProducto(@PathVariable @Positive Long id) {
        productoService.deleteById(requireProduct(id).getId());
        return ResponseEntity.noContent().build();
    }

    private Producto requireProduct(Long id) {
        Producto producto = productoService.findById(id);
        if (producto == null) {
            throw new NoSuchElementException("Producto no encontrado.");
        }
        return producto;
    }

    private Categoria requireCategory(Long id) {
        Categoria categoria = categoriaService.findById(id);
        if (categoria == null) {
            throw new NoSuchElementException("Categoría no encontrada.");
        }
        return categoria;
    }

    private Producto newProduct(ProductoRequest request, Categoria categoria) {
        Producto producto = new Producto();
        apply(request, producto, categoria);
        producto.setStock(0);
        return producto;
    }

    private void apply(ProductoRequest request, Producto producto, Categoria categoria) {
        producto.setNombre(request.nombre());
        producto.setPrecio(request.precio().doubleValue());
        producto.setCategoria(categoria);
    }

    private EntityModel<ProductoResponse> toModel(Producto producto) {
        ProductoResponse response = ResourceMapper.toResponse(producto, precioEfectivo(producto));
        return EntityModel.of(response,
                linkTo(methodOn(ProductoController.class).obtenerProductoPorId(producto.getId())).withSelfRel(),
                linkTo(methodOn(ProductoController.class).listarProductos()).withRel("collection"),
                linkTo(CategoriaController.class).slash(producto.getCategoria().getId()).withRel("categoria"));
    }

    private java.math.BigDecimal precioEfectivo(Producto producto) {
        if (promocionService == null) {
            return java.math.BigDecimal.valueOf(producto.getPrecio());
        }
        java.math.BigDecimal precio = promocionService.calcularPrecioEfectivo(producto.getId(), java.time.LocalDate.now());
        return precio == null ? java.math.BigDecimal.valueOf(producto.getPrecio()) : precio;
    }
}
