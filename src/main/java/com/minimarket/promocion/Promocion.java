package com.minimarket.promocion;

import com.minimarket.entity.Producto;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "promociones")
public class Promocion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
    @Column(precision = 5, scale = 2)
    private BigDecimal porcentajeDescuento;
    @Column(precision = 19, scale = 2)
    private BigDecimal importeDescuento;
    @Column(nullable = false) private LocalDate inicio;
    @Column(nullable = false) private LocalDate fin;
    @Column(nullable = false) private boolean activa;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Producto getProducto() { return producto; } public void setProducto(Producto producto) { this.producto = producto; }
    public BigDecimal getPorcentajeDescuento() { return porcentajeDescuento; } public void setPorcentajeDescuento(BigDecimal value) { this.porcentajeDescuento = value; }
    public BigDecimal getImporteDescuento() { return importeDescuento; } public void setImporteDescuento(BigDecimal value) { this.importeDescuento = value; }
    public LocalDate getInicio() { return inicio; } public void setInicio(LocalDate inicio) { this.inicio = inicio; }
    public LocalDate getFin() { return fin; } public void setFin(LocalDate fin) { this.fin = fin; }
    public boolean isActiva() { return activa; } public void setActiva(boolean activa) { this.activa = activa; }
}
