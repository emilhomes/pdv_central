package com.centraldasbebidas.pdv_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_operador")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Operador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 50)
    private String login;

    // @JsonIgnore garante que o hash da senha NUNCA seja incluído em
    // nenhuma resposta JSON do backend (ex.: GET /operadores).
    @JsonIgnore
    @Column(nullable = false)
    private String senha;

    // "ADMIN" ou "OPERADOR"
    @Column(nullable = false, length = 20)
    private String papel;

    @Column(nullable = false)
    private boolean ativo = true;
}
