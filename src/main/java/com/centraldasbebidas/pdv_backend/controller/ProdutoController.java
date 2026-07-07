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

    //Rota para LISTAR todos os produtos
    @GetMapping
    public List<Produto> listar() {
        return repository.findAll();
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

                    Produto salvo = repository.save(produtoExistente);
                    return ResponseEntity.ok(salvo);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    //Excluir um produto
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
