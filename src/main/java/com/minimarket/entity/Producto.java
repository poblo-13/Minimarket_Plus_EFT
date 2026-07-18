package com.minimarket.entity;

import com.minimarket.abastecimiento.Proveedor;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Proyección global heredada. Los flujos nuevos de pedido y venta operan exclusivamente sobre
     * StockSucursal; este valor permanece sólo por compatibilidad con inventario legado.
     */
    @Column(nullable = false)
    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombre;

    @Column(nullable = false)
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    private Double precio;

    @Column(nullable = false)
    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    @NotNull(message = "La categoria es obligatoria")
    private Categoria categoria;

    /** Proveedor operativo usado por la reposición automática de StockSucursal. */
    @ManyToOne
    @JoinColumn(name = "proveedor_reposicion_id")
    private Proveedor proveedorReposicion;

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }
    public Proveedor getProveedorReposicion() { return proveedorReposicion; }
    public void setProveedorReposicion(Proveedor proveedorReposicion) { this.proveedorReposicion = proveedorReposicion; }
}
