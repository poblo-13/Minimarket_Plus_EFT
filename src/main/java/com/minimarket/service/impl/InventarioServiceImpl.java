package com.minimarket.service.impl;

import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Venta;
import com.minimarket.exception.InsufficientStockException;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.InventarioService;
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
        Producto producto = lockProduct(inventario);
        applyMovement(producto, inventario);
        inventario.setProducto(producto);
        productoRepository.save(producto);
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
        applyMovement(producto, movement);
        productoRepository.save(producto);
        return inventarioRepository.save(movement);
    }

    private Producto lockProduct(Inventario inventario) {
        if (inventario.getProducto() == null || inventario.getProducto().getId() == null) {
            throw new IllegalArgumentException("El producto es obligatorio para registrar inventario");
        }
        return productoRepository.findByIdForUpdate(inventario.getProducto().getId())
                .orElseThrow(() -> new java.util.NoSuchElementException("Producto no encontrado"));
    }

    private void applyMovement(Producto producto, Inventario movement) {
        int stockActual = producto.getStock() == null ? 0 : producto.getStock();
        int cantidad = movement.getCantidad() == null ? 0 : movement.getCantidad();
        int delta = 0;
        if ("Entrada".equalsIgnoreCase(movement.getTipoMovimiento())) {
            delta = cantidad;
        } else if ("Salida".equalsIgnoreCase(movement.getTipoMovimiento())) {
            delta = -cantidad;
        }
        if (delta < 0 && stockActual < -delta) {
            throw new InsufficientStockException();
        }
        producto.setStock(stockActual + delta);
    }
}
