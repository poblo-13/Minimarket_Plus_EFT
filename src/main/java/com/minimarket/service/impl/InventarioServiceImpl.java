package com.minimarket.service.impl;

import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Venta;
import com.minimarket.exception.InsufficientStockException;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.InventarioService;
import com.minimarket.sucursal.StockSucursalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {

    private final InventarioRepository inventarioRepository;
    private final ProductoRepository productoRepository;
    private final StockSucursalService stockSucursalService;

    @Override
    public List<Inventario> findAll() {
        return inventarioRepository.findAll();
    }

    @Override
    public Inventario findById(Long id) {
        return inventarioRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Inventario save(Inventario inventario) {
        if (inventario.getId() != null) {
            throw new UnsupportedOperationException("Los movimientos de inventario no se pueden modificar");
        }
        if (inventario.getProducto() == null || inventario.getProducto().getId() == null
                || inventario.getSucursal() == null || inventario.getSucursal().getId() == null) {
            throw new IllegalArgumentException("Sucursal y producto son obligatorios para registrar inventario operativo");
        }
        if ("Entrada".equals(inventario.getTipoMovimiento())) {
            stockSucursalService.aplicarEntrada(inventario.getSucursal().getId(), inventario.getProducto().getId(), inventario.getCantidad());
        } else if ("Salida".equals(inventario.getTipoMovimiento())) {
            stockSucursalService.aplicarSalidaAdministrada(inventario.getSucursal().getId(), inventario.getProducto().getId(), inventario.getCantidad());
        } else {
            throw new IllegalArgumentException("Tipo de movimiento inválido");
        }
        return inventarioRepository.save(inventario);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        throw new UnsupportedOperationException("Los movimientos de inventario no se pueden eliminar");
    }

    @Override
    public List<Inventario> findByProductoId(Long productoId) {
        return inventarioRepository.findByProductoId(productoId);
    }

    @Override
    @Transactional
    public Inventario registrarSalidaVenta(Producto producto, Integer cantidad, LocalDateTime fecha, Venta venta) {
        if (producto == null || producto.getId() == null) {
            throw new IllegalArgumentException("El producto bloqueado es obligatorio para registrar la salida de venta");
        }
        Inventario movement = new Inventario();
        movement.setProducto(producto);
        movement.setCantidad(cantidad);
        movement.setTipoMovimiento("Salida");
        movement.setFechaMovimiento(fecha);
        movement.setVenta(venta);
        if (venta == null || venta.getSucursal() == null) {
            throw new IllegalArgumentException("La venta con sucursal es obligatoria para registrar salida operativa");
        }
        movement.setSucursal(venta.getSucursal());
        stockSucursalService.aplicarSalidaAdministrada(venta.getSucursal().getId(), producto.getId(), cantidad);
        return inventarioRepository.save(movement);
    }

}
