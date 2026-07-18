package com.minimarket.sucursal;

import com.minimarket.entity.Producto;
import com.minimarket.sucursal.exception.StockSucursalInsuficienteException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Stock operativo por sucursal. El campo {@code Producto.stock} es el stock global legado y no se
 * modifica desde este agregado. Los flujos futuros de venta y reposición deben usar StockSucursal.
 */
@Entity
@Table(name = "stock_sucursal", uniqueConstraints =
        @UniqueConstraint(name = "uk_stock_sucursal_producto", columnNames = {"sucursal_id", "producto_id"}))
public class StockSucursal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sucursal_id", nullable = false)
    @NotNull
    private Sucursal sucursal;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    @NotNull
    private Producto producto;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer disponible;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer stockMinimo;

    @Version
    private Long version;

    public void disminuir(int cantidad) {
        validarCantidad(cantidad);
        if (disponible < cantidad) {
            throw new StockSucursalInsuficienteException();
        }
        disponible -= cantidad;
    }

    public void aumentar(int cantidad) {
        validarCantidad(cantidad);
        try {
            disponible = Math.addExact(disponible, cantidad);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("La cantidad disponible excede el límite permitido", exception);
        }
    }

    private void validarCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Sucursal getSucursal() { return sucursal; }
    public void setSucursal(Sucursal sucursal) { this.sucursal = sucursal; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public Integer getDisponible() { return disponible; }
    public void setDisponible(Integer disponible) {
        validarStock(disponible, "El disponible no puede ser negativo");
        this.disponible = disponible;
    }
    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) {
        validarStock(stockMinimo, "El stock mínimo no puede ser negativo");
        this.stockMinimo = stockMinimo;
    }
    public Long getVersion() { return version; }

    private void validarStock(Integer cantidad, String mensaje) {
        if (cantidad == null || cantidad < 0) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}
