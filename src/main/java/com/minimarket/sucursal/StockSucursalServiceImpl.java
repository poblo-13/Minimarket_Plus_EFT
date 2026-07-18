package com.minimarket.sucursal;

import com.minimarket.abastecimiento.OrdenCompra;
import com.minimarket.abastecimiento.OrdenCompraRepository;
import com.minimarket.abastecimiento.EstadoOrdenCompra;
import com.minimarket.abastecimiento.Proveedor;
import com.minimarket.abastecimiento.ProveedorRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.entity.Inventario;
import com.minimarket.entity.Venta;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** Servicio transaccional del stock por sucursal; nunca actualiza el stock global legado de Producto. */
@Service
@RequiredArgsConstructor
public class StockSucursalServiceImpl implements StockSucursalService {

    private final StockSucursalRepository stockSucursalRepository;
    private final ProveedorRepository proveedorRepository;
    private final OrdenCompraRepository ordenCompraRepository;
    private final SucursalRepository sucursalRepository;
    private final ProductoRepository productoRepository;
    private final InventarioRepository inventarioRepository;

    @Override
    @Transactional(readOnly = true)
    public StockSucursal consultarStock(Long sucursalId, Long productoId) {
        verificarSucursalYProducto(sucursalId, productoId);
        return buscarStock(sucursalId, productoId);
    }

    @Override
    @Transactional(readOnly = true)
    public int consultarDisponibilidad(Long sucursalId, Long productoId) {
        return consultarStock(sucursalId, productoId).getDisponible();
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

    @Override
    @Transactional
    public StockSucursal aplicarSalidaAdministrada(Long sucursalId, Long productoId, int cantidad) {
        return descontarParaVenta(sucursalId, productoId, cantidad, null, LocalDateTime.now());
    }

    @Override
    @Transactional
    public StockSucursal descontarParaConfirmacionPedido(Long sucursalId, Long productoId, int cantidad) {
        StockSucursal stock = buscarStockBloqueado(sucursalId, productoId);
        stock.disminuir(cantidad);
        return stock;
    }

    @Override
    @Transactional
    public StockSucursal descontarParaVenta(Long sucursalId, Long productoId, int cantidad, Venta venta, LocalDateTime fecha) {
        StockSucursal stock = buscarStockBloqueado(sucursalId, productoId);
        stock.disminuir(cantidad);
        Inventario movimiento = new Inventario();
        movimiento.setProducto(stock.getProducto());
        movimiento.setSucursal(stock.getSucursal());
        movimiento.setVenta(venta);
        movimiento.setCantidad(cantidad);
        movimiento.setTipoMovimiento("Salida");
        movimiento.setFechaMovimiento(fecha);
        inventarioRepository.save(movimiento);
        crearReposicionSiCorresponde(stock);
        return stock;
    }

    private void crearReposicionSiCorresponde(StockSucursal stock) {
        if (stock.getDisponible() > stock.getStockMinimo()) return;
        Proveedor proveedor = stock.getProducto().getProveedorReposicion();
        if (proveedor == null) {
            throw new IllegalStateException("El producto no tiene proveedor de reposición configurado");
        }
        if (!ordenCompraRepository.existsBySucursalIdAndProductoIdAndProveedorIdAndEstado(
                stock.getSucursal().getId(), stock.getProducto().getId(), proveedor.getId(), EstadoOrdenCompra.ABIERTA)) {
            OrdenCompra orden = new OrdenCompra();
            orden.setSucursal(stock.getSucursal());
            orden.setProducto(stock.getProducto());
            orden.setProveedor(proveedor);
            orden.setCantidadSolicitada(Math.max(1, stock.getStockMinimo() - stock.getDisponible() + 1));
            ordenCompraRepository.save(orden);
        }
    }

    private StockSucursal buscarStock(Long sucursalId, Long productoId) {
        return stockSucursalRepository.findBySucursalIdAndProductoId(sucursalId, productoId)
                .orElseThrow(() -> new EntityNotFoundException("Stock de sucursal no encontrado"));
    }

    private StockSucursal buscarStockBloqueado(Long sucursalId, Long productoId) {
        return stockSucursalRepository.findBySucursalIdAndProductoIdForUpdate(sucursalId, productoId)
                .orElseThrow(() -> new EntityNotFoundException("Stock de sucursal no encontrado"));
    }

    private void verificarSucursalYProducto(Long sucursalId, Long productoId) {
        if (!sucursalRepository.existsById(sucursalId)) {
            throw new EntityNotFoundException("Sucursal no encontrada: " + sucursalId);
        }
        if (!productoRepository.existsById(productoId)) {
            throw new EntityNotFoundException("Producto no encontrado: " + productoId);
        }
    }
}
