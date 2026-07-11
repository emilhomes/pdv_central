package com.centraldasbebidas.pdv_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Registro histórico de uma reposição/entrada de estoque. Cada registro
 * também é responsável por ter disparado a atualização de
 * {@code Produto.quantidadeEstoque} e {@code Produto.custo} (custo médio
 * ponderado) no momento em que foi criado — ver RelatorioEstoqueService.
 */
@Entity
@Table(name = "tb_reposicao_estoque")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReposicaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @ManyToOne
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @ManyToOne
    @JoinColumn(name = "operador_id")
    private Operador operador;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "custo_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal custoUnitario;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(length = 255)
    private String observacoes;
}
