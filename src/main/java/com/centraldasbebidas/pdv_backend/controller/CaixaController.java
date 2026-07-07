package com.centraldasbebidas.pdv_backend.controller;

import com.centraldasbebidas.pdv_backend.model.Caixa;
import com.centraldasbebidas.pdv_backend.repository.CaixaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/caixa")
public class CaixaController {

    @Autowired
    private CaixaRepository repository;

    //Abrir o caixa
    @PostMapping("/abrir")
    public ResponseEntity<?> abrirCaixa(@RequestBody Caixa novoCaixa) {
        // Validação: Não permite abrir um novo caixa se já existir um aberto
        Optional<Caixa> caixaAberto = repository.findByStatus("ABERTO");
        if (caixaAberto.isPresent()) {
            return ResponseEntity.badRequest().body("Já existe um caixa aberto no sistema!");
        }

        novoCaixa.setDataAbertura(LocalDateTime.now());
        novoCaixa.setStatus("ABERTO");
        novoCaixa.setSaldoFinal(null);
        novoCaixa.setDataFechamento(null);

        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(novoCaixa));
    }

    //Fechar caixa
    @PutMapping("/fechar")
    public ResponseEntity<?> fecharCaixa(@RequestBody Caixa dadosFechamento) {
        Optional<Caixa> caixaAberto = repository.findByStatus("ABERTO");

        if (caixaAberto.isPresent()) {
            Caixa caixaAtivo = caixaAberto.get();
            caixaAtivo.setDataFechamento(LocalDateTime.now());
            caixaAtivo.setStatus("FECHADO");
            caixaAtivo.setSaldoFinal(dadosFechamento.getSaldoFinal()); // Valor contado no cofre/gaveta

            return ResponseEntity.ok(repository.save(caixaAtivo));
        } else {
            return ResponseEntity.badRequest().body("Nenhum caixa aberto encontrado para fechamento.");
        }
    }

    //Listar histórico de caixas
    @GetMapping
    public List<Caixa> listarTodos() {
        return repository.findAll();
    }

}
