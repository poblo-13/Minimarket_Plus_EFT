package com.minimarket.pedido.service.impl;

import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.pedido.api.CheckoutRequest;
import com.minimarket.pedido.domain.DetallePedido;
import com.minimarket.pedido.domain.EstadoPedido;
import com.minimarket.pedido.domain.Pedido;
import com.minimarket.pedido.domain.TipoEntrega;
import com.minimarket.pedido.integration.PedidoVentaIntegration;
import com.minimarket.pedido.repository.PedidoRepository;
import com.minimarket.promocion.PromocionService;
import com.minimarket.pedido.service.PedidoService;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.sucursal.SucursalRepository;
import com.minimarket.sucursal.StockSucursalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
public class PedidoServiceImpl implements PedidoService {
    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PedidoVentaIntegration pedidoVentaIntegration;
    private final SucursalRepository sucursalRepository;
    private final PromocionService promocionService;
    private final CarritoRepository carritoRepository;
    private final StockSucursalRepository stockSucursalRepository;

    public PedidoServiceImpl(PedidoRepository pedidoRepository, UsuarioRepository usuarioRepository,
                             PedidoVentaIntegration pedidoVentaIntegration,
                             SucursalRepository sucursalRepository, PromocionService promocionService,
                             CarritoRepository carritoRepository, StockSucursalRepository stockSucursalRepository) {
        this.pedidoRepository = Objects.requireNonNull(pedidoRepository);
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository);
        this.pedidoVentaIntegration = Objects.requireNonNull(pedidoVentaIntegration);
        this.sucursalRepository = Objects.requireNonNull(sucursalRepository);
        this.promocionService = Objects.requireNonNull(promocionService);
        this.carritoRepository = Objects.requireNonNull(carritoRepository);
        this.stockSucursalRepository = Objects.requireNonNull(stockSucursalRepository);
    }

    @Override
    @Transactional
    public Pedido checkout(String usernameCliente, CheckoutRequest request) {
        validarCheckout(request);
        Usuario cliente = usuarioRepository.findByUsernameForUpdate(usernameCliente)
                .orElseThrow(() -> new NoSuchElementException("Cliente no encontrado"));
        List<com.minimarket.entity.Carrito> carrito = carritoRepository.findByUsuarioId(cliente.getId());
        if (carrito.isEmpty()) throw new IllegalStateException("El carrito está vacío");
        if (!sucursalRepository.existsById(request.sucursalId())) throw new NoSuchElementException("Sucursal no encontrada");
        for (com.minimarket.entity.Carrito item : carrito) {
            int disponible = stockSucursalRepository.findBySucursalIdAndProductoId(request.sucursalId(), item.getProducto().getId())
                    .orElseThrow(() -> new IllegalStateException("No existe stock para el producto " + item.getProducto().getId()))
                    .getDisponible();
            if (disponible < item.getCantidad()) throw new IllegalStateException("Stock insuficiente para el producto " + item.getProducto().getId());
        }
        Pedido pedido = new Pedido();
        pedido.setUsuario(cliente);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setTipoEntrega(request.tipoEntrega());
        pedido.setSucursalId(request.sucursalId());
        pedido.setDireccionEntrega(request.direccionEntrega());
        for (com.minimarket.entity.Carrito item : carrito) {
            Producto producto = item.getProducto();
            if (producto.getPrecio() == null || producto.getNombre() == null || producto.getNombre().isBlank()) {
                throw new IllegalStateException("El producto no tiene datos válidos para crear el pedido");
            }
            DetallePedido detalle = new DetallePedido();
            detalle.setProducto(producto);
            detalle.setNombreProducto(producto.getNombre());
            detalle.setPrecioUnitario(precioEfectivo(producto));
            detalle.setCantidad(item.getCantidad());
            detalle.calcularSubtotal();
            pedido.agregarDetalle(detalle);
        }
        pedido.recalcularTotal();
        Pedido persistido = pedidoRepository.save(pedido);
        carritoRepository.deleteAll(carrito);
        return persistido;
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
    @Transactional
    public Pedido cancelarOperativo(Long pedidoId) {
        Pedido pedido = obtenerPedido(pedidoId);
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
    @Transactional(readOnly = true)
    public List<Pedido> listarOperativos(EstadoPedido estado, Long sucursalId) {
        List<Pedido> pedidos;
        if (estado != null && sucursalId != null) {
            pedidos = pedidoRepository.findByEstadoAndSucursalIdOrderByCreadoEnAsc(estado, sucursalId);
        } else if (estado != null) {
            pedidos = pedidoRepository.findByEstadoOrderByCreadoEnAsc(estado);
        } else if (sucursalId != null) {
            pedidos = pedidoRepository.findBySucursalIdOrderByCreadoEnAsc(sucursalId);
        } else {
            pedidos = pedidoRepository.findByEstadoInOrderByCreadoEnAsc(List.of(
                    EstadoPedido.PENDIENTE, EstadoPedido.CONFIRMADO, EstadoPedido.EN_PREPARACION, EstadoPedido.LISTO));
        }
        pedidos.forEach(this::inicializarDetalles);
        return pedidos;
    }

    @Override
    @Transactional
    public Pedido cambiarEstado(Long pedidoId, EstadoPedido nuevoEstado) {
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("El nuevo estado es obligatorio");
        }
        if (nuevoEstado == EstadoPedido.CONFIRMADO) {
            return pedidoVentaIntegration.confirmarVenta(pedidoId);
        }
        Pedido pedido = obtenerPedido(pedidoId);
        if (!esTransicionValida(pedido.getEstado(), nuevoEstado)) {
            throw new IllegalStateException("Transición de estado no válida");
        }
        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }

    private void validarCheckout(CheckoutRequest request) {
        if (request == null || request.tipoEntrega() == null || request.sucursalId() == null) {
            throw new IllegalArgumentException("El checkout requiere tipo de entrega y sucursal");
        }
        String direccion = request.direccionEntrega();
        if (request.tipoEntrega() == TipoEntrega.RETIRO_TIENDA && direccion != null && !direccion.isBlank()) {
            throw new IllegalArgumentException("El retiro en tienda no admite dirección de entrega");
        }
        if (request.tipoEntrega() == TipoEntrega.DESPACHO_DOMICILIO
                && (direccion == null || direccion.trim().isEmpty() || direccion.trim().length() > 500)) {
            throw new IllegalArgumentException("El despacho a domicilio requiere una dirección de hasta 500 caracteres");
        }
    }

    private Pedido obtenerPedido(Long pedidoId) {
        return pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new NoSuchElementException("Pedido no encontrado"));
    }

    private void inicializarDetalles(Pedido pedido) {
        pedido.getDetalles().size();
    }

    private BigDecimal precioEfectivo(Producto producto) {
        return promocionService.calcularPrecioEfectivo(producto.getId(), java.time.LocalDate.now());
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
