package com.minimarket.pedido.service.impl;

import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.pedido.api.CrearPedidoRequest;
import com.minimarket.pedido.api.LineaPedidoRequest;
import com.minimarket.pedido.domain.DetallePedido;
import com.minimarket.pedido.domain.EstadoPedido;
import com.minimarket.pedido.domain.Pedido;
import com.minimarket.pedido.domain.TipoEntrega;
import com.minimarket.pedido.repository.PedidoRepository;
import com.minimarket.pedido.service.PedidoService;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PedidoServiceImpl implements PedidoService {
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    public PedidoServiceImpl(PedidoRepository pedidoRepository, UsuarioRepository usuarioRepository,
                             ProductoRepository productoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional
    public Pedido crear(Long clienteId, CrearPedidoRequest request) {
        if (clienteId == null) {
            throw new IllegalArgumentException("El cliente autenticado es obligatorio");
        }
        validarRequest(request);
        Usuario cliente = usuarioRepository.findById(clienteId)
                .orElseThrow(() -> new NoSuchElementException("Cliente no encontrado"));
        return crearParaCliente(cliente, request);
    }

    @Override
    @Transactional
    public Pedido crear(String usernameCliente, CrearPedidoRequest request) {
        if (usernameCliente == null || usernameCliente.isBlank()) {
            throw new IllegalArgumentException("El username del cliente autenticado es obligatorio");
        }
        validarRequest(request);
        Usuario cliente = usuarioRepository.findByUsername(usernameCliente)
                .orElseThrow(() -> new NoSuchElementException("Cliente no encontrado"));
        return crearParaCliente(cliente, request);
    }

    @Override
    @Transactional
    public Pedido cancelar(Long pedidoId, Long clienteId) {
        Pedido pedido = obtenerPedido(pedidoId);
        if (!Objects.equals(pedido.getUsuario().getId(), clienteId)) {
            throw new NoSuchElementException("Pedido no encontrado");
        }
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden cancelar pedidos pendientes");
        }
        pedido.setEstado(EstadoPedido.CANCELADO);
        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public Pedido cancelar(Long pedidoId, String usernameCliente) {
        Pedido pedido = obtenerParaCliente(pedidoId, usernameCliente);
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden cancelar pedidos pendientes");
        }
        pedido.setEstado(EstadoPedido.CANCELADO);
        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public Pedido obtenerParaCliente(Long pedidoId, String usernameCliente) {
        Pedido pedido = obtenerPedido(pedidoId);
        if (!pedido.getUsuario().getUsername().equals(usernameCliente)) {
            // Deliberadamente indistinguible de un pedido inexistente.
            throw new NoSuchElementException("Pedido no encontrado");
        }
        inicializarDetalles(pedido);
        return pedido;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listarParaCliente(String usernameCliente) {
        Usuario cliente = usuarioRepository.findByUsername(usernameCliente)
                .orElseThrow(() -> new NoSuchElementException("Cliente no encontrado"));
        List<Pedido> pedidos = pedidoRepository.findByUsuarioId(cliente.getId());
        pedidos.forEach(this::inicializarDetalles);
        return pedidos;
    }

    @Override
    @Transactional
    public Pedido cambiarEstado(Long pedidoId, EstadoPedido nuevoEstado) {
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("El nuevo estado es obligatorio");
        }
        Pedido pedido = obtenerPedido(pedidoId);
        if (!esTransicionValida(pedido.getEstado(), nuevoEstado)) {
            throw new IllegalStateException("Transición de estado no válida");
        }
        pedido.setEstado(nuevoEstado);
        // La creación de Venta y el descuento de inventario se conectarán mediante PedidoVentaIntegration.
        return pedidoRepository.save(pedido);
    }

    private Pedido crearParaCliente(Usuario cliente, CrearPedidoRequest request) {
        Pedido pedido = new Pedido();
        pedido.setUsuario(cliente);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setTipoEntrega(request.tipoEntrega());
        pedido.setSucursalId(request.sucursalId());
        pedido.setDireccionEntrega(request.direccionEntrega());

        for (LineaPedidoRequest linea : request.detalles()) {
            Producto producto = productoRepository.findById(linea.productoId())
                    .orElseThrow(() -> new NoSuchElementException("Producto no encontrado: " + linea.productoId()));
            if (producto.getPrecio() == null || producto.getNombre() == null || producto.getNombre().isBlank()) {
                throw new IllegalStateException("El producto no tiene datos válidos para crear el pedido");
            }
            DetallePedido detalle = new DetallePedido();
            detalle.setProducto(producto);
            detalle.setNombreProducto(producto.getNombre());
            detalle.setPrecioUnitario(BigDecimal.valueOf(producto.getPrecio()).setScale(2, RoundingMode.HALF_UP));
            detalle.setCantidad(linea.cantidad());
            detalle.calcularSubtotal();
            pedido.agregarDetalle(detalle);
        }
        pedido.recalcularTotal();
        return pedidoRepository.save(pedido);
    }

    private void validarRequest(CrearPedidoRequest request) {
        if (request == null || request.tipoEntrega() == null || request.detalles() == null || request.detalles().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe incluir tipo de entrega y al menos un detalle");
        }
        if (request.tipoEntrega() == TipoEntrega.RETIRO_TIENDA && request.sucursalId() == null) {
            throw new IllegalArgumentException("El retiro en tienda requiere sucursalId");
        }
        if (request.tipoEntrega() == TipoEntrega.DESPACHO_DOMICILIO
                && (request.direccionEntrega() == null || request.direccionEntrega().isBlank())) {
            throw new IllegalArgumentException("El despacho a domicilio requiere dirección");
        }
        for (LineaPedidoRequest linea : request.detalles()) {
            if (linea == null || linea.productoId() == null || linea.cantidad() == null || linea.cantidad() < 1) {
                throw new IllegalArgumentException("Cada detalle requiere producto y cantidad positiva");
            }
        }
    }

    private Pedido obtenerPedido(Long pedidoId) {
        return pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new NoSuchElementException("Pedido no encontrado"));
    }

    private void inicializarDetalles(Pedido pedido) {
        pedido.getDetalles().size();
    }

    private boolean esTransicionValida(EstadoPedido actual, EstadoPedido nuevo) {
        return switch (actual) {
            case PENDIENTE -> nuevo == EstadoPedido.CONFIRMADO;
            case CONFIRMADO -> nuevo == EstadoPedido.EN_PREPARACION;
            case EN_PREPARACION -> nuevo == EstadoPedido.LISTO;
            case LISTO -> nuevo == EstadoPedido.ENTREGADO;
            case ENTREGADO, CANCELADO -> false;
        };
    }
}
