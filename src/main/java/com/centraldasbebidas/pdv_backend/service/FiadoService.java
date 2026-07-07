package com.centraldasbebidas.pdv_backend.service;

import com.centraldasbebidas.pdv_backend.model.Cliente;
import com.centraldasbebidas.pdv_backend.model.MovimentacaoFiado;
import com.centraldasbebidas.pdv_backend.repository.ClienteRepository;
import com.centraldasbebidas.pdv_backend.repository.MovimentacaoFiadoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FiadoService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private MovimentacaoFiadoRepository movimentacaoRepository;

    // Lógica para receber o pagamento do cliente e abater a dívida
    @Transactional
    public MovimentacaoFiado receberPagamentoFiado(Long clienteId, BigDecimal valorPago, String observacao) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado!"));

        if (cliente.getSaldoDevedor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Este cliente não possui nenhuma dívida ativa!");
        }

        if (valorPago.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("O valor do pagamento deve ser maior que zero!");
        }

        // Atualizar o saldo devedor do cliente (Dívida atual - Valor Pago)
        BigDecimal novoSaldoDevedor = cliente.getSaldoDevedor().subtract(valorPago);

        // Evita que o saldo devedor fique negativo se ele pagar a mais
        if (novoSaldoDevedor.compareTo(BigDecimal.ZERO) < 0) {
            novoSaldoDevedor = BigDecimal.ZERO;
        }

        cliente.setSaldoDevedor(novoSaldoDevedor);
        clienteRepository.save(cliente);

        // Registrar a movimentação de CRÉDITO no histórico
        MovimentacaoFiado movimentacao = new MovimentacaoFiado();
        movimentacao.setCliente(cliente);
        movimentacao.setDataMovimentacao(LocalDateTime.now());
        movimentacao.setTipo("CREDITO");
        movimentacao.setValor(valorPago);
        movimentacao.setObservacao(observacao != null ? observacao : "Pagamento de conta realizado no caixa.");

        return movimentacaoRepository.save(movimentacao);
    }

    // Consulta o extrato completo do cliente
    public List<MovimentacaoFiado> obterExtrato(Long clienteId) {
        return movimentacaoRepository.findByClienteIdOrderByDataMovimentacaoDesc(clienteId);
    }

}
