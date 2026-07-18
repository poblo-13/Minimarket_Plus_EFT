package com.minimarket.abastecimiento;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {
    boolean existsBySucursalIdAndProductoIdAndProveedorIdAndEstado(
            Long sucursalId, Long productoId, Long proveedorId, EstadoOrdenCompra estado);
}
