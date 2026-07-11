package com.centraldasbebidas.pdv_backend.controller;

import com.centraldasbebidas.pdv_backend.dto.FornecedorRequestDTO;
import com.centraldasbebidas.pdv_backend.model.Fornecedor;
import com.centraldasbebidas.pdv_backend.repository.FornecedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fornecedores")
public class FornecedorController {

    @Autowired
    private FornecedorRepository repository;

    @GetMapping
    public List<Fornecedor> listar() {
        return repository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody FornecedorRequestDTO dto) {
        if (dto.getNome() == null || dto.getNome().isBlank()) {
            return ResponseEntity.badRequest().body("Nome do fornecedor é obrigatório.");
        }
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setNome(dto.getNome());
        fornecedor.setTelefone(dto.getTelefone());
        fornecedor.setEmail(dto.getEmail());
        fornecedor.setObservacoes(dto.getObservacoes());
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(fornecedor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody FornecedorRequestDTO dto) {
        return repository.findById(id).map(f -> {
            f.setNome(dto.getNome());
            f.setTelefone(dto.getTelefone());
            f.setEmail(dto.getEmail());
            f.setObservacoes(dto.getObservacoes());
            return ResponseEntity.ok(repository.save(f));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        try {
            repository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            // Provavelmente há reposições de estoque vinculadas a este
            // fornecedor (restrição de integridade referencial).
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Não é possível excluir: este fornecedor tem reposições de estoque registradas.");
        }
    }
}
