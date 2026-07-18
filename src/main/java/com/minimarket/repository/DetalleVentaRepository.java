package com.minimarket.repository;

import com.minimarket.entity.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {
    List<DetalleVenta> findByVentaId(Long ventaId);
    List<DetalleVenta> findByVentaUsuarioId(Long usuarioId);
    Optional<DetalleVenta> findByIdAndVentaUsuarioId(Long id, Long usuarioId);
}
