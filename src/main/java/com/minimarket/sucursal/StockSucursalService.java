package com.minimarket.sucursal;

public interface StockSucursalService {
    StockSucursal consultarStock(Long sucursalId, Long productoId);
    int consultarDisponibilidad(Long sucursalId, Long productoId);
    StockSucursal aplicarSalida(Long sucursalId, Long productoId, Long proveedorId, int cantidad);
    StockSucursal aplicarEntrada(Long sucursalId, Long productoId, int cantidad);
    StockSucursal descontarParaConfirmacionPedido(Long sucursalId, Long productoId, int cantidad);
}
