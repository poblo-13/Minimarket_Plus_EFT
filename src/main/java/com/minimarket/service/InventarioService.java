package com.minimarket.service;

import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Venta;

import java.time.LocalDateTime;

import java.util.List;

public interface InventarioService {
    List<Inventario> findAll();
    Inventario findById(Long id);
    Inventario save(Inventario inventario);
    void deleteById(Long id);
    List<Inventario> findByProductoId(Long productoId);
    Inventario registrarSalidaVenta(Producto producto, Integer cantidad, LocalDateTime fecha, Venta venta);
}
