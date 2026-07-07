package com.centraldasbebidas.pdv_backend.repository;

import com.centraldasbebidas.pdv_backend.model.MovimentacaoFiado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimentacaoFiadoRepository extends JpaRepository<MovimentacaoFiado, Long> {

    List<MovimentacaoFiado> findByClienteIdOrderByDataMovimentacaoDesc(Long clienteId);

}
