package com.centraldasbebidas.pdv_backend.repository;

import com.centraldasbebidas.pdv_backend.model.PagamentoVenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagamentoVendaRepository extends JpaRepository<PagamentoVenda, Long> {
    List<PagamentoVenda> findByVendaCaixaId(Long caixaId);
}
