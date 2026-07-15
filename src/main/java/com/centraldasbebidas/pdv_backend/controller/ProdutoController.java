package com.centraldasbebidas.pdv_backend.controller;

import com.centraldasbebidas.pdv_backend.model.Produto;
import com.centraldasbebidas.pdv_backend.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/produto")
public class ProdutoController {

    @Autowired
    private ProdutoRepository repository;

    //Rota para SALVAR um novo produto
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Produto salvar(@RequestBody Produto produto) {
        return repository.save(produto);
    }

    //Rota para LISTAR todos os produtos (ativos E inativos — a tela de
    // Estoque precisa ver os dois para poder reativar; quem só deve
    // considerar produtos ativos, como o Caixa, filtra no app pelo campo
    // "ativo" que já vem em cada produto)
    @GetMapping
    public List<Produto> listar() {
        return repository.findAll();
    }

    // Produtos com estoque crítico (< 5 unidades) — endpoint leve, pensado
    // pro banner de alerta do Dashboard (não precisa carregar o
    // relatório inteiro só pra mostrar esse aviso).
    // Nota: o mesmo limite (5) também é usado em RelatorioService — se
    // mudar aqui, ajuste lá também.
    @GetMapping("/estoque-critico")
    public List<Produto> estoqueCritico() {
        return repository.buscarEstoqueCritico(5);
    }

    //Buscar produto por ID
    @GetMapping("/{id}")
    public ResponseEntity<Produto> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(produto -> ResponseEntity.ok(produto))
                .orElse(ResponseEntity.notFound().build());
    }

    //Atualizar um produto
    @PutMapping("/{id}")
    public ResponseEntity<Produto> atualizar(@PathVariable Long id, @RequestBody Produto produtoAtualizado) {
        return repository.findById(id)
                .map(produtoExistente -> {
                    produtoExistente.setCodigoBarras(produtoAtualizado.getCodigoBarras());
                    produtoExistente.setDescricao(produtoAtualizado.getDescricao());
                    produtoExistente.setCusto(produtoAtualizado.getCusto());
                    produtoExistente.setPrecoVenda(produtoAtualizado.getPrecoVenda());
                    produtoExistente.setQuantidadeEstoque(produtoAtualizado.getQuantidadeEstoque());
                    // "ativo" não é tocado aqui de propósito — a edição normal
                    // de dados do produto não deve mexer nesse status; use os
                    // endpoints /desativar e /reativar abaixo para isso.

                    Produto salvo = repository.save(produtoExistente);
                    return ResponseEntity.ok(salvo);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Desativa o produto (soft delete) — some do Caixa, mas continua
    // existindo no banco e no histórico de vendas/reposições.
    @PutMapping("/{id}/desativar")
    public ResponseEntity<Produto> desativar(@PathVariable Long id) {
        return repository.findById(id)
                .map(produto -> {
                    produto.setAtivo(false);
                    return ResponseEntity.ok(repository.save(produto));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Reativa um produto desativado anteriormente.
    @PutMapping("/{id}/reativar")
    public ResponseEntity<Produto> reativar(@PathVariable Long id) {
        return repository.findById(id)
                .map(produto -> {
                    produto.setAtivo(true);
                    return ResponseEntity.ok(repository.save(produto));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    //Excluir um produto DE VERDADE (continua existindo para produtos sem
    // nenhum histórico — o banco recusa com erro se houver venda/reposição
    // vinculada, e o app trata esse erro com uma mensagem amigável).
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        return repository.findById(id)
                .map(produto -> {
                    repository.delete(produto);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

}
