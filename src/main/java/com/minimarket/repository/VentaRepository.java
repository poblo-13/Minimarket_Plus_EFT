package com.minimarket.repository;

import com.minimarket.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VentaRepository extends JpaRepository<Venta, Long> {
    List<Venta> findByUsuarioId(Long usuarioId);
    Optional<Venta> findByIdAndUsuarioId(Long id, Long usuarioId);
}
