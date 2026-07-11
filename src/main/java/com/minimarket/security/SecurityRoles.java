package com.minimarket.security;

/** Centralized application role names and Spring Security authority prefix. */
public final class SecurityRoles {

    public static final String CLIENTE = "CLIENTE";
    public static final String CAJERO = "CAJERO";
    public static final String ADMIN = "ADMIN";
    public static final String AUTHORITY_PREFIX = "ROLE_";

    private SecurityRoles() {
    }
}
