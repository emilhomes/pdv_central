package com.centraldasbebidas.pdv_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "tb_produto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_barras", unique = true, nullable = false, length = 50)
    private String codigoBarras;

    @Column(nullable = false, length = 150)
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal custo;

    @Column(name = "preco_venda", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoVenda;

    @Column(name = "quantidade_estoque", nullable = false)
    private Integer quantidadeEstoque;

    // Soft delete: produtos "desativados" saem da Venda/Caixa mas continuam
    // existindo no banco (preservando o histórico de vendas/reposições que
    // apontam pra eles). Nunca são de fato apagados por padrão — só o
    // DELETE físico (endpoint existente) apaga de verdade, e esse continua
    // bloqueado pelo banco se houver histórico.
    //
    // columnDefinition com "default true" é importante aqui: como a tabela
    // já tem produtos cadastrados no banco de produção (Neon), adicionar
    // uma coluna NOT NULL sem valor padrão quebraria o `ddl-auto=update`
    // na próxima subida do backend. Com o default, os produtos já
    // existentes viram "ativo = true" automaticamente.
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean ativo = true;
}
