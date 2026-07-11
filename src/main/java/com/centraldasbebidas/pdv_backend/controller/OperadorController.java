package com.centraldasbebidas.pdv_backend.controller;

import com.centraldasbebidas.pdv_backend.dto.OperadorRequestDTO;
import com.centraldasbebidas.pdv_backend.model.Operador;
import com.centraldasbebidas.pdv_backend.repository.OperadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/operadores")
public class OperadorController {

    @Autowired
    private OperadorRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Qualquer operador logado pode ver a lista (útil pra exibir "quem
    // vendeu" em relatórios, por exemplo).
    @GetMapping
    public List<Operador> listar() {
        return repository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody OperadorRequestDTO dto) {
        if (!isAdminLogado()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas administradores podem cadastrar operadores.");
        }
        if (dto.getLogin() == null || dto.getLogin().isBlank() || dto.getSenha() == null || dto.getSenha().isBlank()) {
            return ResponseEntity.badRequest().body("Login e senha são obrigatórios.");
        }
        if (repository.findByLogin(dto.getLogin()).isPresent()) {
            return ResponseEntity.badRequest().body("Já existe um operador com esse login.");
        }

        Operador operador = new Operador();
        operador.setNome(dto.getNome());
        operador.setLogin(dto.getLogin());
        operador.setSenha(passwordEncoder.encode(dto.getSenha()));
        operador.setPapel(dto.getPapel() != null && !dto.getPapel().isBlank() ? dto.getPapel().toUpperCase() : "OPERADOR");
        operador.setAtivo(true);

        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(operador));
    }

    // "Excluir" um operador de verdade quebraria o histórico de vendas
    // ligadas a ele — por isso só desativamos (soft delete). Ele deixa de
    // conseguir logar, mas o histórico continua íntegro.
    @PutMapping("/{id}/desativar")
    public ResponseEntity<?> desativar(@PathVariable Long id) {
        if (!isAdminLogado()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas administradores podem desativar operadores.");
        }
        return repository.findById(id).map(op -> {
            op.setAtivo(false);
            return ResponseEntity.ok(repository.save(op));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/reativar")
    public ResponseEntity<?> reativar(@PathVariable Long id) {
        if (!isAdminLogado()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Apenas administradores podem reativar operadores.");
        }
        return repository.findById(id).map(op -> {
            op.setAtivo(true);
            return ResponseEntity.ok(repository.save(op));
        }).orElse(ResponseEntity.notFound().build());
    }

    private boolean isAdminLogado() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Operador logado)) return false;
        return "ADMIN".equalsIgnoreCase(logado.getPapel());
    }
}
