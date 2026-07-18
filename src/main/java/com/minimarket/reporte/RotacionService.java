package com.minimarket.reporte;

import com.minimarket.reporte.api.RotacionProductoResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RotacionService {
    private final RotacionRepository repository;
    public RotacionService(RotacionRepository repository) { this.repository = repository; }
    public List<RotacionProductoResponse> consultar(LocalDate desde, LocalDate hasta) {
        if (hasta.isBefore(desde)) throw new IllegalArgumentException("La fecha hasta no puede ser anterior a desde");
        return repository.rotacionPorProducto(desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay()).stream()
                .map(row -> new RotacionProductoResponse((Long) row[0], (String) row[1], ((Number) row[2]).longValue(), decimal(row[3]))).toList();
    }
    private BigDecimal decimal(Object value) { return value instanceof BigDecimal decimal ? decimal : BigDecimal.valueOf(((Number) value).doubleValue()); }
}
