package com.minimarket.promocion;

import com.minimarket.entity.Producto;
import com.minimarket.promocion.api.PromocionRequest;
import com.minimarket.promocion.api.PromocionResponse;
import com.minimarket.repository.ProductoRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PromocionService {
    private final PromocionRepository promociones;
    private final ProductoRepository productos;
    public PromocionService(PromocionRepository promociones, ProductoRepository productos) { this.promociones = promociones; this.productos = productos; }

    @Transactional(readOnly = true) public List<PromocionResponse> listar() { return promociones.findAll().stream().map(this::respuesta).toList(); }
    @Transactional(readOnly = true) public PromocionResponse obtener(Long id) { return respuesta(buscar(id)); }
    public PromocionResponse crear(PromocionRequest request) { Promocion promocion = new Promocion(); aplicar(promocion, request); return respuesta(promociones.save(promocion)); }
    public PromocionResponse actualizar(Long id, PromocionRequest request) { Promocion promocion = buscar(id); aplicar(promocion, request); return respuesta(promociones.save(promocion)); }
    public void eliminar(Long id) { promociones.delete(buscar(id)); }

    /** Calculates the authoritative price; clients must never provide it. */
    @Transactional(readOnly = true)
    public BigDecimal calcularPrecioEfectivo(Long productoId, LocalDate fecha) {
        Producto producto = productos.findById(productoId).orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        BigDecimal base = BigDecimal.valueOf(producto.getPrecio());
        return promociones.findByProductoIdAndActivaTrueAndInicioLessThanEqualAndFinGreaterThanEqual(productoId, fecha, fecha).stream()
                .map(p -> descuento(p, base)).max(BigDecimal::compareTo).map(base::subtract).orElse(base)
                .max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }
    private BigDecimal descuento(Promocion p, BigDecimal base) { return p.getPorcentajeDescuento() != null ? base.multiply(p.getPorcentajeDescuento()).divide(BigDecimal.valueOf(100)) : p.getImporteDescuento(); }
    private Promocion buscar(Long id) { return promociones.findById(id).orElseThrow(() -> new EntityNotFoundException("Promoción no encontrada")); }
    private void aplicar(Promocion p, PromocionRequest r) {
        if ((r.porcentajeDescuento() == null) == (r.importeDescuento() == null)) throw new IllegalArgumentException("Debe indicar exactamente un tipo de descuento");
        if (r.fin().isBefore(r.inicio())) throw new IllegalArgumentException("La fecha de fin no puede ser anterior al inicio");
        Producto producto = productos.findById(r.productoId()).orElseThrow(() -> new EntityNotFoundException("Producto no encontrado"));
        p.setProducto(producto); p.setPorcentajeDescuento(r.porcentajeDescuento()); p.setImporteDescuento(r.importeDescuento()); p.setInicio(r.inicio()); p.setFin(r.fin()); p.setActiva(r.activa());
    }
    private PromocionResponse respuesta(Promocion p) { return new PromocionResponse(p.getId(), p.getProducto().getId(), p.getPorcentajeDescuento(), p.getImporteDescuento(), p.getInicio(), p.getFin(), p.isActiva()); }
}
