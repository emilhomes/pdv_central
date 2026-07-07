package com.centraldasbebidas.pdv_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_maquininha")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Maquininha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_terminal", nullable = false, length = 100)
    private String nomeTerminal;

    @Column(nullable = false)
    private Boolean ativo = true;
}
