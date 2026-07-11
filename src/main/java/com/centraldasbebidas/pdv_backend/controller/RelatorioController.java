package com.centraldasbebidas.pdv_backend.controller;

import com.centraldasbebidas.pdv_backend.dto.RelatorioDashboardDTO;
import com.centraldasbebidas.pdv_backend.service.RelatorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/relatorios")
public class RelatorioController {

    @Autowired
    private RelatorioService relatorioService;

    // Mesma rota de antes (GET /relatorios/dashboard) — só o conteúdo do
    // retorno ficou mais completo. Não precisa mudar nada no app que já
    // consome esse endpoint além do parsing do novo formato.
    @GetMapping("/dashboard")
    public RelatorioDashboardDTO obterDadosDashboard() {
        return relatorioService.gerarDashboard();
    }
}
