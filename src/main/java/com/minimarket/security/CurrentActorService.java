package com.minimarket.security;

import com.minimarket.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

/** Resolves the authenticated actor once, without exposing authorization checks to controllers. */
@Service
public class CurrentActorService {
    private final UsuarioRepository usuarioRepository;

    public CurrentActorService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Long userId() {
        return usuarioRepository.findByUsername(authentication().getName())
                .orElseThrow(() -> new NoSuchElementException("Usuario autenticado no encontrado"))
                .getId();
    }

    public boolean isStaff() {
        return authentication().getAuthorities().stream().anyMatch(authority ->
                (SecurityRoles.AUTHORITY_PREFIX + SecurityRoles.CAJERO).equals(authority.getAuthority())
                        || (SecurityRoles.AUTHORITY_PREFIX + SecurityRoles.ADMIN).equals(authority.getAuthority()));
    }

    private Authentication authentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new NoSuchElementException("Usuario autenticado no encontrado");
        }
        return authentication;
    }
}
