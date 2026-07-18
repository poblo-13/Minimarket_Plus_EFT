package com.minimarket.reporte;

import com.minimarket.entity.DetalleVenta;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RotacionRepository extends JpaRepository<DetalleVenta, Long> {
    @Query("select d.producto.id, d.producto.nombre, sum(d.cantidad), sum(d.precio * d.cantidad) " +
           "from DetalleVenta d where d.venta.fecha >= :desde and d.venta.fecha < :hasta group by d.producto.id, d.producto.nombre order by sum(d.cantidad) desc")
    List<Object[]> rotacionPorProducto(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
