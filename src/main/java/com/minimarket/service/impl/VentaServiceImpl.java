package com.minimarket.service.impl;

import com.minimarket.api.dto.LineaVentaRequest;
import com.minimarket.api.dto.VentaRequest;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.pedido.domain.DetallePedido;
import com.minimarket.pedido.domain.Pedido;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.InventarioService;
import com.minimarket.service.VentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final InventarioService inventarioService;

    @Override
    public List<Venta> findAll() {
        return ventaRepository.findAll();
    }

    @Override
    public Venta findById(Long id) {
        return ventaRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Venta registrar(VentaRequest request) {
        Usuario usuario = usuarioRepository.findById(request.usuarioId()).orElseThrow(NoSuchElementException::new);
        Map<Long, Integer> lineas = aggregate(request.lineas());
        LocalDateTime fecha = LocalDateTime.now();
        Map<Long, Producto> productosBloqueados = new TreeMap<>();
        // TreeMap order makes every competing sale take all product locks in ascending ID order.
        for (Long productoId : lineas.keySet()) {
            productosBloqueados.put(productoId, productoRepository.findByIdForUpdate(productoId)
                    .orElseThrow(NoSuchElementException::new));
        }

        Venta venta = new Venta();
        venta.setUsuario(usuario);
        venta.setFecha(fecha);
        List<DetalleVenta> detalles = new ArrayList<>();
        for (Long productoId : lineas.keySet()) {
            Producto producto = productosBloqueados.get(productoId);
            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setCantidad(lineas.get(productoId));
            detalle.setPrecio(producto.getPrecio());
            detalles.add(detalle);
        }
        venta.setDetalles(detalles);
        Venta saved = ventaRepository.save(venta);

        for (DetalleVenta detalle : saved.getDetalles()) {
            inventarioService.registrarSalidaVenta(detalle.getProducto(), detalle.getCantidad(), fecha, saved);
        }
        return saved;
    }

    @Override
    @Transactional
    public Venta registrarDesdePedido(Pedido pedido, Map<Long, Producto> productosBloqueados) {
        Venta venta = new Venta();
        venta.setUsuario(pedido.getUsuario());
        venta.setFecha(LocalDateTime.now());
        List<DetalleVenta> detalles = new ArrayList<>();
        for (DetallePedido detallePedido : pedido.getDetalles()) {
            Producto producto = productosBloqueados.get(detallePedido.getProducto().getId());
            if (producto == null) {
                throw new IllegalStateException("El producto del pedido no está bloqueado");
            }
            DetalleVenta detalleVenta = new DetalleVenta();
            detalleVenta.setVenta(venta);
            detalleVenta.setProducto(producto);
            detalleVenta.setCantidad(detallePedido.getCantidad());
            detalleVenta.setPrecio(detallePedido.getPrecioUnitario().doubleValue());
            detalles.add(detalleVenta);
        }
        venta.setDetalles(detalles);
        return ventaRepository.save(venta);
    }

    @Override
    public Venta save(Venta venta) {
        return ventaRepository.save(venta);
    }

    @Override
    public List<Venta> findByUsuarioId(Long usuarioId) {
        return ventaRepository.findByUsuarioId(usuarioId);
    }

    private Map<Long, Integer> aggregate(List<LineaVentaRequest> lineas) {
        Map<Long, Integer> aggregated = new TreeMap<>();
        for (LineaVentaRequest linea : lineas) {
            try {
                aggregated.merge(linea.productoId(), linea.cantidad(), Math::addExact);
            } catch (ArithmeticException exception) {
                throw new IllegalArgumentException("La cantidad solicitada es demasiado grande");
            }
        }
        return aggregated;
    }
}
