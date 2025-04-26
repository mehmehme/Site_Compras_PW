package com.example.SiteCompras.controller;

import com.example.SiteCompras.config.DatabaseConfig;
import com.example.SiteCompras.model.Produto;
import com.example.SiteCompras.model.Lojista;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Controller
public class AdminController {

    @GetMapping("/admin/produtos")
    public void listarProdutosAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !"lojista".equals(session.getAttribute("tipo"))) {
            response.sendRedirect("/login");
            return;
        }

        response.setContentType("text/html");
        var writer = response.getWriter();
        writer.println("<h1>Painel Administrativo</h1>");

        boolean isAdmin = Boolean.TRUE.equals(session.getAttribute("is_admin"));

        // Lista de produtos do banco de dados
        List<Produto> produtos = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM produtos");
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Produto produto = new Produto(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("descricao"),
                        rs.getDouble("preco"),
                        rs.getInt("quantidade")
                );
                produtos.add(produto);
            }
        } catch (SQLException e) {
            writer.println("<p style='color:red;'>Erro ao carregar produtos!</p>");
            e.printStackTrace();
        }

        writer.println("<h2>Produtos Cadastrados</h2>");
        writer.println("<ul>");
        produtos.forEach(p ->
                writer.println("<li>" + p.getNome() +
                        " (Estoque: " + p.getQuantidade() +
                        ") - Preço: R$ " + p.getPreco() + "</li>")
        );
        writer.println("</ul>");

        writer.println("<a href='/admin/novo-produto'>Cadastrar Novo Produto</a><br>");

        // Mostra opções de admin apenas para administradores
        if (isAdmin) {
            writer.println("<a href='/admin/novo-lojista'>Cadastrar Novo Lojista</a><br>");
            writer.println("<a href='/admin/lojistas'>Listar Lojistas</a><br>");
        }

        writer.println("<a href='/logout'>Sair</a>");
    }

    @GetMapping("/admin/novo-produto")
    public void formularioNovoProduto(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !"lojista".equals(session.getAttribute("tipo"))) {
            response.sendRedirect("/login");
            return;
        }

        response.setContentType("text/html");
        var writer = response.getWriter();

        if ("1".equals(request.getParameter("erro"))) {
            writer.println("<p style='color:red;'>Preencha todos os campos!</p>");
        }

        writer.println("<h1>Cadastrar Produto</h1>");
        writer.println("<form action='/admin/produtos' method='POST'>");
        writer.println("Nome: <input type='text' name='nome' required><br>");
        writer.println("Descrição: <input type='text' name='descricao'><br>");
        writer.println("Preço: <input type='number' step='0.01' name='preco' required><br>");
        writer.println("Estoque: <input type='number' name='quantidade' required><br>");
        writer.println("<button type='submit'>Salvar</button>");
        writer.println("</form>");
        writer.println("<a href='/admin/produtos'>Voltar</a>");
    }

    @PostMapping("/admin/produtos")
    public void cadastrarProduto(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !"lojista".equals(session.getAttribute("tipo"))) {
            response.sendRedirect("/login");
            return;
        }

        String nome = request.getParameter("nome");
        String descricao = request.getParameter("descricao");
        String precoStr = request.getParameter("preco");
        String quantidadeStr = request.getParameter("quantidade");

        if (nome == null || nome.isEmpty() || precoStr == null || quantidadeStr == null) {
            response.sendRedirect("/admin/novo-produto?erro=1");
            return;
        }

        try {
            double preco = Double.parseDouble(precoStr);
            int quantidade = Integer.parseInt(quantidadeStr);

            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(
                         "INSERT INTO produtos (nome, descricao, preco, quantidade) VALUES (?, ?, ?, ?)")) {

                pstmt.setString(1, nome);
                pstmt.setString(2, descricao != null ? descricao : "");
                pstmt.setDouble(3, preco);
                pstmt.setInt(4, quantidade);
                pstmt.executeUpdate();

                response.sendRedirect("/admin/produtos");
            } catch (SQLException e) {
                e.printStackTrace();
                response.sendRedirect("/admin/novo-produto?erro=1");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect("/admin/novo-produto?erro=1");
        }
    }

    @GetMapping("/admin/novo-lojista")
    public void formularioNovoLojista(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (!isAdmin(session)) {
            response.sendRedirect("/admin/produtos");
            return;
        }

        response.setContentType("text/html");
        var writer = response.getWriter();

        String erro = request.getParameter("erro");
        if ("email".equals(erro)) {
            writer.println("<p style='color:red;'>Email já cadastrado!</p>");
        } else if ("campos".equals(erro)) {
            writer.println("<p style='color:red;'>Preencha todos os campos!</p>");
        }

        writer.println("<h1>Cadastrar Lojista</h1>");
        writer.println("<form action='/admin/cadastrar-lojista' method='POST'>");
        writer.println("Nome: <input type='text' name='nome' required><br>");
        writer.println("Email: <input type='email' name='email' required><br>");
        writer.println("Senha: <input type='password' name='senha' required><br>");
        writer.println("Admin: <input type='checkbox' name='is_admin'><br>");
        writer.println("<button type='submit'>Salvar</button>");
        writer.println("</form>");
        writer.println("<a href='/admin/produtos'>Voltar</a>");
    }

    @GetMapping("/admin/lojistas")
    public void listarLojistas(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (!isAdmin(session)) {
            response.sendRedirect("/admin/produtos");
            return;
        }

        response.setContentType("text/html");
        var writer = response.getWriter();
        writer.println("<h1>Lojistas Cadastrados</h1>");

        List<Lojista> lojistas = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM lojistas");
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Lojista lojista = new Lojista(
                        rs.getString("nome"),
                        rs.getString("email"),
                        rs.getString("senha"),
                        rs.getBoolean("is_admin")
                );
                lojista.setId(rs.getLong("id"));
                lojistas.add(lojista);
            }
        } catch (SQLException e) {
            writer.println("<p style='color:red;'>Erro ao carregar lojistas!</p>");
            e.printStackTrace();
        }

        writer.println("<table border='1'>");
        writer.println("<tr><th>Nome</th><th>Email</th><th>Tipo</th></tr>");
        lojistas.forEach(l -> {
            writer.println("<tr>");
            writer.println("<td>" + l.getNome() + "</td>");
            writer.println("<td>" + l.getEmail() + "</td>");
            writer.println("<td>" + (l.isAdmin() ? "Administrador" : "Lojista") + "</td>");
            writer.println("</tr>");
        });
        writer.println("</table>");

        writer.println("<a href='/admin/novo-lojista'>Cadastrar Novo Lojista</a><br>");
        writer.println("<a href='/admin/produtos'>Voltar</a>");
    }

    @PostMapping("/admin/cadastrar-lojista")
    public void cadastrarLojista(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (!isAdmin(session)) {
            response.sendRedirect("/admin/produtos");
            return;
        }

        String nome = request.getParameter("nome");
        String email = request.getParameter("email");
        String senha = request.getParameter("senha");
        boolean isAdmin = "on".equals(request.getParameter("is_admin"));

        if (nome == null || email == null || senha == null) {
            response.sendRedirect("/admin/novo-lojista?erro=campos");
            return;
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            // Verifica se email já existe
            String sqlCheck = "SELECT email FROM lojistas WHERE email = ?";
            try (PreparedStatement pstmtCheck = conn.prepareStatement(sqlCheck)) {
                pstmtCheck.setString(1, email);
                ResultSet rs = pstmtCheck.executeQuery();
                if (rs.next()) {
                    response.sendRedirect("/admin/novo-lojista?erro=email");
                    return;
                }
            }

            // Insere novo lojista
            String sqlInsert = "INSERT INTO lojistas (nome, email, senha, is_admin) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsert)) {
                pstmtInsert.setString(1, nome);
                pstmtInsert.setString(2, email);
                pstmtInsert.setString(3, senha);
                pstmtInsert.setBoolean(4, isAdmin);
                pstmtInsert.executeUpdate();
            }

            response.sendRedirect("/admin/lojistas?sucesso=1");

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("/admin/novo-lojista?erro=banco");
        }
    }

    // Método auxiliar para verificar se é admin
    private boolean isAdmin(HttpSession session) {
        return session != null &&
                "lojista".equals(session.getAttribute("tipo")) &&
                Boolean.TRUE.equals(session.getAttribute("is_admin"));
    }
}