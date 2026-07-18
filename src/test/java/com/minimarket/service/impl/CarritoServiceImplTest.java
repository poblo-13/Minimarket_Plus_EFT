package com.minimarket.service.impl;

import com.minimarket.entity.Carrito;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.promocion.PromocionService;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarritoServiceImplTest {
    @Mock CarritoRepository carritoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock ProductoRepository productoRepository;
    @Mock PromocionService promocionService;
    @InjectMocks CarritoServiceImpl carritoService;

    @Test
    void upsertUsaUsuarioAutenticadoYCantidadAbsoluta() {
        Usuario usuario = new Usuario(); usuario.setId(5L);
        Producto producto = new Producto(); producto.setId(9L);
        Carrito existente = new Carrito(); existente.setUsuario(usuario); existente.setProducto(producto); existente.setCantidad(1);
        when(usuarioRepository.findByUsernameForUpdate("cliente")).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(9L)).thenReturn(Optional.of(producto));
        when(carritoRepository.findByUsuarioIdAndProductoId(5L, 9L)).thenReturn(Optional.of(existente));

        carritoService.upsert("cliente", 9L, 4);

        assertEquals(4, existente.getCantidad());
        verify(carritoRepository).save(existente);
    }

    @Test
    void eliminarEsIdempotenteYSeLimitaAlUsuarioAutenticado() {
        Usuario usuario = new Usuario(); usuario.setId(5L);
        when(usuarioRepository.findByUsernameForUpdate("cliente")).thenReturn(Optional.of(usuario));

        carritoService.eliminar("cliente", 9L);

        verify(carritoRepository).deleteByUsuarioIdAndProductoId(5L, 9L);
    }

    @Test
    void obtenerCalculaPrecioPromocionalSubtotalYTotalEstimado() {
        Usuario usuario = usuario(5L);
        Producto pan = producto(9L, "Pan");
        Producto leche = producto(10L, "Leche");
        Carrito primerItem = carrito(usuario, pan, 2);
        Carrito segundoItem = carrito(usuario, leche, 3);
        when(usuarioRepository.findByUsername("cliente")).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioId(5L)).thenReturn(List.of(primerItem, segundoItem));
        when(promocionService.calcularPrecioEfectivo(9L, LocalDate.now())).thenReturn(new BigDecimal("1.25"));
        when(promocionService.calcularPrecioEfectivo(10L, LocalDate.now())).thenReturn(new BigDecimal("2.10"));

        var respuesta = carritoService.obtener("cliente");

        assertEquals(new BigDecimal("8.80"), respuesta.total());
        assertEquals(new BigDecimal("2.50"), respuesta.items().getFirst().subtotal());
        assertEquals(new BigDecimal("6.30"), respuesta.items().getLast().subtotal());
        assertEquals(9L, respuesta.items().getFirst().productoId());
    }

    @Test
    void obtenerCarritoVacioDevuelveTotalCero() {
        Usuario usuario = usuario(5L);
        when(usuarioRepository.findByUsername("cliente")).thenReturn(Optional.of(usuario));
        when(carritoRepository.findByUsuarioId(5L)).thenReturn(List.of());

        var respuesta = carritoService.obtener("cliente");

        assertEquals(List.of(), respuesta.items());
        assertEquals(new BigDecimal("0.00"), respuesta.total());
    }

    @Test
    void obtenerYMutarRechazanUsuarioOProductoInexistente() {
        when(usuarioRepository.findByUsername("desconocido")).thenReturn(Optional.empty());
        when(usuarioRepository.findByUsernameForUpdate("cliente")).thenReturn(Optional.of(usuario(5L)));
        when(productoRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> carritoService.obtener("desconocido"));
        assertThrows(NoSuchElementException.class, () -> carritoService.upsert("cliente", 9L, 1));
    }

    @Test
    void upsertRechazaCantidadInvalidaYUsuarioNoAutenticado() {
        assertThrows(IllegalArgumentException.class, () -> carritoService.upsert("cliente", 9L, 0));
        when(usuarioRepository.findByUsernameForUpdate("desconocido")).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> carritoService.upsert("desconocido", 9L, 1));
    }

    @Test
    void eliminarRechazaUsuarioNoAutenticado() {
        when(usuarioRepository.findByUsernameForUpdate("desconocido")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> carritoService.eliminar("desconocido", 9L));
        verify(carritoRepository, never()).deleteByUsuarioIdAndProductoId(anyLong(), anyLong());
    }

    private Usuario usuario(Long id) {
        Usuario usuario = new Usuario(); usuario.setId(id); return usuario;
    }

    private Producto producto(Long id, String nombre) {
        Producto producto = new Producto(); producto.setId(id); producto.setNombre(nombre); return producto;
    }

    private Carrito carrito(Usuario usuario, Producto producto, int cantidad) {
        Carrito carrito = new Carrito();
        carrito.setUsuario(usuario); carrito.setProducto(producto); carrito.setCantidad(cantidad);
        return carrito;
    }
}
