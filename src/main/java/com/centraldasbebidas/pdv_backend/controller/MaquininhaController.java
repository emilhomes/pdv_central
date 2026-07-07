package com.centraldasbebidas.pdv_backend.controller;

import com.centraldasbebidas.pdv_backend.model.Maquininha;
import com.centraldasbebidas.pdv_backend.repository.MaquininhaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/maquininha")
public class MaquininhaController {

    @Autowired
    private MaquininhaRepository repository;

    //Cadastrar maquininha
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Maquininha salvar(@RequestBody Maquininha maquininha) {
        return repository.save(maquininha);
    }

    //Listar as maquininhas
    @GetMapping
    public List<Maquininha> listar() {
        return repository.findAll();
    }

    //Buscar maquininha
    @GetMapping("/{id}")
    public ResponseEntity<Maquininha> buscarPorId(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Atualizar dados da maquininha
    @PutMapping("/{id}")
    public ResponseEntity<Maquininha> atualizar(@PathVariable Long id, @RequestBody Maquininha maquininhaAtualizada) {
        return repository.findById(id)
                .map(maquininhaExistente -> {
                    maquininhaExistente.setNomeTerminal(maquininhaAtualizada.getNomeTerminal());
                    maquininhaExistente.setAtivo(maquininhaAtualizada.getAtivo());
                    return ResponseEntity.ok(repository.save(maquininhaExistente));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Deletar maquininha
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        return repository.findById(id)
                .map(maquininha -> {
                    repository.delete(maquininha);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
