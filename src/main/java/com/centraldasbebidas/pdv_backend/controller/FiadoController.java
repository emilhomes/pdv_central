package com.centraldasbebidas.pdv_backend.controller;

import com.centraldasbebidas.pdv_backend.dto.PagamentoDTO;
import com.centraldasbebidas.pdv_backend.model.MovimentacaoFiado;
import com.centraldasbebidas.pdv_backend.service.FiadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fiado")
public class FiadoController {

    @Autowired
    private FiadoService fiadoService;

    @PostMapping("/cliente/{clienteId}/pagar")
    public ResponseEntity<?> pagar(@PathVariable Long clienteId, @RequestBody PagamentoDTO request) {
        try {
            MovimentacaoFiado comprovante = fiadoService.receberPagamentoFiado(
                    clienteId,
                    request.getValorPago(),
                    "Recebimento em caixa: " + request.getMetodoPagamento()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(comprovante);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/cliente/{clienteId}/extrato")
    public List<MovimentacaoFiado> extrairHistorico(@PathVariable Long clienteId) {
        return fiadoService.obterExtrato(clienteId);
    }

}
