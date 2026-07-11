package com.centraldasbebidas.pdv_backend.repository;

import com.centraldasbebidas.pdv_backend.model.ItemVenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ItemVendaRepository extends JpaRepository<ItemVenda, Long> {

    // Busca os itens vendidos desde uma data, já trazendo o Produto junto
    // (JOIN FETCH) para evitar consultas extras (N+1) ao calcular o lucro
    // e os produtos mais vendidos.
    @Query("SELECT iv FROM ItemVenda iv JOIN FETCH iv.produto JOIN FETCH iv.venda v " +
           "WHERE v.dataHora >= :inicio AND v.status = 'CONCLUIDA'")
    List<ItemVenda> buscarItensVendidosDesde(@Param("inicio") LocalDateTime inicio);

    // Itens vendidos dentro de um intervalo [inicio, fim) — usada para
    // calcular o lucro de períodos passados fechados (ex.: lucro de ontem,
    // lucro do mês anterior), já que esses períodos podem ficar fora da
    // janela "desde o início do ano" em casos de virada de ano/mês.
    @Query("SELECT iv FROM ItemVenda iv JOIN FETCH iv.produto JOIN FETCH iv.venda v " +
           "WHERE v.dataHora >= :inicio AND v.dataHora < :fim AND v.status = 'CONCLUIDA'")
    List<ItemVenda> buscarItensVendidosEntre(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}
