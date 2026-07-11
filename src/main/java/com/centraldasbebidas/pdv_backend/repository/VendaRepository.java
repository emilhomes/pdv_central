package com.centraldasbebidas.pdv_backend.repository;

import com.centraldasbebidas.pdv_backend.model.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Long> {

    List<Venda> findByClienteIdOrderByDataHoraDesc(Long clienteId);

    // Soma o valor total de vendas concluídas a partir de uma data/hora.
    @Query("SELECT COALESCE(SUM(v.valorTotal), 0) FROM Venda v " +
           "WHERE v.dataHora >= :inicio AND v.status = 'CONCLUIDA'")
    BigDecimal somarFaturamentoDesde(@Param("inicio") LocalDateTime inicio);

    // Soma o valor total de vendas concluídas dentro de um intervalo
    // [inicio, fim) — usada para comparar com períodos passados fechados
    // (ex.: faturamento de ontem, faturamento do mês anterior).
    @Query("SELECT COALESCE(SUM(v.valorTotal), 0) FROM Venda v " +
           "WHERE v.dataHora >= :inicio AND v.dataHora < :fim AND v.status = 'CONCLUIDA'")
    BigDecimal somarFaturamentoEntre(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    // Conta quantas vendas concluídas ocorreram a partir de uma data/hora.
    @Query("SELECT COUNT(v) FROM Venda v WHERE v.dataHora >= :inicio AND v.status = 'CONCLUIDA'")
    Long contarVendasDesde(@Param("inicio") LocalDateTime inicio);

    // Lista de vendas concluídas desde uma data — usada para montar o
    // gráfico de faturamento por dia (agrupamento é feito em memória no service).
    List<Venda> findByDataHoraGreaterThanEqualAndStatusOrderByDataHoraAsc(
            LocalDateTime inicio, String status);
}
