package com.centraldasbebidas.pdv_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    private Long id;
    private String nome;
    private String login;
    private String papel;
}
