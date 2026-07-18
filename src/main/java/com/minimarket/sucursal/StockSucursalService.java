package com.minimarket.sucursal;

import com.minimarket.entity.Venta;
import java.time.LocalDateTime;

public interface StockSucursalService {
    StockSucursal consultarStock(Long sucursalId, Long productoId);
    int consultarDisponibilidad(Long sucursalId, Long productoId);
    StockSucursal aplicarSalida(Long sucursalId, Long productoId, Long proveedorId, int cantidad);
    StockSucursal aplicarEntrada(Long sucursalId, Long productoId, int cantidad);
    StockSucursal aplicarSalidaAdministrada(Long sucursalId, Long productoId, int cantidad);
    StockSucursal descontarParaConfirmacionPedido(Long sucursalId, Long productoId, int cantidad);
    StockSucursal descontarParaVenta(Long sucursalId, Long productoId, int cantidad, Venta venta, LocalDateTime fecha);
}
