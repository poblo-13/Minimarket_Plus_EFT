package com.minimarket.api.mapper;

import com.minimarket.api.dto.*;
import com.minimarket.entity.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/** Stateless, entity-to-API mapping that never exposes persistence graphs. */
public final class ResourceMapper {
    private ResourceMapper() { }

    public static CategoriaResponse toResponse(Categoria value) {
        return new CategoriaResponse(value.getId(), value.getNombre());
    }

    public static ProductoResponse toResponse(Producto value) {
        return new ProductoResponse(value.getId(), value.getNombre(), decimal(value.getPrecio()), value.getStock(),
                id(value.getCategoria()));
    }

    public static InventarioResponse toResponse(Inventario value) {
        return new InventarioResponse(value.getId(), id(value.getProducto()), value.getCantidad(),
                value.getTipoMovimiento(), value.getFechaMovimiento());
    }

    public static CarritoResponse toResponse(Carrito value) {
        return new CarritoResponse(value.getId(), id(value.getUsuario()), id(value.getProducto()), value.getCantidad());
    }

    public static VentaResponse toResponse(Venta value) {
        List<Long> detalleIds = value.getDetalles() == null ? List.of() : value.getDetalles().stream()
                .map(DetalleVenta::getId).toList();
        return new VentaResponse(value.getId(), id(value.getUsuario()), value.getFecha(), detalleIds, value.calcularTotal());
    }

    public static DetalleVentaResponse toResponse(DetalleVenta value) {
        return new DetalleVentaResponse(value.getId(), id(value.getVenta()), id(value.getProducto()), value.getCantidad(),
                decimal(value.getPrecio()));
    }

    public static UsuarioResponse toResponse(Usuario value) {
        Set<Long> rolIds = value.getRoles() == null ? Set.of() : value.getRoles().stream().map(Rol::getId)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        return new UsuarioResponse(value.getId(), value.getUsername(), rolIds);
    }

    private static Long id(Categoria value) { return value == null ? null : value.getId(); }
    private static Long id(Producto value) { return value == null ? null : value.getId(); }
    private static Long id(Usuario value) { return value == null ? null : value.getId(); }
    private static Long id(Venta value) { return value == null ? null : value.getId(); }
    private static BigDecimal decimal(Double value) { return value == null ? null : BigDecimal.valueOf(value); }
}
