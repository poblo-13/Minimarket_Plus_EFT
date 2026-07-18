package com.minimarket.pedido;

import com.minimarket.abastecimiento.EstadoOrdenCompra;
import com.minimarket.abastecimiento.OrdenCompra;
import com.minimarket.abastecimiento.OrdenCompraRepository;
import com.minimarket.abastecimiento.Proveedor;
import com.minimarket.entity.Categoria;
import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.pedido.api.CrearPedidoRequest;
import com.minimarket.pedido.api.LineaPedidoRequest;
import com.minimarket.pedido.domain.EstadoPedido;
import com.minimarket.pedido.domain.Pedido;
import com.minimarket.pedido.domain.TipoEntrega;
import com.minimarket.pedido.repository.PedidoRepository;
import com.minimarket.pedido.service.PedidoService;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.sucursal.AdministracionStockService;
import com.minimarket.sucursal.StockSucursal;
import com.minimarket.sucursal.StockSucursalRepository;
import com.minimarket.sucursal.Sucursal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Verifies the complete, persisted order-confirmation replenishment chain using H2/JPA repositories. */
@SpringBootTest
class PedidoReposicionAutomaticaE2EIntegrationTest {

    @Autowired private PedidoService pedidoService;
    @Autowired private AdministracionStockService administracionStockService;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private VentaRepository ventaRepository;
    @Autowired private InventarioRepository inventarioRepository;
    @Autowired private OrdenCompraRepository ordenCompraRepository;
    @Autowired private StockSucursalRepository stockSucursalRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private ProductoRepository productoRepository;

    @Test
    void confirmarPedidoBajoMinimoCreaVentaMovimientoYUnaUnicaOrdenAbiertaSinDuplicarEnReintento() {
        String unique = String.valueOf(System.nanoTime());
        Usuario cliente = crearCliente(unique);
        Producto producto = crearProducto(unique);
        Sucursal sucursal = administracionStockService.crearSucursal("sucursal-e2e-" + unique);
        Proveedor proveedor = administracionStockService.crearProveedor("proveedor-e2e-" + unique);
        StockSucursal stockConfigurado = administracionStockService.configurarStock(
                sucursal.getId(), producto.getId(), 3, 2, proveedor.getId());

        Pedido pedido = pedidoService.crear(cliente.getId(), new CrearPedidoRequest(
                TipoEntrega.RETIRO_TIENDA, sucursal.getId(), null,
                List.of(new LineaPedidoRequest(producto.getId(), 1))));
        long ventasAntes = ventaRepository.count();

        pedidoService.cambiarEstado(pedido.getId(), EstadoPedido.CONFIRMADO);

        Pedido confirmado = pedidoRepository.findById(pedido.getId()).orElseThrow();
        assertEquals(EstadoPedido.CONFIRMADO, confirmado.getEstado());
        assertNotNull(confirmado.getVenta());

        Venta venta = ventaRepository.findById(confirmado.getVenta().getId()).orElseThrow();
        assertEquals(ventasAntes + 1, ventaRepository.count());
        assertEquals(sucursal.getId(), venta.getSucursal().getId());

        List<Inventario> movimientosDeVenta = inventarioRepository.findByProductoId(producto.getId()).stream()
                .filter(movimiento -> venta.getId().equals(movimiento.getVenta().getId()))
                .toList();
        assertEquals(1, movimientosDeVenta.size());
        Inventario movimiento = movimientosDeVenta.getFirst();
        assertEquals("Salida", movimiento.getTipoMovimiento());
        assertEquals(1, movimiento.getCantidad());
        assertEquals(sucursal.getId(), movimiento.getSucursal().getId());

        StockSucursal stockTrasConfirmacion = stockSucursalRepository.findById(stockConfigurado.getId()).orElseThrow();
        assertEquals(2, stockTrasConfirmacion.getDisponible());

        List<OrdenCompra> ordenesAbiertas = ordenCompraRepository.findAll().stream()
                .filter(orden -> orden.getEstado() == EstadoOrdenCompra.ABIERTA)
                .filter(orden -> sucursal.getId().equals(orden.getSucursal().getId()))
                .filter(orden -> producto.getId().equals(orden.getProducto().getId()))
                .filter(orden -> proveedor.getId().equals(orden.getProveedor().getId()))
                .toList();
        assertEquals(1, ordenesAbiertas.size());

        assertThrows(IllegalStateException.class,
                () -> pedidoService.cambiarEstado(pedido.getId(), EstadoPedido.CONFIRMADO));

        assertEquals(ventasAntes + 1, ventaRepository.count());
        assertEquals(1, inventarioRepository.findByProductoId(producto.getId()).stream()
                .filter(candidate -> candidate.getVenta() != null && venta.getId().equals(candidate.getVenta().getId())).count());
        assertEquals(2, stockSucursalRepository.findById(stockConfigurado.getId()).orElseThrow().getDisponible());
        assertEquals(1, ordenCompraRepository.findAll().stream()
                .filter(candidate -> candidate.getEstado() == EstadoOrdenCompra.ABIERTA)
                .filter(candidate -> sucursal.getId().equals(candidate.getSucursal().getId()))
                .filter(candidate -> producto.getId().equals(candidate.getProducto().getId()))
                .filter(candidate -> proveedor.getId().equals(candidate.getProveedor().getId())).count());
    }

    private Usuario crearCliente(String unique) {
        Usuario cliente = new Usuario();
        cliente.setUsername("cliente-e2e-" + unique);
        cliente.setPassword("password");
        cliente.setRoles(Set.of());
        return usuarioRepository.save(cliente);
    }

    private Producto crearProducto(String unique) {
        Categoria categoria = new Categoria();
        categoria.setNombre("categoria-e2e-" + unique);
        categoria = categoriaRepository.save(categoria);

        Producto producto = new Producto();
        producto.setNombre("producto-e2e-" + unique);
        producto.setPrecio(100D);
        producto.setStock(99);
        producto.setCategoria(categoria);
        return productoRepository.save(producto);
    }
}
