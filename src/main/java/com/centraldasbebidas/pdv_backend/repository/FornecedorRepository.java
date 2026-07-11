package com.centraldasbebidas.pdv_backend.repository;

import com.centraldasbebidas.pdv_backend.model.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {
}
