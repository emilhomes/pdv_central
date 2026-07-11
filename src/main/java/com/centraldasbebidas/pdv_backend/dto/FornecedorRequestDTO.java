package com.centraldasbebidas.pdv_backend.dto;

import lombok.Data;

@Data
public class FornecedorRequestDTO {
    private String nome;
    private String telefone;
    private String email;
    private String observacoes;
}
