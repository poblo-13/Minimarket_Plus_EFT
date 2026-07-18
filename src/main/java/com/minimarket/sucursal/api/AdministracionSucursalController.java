package com.minimarket.sucursal.api;

import com.minimarket.sucursal.AdministracionStockService;
import com.minimarket.sucursal.StockSucursalService;
import com.minimarket.sucursal.SucursalRepository;
import com.minimarket.sucursal.api.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/sucursales")
@PreAuthorize("hasRole('ADMIN')")
public class AdministracionSucursalController {
    private final AdministracionStockService administracion;
    private final SucursalRepository sucursales;
    private final StockSucursalService stocks;
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public EntityModel<SucursalResponse> crear(@Valid @RequestBody CrearSucursalRequest request) {
        var sucursal = administracion.crearSucursal(request.nombre());
        return EntityModel.of(new SucursalResponse(sucursal.getId(), sucursal.getNombre()), linkTo(methodOn(AdministracionSucursalController.class).obtener(sucursal.getId())).withSelfRel());
    }
    @GetMapping("/{id}") public EntityModel<SucursalResponse> obtener(@PathVariable @Positive Long id) {
        var s = sucursales.findById(id).orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Sucursal no encontrada"));
        return EntityModel.of(new SucursalResponse(s.getId(), s.getNombre()), linkTo(methodOn(AdministracionSucursalController.class).obtener(id)).withSelfRel());
    }
    @PutMapping("/{id}/stock") public EntityModel<StockSucursalResponse> configurar(@PathVariable @Positive Long id, @Valid @RequestBody ConfigurarStockRequest r) { return model(administracion.configurarStock(id, r.productoId(), r.disponible(), r.minimo(), r.proveedorId())); }
    @PostMapping("/{id}/entradas") public EntityModel<StockSucursalResponse> entrada(@PathVariable @Positive Long id, @Valid @RequestBody MovimientoStockRequest r) { return model(administracion.entrada(id, r.productoId(), r.cantidad())); }
    @PostMapping("/{id}/salidas") public EntityModel<StockSucursalResponse> salida(@PathVariable @Positive Long id, @Valid @RequestBody MovimientoStockRequest r) { return model(administracion.salida(id, r.productoId(), r.cantidad())); }
    private EntityModel<StockSucursalResponse> model(com.minimarket.sucursal.StockSucursal s) { return EntityModel.of(new StockSucursalResponse(s.getSucursal().getId(), s.getProducto().getId(), s.getDisponible(), s.getStockMinimo())); }
}
