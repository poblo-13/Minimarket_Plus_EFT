package com.minimarket.abastecimiento;

import com.minimarket.entity.Producto;
import com.minimarket.sucursal.Sucursal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "ordenes_compra")
public class OrdenCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name = "sucursal_id", nullable = false) @NotNull
    private Sucursal sucursal;
    @ManyToOne(optional = false) @JoinColumn(name = "producto_id", nullable = false) @NotNull
    private Producto producto;
    @ManyToOne(optional = false) @JoinColumn(name = "proveedor_id", nullable = false) @NotNull
    private Proveedor proveedor;
    @Column(nullable = false) @NotNull @Min(1)
    private Integer cantidadSolicitada;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private EstadoOrdenCompra estado;

    @PrePersist
    void abrirSiNoFueDefinida() {
        if (estado == null) estado = EstadoOrdenCompra.ABIERTA;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Sucursal getSucursal() { return sucursal; }
    public void setSucursal(Sucursal sucursal) { this.sucursal = sucursal; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }
    public Integer getCantidadSolicitada() { return cantidadSolicitada; }
    public void setCantidadSolicitada(Integer cantidadSolicitada) { this.cantidadSolicitada = cantidadSolicitada; }
    public EstadoOrdenCompra getEstado() { return estado; }
    public void setEstado(EstadoOrdenCompra estado) { this.estado = estado; }
}
