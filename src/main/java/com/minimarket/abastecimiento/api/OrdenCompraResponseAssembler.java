package com.minimarket.abastecimiento.api;

import com.minimarket.abastecimiento.OrdenCompra;
import com.minimarket.abastecimiento.api.dto.OrdenCompraResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class OrdenCompraResponseAssembler implements RepresentationModelAssembler<OrdenCompra, EntityModel<OrdenCompraResponse>> {
    @Override
    public EntityModel<OrdenCompraResponse> toModel(OrdenCompra orden) {
        return EntityModel.of(new OrdenCompraResponse(orden.getId(), orden.getSucursal().getId(),
                        orden.getProducto().getId(), orden.getProveedor().getId(), orden.getCantidadSolicitada(), orden.getEstado()),
                linkTo(methodOn(OrdenCompraController.class).obtenerOrdenCompra(orden.getId())).withSelfRel(),
                linkTo(methodOn(OrdenCompraController.class).listarOrdenesCompra()).withRel("collection"));
    }
}
