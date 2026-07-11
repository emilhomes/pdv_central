package com.centraldasbebidas.pdv_backend.repository;

import com.centraldasbebidas.pdv_backend.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    @Query("SELECT COALESCE(SUM(c.saldoDevedor), 0) FROM Cliente c")
    java.math.BigDecimal somarTotalFiado();

    // Nota: a query de faturamento por período que estava aqui foi movida
    // para VendaRepository.somarFaturamentoDesde(...), local mais correto
    // já que ela consulta a entidade Venda, não Cliente.
}
