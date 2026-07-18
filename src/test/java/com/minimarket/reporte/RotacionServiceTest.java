package com.minimarket.reporte;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RotacionServiceTest {
    @Mock RotacionRepository repository; @InjectMocks RotacionService service;
    @Test void agregaLosDetallesPorProducto() {
        LocalDate fecha = LocalDate.of(2026, 1, 1);
        when(repository.rotacionPorProducto(fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay())).thenReturn(List.<Object[]>of(new Object[]{1L, "Arroz", 3L, 1500D}));
        var resultado = service.consultar(fecha, fecha);
        assertEquals(3L, resultado.getFirst().cantidadVendida()); assertEquals("1500.0", resultado.getFirst().importeVendido().toString());
    }
}
