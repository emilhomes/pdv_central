package com.centraldasbebidas.pdv_backend.repository;

import com.centraldasbebidas.pdv_backend.model.ItemVenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemVendaRepository extends JpaRepository<ItemVenda, Long> {

}
