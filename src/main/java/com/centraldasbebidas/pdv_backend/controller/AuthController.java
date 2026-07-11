package com.centraldasbebidas.pdv_backend.controller;

import com.centraldasbebidas.pdv_backend.dto.LoginRequestDTO;
import com.centraldasbebidas.pdv_backend.dto.LoginResponseDTO;
import com.centraldasbebidas.pdv_backend.model.Operador;
import com.centraldasbebidas.pdv_backend.repository.OperadorRepository;
import com.centraldasbebidas.pdv_backend.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private OperadorRepository operadorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        Optional<Operador> operadorOpt = operadorRepository.findByLogin(request.getLogin());

        boolean credenciaisValidas = operadorOpt.isPresent()
                && operadorOpt.get().isAtivo()
                && passwordEncoder.matches(request.getSenha(), operadorOpt.get().getSenha());

        if (!credenciaisValidas) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login ou senha inválidos.");
        }

        Operador operador = operadorOpt.get();
        String token = jwtService.gerarToken(operador);

        return ResponseEntity.ok(new LoginResponseDTO(
                token, operador.getId(), operador.getNome(), operador.getLogin(), operador.getPapel()));
    }
}
