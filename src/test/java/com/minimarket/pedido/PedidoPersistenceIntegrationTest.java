package com.minimarket.pedido;

import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.pedido.api.CrearPedidoRequest;
import com.minimarket.pedido.api.LineaPedidoRequest;
import com.minimarket.pedido.domain.EstadoPedido;
import com.minimarket.pedido.domain.Pedido;
import com.minimarket.pedido.domain.TipoEntrega;
import com.minimarket.pedido.repository.PedidoRepository;
import com.minimarket.pedido.service.PedidoService;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PedidoPersistenceIntegrationTest {
    @Autowired PedidoService pedidoService;
    @Autowired PedidoRepository pedidoRepository;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired ProductoRepository productoRepository;
    @Autowired CategoriaRepository categoriaRepository;
    @Autowired EntityManager entityManager;

    @Test
    @Transactional
    void persisteDetallesEnCascadeSnapshotsTimestampsYVersion() {
        Usuario usuario = usuario();
        Producto producto = producto();

        Pedido creado = pedidoService.crear(usuario.getId(), new CrearPedidoRequest(TipoEntrega.DESPACHO_DOMICILIO,
                null, "Calle Uno 123", List.of(new LineaPedidoRequest(producto.getId(), 2))));
        Pedido persistido = pedidoRepository.findById(creado.getId()).orElseThrow();

        assertEquals(new BigDecimal("25.00"), persistido.getTotal());
        assertEquals(1, persistido.getDetalles().size());
        assertEquals("Leche", persistido.getDetalles().getFirst().getNombreProducto());
        assertEquals(new BigDecimal("12.50"), persistido.getDetalles().getFirst().getPrecioUnitario());
        assertNotNull(persistido.getCreadoEn());
        assertNotNull(persistido.getActualizadoEn());
        assertEquals(0L, persistido.getVersion());

        Pedido confirmado = pedidoService.cancelar(creado.getId(), usuario.getId());
        entityManager.flush();
        assertEquals(1L, confirmado.getVersion());
        assertEquals(EstadoPedido.CANCELADO, confirmado.getEstado());
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setUsername("pedido-user-" + System.nanoTime());
        usuario.setPassword("password");
        usuario.setRoles(Set.of());
        return usuarioRepository.save(usuario);
    }

    private Producto producto() {
        Categoria categoria = new Categoria();
        categoria.setNombre("pedido-category-" + System.nanoTime());
        categoria = categoriaRepository.save(categoria);
        Producto producto = new Producto();
        producto.setNombre("Leche");
        producto.setPrecio(12.50D);
        producto.setStock(5);
        producto.setCategoria(categoria);
        return productoRepository.save(producto);
    }
}
