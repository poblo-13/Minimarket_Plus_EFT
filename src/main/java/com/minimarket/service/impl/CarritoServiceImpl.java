package com.minimarket.service.impl;

import com.minimarket.entity.Carrito;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.api.dto.CarritoResponse;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.promocion.PromocionService;
import com.minimarket.service.CarritoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CarritoServiceImpl implements CarritoService {

    private final CarritoRepository carritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final PromocionService promocionService;

    @Override
    @Transactional(readOnly = true)
    public CarritoResponse obtener(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Usuario autenticado no encontrado"));
        List<CarritoResponse.Item> items = carritoRepository.findByUsuarioId(usuario.getId()).stream()
                .map(this::itemResponse).toList();
        BigDecimal total = items.stream().map(CarritoResponse.Item::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        return new CarritoResponse(items, total);
    }

    @Override
    @Transactional
    public void upsert(String username, Long productoId, Integer cantidad) {
        if (cantidad == null || cantidad < 1) throw new IllegalArgumentException("La cantidad debe ser positiva");
        Usuario usuario = usuarioRepository.findByUsernameForUpdate(username)
                .orElseThrow(() -> new NoSuchElementException("Usuario autenticado no encontrado"));
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new NoSuchElementException("Producto no encontrado"));
        Carrito item = carritoRepository.findByUsuarioIdAndProductoId(usuario.getId(), productoId)
                .orElseGet(() -> { Carrito nuevo = new Carrito(); nuevo.setUsuario(usuario); nuevo.setProducto(producto); return nuevo; });
        item.setCantidad(cantidad);
        carritoRepository.save(item);
    }

    @Override
    @Transactional
    public void eliminar(String username, Long productoId) {
        Usuario usuario = usuarioRepository.findByUsernameForUpdate(username)
                .orElseThrow(() -> new NoSuchElementException("Usuario autenticado no encontrado"));
        carritoRepository.deleteByUsuarioIdAndProductoId(usuario.getId(), productoId);
    }

    private CarritoResponse.Item itemResponse(Carrito item) {
        BigDecimal precio = promocionService.calcularPrecioEfectivo(item.getProducto().getId(), LocalDate.now());
        BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(item.getCantidad())).setScale(2, RoundingMode.HALF_UP);
        return new CarritoResponse.Item(item.getProducto().getId(), item.getProducto().getNombre(), item.getCantidad(), precio, subtotal);
    }
}
