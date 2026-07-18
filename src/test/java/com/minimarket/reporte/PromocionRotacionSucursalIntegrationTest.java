package com.minimarket.reporte;

import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.pedido.api.CheckoutRequest;
import com.minimarket.pedido.domain.EstadoPedido;
import com.minimarket.pedido.domain.Pedido;
import com.minimarket.pedido.domain.TipoEntrega;
import com.minimarket.pedido.service.PedidoService;
import com.minimarket.promocion.Promocion;
import com.minimarket.promocion.PromocionRepository;
import com.minimarket.reporte.api.RotacionProductoResponse;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.repository.DetalleVentaRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.sucursal.StockSucursal;
import com.minimarket.sucursal.StockSucursalRepository;
import com.minimarket.sucursal.Sucursal;
import com.minimarket.sucursal.SucursalRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PromocionRotacionSucursalIntegrationTest {
    @Autowired PedidoService pedidos;
    @Autowired RotacionService rotacion;
    @Autowired PromocionRepository promociones;
    @Autowired UsuarioRepository usuarios;
    @Autowired CategoriaRepository categorias;
    @Autowired ProductoRepository productos;
    @Autowired SucursalRepository sucursales;
    @Autowired StockSucursalRepository stocks;
    @Autowired DetalleVentaRepository detallesVenta;
    @Autowired CarritoRepository carritos;

    @Test
    void pedidoConPromocionVigenteSeVendeYSoloApareceEnReporteDeSuSucursal() {
        Usuario usuario = usuario();
        Producto producto = producto();
        Sucursal origen = sucursal("origen");
        Sucursal otra = sucursal("otra");
        stock(origen, producto);
        promocionVigente(producto);

        com.minimarket.entity.Carrito carrito = new com.minimarket.entity.Carrito();
        carrito.setUsuario(usuario); carrito.setProducto(producto); carrito.setCantidad(2);
        carritos.saveAndFlush(carrito);
        Pedido pedido = pedidos.checkout(usuario.getUsername(),
                new CheckoutRequest(TipoEntrega.RETIRO_TIENDA, origen.getId(), null));
        pedidos.cambiarEstado(pedido.getId(), EstadoPedido.CONFIRMADO);

        assertEquals(new BigDecimal("80.00"), pedido.getDetalles().getFirst().getPrecioUnitario());
        assertEquals(80D, detallesVenta.findAll().stream().filter(d -> d.getProducto().getId().equals(producto.getId())).findFirst().orElseThrow().getPrecio());
        List<RotacionProductoResponse> origenReporte = rotacion.consultar(LocalDate.now(), LocalDate.now(), origen.getId());
        List<RotacionProductoResponse> otroReporte = rotacion.consultar(LocalDate.now(), LocalDate.now(), otra.getId());
        assertTrue(origenReporte.stream().anyMatch(r -> r.productoId().equals(producto.getId()) && r.cantidadVendida() == 2L && r.posicionRotacion() == 1));
        assertFalse(otroReporte.stream().anyMatch(r -> r.productoId().equals(producto.getId())));
    }

    private Usuario usuario() { Usuario u = new Usuario(); u.setUsername("promo-report-" + System.nanoTime()); u.setPassword("password"); u.setRoles(Set.of()); return usuarios.save(u); }
    private Producto producto() { Categoria c = new Categoria(); c.setNombre("promo-report-cat-" + System.nanoTime()); c = categorias.save(c); Producto p = new Producto(); p.setNombre("promo-report-product-" + System.nanoTime()); p.setPrecio(100D); p.setStock(20); p.setCategoria(c); return productos.save(p); }
    private Sucursal sucursal(String prefix) { Sucursal s = new Sucursal(); s.setNombre(prefix + "-" + System.nanoTime()); return sucursales.save(s); }
    private void stock(Sucursal sucursal, Producto producto) { StockSucursal stock = new StockSucursal(); stock.setSucursal(sucursal); stock.setProducto(producto); stock.setDisponible(10); stock.setStockMinimo(0); stocks.saveAndFlush(stock); }
    private void promocionVigente(Producto producto) { Promocion p = new Promocion(); p.setProducto(producto); p.setPorcentajeDescuento(new BigDecimal("20")); p.setInicio(LocalDate.now().minusDays(1)); p.setFin(LocalDate.now().plusDays(1)); p.setActiva(true); promociones.save(p); }
}
