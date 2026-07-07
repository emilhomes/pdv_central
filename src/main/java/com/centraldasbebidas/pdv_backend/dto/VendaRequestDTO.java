package com.centraldasbebidas.pdv_backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class VendaRequestDTO {
    private Long clienteId;
    private List<ItemVendaDTO> itens;
    private List<PagamentoDTO> pagamentos;
}
