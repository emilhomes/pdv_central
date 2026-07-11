package com.centraldasbebidas.pdv_backend.repository;

import com.centraldasbebidas.pdv_backend.model.PagamentoFiado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PagamentoFiadoRepository extends JpaRepository<PagamentoFiado, Long> {
    List<PagamentoFiado> findByClienteId(Long clienteId);
}