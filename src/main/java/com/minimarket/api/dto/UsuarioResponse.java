package com.minimarket.api.dto;

import java.util.Set;

/** Excluye deliberadamente la contraseña y todos los grafos de relaciones JPA. */
public record UsuarioResponse(Long id, String username, Set<Long> rolIds) { }
