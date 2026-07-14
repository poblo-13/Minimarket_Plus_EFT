package com.minimarket.service;

import com.minimarket.api.dto.VentaRequest;
import com.minimarket.entity.Venta;

import java.util.List;

public interface VentaService {
    List<Venta> findAll();
    Venta findById(Long id);
    Venta registrar(VentaRequest request);
    Venta save(Venta venta);
    List<Venta> findByUsuarioId(Long usuarioId);
}
