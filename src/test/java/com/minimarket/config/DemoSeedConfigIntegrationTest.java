package com.minimarket.config;

import com.minimarket.abastecimiento.ProveedorRepository;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.sucursal.StockSucursalRepository;
import com.minimarket.sucursal.SucursalRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("dev")
@TestPropertySource(properties = {
        "app.seed.enabled=true",
        "DEMO_ADMIN_USERNAME=seed-admin-user",
        "DEMO_ADMIN_PASSWORD=seed-admin-test-password",
        "DEMO_CAJERO_USERNAME=seed-cajero-user",
        "DEMO_CAJERO_PASSWORD=seed-cajero-test-password",
        "DEMO_CLIENTE_USERNAME=seed-cliente-user",
        "DEMO_CLIENTE_PASSWORD=seed-cliente-test-password"
})
class DemoSeedConfigIntegrationTest {
    private static final Set<String> CATEGORIAS = Set.of("Bebidas Demo", "Abarrotes Demo", "Snacks Demo");
    private static final Set<String> PRODUCTOS = Set.of(
            "Agua Mineral Demo 600 ml", "Bebida Cola Demo 1.5 L",
            "Arroz Grano Largo Demo 1 kg", "Barra Cereal Demo");
    private static final Set<String> SUCURSALES = Set.of("Sucursal Centro Demo", "Sucursal Norte Demo");

    @Autowired @Qualifier("seedDatosDemo") CommandLineRunner seedDatosDemo;
    @Autowired RolRepository rolRepository;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired CategoriaRepository categoriaRepository;
    @Autowired ProductoRepository productoRepository;
    @Autowired SucursalRepository sucursalRepository;
    @Autowired ProveedorRepository proveedorRepository;
    @Autowired StockSucursalRepository stockSucursalRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void seedEsIdempotenteYCreaRelacionesDeDemoCoherentes() throws Exception {
        SeedSnapshot antes = snapshot();

        seedDatosDemo.run();

        assertEquals(antes, snapshot());
        assertEquals(3, rolRepository.count());
        assertEquals(3, usuariosDemo().size());
        Usuario admin = usuarioRepository.findByUsername("seed-admin-user").orElseThrow();
        assertTrue(passwordEncoder.matches("seed-admin-test-password", admin.getPassword()));
        assertTrue(admin.getRoles().stream().anyMatch(rol -> rol.getNombre().equals("ADMIN")));
        assertEquals(3, categoriaRepository.findAll().stream().filter(c -> CATEGORIAS.contains(c.getNombre())).count());
        assertEquals(4, productoRepository.findAll().stream().filter(p -> PRODUCTOS.contains(p.getNombre())).count());
        assertTrue(productoRepository.findAll().stream().filter(p -> PRODUCTOS.contains(p.getNombre()))
                .allMatch(producto -> producto.getCategoria() != null && producto.getStock() == 0));
        assertEquals(2, sucursalRepository.findAll().stream().filter(s -> SUCURSALES.contains(s.getNombre())).count());
        assertEquals(1, proveedorRepository.findAll().stream()
                .filter(proveedor -> proveedor.getNombre().equals("Proveedor Sintético Demo")).count());
        assertEquals(8, stockSucursalRepository.findAll().stream()
                .filter(stock -> SUCURSALES.contains(stock.getSucursal().getNombre()))
                .filter(stock -> PRODUCTOS.contains(stock.getProducto().getNombre()))
                .count());
        assertTrue(stockSucursalRepository.findAll().stream()
                .filter(stock -> SUCURSALES.contains(stock.getSucursal().getNombre()))
                .allMatch(stock -> stock.getDisponible() > stock.getStockMinimo()));
    }

    private SeedSnapshot snapshot() {
        return new SeedSnapshot(
                usuariosDemo().size(),
                categoriaRepository.findAll().stream().filter(c -> CATEGORIAS.contains(c.getNombre())).count(),
                productoRepository.findAll().stream().filter(p -> PRODUCTOS.contains(p.getNombre())).count(),
                sucursalRepository.findAll().stream().filter(s -> SUCURSALES.contains(s.getNombre())).count(),
                proveedorRepository.findAll().stream().filter(p -> p.getNombre().equals("Proveedor Sintético Demo")).count(),
                stockSucursalRepository.findAll().stream()
                        .filter(stock -> SUCURSALES.contains(stock.getSucursal().getNombre()))
                        .filter(stock -> PRODUCTOS.contains(stock.getProducto().getNombre())).count());
    }

    private Set<Usuario> usuariosDemo() {
        return Set.of(
                usuarioRepository.findByUsername("seed-admin-user").orElseThrow(),
                usuarioRepository.findByUsername("seed-cajero-user").orElseThrow(),
                usuarioRepository.findByUsername("seed-cliente-user").orElseThrow());
    }

    private record SeedSnapshot(long usuarios, long categorias, long productos, long sucursales,
                                long proveedores, long stocks) { }
}
