package com.minimarket.sucursal.api;

import com.minimarket.sucursal.StockSucursal;
import com.minimarket.sucursal.api.dto.DisponibilidadResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class DisponibilidadResponseAssembler implements RepresentationModelAssembler<StockSucursal, EntityModel<DisponibilidadResponse>> {

    @Override
    public EntityModel<DisponibilidadResponse> toModel(StockSucursal stock) {
        Long sucursalId = stock.getSucursal().getId();
        Long productoId = stock.getProducto().getId();
        return EntityModel.of(new DisponibilidadResponse(sucursalId, productoId,
                        stock.getDisponible(), stock.getStockMinimo()),
                linkTo(methodOn(SucursalController.class).consultarDisponibilidad(sucursalId, productoId)).withSelfRel(),
                linkTo(methodOn(SucursalController.class).listarSucursales()).withRel("sucursales"));
    }
}
