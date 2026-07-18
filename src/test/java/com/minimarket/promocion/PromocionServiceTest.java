package com.minimarket.promocion;

import com.minimarket.entity.Producto;
import com.minimarket.promocion.api.PromocionRequest;
import com.minimarket.repository.ProductoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromocionServiceTest {
    @Mock PromocionRepository promociones; @Mock ProductoRepository productos; @InjectMocks PromocionService service;
    @Test void calculaPrecioEfectivoConElMayorDescuentoVigente() {
        Producto producto = new Producto(); producto.setPrecio(1000D);
        Promocion promocion = new Promocion(); promocion.setPorcentajeDescuento(new BigDecimal("20"));
        LocalDate hoy = LocalDate.now();
        when(productos.findById(1L)).thenReturn(Optional.of(producto));
        when(promociones.findByProductoIdAndActivaTrueAndInicioLessThanEqualAndFinGreaterThanEqual(1L, hoy, hoy)).thenReturn(List.of(promocion));
        assertEquals(new BigDecimal("800.00"), service.calcularPrecioEfectivo(1L, hoy));
    }
    @Test void rechazaDosTiposDeDescuento() {
        PromocionRequest request = new PromocionRequest(1L, BigDecimal.TEN, BigDecimal.ONE, LocalDate.now(), LocalDate.now(), true);
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.crear(request));
    }
    @Test void noDescuentaPromocionVencida() {
        Producto producto = new Producto(); producto.setPrecio(1000D);
        LocalDate hoy = LocalDate.now();
        when(productos.findById(1L)).thenReturn(Optional.of(producto));
        when(promociones.findByProductoIdAndActivaTrueAndInicioLessThanEqualAndFinGreaterThanEqual(1L, hoy, hoy)).thenReturn(List.of());
        assertEquals(new BigDecimal("1000.00"), service.calcularPrecioEfectivo(1L, hoy));
    }
    @Test void noDescuentaPromocionInactiva() {
        Producto producto = new Producto(); producto.setPrecio(1000D);
        LocalDate hoy = LocalDate.now();
        when(productos.findById(1L)).thenReturn(Optional.of(producto));
        when(promociones.findByProductoIdAndActivaTrueAndInicioLessThanEqualAndFinGreaterThanEqual(1L, hoy, hoy)).thenReturn(List.of());
        assertEquals(new BigDecimal("1000.00"), service.calcularPrecioEfectivo(1L, hoy));
    }
}
