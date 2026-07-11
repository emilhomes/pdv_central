package com.centraldasbebidas.pdv_backend.dto;

import lombok.Data;

@Data
public class OperadorRequestDTO {
    private String nome;
    private String login;
    private String senha;
    private String papel; // "ADMIN" ou "OPERADOR" — se vier nulo, vira "OPERADOR"
}
