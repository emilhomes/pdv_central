package com.centraldasbebidas.pdv_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_venda")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(nullable = false, length = 20)
    private String status;

    @ManyToOne
    @JoinColumn(name = "caixa_id", nullable = false)
    private Caixa caixa;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    // Nullable de propósito: vendas registradas antes do login existir
    // ficam sem operador vinculado, sem quebrar nada.
    @ManyToOne
    @JoinColumn(name = "operador_id")
    private Operador operador;

}
