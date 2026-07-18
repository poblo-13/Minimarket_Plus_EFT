package com.minimarket.security.config;

import com.minimarket.security.handler.ProblemAccessDeniedHandler;
import com.minimarket.security.handler.ProblemAuthenticationEntryPoint;
import com.minimarket.security.service.CustomUserDetailsService;
import com.minimarket.security.SecurityRoles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
import com.minimarket.security.filter.JwtAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final ProblemAuthenticationEntryPoint problemAuthenticationEntryPoint;
    private final ProblemAccessDeniedHandler problemAccessDeniedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            CustomUserDetailsService customUserDetailsService,
            ProblemAuthenticationEntryPoint problemAuthenticationEntryPoint,
            ProblemAccessDeniedHandler problemAccessDeniedHandler,
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.customUserDetailsService = customUserDetailsService;
        this.problemAuthenticationEntryPoint = problemAuthenticationEntryPoint;
        this.problemAccessDeniedHandler = problemAccessDeniedHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/register").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/usuarios/**").hasRole(SecurityRoles.ADMIN)
                        .requestMatchers("/api/admin/**").hasRole(SecurityRoles.ADMIN)
                        .requestMatchers("/api/inventario/**").hasRole(SecurityRoles.ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/promociones/**").hasRole(SecurityRoles.ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/promociones/**").hasRole(SecurityRoles.ADMIN)
                        .requestMatchers(HttpMethod.PATCH, "/api/promociones/**").hasRole(SecurityRoles.ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/promociones/**").hasRole(SecurityRoles.ADMIN)
                        .requestMatchers("/api/reportes/**").hasRole(SecurityRoles.ADMIN)
                        .requestMatchers("/api/ordenes-compra/**").hasRole(SecurityRoles.ADMIN)
                        .requestMatchers("/api/sucursales/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/pedidos/*/estado")
                        .hasAnyRole(SecurityRoles.CAJERO, SecurityRoles.ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/pedidos/**")
                        .hasRole(SecurityRoles.CLIENTE)
                        .requestMatchers(HttpMethod.DELETE, "/api/pedidos/**")
                        .hasRole(SecurityRoles.CLIENTE)
                        .requestMatchers(HttpMethod.GET, "/api/pedidos")
                        .hasAnyRole(SecurityRoles.CAJERO, SecurityRoles.ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/pedidos/**")
                        .hasAnyRole(SecurityRoles.CLIENTE, SecurityRoles.CAJERO, SecurityRoles.ADMIN)
                        .requestMatchers("/api/carrito/**").hasRole(SecurityRoles.CLIENTE)
                        .requestMatchers("/api/detalle-ventas/**")
                        .hasAnyRole(SecurityRoles.CLIENTE, SecurityRoles.CAJERO, SecurityRoles.ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/categorias/**").hasRole(SecurityRoles.ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/categorias/**").hasRole(SecurityRoles.ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/categorias/**").hasRole(SecurityRoles.ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/productos/**").hasRole(SecurityRoles.ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/productos/**").hasRole(SecurityRoles.ADMIN)
                        .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasRole(SecurityRoles.ADMIN)
                        .requestMatchers(HttpMethod.POST, "/api/ventas/**")
                        .hasAnyRole(SecurityRoles.CAJERO, SecurityRoles.ADMIN)
                        .requestMatchers("/api/promociones/**", "/api/categorias/**", "/api/productos/**", "/api/ventas/**")
                        .authenticated()
                        .anyRequest().denyAll()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(problemAuthenticationEntryPoint)
                        .accessDeniedHandler(problemAccessDeniedHandler))
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Configuracion de encriptacion de contraseñas
    }
}
