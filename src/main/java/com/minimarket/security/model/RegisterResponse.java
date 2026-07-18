package com.minimarket.security.model;

/** Registration result without credentials or password hashes. */
public record RegisterResponse(Long id, String username) {
}
