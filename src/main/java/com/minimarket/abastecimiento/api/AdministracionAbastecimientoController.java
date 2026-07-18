package com.minimarket.abastecimiento.api;

import com.minimarket.abastecimiento.api.dto.*;
import com.minimarket.sucursal.AdministracionStockService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdministracionAbastecimientoController {
    private final AdministracionStockService administracion;
    @PostMapping("/proveedores") @ResponseStatus(HttpStatus.CREATED)
    public ProveedorResponse crearProveedor(@Valid @RequestBody CrearProveedorRequest request) {
        var proveedor = administracion.crearProveedor(request.nombre());
        return new ProveedorResponse(proveedor.getId(), proveedor.getNombre());
    }
    @PutMapping("/productos/{productoId}/proveedor-reposicion")
    public void configurarProveedor(@PathVariable @Positive Long productoId, @Valid @RequestBody ConfigurarProveedorProductoRequest request) {
        administracion.configurarProveedor(productoId, request.proveedorId());
    }
}
