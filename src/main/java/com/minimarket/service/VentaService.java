package com.minimarket.service;

import com.minimarket.api.dto.VentaRequest;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Venta;
import com.minimarket.pedido.domain.Pedido;

import java.util.List;
import java.util.Map;

public interface VentaService {
    List<Venta> findAll();
    Venta findById(Long id);
    Venta registrar(VentaRequest request);
    Venta registrarDesdePedido(Pedido pedido, Map<Long, Producto> productosBloqueados);
    Venta save(Venta venta);
    List<Venta> findByUsuarioId(Long usuarioId);
}
