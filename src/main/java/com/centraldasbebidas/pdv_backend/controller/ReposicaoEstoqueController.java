package com.centraldasbebidas.pdv_backend.controller;

import com.centraldasbebidas.pdv_backend.dto.ReposicaoRequestDTO;
import com.centraldasbebidas.pdv_backend.model.Operador;
import com.centraldasbebidas.pdv_backend.model.ReposicaoEstoque;
import com.centraldasbebidas.pdv_backend.repository.ReposicaoEstoqueRepository;
import com.centraldasbebidas.pdv_backend.service.ReposicaoEstoqueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reposicoes")
public class ReposicaoEstoqueController {

    @Autowired
    private ReposicaoEstoqueService service;

    @Autowired
    private ReposicaoEstoqueRepository repository;

    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody ReposicaoRequestDTO dto) {
        try {
            ReposicaoEstoque reposicao = service.registrarReposicao(dto, getOperadorLogado());
            return ResponseEntity.status(HttpStatus.CREATED).body(reposicao);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public List<ReposicaoEstoque> listarTodas() {
        return repository.findAllByOrderByDataHoraDesc();
    }

    @GetMapping("/produto/{produtoId}")
    public List<ReposicaoEstoque> historicoPorProduto(@PathVariable Long produtoId) {
        return repository.findByProdutoIdOrderByDataHoraDesc(produtoId);
    }

    private Operador getOperadorLogado() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.getPrincipal() instanceof Operador op) ? op : null;
    }
}
