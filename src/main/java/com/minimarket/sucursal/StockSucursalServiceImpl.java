package com.minimarket.sucursal;

import com.minimarket.abastecimiento.OrdenCompra;
import com.minimarket.abastecimiento.OrdenCompraRepository;
import com.minimarket.abastecimiento.EstadoOrdenCompra;
import com.minimarket.abastecimiento.Proveedor;
import com.minimarket.abastecimiento.ProveedorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Servicio transaccional del stock por sucursal; nunca actualiza el stock global legado de Producto. */
@Service
@RequiredArgsConstructor
public class StockSucursalServiceImpl implements StockSucursalService {

    private final StockSucursalRepository stockSucursalRepository;
    private final ProveedorRepository proveedorRepository;
    private final OrdenCompraRepository ordenCompraRepository;

    @Override
    @Transactional(readOnly = true)
    public int consultarDisponibilidad(Long sucursalId, Long productoId) {
        return buscarStock(sucursalId, productoId).getDisponible();
    }

    @Override
    @Transactional
    public StockSucursal aplicarSalida(Long sucursalId, Long productoId, Long proveedorId, int cantidad) {
        StockSucursal stock = buscarStockBloqueado(sucursalId, productoId);
        stock.disminuir(cantidad);
        if (stock.getDisponible() <= stock.getStockMinimo()) {
            Proveedor proveedor = proveedorRepository.findById(proveedorId)
                    .orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado: " + proveedorId));
            if (!ordenCompraRepository.existsBySucursalIdAndProductoIdAndProveedorIdAndEstado(
                    sucursalId, productoId, proveedorId, EstadoOrdenCompra.ABIERTA)) {
                OrdenCompra orden = new OrdenCompra();
                orden.setSucursal(stock.getSucursal());
                orden.setProducto(stock.getProducto());
                orden.setProveedor(proveedor);
                orden.setCantidadSolicitada(Math.max(1, stock.getStockMinimo() - stock.getDisponible() + 1));
                ordenCompraRepository.save(orden);
            }
        }
        return stock;
    }

    @Override
    @Transactional
    public StockSucursal aplicarEntrada(Long sucursalId, Long productoId, int cantidad) {
        StockSucursal stock = buscarStockBloqueado(sucursalId, productoId);
        stock.aumentar(cantidad);
        return stock;
    }

    private StockSucursal buscarStock(Long sucursalId, Long productoId) {
        return stockSucursalRepository.findBySucursalIdAndProductoId(sucursalId, productoId)
                .orElseThrow(() -> new EntityNotFoundException("Stock de sucursal no encontrado"));
    }

    private StockSucursal buscarStockBloqueado(Long sucursalId, Long productoId) {
        return stockSucursalRepository.findBySucursalIdAndProductoIdForUpdate(sucursalId, productoId)
                .orElseThrow(() -> new EntityNotFoundException("Stock de sucursal no encontrado"));
    }
}
