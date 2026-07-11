package com.centraldasbebidas.pdv_backend.config;

import com.centraldasbebidas.pdv_backend.model.Operador;
import com.centraldasbebidas.pdv_backend.repository.OperadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Roda uma vez ao ligar o backend. Se ainda não existir NENHUM operador
 * cadastrado (ex.: primeira vez que o login está sendo ativado no
 * projeto), cria um operador ADMIN padrão para você conseguir entrar e
 * cadastrar os operadores de verdade pela tela de Operadores.
 *
 * IMPORTANTE: troque a senha (ou crie outro admin e desative este) assim
 * que possível — login "admin" / senha "admin123" é só pra destravar o
 * primeiro acesso.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private OperadorRepository operadorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (operadorRepository.count() == 0) {
            Operador admin = new Operador();
            admin.setNome("Administrador");
            admin.setLogin("admin");
            admin.setSenha(passwordEncoder.encode("admin123"));
            admin.setPapel("ADMIN");
            admin.setAtivo(true);
            operadorRepository.save(admin);

            System.out.println("=====================================================");
            System.out.println("Operador ADMIN padrão criado: login=admin  senha=admin123");
            System.out.println("IMPORTANTE: troque essa senha assim que possível!");
            System.out.println("=====================================================");
        }
    }
}
