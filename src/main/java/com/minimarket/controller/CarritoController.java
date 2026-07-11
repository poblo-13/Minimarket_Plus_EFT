package com.minimarket.controller;

import com.minimarket.api.dto.CarritoRequest;
import com.minimarket.api.dto.CarritoResponse;
import com.minimarket.entity.Carrito;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.service.CarritoService;
import com.minimarket.service.ProductoService;
import com.minimarket.service.UsuarioService;
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
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
@Validated
@Tag(name = "Carrito", description = "Ítems del carrito de compra")
@SecurityRequirement(name = "basicAuth")
public class CarritoController {

    private final CarritoService carritoService;
    private final ProductoService productoService;
    private final UsuarioService usuarioService;

    @GetMapping
    @Operation(summary = "Listar ítems del carrito")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ítems encontrados"),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public CollectionModel<EntityModel<CarritoResponse>> listarCarrito() {
        List<EntityModel<CarritoResponse>> items = carritoService.findAll().stream().map(this::toModel).toList();
        return CollectionModel.of(items, linkTo(methodOn(CarritoController.class).listarCarrito()).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un ítem del carrito")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Ítem encontrado"), @ApiResponse(responseCode = "400", description = "Identificador inválido", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))), @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))), @ApiResponse(responseCode = "404", description = "Ítem no encontrado", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))})
    public EntityModel<CarritoResponse> obtenerCarritoPorId(@PathVariable @Positive Long id) { return toModel(requireCartItem(id)); }

    @PostMapping
    @Operation(summary = "Agregar un producto al carrito")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Ítem creado"), @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))), @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))), @ApiResponse(responseCode = "404", description = "Usuario o producto no encontrado", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))})
    public ResponseEntity<EntityModel<CarritoResponse>> agregarProductoAlCarrito(@Valid @RequestBody CarritoRequest request) {
        EntityModel<CarritoResponse> model = toModel(carritoService.save(fromRequest(request, new Carrito())));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un ítem del carrito")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Ítem actualizado"), @ApiResponse(responseCode = "400", description = "Solicitud o identificador inválido", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))), @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))), @ApiResponse(responseCode = "404", description = "Ítem, usuario o producto no encontrado", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))})
    public EntityModel<CarritoResponse> actualizarCarrito(@PathVariable @Positive Long id, @Valid @RequestBody CarritoRequest request) { return toModel(carritoService.save(fromRequest(request, requireCartItem(id)))); }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un ítem del carrito")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Ítem eliminado"), @ApiResponse(responseCode = "400", description = "Identificador inválido", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))), @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))), @ApiResponse(responseCode = "404", description = "Ítem no encontrado", content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))})
    public ResponseEntity<Void> eliminarProductoDelCarrito(@PathVariable @Positive Long id) { carritoService.deleteById(requireCartItem(id).getId()); return ResponseEntity.noContent().build(); }

    private Carrito requireCartItem(Long id) { Carrito item = carritoService.findById(id); if (item == null) throw new NoSuchElementException("Carrito no encontrado"); return item; }
    private Carrito fromRequest(CarritoRequest request, Carrito item) {
        Usuario user = usuarioService.findById(request.usuarioId()).orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
        Producto product = productoService.findById(request.productoId());
        if (product == null) throw new NoSuchElementException("Producto no encontrado");
        item.setUsuario(user); item.setProducto(product); item.setCantidad(request.cantidad()); return item;
    }
    private EntityModel<CarritoResponse> toModel(Carrito item) {
        CarritoResponse response = new CarritoResponse(item.getId(), item.getUsuario().getId(), item.getProducto().getId(), item.getCantidad());
        return EntityModel.of(response, linkTo(methodOn(CarritoController.class).obtenerCarritoPorId(item.getId())).withSelfRel(), linkTo(methodOn(UsuarioController.class).obtenerUsuarioPorId(item.getUsuario().getId())).withRel("usuario"), linkTo(methodOn(ProductoController.class).obtenerProductoPorId(item.getProducto().getId())).withRel("producto"));
    }
}
