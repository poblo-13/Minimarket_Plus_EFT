package com.minimarket.abastecimiento;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrdenCompraConsultaService {
    private final OrdenCompraRepository ordenCompraRepository;

    @Transactional(readOnly = true)
    public List<OrdenCompra> listar() {
        return ordenCompraRepository.findAll();
    }
}
