package com.example.compras.controllers;

import com.example.compras.model.Usuario;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class LoginCompras {

    private final JdbcTemplate jdbcTemplate;
    private final HttpServletRequest request;

    public LoginCompras(JdbcTemplate jdbcTemplate, HttpServletRequest request) {
        this.jdbcTemplate = jdbcTemplate;
        this.request = request;
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "HtmlLogin";
    }

    @PostMapping("/login")
    @ResponseBody
    public String login(
            @RequestParam String username,  // Email do formulário
            @RequestParam String password,
            HttpServletResponse response) {

        try {
            // Consulta SQL segura
            String sql = "SELECT nome, email, tipo FROM usuarios WHERE email = ? AND senha = ?";

            Usuario usuario = jdbcTemplate.queryForObject(sql,
                    (rs, rowNum) -> {
                        String tipo = rs.getString("tipo");
                        // Garante que o tipo seja cliente ou vendedor
                        if (tipo == null || tipo.isEmpty()) {
                            tipo = "cliente"; // Valor padrão
                        } else if (tipo.contains(",")) {
                            tipo = tipo.split(",")[0].trim(); // Pega o primeiro tipo se houver múltiplos
                        }

                        return new Usuario(
                                rs.getString("nome"),
                                rs.getString("email"),
                                tipo.toLowerCase() // Garante lowercase
                        );
                    },
                    username, password);

            if (usuario != null) {
                // Configura a sessão
                HttpSession session = request.getSession();
                session.setAttribute("usuario", usuario);
                session.setMaxInactiveInterval(1200); // 20 minutos

                // Configura cookies
                criarCookie(response, "username", usuario.getNome(), 1200);
                criarCookie(response, "tipo", usuario.getTipo(), 1200);
                criarCookie(response, "email", usuario.getEmail(), 1200);

                return "Logado com sucesso como " + usuario.getTipo();
            }
        } catch (EmptyResultDataAccessException e) {
            return "Email ou senha incorretos";
        } catch (Exception e) {
            System.err.println("Erro no login: " + e.getMessage());
            return "Erro durante o login";
        }
        return "Credenciais inválidas";
    }

    private void criarCookie(HttpServletResponse response, String name, String value, int maxAge) {
        try {
            String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
            Cookie cookie = new Cookie(name, encodedValue);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(maxAge);
            response.addCookie(cookie);
        } catch (Exception e) {
            System.err.println("Erro ao criar cookie: " + e.getMessage());
        }
    }
}