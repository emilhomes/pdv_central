package com.centraldasbebidas.pdv_backend.service;

import com.centraldasbebidas.pdv_backend.dto.ReposicaoRequestDTO;
import com.centraldasbebidas.pdv_backend.model.Fornecedor;
import com.centraldasbebidas.pdv_backend.model.Operador;
import com.centraldasbebidas.pdv_backend.model.Produto;
import com.centraldasbebidas.pdv_backend.model.ReposicaoEstoque;
import com.centraldasbebidas.pdv_backend.repository.FornecedorRepository;
import com.centraldasbebidas.pdv_backend.repository.ProdutoRepository;
import com.centraldasbebidas.pdv_backend.repository.ReposicaoEstoqueRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class ReposicaoEstoqueService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Autowired
    private ReposicaoEstoqueRepository reposicaoEstoqueRepository;

    @Transactional
    public ReposicaoEstoque registrarReposicao(ReposicaoRequestDTO dto, Operador operadorLogado) {
        if (dto.getQuantidade() == null || dto.getQuantidade() <= 0) {
            throw new RuntimeException("Quantidade precisa ser maior que zero.");
        }
        if (dto.getCustoUnitario() == null || dto.getCustoUnitario().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Informe um custo unitário válido.");
        }

        Produto produto = produtoRepository.findById(dto.getProdutoId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado!"));

        Fornecedor fornecedor = null;
        if (dto.getFornecedorId() != null) {
            fornecedor = fornecedorRepository.findById(dto.getFornecedorId())
                    .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado!"));
        }

        int estoqueAtual = produto.getQuantidadeEstoque() != null ? produto.getQuantidadeEstoque() : 0;
        BigDecimal custoAtual = produto.getCusto() != null ? produto.getCusto() : BigDecimal.ZERO;

        // Custo médio ponderado: (estoque atual x custo atual + quantidade
        // entrando x custo da entrada) / novo estoque total. Isso evita que
        // o preço de custo "salte" pro valor da última compra quando o
        // fornecedor muda de preço — reflete melhor o custo real do que
        // ainda está no estoque físico.
        int novoEstoque = estoqueAtual + dto.getQuantidade();
        BigDecimal valorEstoqueAtual = custoAtual.multiply(BigDecimal.valueOf(estoqueAtual));
        BigDecimal valorEntrada = dto.getCustoUnitario().multiply(BigDecimal.valueOf(dto.getQuantidade()));
        BigDecimal novoCustoMedio = novoEstoque > 0
                ? valorEstoqueAtual.add(valorEntrada).divide(BigDecimal.valueOf(novoEstoque), 2, RoundingMode.HALF_UP)
                : custoAtual;

        produto.setQuantidadeEstoque(novoEstoque);
        produto.setCusto(novoCustoMedio);
        produtoRepository.save(produto);

        ReposicaoEstoque reposicao = new ReposicaoEstoque();
        reposicao.setProduto(produto);
        reposicao.setFornecedor(fornecedor);
        reposicao.setOperador(operadorLogado);
        reposicao.setQuantidade(dto.getQuantidade());
        reposicao.setCustoUnitario(dto.getCustoUnitario());
        reposicao.setDataHora(LocalDateTime.now());
        reposicao.setObservacoes(dto.getObservacoes());

        return reposicaoEstoqueRepository.save(reposicao);
    }
}
