package com.minimarket.repository;

import com.minimarket.entity.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarritoRepository extends JpaRepository<Carrito, Long> {
    List<Carrito> findByUsuarioId(Long usuarioId);
    Optional<Carrito> findByUsuarioIdAndProductoId(Long usuarioId, Long productoId);
    /** @deprecated El API usa productoId, no el id interno del ítem. */
    @Deprecated Optional<Carrito> findByIdAndUsuarioId(Long id, Long usuarioId);
    void deleteByUsuarioIdAndProductoId(Long usuarioId, Long productoId);
}
