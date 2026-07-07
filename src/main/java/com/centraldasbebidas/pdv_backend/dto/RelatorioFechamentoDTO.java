package com.centraldasbebidas.pdv_backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
public class RelatorioFechamentoDTO {

    private Long caixaId;
    private String statusCaixa;
    private BigDecimal totalGeral;
    private Map<String, BigDecimal> resumoPorMetodo = new HashMap<>();
    private Map<String, BigDecimal> resumoPorMaquininha = new HashMap<>();

}
