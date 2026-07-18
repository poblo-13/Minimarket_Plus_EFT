package com.minimarket.sucursal.api;

import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.sucursal.AdministracionStockService;
import com.minimarket.sucursal.StockSucursal;
import com.minimarket.sucursal.StockSucursalService;
import com.minimarket.sucursal.Sucursal;
import com.minimarket.sucursal.SucursalRepository;
import com.minimarket.sucursal.api.dto.ConfigurarStockRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdministracionSucursalControllerHateoasTest {
    @Mock AdministracionStockService administracion;
    @Mock SucursalRepository sucursales;
    @Mock StockSucursalService stocks;
    @InjectMocks AdministracionSucursalController controller;

    @Test
    void stockAdministrativoExponeSelfConsultableYLaSucursalAdministrable() {
        StockSucursal stock = stock();
        when(administracion.configurarStock(2L, 10L, 24, 5, 3L)).thenReturn(stock);

        var response = controller.configurar(2L, new ConfigurarStockRequest(10L, 24, 5, 3L));

        assertEquals("/api/sucursales/2/productos/10/disponibilidad", response.getRequiredLink("self").getHref());
        assertEquals("/api/admin/sucursales/2", response.getRequiredLink("sucursal").getHref());
    }

    private StockSucursal stock() {
        Sucursal sucursal = new Sucursal(); sucursal.setId(2L); sucursal.setNombre("Centro");
        Categoria categoria = new Categoria(); categoria.setId(1L); categoria.setNombre("General");
        Producto producto = new Producto(); producto.setId(10L); producto.setCategoria(categoria);
        StockSucursal stock = new StockSucursal();
        stock.setSucursal(sucursal); stock.setProducto(producto); stock.setDisponible(24); stock.setStockMinimo(5);
        return stock;
    }
}
