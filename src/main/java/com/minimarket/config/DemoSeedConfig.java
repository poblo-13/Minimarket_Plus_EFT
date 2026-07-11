package com.minimarket.config;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.SecurityRoles;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DemoSeedConfig {

    @Bean
    CommandLineRunner seedUsuariosDemo(
            RolRepository rolRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.seed.enabled:false}") boolean seedEnabled,
            @Value("${app.admin.username:admin}") String adminUsername,
            @Value("${app.admin.password:admin123}") String adminPassword,
            @Value("${app.cajero.username:cajero}") String cajeroUsername,
            @Value("${app.cajero.password:cajero123}") String cajeroPassword,
            @Value("${app.cliente.username:cliente}") String clienteUsername,
            @Value("${app.cliente.password:cliente123}") String clientePassword) {
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
}
