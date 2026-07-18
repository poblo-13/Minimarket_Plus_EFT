package com.minimarket.sucursal.exception;

public class StockSucursalInsuficienteException extends RuntimeException {
    public StockSucursalInsuficienteException() {
        super("Stock insuficiente en la sucursal para registrar la salida");
    }
}
