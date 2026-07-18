package com.minimarket.sucursal.api;

import com.minimarket.sucursal.Sucursal;
import com.minimarket.sucursal.api.dto.SucursalResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SucursalResponseAssembler implements RepresentationModelAssembler<Sucursal, EntityModel<SucursalResponse>> {

    @Override
    public EntityModel<SucursalResponse> toModel(Sucursal sucursal) {
        return EntityModel.of(new SucursalResponse(sucursal.getId(), sucursal.getNombre()),
                linkTo(methodOn(SucursalController.class).listarSucursales()).withRel("collection"));
    }
}
