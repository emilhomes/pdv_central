package com.centraldasbebidas.pdv_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "tb_pagamento_venda")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoVenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "venda_id", nullable = false)
    private Venda venda;

    @Column(name = "metodo_pagamento", nullable = false, length = 30)
    private String metodoPagamento; // "DINHEIRO", "PIX", "DEBITO", "CREDITO", "FIADO"

    @Column(name = "valor_pago", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorPago;

    @ManyToOne
    @JoinColumn(name = "maquininha_id") // Nulo se for dinheiro/pix/fiado
    private Maquininha maquininha;

}
