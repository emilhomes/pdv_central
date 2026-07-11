package com.centraldasbebidas.pdv_backend.security;

import com.centraldasbebidas.pdv_backend.model.Operador;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Geração e validação de tokens JWT para o login de operadores.
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretBase64;

    // 12 horas — dá pra cobrir um turno inteiro sem precisar logar de novo.
    private static final long EXPIRACAO_MS = 1000L * 60 * 60 * 12;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretBase64));
    }

    public String gerarToken(Operador operador) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + EXPIRACAO_MS);

        return Jwts.builder()
                .setSubject(operador.getLogin())     // Ajustado para v0.11.5
                .claim("id", operador.getId())
                .claim("nome", operador.getNome())
                .claim("papel", operador.getPapel())
                .setIssuedAt(agora)                  // Ajustado para v0.11.5
                .setExpiration(expiracao)            // Ajustado para v0.11.5
                .signWith(getKey())
                .compact();
    }

    /**
     * Valida a assinatura/expiração do token e devolve os claims.
     * Lança exceção (JwtException) se o token for inválido ou expirado.
     */
    public Claims validarEExtrairClaims(String token) {
        return Jwts.parserBuilder()                  // Ajustado de parser() para parserBuilder()
                .setSigningKey(getKey())             // Ajustado de verifyWith() para setSigningKey()
                .build()
                .parseClaimsJws(token)               // Ajustado para parseClaimsJws
                .getBody();                          // Ajustado de getPayload() para getBody()
    }
}