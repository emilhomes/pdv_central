package com.centraldasbebidas.pdv_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Payload retornado por GET /relatorios/dashboard.
 * Reúne tudo que a tela inicial de Relatórios precisa em uma única chamada,
 * evitando múltiplas idas ao backend a partir do Flutter.
 */
@Data
public class RelatorioDashboardDTO {

    // --- Fiado ---
    private BigDecimal totalFiado;

    // --- Faturamento (soma do valor das vendas) ---
    private BigDecimal faturamentoHoje;
    private BigDecimal faturamentoMes;
    private BigDecimal faturamentoAno;

    // --- Comparação com o período anterior (%). Null quando não há
    // base de comparação (ex.: não houve nenhuma venda no período anterior). ---
    private Double variacaoFaturamentoHojePercentual; // vs. ontem
    private Double variacaoLucroHojePercentual;        // vs. ontem
    private Double variacaoFaturamentoMesPercentual;    // vs. mês anterior

    // --- Lucro (preço de venda - custo, por item vendido) ---
    private BigDecimal lucroHoje;
    private BigDecimal lucroMes;
    private BigDecimal lucroAno;

    // --- Ticket médio e volume ---
    private BigDecimal ticketMedioHoje;
    private Long numeroVendasHoje;

    // --- Estoque crítico ---
    private Integer limiteEstoqueCritico;
    private List<ProdutoCriticoDTO> produtosEstoqueCritico;

    // --- Gráfico simples: faturamento dos últimos 7 dias ---
    private List<PontoGraficoDTO> faturamentoUltimos7Dias;

    // --- Vendas por forma de pagamento (últimos 30 dias) ---
    private Map<String, BigDecimal> vendasPorFormaPagamento;

    // --- Produtos mais vendidos (últimos 30 dias, por quantidade) ---
    private List<ProdutoMaisVendidoDTO> produtosMaisVendidos;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProdutoCriticoDTO {
        private Long id;
        private String nome;
        private Integer quantidadeEstoque;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PontoGraficoDTO {
        private String label; // ex: "05/07"
        private BigDecimal valor;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProdutoMaisVendidoDTO {
        private String nome;
        private Integer quantidadeVendida;
    }
}
