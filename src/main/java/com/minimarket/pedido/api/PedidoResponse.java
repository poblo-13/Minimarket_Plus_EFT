package com.minimarket.pedido.api;

import com.minimarket.pedido.domain.EstadoPedido;
import com.minimarket.pedido.domain.Pedido;
import com.minimarket.pedido.domain.TipoEntrega;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(Long id, EstadoPedido estado, TipoEntrega tipoEntrega, BigDecimal total,
                              Long sucursalId, String direccionEntrega, List<DetallePedidoResponse> detalles,
                              LocalDateTime creadoEn, LocalDateTime actualizadoEn, Long version) {
    public static PedidoResponse desde(Pedido pedido) {
        return new PedidoResponse(pedido.getId(), pedido.getEstado(), pedido.getTipoEntrega(), pedido.getTotal(),
                pedido.getSucursalId(), pedido.getDireccionEntrega(),
                pedido.getDetalles().stream().map(DetallePedidoResponse::desde).toList(),
                pedido.getCreadoEn(), pedido.getActualizadoEn(), pedido.getVersion());
    }
}
