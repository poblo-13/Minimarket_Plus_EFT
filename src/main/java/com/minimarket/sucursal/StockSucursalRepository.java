package com.minimarket.sucursal;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StockSucursalRepository extends JpaRepository<StockSucursal, Long> {

    Optional<StockSucursal> findBySucursalIdAndProductoId(Long sucursalId, Long productoId);

    List<StockSucursal> findBySucursalId(Long sucursalId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select stock from StockSucursal stock where stock.sucursal.id = :sucursalId and stock.producto.id = :productoId")
    Optional<StockSucursal> findBySucursalIdAndProductoIdForUpdate(Long sucursalId, Long productoId);
}
