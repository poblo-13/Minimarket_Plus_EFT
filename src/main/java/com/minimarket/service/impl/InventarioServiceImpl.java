package com.minimarket.service.impl;

import com.minimarket.entity.Inventario;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public Inventario save(Inventario inventario) {
        var producto = inventario.getProducto();
        if (producto == null) {
            throw new IllegalArgumentException("El producto es obligatorio para registrar inventario");
        }
        int stockActual = producto.getStock() == null ? 0 : producto.getStock();
        int cantidad = inventario.getCantidad() == null ? 0 : inventario.getCantidad();

        if ("Entrada".equalsIgnoreCase(inventario.getTipoMovimiento())) {
            producto.setStock(stockActual + cantidad);
        } else if ("Salida".equalsIgnoreCase(inventario.getTipoMovimiento())) {
            if (stockActual < cantidad) {
                throw new IllegalArgumentException("Stock insuficiente para registrar la salida");
            }
            producto.setStock(stockActual - cantidad);
        }
        productoRepository.save(producto);
        return inventarioRepository.save(inventario);
    }

    @Override
    public void deleteById(Long id) {
        inventarioRepository.deleteById(id);
    }

    @Override
    public List<Inventario> findByProductoId(Long productoId) {
        return inventarioRepository.findByProductoId(productoId);
    }
}
