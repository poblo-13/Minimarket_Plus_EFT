package com.minimarket.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException() {
        super("Stock insuficiente para registrar la salida");
    }
}
