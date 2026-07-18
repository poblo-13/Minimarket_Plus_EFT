package com.minimarket.config;

import com.minimarket.abastecimiento.Proveedor;
import com.minimarket.abastecimiento.ProveedorRepository;
import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.SecurityRoles;
import com.minimarket.sucursal.StockSucursal;
import com.minimarket.sucursal.StockSucursalRepository;
import com.minimarket.sucursal.Sucursal;
import com.minimarket.sucursal.SucursalRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@Profile("dev")
public class DemoSeedConfig {

    @Bean
    CommandLineRunner seedDatosDemo(
            RolRepository rolRepository,
            UsuarioRepository usuarioRepository,
            CategoriaRepository categoriaRepository,
            ProductoRepository productoRepository,
            SucursalRepository sucursalRepository,
            ProveedorRepository proveedorRepository,
            StockSucursalRepository stockSucursalRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.enabled:false}") boolean seedEnabled,
            @Value("${app.admin.username}") String adminUsername,
            @Value("${app.admin.password}") String adminPassword,
            @Value("${app.cajero.username}") String cajeroUsername,
            @Value("${app.cajero.password}") String cajeroPassword,
            @Value("${app.cliente.username}") String clienteUsername,
            @Value("${app.cliente.password}") String clientePassword) {
        return args -> {
            if (!seedEnabled) {
                return;
            }

            Rol admin = obtenerOCrearRol(rolRepository, SecurityRoles.ADMIN);
            Rol cajero = obtenerOCrearRol(rolRepository, SecurityRoles.CAJERO);
            Rol cliente = obtenerOCrearRol(rolRepository, SecurityRoles.CLIENTE);

            crearUsuarioSiNoExiste(usuarioRepository, passwordEncoder, adminUsername, adminPassword, admin);
            crearUsuarioSiNoExiste(usuarioRepository, passwordEncoder, cajeroUsername, cajeroPassword, cajero);
            crearUsuarioSiNoExiste(usuarioRepository, passwordEncoder, clienteUsername, clientePassword, cliente);

            Categoria bebidas = obtenerOCrearCategoria(categoriaRepository, "Bebidas Demo");
            Categoria abarrotes = obtenerOCrearCategoria(categoriaRepository, "Abarrotes Demo");
            Categoria snacks = obtenerOCrearCategoria(categoriaRepository, "Snacks Demo");

            Producto agua = obtenerOCrearProducto(productoRepository, bebidas, "Agua Mineral Demo 600 ml", 990D);
            Producto bebida = obtenerOCrearProducto(productoRepository, bebidas, "Bebida Cola Demo 1.5 L", 1990D);
            Producto arroz = obtenerOCrearProducto(productoRepository, abarrotes, "Arroz Grano Largo Demo 1 kg", 1590D);
            Producto barras = obtenerOCrearProducto(productoRepository, snacks, "Barra Cereal Demo", 790D);

            Sucursal centro = obtenerOCrearSucursal(sucursalRepository, "Sucursal Centro Demo");
            Sucursal norte = obtenerOCrearSucursal(sucursalRepository, "Sucursal Norte Demo");
            Proveedor proveedor = obtenerOCrearProveedor(proveedorRepository, "Proveedor Sintético Demo");

            obtenerOCrearStock(stockSucursalRepository, centro, agua, 24, 8);
            obtenerOCrearStock(stockSucursalRepository, centro, bebida, 12, 6);
            obtenerOCrearStock(stockSucursalRepository, centro, arroz, 16, 5);
            obtenerOCrearStock(stockSucursalRepository, centro, barras, 8, 4);
            obtenerOCrearStock(stockSucursalRepository, norte, agua, 10, 6);
            obtenerOCrearStock(stockSucursalRepository, norte, bebida, 7, 5);
            obtenerOCrearStock(stockSucursalRepository, norte, arroz, 14, 5);
            obtenerOCrearStock(stockSucursalRepository, norte, barras, 5, 4);
        };
    }

    private Rol obtenerOCrearRol(RolRepository rolRepository, String nombre) {
        return rolRepository.findByNombre(nombre).orElseGet(() -> rolRepository.save(new Rol(nombre)));
    }

    private void crearUsuarioSiNoExiste(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                                        String username, String password, Rol rol) {
        if (usuarioRepository.findByUsername(username).isPresent()) {
            return;
        }
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRoles(Set.of(rol));
        usuarioRepository.save(usuario);
    }

    private Categoria obtenerOCrearCategoria(CategoriaRepository categoriaRepository, String nombre) {
        return categoriaRepository.findAll().stream()
                .filter(categoria -> categoria.getNombre().equals(nombre))
                .findFirst()
                .orElseGet(() -> {
                    Categoria categoria = new Categoria();
                    categoria.setNombre(nombre);
                    return categoriaRepository.save(categoria);
                });
    }

    private Producto obtenerOCrearProducto(ProductoRepository productoRepository, Categoria categoria,
                                             String nombre, double precio) {
        return productoRepository.findAll().stream()
                .filter(producto -> producto.getNombre().equals(nombre))
                .findFirst()
                .orElseGet(() -> {
                    Producto producto = new Producto();
                    producto.setNombre(nombre);
                    producto.setPrecio(precio);
                    // Compatibilidad con la entidad legado: el inventario operativo se guarda en StockSucursal.
                    producto.setStock(0);
                    producto.setCategoria(categoria);
                    return productoRepository.save(producto);
                });
    }

    private Sucursal obtenerOCrearSucursal(SucursalRepository sucursalRepository, String nombre) {
        return sucursalRepository.findAll().stream()
                .filter(sucursal -> sucursal.getNombre().equals(nombre))
                .findFirst()
                .orElseGet(() -> {
                    Sucursal sucursal = new Sucursal();
                    sucursal.setNombre(nombre);
                    return sucursalRepository.save(sucursal);
                });
    }

    private Proveedor obtenerOCrearProveedor(ProveedorRepository proveedorRepository, String nombre) {
        return proveedorRepository.findAll().stream()
                .filter(proveedor -> proveedor.getNombre().equals(nombre))
                .findFirst()
                .orElseGet(() -> {
                    Proveedor proveedor = new Proveedor();
                    proveedor.setNombre(nombre);
                    return proveedorRepository.save(proveedor);
                });
    }

    private void obtenerOCrearStock(StockSucursalRepository stockSucursalRepository, Sucursal sucursal,
                                    Producto producto, int disponible, int minimo) {
        if (stockSucursalRepository.findBySucursalIdAndProductoId(sucursal.getId(), producto.getId()).isPresent()) {
            return;
        }
        StockSucursal stock = new StockSucursal();
        stock.setSucursal(sucursal);
        stock.setProducto(producto);
        stock.setDisponible(disponible);
        stock.setStockMinimo(minimo);
        stockSucursalRepository.save(stock);
    }
}
