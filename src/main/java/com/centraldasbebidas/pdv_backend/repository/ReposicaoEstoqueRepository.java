package com.centraldasbebidas.pdv_backend.repository;

import com.centraldasbebidas.pdv_backend.model.ReposicaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReposicaoEstoqueRepository extends JpaRepository<ReposicaoEstoque, Long> {
    List<ReposicaoEstoque> findByProdutoIdOrderByDataHoraDesc(Long produtoId);
    List<ReposicaoEstoque> findAllByOrderByDataHoraDesc();
}
