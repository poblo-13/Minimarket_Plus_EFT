package com.minimarket.service;

import com.minimarket.api.dto.CarritoResponse;
import com.minimarket.entity.Carrito;

public interface CarritoService {
    /** Compatibilidad de compilación para clientes retirados; no existe implementación funcional. */
    @Deprecated(forRemoval = true) default Carrito save(Carrito carrito) { throw new UnsupportedOperationException("CRUD de carrito retirado"); }
    @Deprecated(forRemoval = true) default void deleteById(Long id) { throw new UnsupportedOperationException("CRUD de carrito retirado"); }
    CarritoResponse obtener(String username);
    void upsert(String username, Long productoId, Integer cantidad);
    void eliminar(String username, Long productoId);
}
