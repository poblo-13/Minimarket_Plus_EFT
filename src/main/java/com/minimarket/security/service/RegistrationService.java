package com.minimarket.security.service;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.security.SecurityRoles;
import com.minimarket.security.model.RegisterRequest;
import com.minimarket.security.model.RegisterResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
public class RegistrationService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UsuarioRepository usuarioRepository,
                               RolRepository rolRepository,
                               PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Optional<RegisterResponse> register(RegisterRequest request) {
        if (usuarioRepository.findByUsername(request.username()).isPresent()) {
            return Optional.empty();
        }

        Rol cliente = rolRepository.findByNombre(SecurityRoles.CLIENTE)
                .orElseThrow(() -> new IllegalStateException("CLIENTE role is not configured"));

        Usuario usuario = new Usuario();
        usuario.setUsername(request.username());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setRoles(Set.of(cliente));

        Usuario saved = usuarioRepository.save(usuario);
        return Optional.of(new RegisterResponse(saved.getId(), saved.getUsername()));
    }
}
