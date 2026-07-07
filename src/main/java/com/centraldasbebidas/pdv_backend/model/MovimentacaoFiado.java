package com.centraldasbebidas.pdv_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_movimentacao_fiado")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimentacaoFiado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "venda_id") // Pode ser nulo se for um pagamento avulso
    private Venda venda;

    @Column(name = "data_movimentacao", nullable = false)
    private LocalDateTime dataMovimentacao;

    @Column(nullable = false, length = 10)
    private String tipo; // "DEBITO" (comprou) ou "CREDITO" (pagou)

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(length = 255)
    private String observacao;

}
