package com.centraldasbebidas.pdv_backend.controller;

import com.centraldasbebidas.pdv_backend.model.Cliente;
import com.centraldasbebidas.pdv_backend.repository.ClienteRepository;
import com.centraldasbebidas.pdv_backend.repository.VendaRepository;
import com.centraldasbebidas.pdv_backend.repository.PagamentoFiadoRepository; // <- Novo repositório
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cliente")
public class ClienteController {

    @Autowired
    private ClienteRepository repository;

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private PagamentoFiadoRepository pagamentoRepository; // <- Injetado!

    // Registrar um novo cliente
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Cliente salvar(@RequestBody Cliente cliente) {
        if(cliente.getSaldoDevedor() == null) {
            cliente.setSaldoDevedor(java.math.BigDecimal.ZERO);
        }
        return repository.save(cliente);
    }

    // Listar todos os clientes
    @GetMapping
    public List<Cliente> listar() {
        return repository.findAll();
    }

    // Buscar cliente por ID
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(cliente -> ResponseEntity.ok(cliente))
                .orElse(ResponseEntity.notFound().build());
    }

    // NOVA ROTA: Registar um Pagamento e Abater a Dívida
    @PostMapping("/{id}/pagar")
    public ResponseEntity<Map<String, Object>> registrarPagamento(@PathVariable Long id, @RequestBody Map<String, java.math.BigDecimal> payload) {
        return repository.findById(id).map(cliente -> {
            java.math.BigDecimal valorPago = payload.get("valorPago");

            // 1. Desconta o valor pago do saldo devedor do cliente
            cliente.setSaldoDevedor(cliente.getSaldoDevedor().subtract(valorPago));
            repository.save(cliente);

            // 2. Regista o pagamento no histórico
            com.centraldasbebidas.pdv_backend.model.PagamentoFiado pagamento = new com.centraldasbebidas.pdv_backend.model.PagamentoFiado();
            pagamento.setCliente(cliente);
            pagamento.setValorPago(valorPago);
            pagamento.setDataHora(LocalDateTime.now());
            pagamentoRepository.save(pagamento);

            Map<String, Object> response = new HashMap<>();
            response.put("mensagem", "Pagamento registado com sucesso");
            response.put("novoSaldo", cliente.getSaldoDevedor());

            return ResponseEntity.ok(response);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ROTA ATUALIZADA: Buscar o Histórico Misturado (Vendas + Pagamentos)
    @GetMapping("/{id}/historico")
    public ResponseEntity<List<Map<String, Object>>> buscarHistorico(@PathVariable Long id) {
        return repository.findById(id).map(cliente -> {

            List<Map<String, Object>> historico = new ArrayList<>();

            // 1. Vai buscar as Vendas (Dívidas / Vermelho)
            List<com.centraldasbebidas.pdv_backend.model.Venda> vendas = vendaRepository.findByClienteIdOrderByDataHoraDesc(id);
            for (com.centraldasbebidas.pdv_backend.model.Venda venda : vendas) {
                Map<String, Object> transacao = new HashMap<>();
                transacao.put("dataHora", venda.getDataHora()); // Usado apenas para ordenar
                transacao.put("data", venda.getDataHora().toString());
                transacao.put("isDebito", true); // É dívida
                transacao.put("observacao", "Venda #" + venda.getId() + " - " + venda.getStatus());
                transacao.put("valor", venda.getValorTotal());
                historico.add(transacao);
            }

            // 2. Vai buscar os Pagamentos (Créditos / Verde)
            List<com.centraldasbebidas.pdv_backend.model.PagamentoFiado> pagamentos = pagamentoRepository.findByClienteId(id);
            for (com.centraldasbebidas.pdv_backend.model.PagamentoFiado pag : pagamentos) {
                Map<String, Object> transacao = new HashMap<>();
                transacao.put("dataHora", pag.getDataHora()); // Usado apenas para ordenar
                transacao.put("data", pag.getDataHora().toString());
                transacao.put("isDebito", false); // É pagamento
                transacao.put("observacao", "Pagamento de Fiado recebido");
                transacao.put("valor", pag.getValorPago());
                historico.add(transacao);
            }

            // 3. Ordena tudo (Vendas e Pagamentos) do mais recente para o mais antigo
            historico.sort((m1, m2) -> {
                LocalDateTime d1 = (LocalDateTime) m1.get("dataHora");
                LocalDateTime d2 = (LocalDateTime) m2.get("dataHora");
                return d2.compareTo(d1); // Ordem decrescente
            });

            return ResponseEntity.ok(historico);
        }).orElse(ResponseEntity.notFound().build());
    }

    // Atualizar dados do cliente
    @PutMapping("/{id}")
    public ResponseEntity<Cliente> atualizar(@PathVariable Long id, @RequestBody Cliente clienteAtualizado) {
        return repository.findById(id)
                .map(clienteExistente -> {
                    clienteExistente.setNome(clienteAtualizado.getNome());
                    clienteExistente.setTelefone(clienteAtualizado.getTelefone());
                    clienteExistente.setLimiteCredito(clienteAtualizado.getLimiteCredito());
                    clienteExistente.setSaldoDevedor(clienteAtualizado.getSaldoDevedor());

                    Cliente salvo = repository.save(clienteExistente);
                    return ResponseEntity.ok(salvo);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Excluir um cliente
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        return repository.findById(id)
                .map(cliente -> {
                    repository.delete(cliente);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}