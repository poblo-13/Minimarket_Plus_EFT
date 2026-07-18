package com.minimarket.controller;

import com.minimarket.api.dto.CarritoRequest;
import com.minimarket.api.dto.CarritoResponse;
import com.minimarket.pedido.api.CheckoutRequest;
import com.minimarket.pedido.api.PedidoResponse;
import com.minimarket.pedido.service.PedidoService;
import com.minimarket.service.CarritoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** El carrito pertenece exclusivamente al usuario resuelto desde el JWT. */
@RestController
@RequestMapping("/api/carrito")
@RequiredArgsConstructor
@Validated
public class CarritoController {
    private final CarritoService carritoService;
    private final PedidoService pedidoService;

    @GetMapping
    public CarritoResponse obtener(Authentication authentication) {
        return carritoService.obtener(authentication.getName());
    }

    @PutMapping("/items/{productoId}")
    public CarritoResponse upsert(@PathVariable @Positive Long productoId,
                                  @Valid @RequestBody CarritoRequest request,
                                  Authentication authentication) {
        carritoService.upsert(authentication.getName(), productoId, request.cantidad());
        return carritoService.obtener(authentication.getName());
    }

    @DeleteMapping("/items/{productoId}")
    public ResponseEntity<Void> eliminar(@PathVariable @Positive Long productoId, Authentication authentication) {
        carritoService.eliminar(authentication.getName(), productoId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/checkout")
    public ResponseEntity<PedidoResponse> checkout(@Valid @RequestBody CheckoutRequest request,
                                                    Authentication authentication) {
        PedidoResponse pedido = PedidoResponse.desde(pedidoService.checkout(authentication.getName(), request));
        return ResponseEntity.status(201).body(pedido);
    }
}
