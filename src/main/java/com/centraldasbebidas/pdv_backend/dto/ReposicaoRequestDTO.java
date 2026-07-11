package com.centraldasbebidas.pdv_backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReposicaoRequestDTO {
    private Long produtoId;
    private Long fornecedorId; // opcional
    private Integer quantidade;
    private BigDecimal custoUnitario;
    private String observacoes;
}
