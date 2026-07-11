package com.centraldasbebidas.pdv_backend.service;

import com.centraldasbebidas.pdv_backend.dto.RelatorioDashboardDTO;
import com.centraldasbebidas.pdv_backend.model.ItemVenda;
import com.centraldasbebidas.pdv_backend.model.PagamentoVenda;
import com.centraldasbebidas.pdv_backend.model.Produto;
import com.centraldasbebidas.pdv_backend.model.Venda;
import com.centraldasbebidas.pdv_backend.repository.ClienteRepository;
import com.centraldasbebidas.pdv_backend.repository.ItemVendaRepository;
import com.centraldasbebidas.pdv_backend.repository.PagamentoVendaRepository;
import com.centraldasbebidas.pdv_backend.repository.ProdutoRepository;
import com.centraldasbebidas.pdv_backend.repository.VendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RelatorioService {

    // Abaixo dessa quantidade, o produto entra na lista de "estoque crítico".
    // Ajuste aqui se quiser um limite diferente de 5 unidades.
    private static final int LIMITE_ESTOQUE_CRITICO = 5;

    private static final DateTimeFormatter FORMATO_DIA = DateTimeFormatter.ofPattern("dd/MM");

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private ItemVendaRepository itemVendaRepository;

    @Autowired
    private PagamentoVendaRepository pagamentoVendaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    public RelatorioDashboardDTO gerarDashboard() {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicioDia = hoje.atStartOfDay();
        LocalDateTime inicioMes = hoje.withDayOfMonth(1).atStartOfDay();
        LocalDateTime inicioAno = hoje.withDayOfYear(1).atStartOfDay();
        LocalDateTime inicio7Dias = hoje.minusDays(6).atStartOfDay(); // hoje + 6 dias anteriores
        LocalDateTime inicio30Dias = hoje.minusDays(29).atStartOfDay();

        // Período anterior, para as comparações percentuais.
        LocalDateTime inicioOntem = hoje.minusDays(1).atStartOfDay();
        LocalDateTime inicioMesAnterior = hoje.withDayOfMonth(1).minusMonths(1).atStartOfDay();

        RelatorioDashboardDTO dto = new RelatorioDashboardDTO();

        // --- Fiado ---
        BigDecimal totalFiado = clienteRepository.somarTotalFiado();
        dto.setTotalFiado(arredondar(totalFiado));

        // --- Faturamento ---
        BigDecimal faturamentoHoje = vendaRepository.somarFaturamentoDesde(inicioDia);
        BigDecimal faturamentoMes = vendaRepository.somarFaturamentoDesde(inicioMes);
        BigDecimal faturamentoAno = vendaRepository.somarFaturamentoDesde(inicioAno);
        dto.setFaturamentoHoje(arredondar(faturamentoHoje));
        dto.setFaturamentoMes(arredondar(faturamentoMes));
        dto.setFaturamentoAno(arredondar(faturamentoAno));

        // --- Comparação com o período anterior ---
        BigDecimal faturamentoOntem = vendaRepository.somarFaturamentoEntre(inicioOntem, inicioDia);
        BigDecimal faturamentoMesAnterior = vendaRepository.somarFaturamentoEntre(inicioMesAnterior, inicioMes);
        dto.setVariacaoFaturamentoHojePercentual(calcularVariacaoPercentual(faturamentoHoje, faturamentoOntem));
        dto.setVariacaoFaturamentoMesPercentual(calcularVariacaoPercentual(faturamentoMes, faturamentoMesAnterior));

        // --- Ticket médio e número de vendas ---
        Long numeroVendasHoje = vendaRepository.contarVendasDesde(inicioDia);
        dto.setNumeroVendasHoje(numeroVendasHoje);
        dto.setTicketMedioHoje(numeroVendasHoje != null && numeroVendasHoje > 0
                ? arredondar(faturamentoHoje.divide(BigDecimal.valueOf(numeroVendasHoje), 2, RoundingMode.HALF_UP))
                : BigDecimal.ZERO);

        // --- Lucro (dia / mês / ano) a partir dos itens vendidos no ano ---
        // Buscamos uma única vez a janela mais ampla (ano) e derivamos as
        // janelas menores (mês, dia) filtrando em memória — evita 3 consultas.
        List<ItemVenda> itensDoAno = itemVendaRepository.buscarItensVendidosDesde(inicioAno);

        BigDecimal lucroAno = BigDecimal.ZERO;
        BigDecimal lucroMes = BigDecimal.ZERO;
        BigDecimal lucroHoje = BigDecimal.ZERO;

        // Também aproveitamos essa mesma lista para "produtos mais vendidos"
        // (considerando os últimos 30 dias).
        Map<String, Integer> quantidadePorProduto = new LinkedHashMap<>();

        for (ItemVenda item : itensDoAno) {
            BigDecimal custoUnitario = item.getProduto().getCusto() != null
                    ? item.getProduto().getCusto()
                    : BigDecimal.ZERO;
            BigDecimal lucroItem = item.getPrecoUnitario()
                    .subtract(custoUnitario)
                    .multiply(BigDecimal.valueOf(item.getQuantidade()));

            lucroAno = lucroAno.add(lucroItem);

            LocalDateTime dataVenda = item.getVenda().getDataHora();
            if (!dataVenda.isBefore(inicioMes)) {
                lucroMes = lucroMes.add(lucroItem);
            }
            if (!dataVenda.isBefore(inicioDia)) {
                lucroHoje = lucroHoje.add(lucroItem);
            }
            if (!dataVenda.isBefore(inicio30Dias)) {
                String nomeProduto = item.getProduto().getDescricao();
                quantidadePorProduto.merge(nomeProduto, item.getQuantidade(), Integer::sum);
            }
        }

        dto.setLucroAno(arredondar(lucroAno));
        dto.setLucroMes(arredondar(lucroMes));
        dto.setLucroHoje(arredondar(lucroHoje));

        // Lucro de ontem — busco à parte (intervalo fechado) em vez de
        // derivar de itensDoAno, pois em 1º de janeiro "ontem" cairia no
        // ano anterior, fora dessa lista.
        List<ItemVenda> itensOntem = itemVendaRepository.buscarItensVendidosEntre(inicioOntem, inicioDia);
        BigDecimal lucroOntem = BigDecimal.ZERO;
        for (ItemVenda item : itensOntem) {
            BigDecimal custoUnitario = item.getProduto().getCusto() != null
                    ? item.getProduto().getCusto()
                    : BigDecimal.ZERO;
            lucroOntem = lucroOntem.add(item.getPrecoUnitario()
                    .subtract(custoUnitario)
                    .multiply(BigDecimal.valueOf(item.getQuantidade())));
        }
        dto.setVariacaoLucroHojePercentual(calcularVariacaoPercentual(lucroHoje, lucroOntem));

        List<RelatorioDashboardDTO.ProdutoMaisVendidoDTO> maisVendidos = new ArrayList<>();
        quantidadePorProduto.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> maisVendidos.add(
                        new RelatorioDashboardDTO.ProdutoMaisVendidoDTO(e.getKey(), e.getValue())));
        dto.setProdutosMaisVendidos(maisVendidos);

        // --- Estoque crítico ---
        dto.setLimiteEstoqueCritico(LIMITE_ESTOQUE_CRITICO);
        List<Produto> produtosCriticos = produtoRepository.buscarEstoqueCritico(LIMITE_ESTOQUE_CRITICO);
        List<RelatorioDashboardDTO.ProdutoCriticoDTO> criticosDTO = new ArrayList<>();
        for (Produto p : produtosCriticos) {
            criticosDTO.add(new RelatorioDashboardDTO.ProdutoCriticoDTO(
                    p.getId(), p.getDescricao(), p.getQuantidadeEstoque()));
        }
        dto.setProdutosEstoqueCritico(criticosDTO);

        // --- Gráfico: faturamento dos últimos 7 dias ---
        List<Venda> vendas7Dias = vendaRepository
                .findByDataHoraGreaterThanEqualAndStatusOrderByDataHoraAsc(inicio7Dias, "CONCLUIDA");

        Map<LocalDate, BigDecimal> faturamentoPorDia = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            faturamentoPorDia.put(hoje.minusDays(6 - i), BigDecimal.ZERO);
        }
        for (Venda v : vendas7Dias) {
            LocalDate dia = v.getDataHora().toLocalDate();
            faturamentoPorDia.merge(dia, v.getValorTotal(), BigDecimal::add);
        }
        List<RelatorioDashboardDTO.PontoGraficoDTO> grafico7Dias = new ArrayList<>();
        faturamentoPorDia.forEach((dia, valor) -> grafico7Dias.add(
                new RelatorioDashboardDTO.PontoGraficoDTO(dia.format(FORMATO_DIA), arredondar(valor))));
        dto.setFaturamentoUltimos7Dias(grafico7Dias);

        // --- Vendas por forma de pagamento (últimos 30 dias) ---
        List<PagamentoVenda> pagamentos30Dias = pagamentoVendaRepository.buscarPagamentosDesde(inicio30Dias);
        Map<String, BigDecimal> porFormaPagamento = new LinkedHashMap<>();
        for (PagamentoVenda p : pagamentos30Dias) {
            porFormaPagamento.merge(p.getMetodoPagamento(), p.getValorPago(), BigDecimal::add);
        }
        porFormaPagamento.replaceAll((k, v) -> arredondar(v));
        dto.setVendasPorFormaPagamento(porFormaPagamento);

        return dto;
    }

    private BigDecimal arredondar(BigDecimal valor) {
        return (valor != null ? valor : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula a variação percentual de {@code atual} em relação a
     * {@code anterior}. Retorna {@code null} quando não há base de
     * comparação válida (período anterior igual a zero), pois "infinito
     * por cento" não é uma informação útil para mostrar ao usuário.
     */
    private Double calcularVariacaoPercentual(BigDecimal atual, BigDecimal anterior) {
        if (anterior == null || anterior.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        BigDecimal atualSeguro = atual != null ? atual : BigDecimal.ZERO;
        BigDecimal variacao = atualSeguro.subtract(anterior)
                .divide(anterior, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        return variacao.setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
