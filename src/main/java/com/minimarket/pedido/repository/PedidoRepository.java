package com.minimarket.pedido.repository;

import com.minimarket.pedido.domain.Pedido;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByUsuarioId(Long usuarioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select distinct pedido from Pedido pedido left join fetch pedido.detalles where pedido.id = :pedidoId")
    java.util.Optional<Pedido> findByIdForUpdateWithDetalles(Long pedidoId);
}
