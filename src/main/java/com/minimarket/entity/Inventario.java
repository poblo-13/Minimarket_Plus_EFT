package com.minimarket.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import com.minimarket.sucursal.Sucursal;

@Entity
public class Inventario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    @NotNull(message = "El producto no puede ser nulo")
    private Producto producto;

    @Column(nullable = false)
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    @Column(nullable = false)
    @NotBlank(message = "El tipo de movimiento no puede estar en blanco")
    @Pattern(regexp = "^(Entrada|Salida)$", message = "El tipo de movimiento debe ser exactamente 'Entrada' o 'Salida'")
    private String tipoMovimiento; 

    @Column(nullable = false)
    @NotNull(message = "La fecha de movimiento es obligatoria")
    private LocalDateTime fechaMovimiento;

    /** Venta que originó esta salida; el inventario solo permite agregar registros. */
    @ManyToOne
    @JoinColumn(name = "venta_id")
    private Venta venta;

    /** Sucursal cuya existencia operativa fue afectada; Producto.stock es sólo legado. */
    @ManyToOne
    @JoinColumn(name = "sucursal_id")
    private Sucursal sucursal;

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public LocalDateTime getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(LocalDateTime fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
    }

    public Venta getVenta() { return venta; }

    public void setVenta(Venta venta) { this.venta = venta; }
    public Sucursal getSucursal() { return sucursal; }
    public void setSucursal(Sucursal sucursal) { this.sucursal = sucursal; }
}
