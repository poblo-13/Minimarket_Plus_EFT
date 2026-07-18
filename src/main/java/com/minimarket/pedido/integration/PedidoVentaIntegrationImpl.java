package com.minimarket.pedido.integration;

import com.minimarket.entity.Producto;
import com.minimarket.pedido.domain.DetallePedido;
import com.minimarket.pedido.domain.EstadoPedido;
import com.minimarket.pedido.domain.Pedido;
import com.minimarket.pedido.repository.PedidoRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.VentaService;
import com.minimarket.sucursal.StockSucursalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

/** Atomic internal confirmation for Pedido, Venta and StockSucursal. */
@Service
@RequiredArgsConstructor
public class PedidoVentaIntegrationImpl implements PedidoVentaIntegration {
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final StockSucursalService stockSucursalService;
    private final VentaService ventaService;

    @Override
    @Transactional
    public Pedido confirmarVenta(Long pedidoId) {
        Pedido pedido;
        try {
            pedido = pedidoRepository.findByIdForUpdateWithDetalles(pedidoId)
                    .orElseThrow(() -> new NoSuchElementException("Pedido no encontrado"));
        } catch (OptimisticLockingFailureException exception) {
            // H2 may report a racing pessimistic-lock acquisition as an optimistic conflict.
            throw new IllegalStateException("El pedido ya fue confirmado por otra transacción", exception);
        }
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden confirmar pedidos pendientes");
        }
        if (pedido.getSucursalId() == null) {
            throw new IllegalStateException("La confirmación requiere una sucursal para descontar stock");
        }

        Map<Long, Integer> cantidades = new TreeMap<>();
        for (DetallePedido detalle : pedido.getDetalles()) {
            cantidades.merge(detalle.getProducto().getId(), detalle.getCantidad(), Math::addExact);
        }
        Map<Long, Producto> productosBloqueados = new TreeMap<>();
        for (Long productoId : cantidades.keySet()) {
            productosBloqueados.put(productoId, productoRepository.findByIdForUpdate(productoId)
                    .orElseThrow(() -> new NoSuchElementException("Producto no encontrado: " + productoId)));
        }
        var venta = ventaService.registrarDesdePedido(pedido, productosBloqueados);
        for (Map.Entry<Long, Integer> linea : cantidades.entrySet()) {
            stockSucursalService.descontarParaVenta(pedido.getSucursalId(), linea.getKey(), linea.getValue(), venta, venta.getFecha());
        }
        pedido.setVenta(venta);
        pedido.setEstado(EstadoPedido.CONFIRMADO);
        return pedido;
    }
}
