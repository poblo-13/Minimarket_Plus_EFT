package com.minimarket.api.dto;

import java.util.Set;

/** Deliberately excludes the password and all JPA relationship graphs. */
public record UsuarioResponse(Long id, String username, Set<Long> rolIds) { }
