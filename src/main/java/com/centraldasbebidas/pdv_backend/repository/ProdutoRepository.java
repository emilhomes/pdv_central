package com.centraldasbebidas.pdv_backend.repository;

import com.centraldasbebidas.pdv_backend.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    // Produtos com estoque abaixo do limite informado (ex.: 5 unidades),
    // ordenados do mais crítico para o menos crítico.
    @Query("SELECT p FROM Produto p WHERE p.quantidadeEstoque < :limite ORDER BY p.quantidadeEstoque ASC")
    List<Produto> buscarEstoqueCritico(@Param("limite") Integer limite);
}
