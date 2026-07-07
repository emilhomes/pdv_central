package com.centraldasbebidas.pdv_backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PagamentoDTO {
    private String metodoPagamento;
    private BigDecimal valorPago;
    private Long maquininhaId;
}
