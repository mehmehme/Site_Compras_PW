package com.example.compras.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Cadastro {

    private final JdbcTemplate jdbcTemplate;

    public Cadastro(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<String> cadastrar(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email,
            @RequestParam String tipo,
            HttpServletResponse response) {

        try {
            // Normalização do tipo de usuário
            String tipoNormalizado = "vendedor".equalsIgnoreCase(tipo.trim()) ? "vendedor" : "cliente";

            // Verificação de email existente
            if (emailJaCadastrado(email)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Email já cadastrado");
            }

            // Inserção no banco de dados
            inserirUsuario(username, email, password, tipoNormalizado);

            // Configuração dos cookies
            configurarCookies(response, username, email, tipoNormalizado);

            return ResponseEntity.ok("Cadastrado com sucesso como " + tipoNormalizado);

        } catch (DataIntegrityViolationException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Erro de integridade: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro no servidor: " + e.getMessage());
        }
    }

    private boolean emailJaCadastrado(String email) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, email) > 0;
    }

    private void inserirUsuario(String nome, String email, String senha, String tipo) {
        String sql = "INSERT INTO usuarios (nome, email, senha, tipo) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, nome, email, senha, tipo);
    }

    private void configurarCookies(HttpServletResponse response, String username, String email, String tipo) {
        Cookie[] cookies = {
                criarCookie("username", username, 1200),
                criarCookie("email", email, 1200),
                criarCookie("tipo", tipo, 1200)
        };

        for (Cookie cookie : cookies) {
            response.addCookie(cookie);
        }
    }

    private Cookie criarCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }
}