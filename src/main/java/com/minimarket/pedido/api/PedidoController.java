package com.minimarket.pedido.api;

import com.minimarket.pedido.domain.EstadoPedido;
import com.minimarket.pedido.domain.Pedido;
import com.minimarket.pedido.repository.PedidoRepository;
import com.minimarket.pedido.service.PedidoService;
import com.minimarket.security.SecurityRoles;
import com.minimarket.controller.VentaController;
import com.minimarket.sucursal.api.AdministracionSucursalController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Pedidos creados por el cliente autenticado")
@SecurityRequirement(name = "bearerAuth")
public class PedidoController {
    private final PedidoService pedidoService;
    private final PedidoRepository pedidoRepository;

    public PedidoController(PedidoService pedidoService, PedidoRepository pedidoRepository) {
        this.pedidoService = pedidoService;
        this.pedidoRepository = pedidoRepository;
    }

    @PostMapping
    @Operation(summary = "Crear pedido", description = "El cliente se obtiene del JWT; estado, precios y fechas los calcula el servidor.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CrearPedidoRequest.class))))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido creado", content = @Content(mediaType = "application/hal+json", schema = @Schema(implementation = PedidoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Producto o cliente no encontrado", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflicto de estado", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))})
    public ResponseEntity<EntityModel<PedidoResponse>> crear(@Valid @RequestBody CrearPedidoRequest request,
                                                               Authentication authentication) {
        EntityModel<PedidoResponse> resource = resource(pedidoService.crear(authentication.getName(), request), authentication);
        return ResponseEntity.created(resource.getRequiredLink("self").toUri()).body(resource);
    }

    @GetMapping
    public CollectionModel<EntityModel<PedidoResponse>> listarOperativos(
            @RequestParam(required = false) EstadoPedido estado,
            @RequestParam(required = false) Long sucursalId,
            Authentication authentication) {
        List<EntityModel<PedidoResponse>> resources = pedidoService.listarOperativos(estado, sucursalId).stream()
                .map(pedido -> resource(pedido, authentication)).toList();
        return CollectionModel.of(resources,
                linkTo(methodOn(PedidoController.class).listarOperativos(null, null, null)).withSelfRel());
    }

    @GetMapping("/mis-pedidos")
    @Operation(summary = "Listar mis pedidos")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Pedidos del cliente", content = @Content(mediaType = "application/hal+json", array = @ArraySchema(schema = @Schema(implementation = PedidoResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))})
    public CollectionModel<EntityModel<PedidoResponse>> listarMisPedidos(Authentication authentication) {
        List<Pedido> pedidos = isStaff(authentication) ? pedidoRepository.findAll()
                : pedidoService.listarParaCliente(authentication.getName());
        List<EntityModel<PedidoResponse>> resources = pedidos.stream().map(pedido -> resource(pedido, authentication)).toList();
        return CollectionModel.of(resources, linkTo(methodOn(PedidoController.class).listarMisPedidos(null)).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener uno de mis pedidos")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Pedido encontrado", content = @Content(mediaType = "application/hal+json", schema = @Schema(implementation = PedidoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Identificador inválido", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Pedido inexistente o ajeno", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))})
    public EntityModel<PedidoResponse> obtener(@PathVariable @Positive Long id, Authentication authentication) {
        Pedido pedido = isStaff(authentication)
                ? pedidoRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Pedido no encontrado"))
                : pedidoService.obtenerParaCliente(id, authentication.getName());
        return resource(pedido, authentication);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar un pedido pendiente propio")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Pedido cancelado"),
            @ApiResponse(responseCode = "400", description = "Identificador inválido", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Pedido inexistente o ajeno", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "El pedido no está pendiente", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))})
    public ResponseEntity<Void> cancelar(@PathVariable @Positive Long id, Authentication authentication) {
        pedidoService.cancelar(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Transicionar estado", description = "Endpoint preparado para protegerse con CAJERO/ADMIN. CANCELADO solo puede ejecutarlo el dueño mediante DELETE.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CambiarEstadoPedidoRequest.class))))
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Estado actualizado", content = @Content(mediaType = "application/hal+json", schema = @Schema(implementation = PedidoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Pedido inexistente", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Transición inválida", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))})
    public EntityModel<PedidoResponse> cambiarEstado(@PathVariable @Positive Long id,
                                                       @Valid @RequestBody CambiarEstadoPedidoRequest request,
                                                       Authentication authentication) {
        if (request.estado() == EstadoPedido.CANCELADO) {
            throw new IllegalArgumentException("CANCELADO solo puede realizarse por el dueño del pedido");
        }
        return resource(pedidoService.cambiarEstado(id, request.estado()), authentication);
    }

    private EntityModel<PedidoResponse> resource(Pedido pedido, Authentication authentication) {
        EntityModel<PedidoResponse> model = EntityModel.of(PedidoResponse.desde(pedido),
                linkTo(methodOn(PedidoController.class).obtener(pedido.getId(), null)).withSelfRel(),
                linkTo(methodOn(PedidoController.class).listarMisPedidos(null)).withRel("collection"));
        EstadoPedido siguiente = siguienteEstado(pedido.getEstado());
        if (!isStaff(authentication) && pedido.getEstado() == EstadoPedido.PENDIENTE) {
            model.add(linkTo(methodOn(PedidoController.class).cancelar(pedido.getId(), null)).withRel("cancelar"));
        }
        if (isStaff(authentication) && siguiente != null) {
            model.add(linkTo(methodOn(PedidoController.class).cambiarEstado(pedido.getId(), null, null))
                    .withRel("transicionar-" + siguiente.name().toLowerCase()));
        }
        if (pedido.getVenta() != null && pedido.getVenta().getId() != null) {
            model.add(linkTo(methodOn(VentaController.class).obtenerVentaPorId(pedido.getVenta().getId())).withRel("venta"));
        }
        if (isStaff(authentication) && pedido.getSucursalId() != null) {
            model.add(linkTo(methodOn(AdministracionSucursalController.class).obtener(pedido.getSucursalId())).withRel("sucursal"));
        }
        return model;
    }

    private boolean isStaff(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream().anyMatch(authority ->
                (SecurityRoles.AUTHORITY_PREFIX + SecurityRoles.CAJERO).equals(authority.getAuthority())
                        || (SecurityRoles.AUTHORITY_PREFIX + SecurityRoles.ADMIN).equals(authority.getAuthority()));
    }

    private EstadoPedido siguienteEstado(EstadoPedido estado) {
        return switch (estado) {
            case PENDIENTE -> EstadoPedido.CONFIRMADO;
            case CONFIRMADO -> EstadoPedido.EN_PREPARACION;
            case EN_PREPARACION -> EstadoPedido.LISTO;
            case LISTO -> EstadoPedido.ENTREGADO;
            case ENTREGADO, CANCELADO -> null;
        };
    }
}
