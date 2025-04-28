package com.example.SiteCompras.controller;

import com.example.SiteCompras.config.DatabaseConfig;
import com.example.SiteCompras.model.Cliente;
import com.example.SiteCompras.model.Lojista;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class LoginController {

    // Listas em memória como fallback (opcional)
    private static final List<Cliente> CLIENTES = new ArrayList<>();
    private static final List<Lojista> LOJISTAS = new ArrayList<>();

    static {
        // Inicializa com dados padrão (Tabelas 1 e 2 do trabalho)
        CLIENTES.add(new Cliente("João Pedro", "jp2017@uol.com.br", "12345jaum"));
        CLIENTES.add(new Cliente("Amara Silva", "amarasil@bol.com.br", "amara82"));
        CLIENTES.add(new Cliente("Maria Pereira", "mariape@terra.com.br", "145aektm"));

        LOJISTAS.add(new Lojista("Taniro Rodrigues", "tanirocr@gmail.com", "123456abc", false));
        LOJISTAS.add(new Lojista("Lorena Silva", "lore_sil@yahoo.com.br", "12uhuuu@", false));
        LOJISTAS.add(new Lojista("Admin", "admin@gmail.com", "admin", true));
    }

    @GetMapping("/login")
    public void mostrarLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        var writer = response.getWriter();
        writer.println("<h1>Login</h1>");

        if ("1".equals(request.getParameter("cadastro"))) {
            writer.println("<p style='color:green;'>Cadastro realizado com sucesso!</p>");
        }

        if ("1".equals(request.getParameter("erro"))) {
            writer.println("<p style='color:red;'>Email ou senha incorretos!</p>");
        }

        writer.println("<form action='/login' method='POST'>");
        writer.println("Email: <input type='text' name='email'><br>");
        writer.println("Senha: <input type='password' name='senha'><br>");
        writer.println("<button type='submit'>Entrar</button>");
        writer.println("<p>Novo usuário? <a href='/cadastro'>Cadastre-se aqui</a></p>");
        writer.println("</form>");
    }

    @GetMapping("/cadastro")
    public void mostrarCadastro(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        var writer = response.getWriter();
        writer.println("<h1>Cadastro de Cliente</h1>");

        String erro = request.getParameter("erro");
        if ("1".equals(erro)) {
            writer.println("<p style='color:red;'>Email já cadastrado!</p>");
        } else if ("2".equals(erro)) {
            writer.println("<p style='color:red;'>Email inválido!</p>");
        } else if ("3".equals(erro)) {
            writer.println("<p style='color:red;'>Erro no banco de dados!</p>");
        }

        writer.println("<form action='/cadastro' method='POST'>");
        writer.println("Nome: <input type='text' name='nome' required><br>");
        writer.println("Email: <input type='text' name='email' required><br>");
        writer.println("Senha: <input type='password' name='senha' required><br>");
        writer.println("<button type='submit'>Cadastrar</button>");
        writer.println("</form>");
    }

    @PostMapping("/cadastro")
    public void processarCadastro(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String nome = request.getParameter("nome");
        String email = request.getParameter("email");
        String senha = request.getParameter("senha");

        // Validação básica
        if (email == null || !email.contains("@")) {
            response.sendRedirect("/cadastro?erro=2");
            return;
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            // Verifica se email já existe no PostgreSQL
            String sqlCheck = "SELECT email FROM clientes WHERE email = ?";
            try (PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheck)) {
                pstmtCheck.setString(1, email);
                ResultSet rs = pstmtCheck.executeQuery();
                if (rs.next()) {
                    response.sendRedirect("/cadastro?erro=1");
                    return;
                }
            }

            // Insere no PostgreSQL
            String sqlInsert = "INSERT INTO clientes (nome, email, senha) VALUES (?, ?, ?)";
            try (PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsert)) {
                pstmtInsert.setString(1, nome);
                pstmtInsert.setString(2, email);
                pstmtInsert.setString(3, senha);
                pstmtInsert.executeUpdate();
            }

            // Opcional: Mantém sincronizado com a lista em memória
            CLIENTES.add(new Cliente(nome, email, senha));

            response.sendRedirect("/login?cadastro=1");

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("/cadastro?erro=3");
        }
    }

    @PostMapping("/login")
    public void processarLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = request.getParameter("email");
        String senha = request.getParameter("senha");

        // Tenta autenticar via PostgreSQL primeiro
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Verifica cliente
            String sqlCliente = "SELECT nome, senha FROM clientes WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlCliente)) {
                pstmt.setString(1, email);
                ResultSet rs = pstmt.executeQuery();

                if  (rs.next() && rs.getString("senha").equals(senha)) {
                    HttpSession session = request.getSession();
                    session.setAttribute("tipo", "cliente");
                    session.setAttribute("usuario", rs.getString("nome"));
                    response.sendRedirect("/listaProdutos");
                    return;
                }
            }

            // Verifica lojista (similar)
            String sqlLojista = "SELECT nome, senha, is_admin FROM lojistas WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlLojista)) {
                pstmt.setString(1, email);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next() && rs.getString("senha").equals(senha)) {
                    HttpSession session = request.getSession();
                    session.setAttribute("tipo", "lojista");
                    session.setAttribute("usuario", rs.getString("nome"));
                    session.setAttribute("is_admin", rs.getBoolean("is_admin"));

                    // Redireciona para /lojista/home se não for admin
                    if (!rs.getBoolean("is_admin")) {
                        response.sendRedirect("/lojista/home");
                        return;
                    }
                    response.sendRedirect("/admin/produtos");
                    return;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Fallback para lista em memória se o banco falhar
            fallbackLogin(email, senha, request, response);
            return;
        }

        response.sendRedirect("/login?erro=1");
    }

    // Método fallback para login em memória (caso o banco falhe)
    private void fallbackLogin(String email, String senha, HttpServletRequest request, HttpServletResponse response) throws IOException {
        for (Cliente c : CLIENTES) {
            if (c.getEmail().equals(email) && c.getSenha().equals(senha)) {
                HttpSession session = request.getSession();
                session.setAttribute("tipo", "cliente");
                session.setAttribute("usuario", c.getNome());
                response.sendRedirect("/listaProdutos");
                return;
            }
        }

        for (Lojista l : LOJISTAS) {
            if (l.getEmail().equals(email) && l.getSenha().equals(senha)) {
                HttpSession session = request.getSession();
                session.setAttribute("tipo", "lojista");
                session.setAttribute("usuario", l.getNome());
                session.setAttribute("is_admin", l.isAdmin()); // Nova linha
                response.sendRedirect("/admin/produtos");
                return;
            }
        }
        response.sendRedirect("/login?erro=1");
    }

    @GetMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute("is_admin");
            session.invalidate();
        }
        response.sendRedirect("/login");
    }

    // Métodos auxiliares (opcionais - mantenha se outros controllers usarem)
    public static List<Cliente> getClientes() {
        return CLIENTES;
    }

    public static List<Lojista> getLojistas() {
        return LOJISTAS;
    }
}