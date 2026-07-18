package com.minimarket.sucursal;

import com.minimarket.abastecimiento.Proveedor;
import com.minimarket.abastecimiento.ProveedorRepository;
import com.minimarket.entity.Producto;
import com.minimarket.repository.ProductoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdministracionStockService {
    private final SucursalRepository sucursales;
    private final StockSucursalRepository stocks;
    private final ProductoRepository productos;
    private final ProveedorRepository proveedores;
    private final StockSucursalService stockService;

    @Transactional public Sucursal crearSucursal(String nombre) {
        Sucursal sucursal = new Sucursal(); sucursal.setNombre(nombre); return sucursales.save(sucursal);
    }
    @Transactional public Proveedor crearProveedor(String nombre) {
        Proveedor proveedor = new Proveedor(); proveedor.setNombre(nombre); return proveedores.save(proveedor);
    }
    @Transactional public Producto configurarProveedor(Long productoId, Long proveedorId) {
        Producto producto = producto(productoId); producto.setProveedorReposicion(proveedor(proveedorId)); return producto;
    }
    @Transactional public StockSucursal configurarStock(Long sucursalId, Long productoId, int disponible, int minimo, Long proveedorId) {
        Sucursal sucursal = sucursales.findById(sucursalId).orElseThrow(() -> new EntityNotFoundException("Sucursal no encontrada"));
        Producto producto = producto(productoId); producto.setProveedorReposicion(proveedor(proveedorId));
        StockSucursal stock = stocks.findBySucursalIdAndProductoId(sucursalId, productoId).orElseGet(StockSucursal::new);
        stock.setSucursal(sucursal); stock.setProducto(producto); stock.setDisponible(disponible); stock.setStockMinimo(minimo);
        return stocks.save(stock);
    }
    @Transactional public StockSucursal entrada(Long sucursalId, Long productoId, int cantidad) { return stockService.aplicarEntrada(sucursalId, productoId, cantidad); }
    @Transactional public StockSucursal salida(Long sucursalId, Long productoId, int cantidad) { return stockService.aplicarSalidaAdministrada(sucursalId, productoId, cantidad); }
    private Producto producto(Long id) { return productos.findById(id).orElseThrow(() -> new EntityNotFoundException("Producto no encontrado")); }
    private Proveedor proveedor(Long id) { return proveedores.findById(id).orElseThrow(() -> new EntityNotFoundException("Proveedor no encontrado")); }
}
