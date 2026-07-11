package com.centraldasbebidas.pdv_backend.repository;

import com.centraldasbebidas.pdv_backend.model.PagamentoVenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PagamentoVendaRepository extends JpaRepository<PagamentoVenda, Long> {

    List<PagamentoVenda> findByVendaCaixaId(Long caixaId);

    // Pagamentos de vendas concluídas desde uma data — usado para montar o
    // resumo "vendas por forma de pagamento".
    @Query("SELECT p FROM PagamentoVenda p JOIN FETCH p.venda v " +
           "WHERE v.dataHora >= :inicio AND v.status = 'CONCLUIDA'")
    List<PagamentoVenda> buscarPagamentosDesde(@Param("inicio") LocalDateTime inicio);
}
