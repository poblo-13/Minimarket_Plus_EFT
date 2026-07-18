package com.minimarket.sucursal.api;

import com.minimarket.sucursal.StockSucursalService;
import com.minimarket.sucursal.SucursalRepository;
import com.minimarket.sucursal.api.dto.DisponibilidadResponse;
import com.minimarket.sucursal.api.dto.SucursalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping(value = "/api/sucursales", produces = MediaTypes.HAL_JSON_VALUE)
@Tag(name = "Sucursales", description = "Consulta de sucursales y su disponibilidad operativa en HAL.")
@SecurityRequirement(name = "bearerAuth")
public class SucursalController {
    private final SucursalRepository sucursalRepository;
    private final StockSucursalService stockSucursalService;
    private final SucursalResponseAssembler sucursalAssembler;
    private final DisponibilidadResponseAssembler disponibilidadAssembler;

    @GetMapping
    @Operation(summary = "Lista sucursales", description = "Consulta autenticada de sucursales.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Colección HAL de sucursales.", content = @Content(mediaType = MediaTypes.HAL_JSON_VALUE, schema = @Schema(implementation = SucursalResponse.class))),
            @ApiResponse(responseCode = "401", description = "No autenticado.", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))})
    public CollectionModel<EntityModel<SucursalResponse>> listarSucursales() {
        return CollectionModel.of(sucursalRepository.findAll().stream().map(sucursalAssembler::toModel).toList(),
                linkTo(methodOn(SucursalController.class).listarSucursales()).withSelfRel());
    }

    @GetMapping("/{sucursalId}/stock")
    public List<DisponibilidadResponse> listarStockPorSucursal(@PathVariable @Positive Long sucursalId) {
        return stockSucursalService.listarStockPorSucursal(sucursalId).stream()
                .map(stock -> new DisponibilidadResponse(stock.getSucursal().getId(), stock.getProducto().getId(),
                        stock.getDisponible(), stock.getStockMinimo()))
                .toList();
    }

    @GetMapping("/{sucursalId}/productos/{productoId}/disponibilidad")
    @Operation(summary = "Consulta disponibilidad", description = "Devuelve cantidad y mínimo sin exponer entidades ni grafos de persistencia.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Disponibilidad HAL.", content = @Content(mediaType = MediaTypes.HAL_JSON_VALUE, schema = @Schema(implementation = DisponibilidadResponse.class))),
            @ApiResponse(responseCode = "400", description = "ID inválido.", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Sucursal, producto o disponibilidad inexistente.", content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))})
    public EntityModel<DisponibilidadResponse> consultarDisponibilidad(
            @PathVariable @Positive Long sucursalId, @PathVariable @Positive Long productoId) {
        return disponibilidadAssembler.toModel(stockSucursalService.consultarStock(sucursalId, productoId));
    }
}
