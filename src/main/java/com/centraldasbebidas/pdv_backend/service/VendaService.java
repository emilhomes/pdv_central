package com.centraldasbebidas.pdv_backend.service;

import com.centraldasbebidas.pdv_backend.dto.ItemVendaDTO;
import com.centraldasbebidas.pdv_backend.dto.PagamentoDTO;
import com.centraldasbebidas.pdv_backend.dto.VendaRequestDTO;
import com.centraldasbebidas.pdv_backend.model.*;
import com.centraldasbebidas.pdv_backend.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class VendaService {

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private ItemVendaRepository itemVendaRepository;

    @Autowired
    private PagamentoVendaRepository pagamentoVendaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CaixaRepository caixaRepository;

    @Autowired
    private MaquininhaRepository maquininhaRepository;

    @Autowired
    private MovimentacaoFiadoRepository movimentacaoFiadoRepository;

    @Transactional
    public Venda processarVenda(VendaRequestDTO request) {
        // 1. Validar se o Caixa está aberto
        Caixa caixaAtivo = caixaRepository.findByStatus("ABERTO")
                .orElseThrow(() -> new RuntimeException("Operação negada: O caixa está fechado!"));

        // 2. Criar e salvar o cabeçalho da Venda temporariamente (para calcular o total)
        Venda venda = new Venda();
        venda.setDataHora(LocalDateTime.now());
        venda.setStatus("CONCLUIDA");
        venda.setCaixa(caixaAtivo);
        venda.setOperador(getOperadorLogado()); // quem está logado registrando a venda

        if (request.getClienteId() != null) {
            Cliente cliente = clienteRepository.findById(request.getClienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente não encontrado!"));
            venda.setCliente(cliente);
        }

        venda.setValorTotal(BigDecimal.ZERO);
        venda = vendaRepository.save(venda);

        BigDecimal valorTotalCalculado = BigDecimal.ZERO;

        // 3. Processar os Itens do Carrinho
        for (ItemVendaDTO itemDTO : request.getItens()) {
            Produto produto = produtoRepository.findById(itemDTO.getProdutoId())
                    .orElseThrow(() -> new RuntimeException("Produto ID " + itemDTO.getProdutoId() + " não encontrado!"));

            // Validar Estoque
            if (produto.getQuantidadeEstoque() < itemDTO.getQuantidade()) {
                throw new RuntimeException("Estoque insuficiente para o produto: " + produto.getDescricao());
            }

            // Atualizar o Estoque do Produto
            produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - itemDTO.getQuantidade());
            produtoRepository.save(produto);

            // Criar o Item da Venda
            ItemVenda itemVenda = new ItemVenda();
            itemVenda.setVenda(venda);
            itemVenda.setProduto(produto);
            itemVenda.setQuantidade(itemDTO.getQuantidade());
            itemVenda.setPrecoUnitario(produto.getPrecoVenda()); // Congela o preço atual

            BigDecimal subtotal = produto.getPrecoVenda().multiply(BigDecimal.valueOf(itemDTO.getQuantidade()));
            itemVenda.setSubtotal(subtotal);
            valorTotalCalculado = valorTotalCalculado.add(subtotal);

            itemVendaRepository.save(itemVenda);
        }

        // Atualizar o valor total real da venda
        venda.setValorTotal(valorTotalCalculado);
        vendaRepository.save(venda);

        // 4. Processar os Pagamentos
        BigDecimal totalPago = BigDecimal.ZERO;

        for (PagamentoDTO pagDTO : request.getPagamentos()) {
            PagamentoVenda pagamento = new PagamentoVenda();
            pagamento.setVenda(venda);
            pagamento.setMetodoPagamento(pagDTO.getMetodoPagamento().toUpperCase());
            pagamento.setValorPago(pagDTO.getValorPago());

            totalPago = totalPago.add(pagDTO.getValorPago());

            // Lógica se for Fiado
            if ("FIADO".equals(pagamento.getMetodoPagamento())) {
                if (venda.getCliente() == null) {
                    throw new RuntimeException("Venda no fiado exige a seleção de um cliente válido!");
                }

                Cliente cliente = venda.getCliente();
                BigDecimal novoSaldoDevedor = cliente.getSaldoDevedor().add(pagDTO.getValorPago());

                // Validar Limite de Crédito
                if (novoSaldoDevedor.compareTo(cliente.getLimiteCredito()) > 0) {
                    throw new RuntimeException("Venda recusada: O cliente " + cliente.getNome() + " excedeu o limite de crédito!");
                }

                cliente.setSaldoDevedor(novoSaldoDevedor);
                clienteRepository.save(cliente);

                // Gerar histórico de débito no extrato do cliente
                MovimentacaoFiado hist = new MovimentacaoFiado();
                hist.setCliente(cliente);
                hist.setVenda(venda);
                hist.setDataMovimentacao(LocalDateTime.now());
                hist.setTipo("DEBITO");
                hist.setValor(pagDTO.getValorPago());
                hist.setObservacao("Compra efetuada via cupom/venda #" + venda.getId());
                movimentacaoFiadoRepository.save(hist);
            }

            // Lógica se for Cartão (Vincular à Maquininha)
            if ("DEBITO".equals(pagamento.getMetodoPagamento()) || "CREDITO".equals(pagamento.getMetodoPagamento())) {
                if (pagDTO.getMaquininhaId() == null) {
                    throw new RuntimeException("Para pagamentos em cartão, é obrigatório selecionar a maquininha!");
                }
                Maquininha maq = maquininhaRepository.findById(pagDTO.getMaquininhaId())
                        .orElseThrow(() -> new RuntimeException("Maquininha não encontrada!"));
                pagamento.setMaquininha(maq);
            }

            pagamentoVendaRepository.save(pagamento);
        }

        // Validação final: Garantir que o valor pago bate com o total da compra
        if (totalPago.compareTo(valorTotalCalculado) != 0) {
            throw new RuntimeException("O valor total dos pagamentos não bate com o valor total da venda!");
        }

        return venda;
    }

    private Operador getOperadorLogado() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getPrincipal() instanceof Operador op) ? op : null;
    }

}
