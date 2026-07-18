package com.minimarket.promocion;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromocionRepository extends JpaRepository<Promocion, Long> {
    List<Promocion> findByProductoIdAndActivaTrueAndInicioLessThanEqualAndFinGreaterThanEqual(Long productoId, LocalDate inicio, LocalDate fin);
}
