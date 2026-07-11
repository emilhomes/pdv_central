package com.centraldasbebidas.pdv_backend.controller;

import com.centraldasbebidas.pdv_backend.dto.VendaRequestDTO;
import com.centraldasbebidas.pdv_backend.model.Venda;
import com.centraldasbebidas.pdv_backend.service.VendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/venda")
public class VendaController {

    @Autowired
    private VendaService service;

    @PostMapping
    public ResponseEntity<?> realizarVenda(@RequestBody VendaRequestDTO request) {
        try {
            Venda vendaFinalizada = service.processarVenda(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(vendaFinalizada);
        } catch (RuntimeException e) {
            e.printStackTrace(); // <-- Adicione isto para o Java confessar o problema no terminal!
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}