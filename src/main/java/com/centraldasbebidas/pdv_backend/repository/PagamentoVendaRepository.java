package com.centraldasbebidas.pdv_backend.repository;

import com.centraldasbebidas.pdv_backend.model.PagamentoVenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PagamentoVendaRepository extends JpaRepository<PagamentoVenda, Long> {
}
