package com.centraldasbebidas.pdv_backend.repository;

import com.centraldasbebidas.pdv_backend.model.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Long> {

}
