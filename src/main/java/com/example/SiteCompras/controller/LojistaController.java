package com.example.SiteCompras.controller;

import com.example.SiteCompras.config.DatabaseConfig;
import com.example.SiteCompras.model.Produto;
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
public class LojistaController {

    // Página inicial do lojista
    @GetMapping("/lojista/home")
    public void homeLojista(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !"lojista".equals(session.getAttribute("tipo"))) {
            response.sendRedirect("/login");
            return;
        }

        response.setContentType("text/html");
        var writer = response.getWriter();
        writer.println("<html><head><title>Painel do Lojista</title></head><body>");
        writer.println("<h1>Bem-vindo, Lojista!</h1>");

        // Link para listar produtos
        writer.println("<a href='/lojista/produtos'>Gerenciar Produtos</a><br>");
        writer.println("<a href='/logout'>Sair</a>");
        writer.println("</body></html>");
    }

    // Listar produtos (versão lojista)
    @GetMapping("/lojista/produtos")
    public void listarProdutosLojista(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !"lojista".equals(session.getAttribute("tipo"))) {
            response.sendRedirect("/login");
            return;
        }

        response.setContentType("text/html");
        var writer = response.getWriter();
        writer.println("<html><head><title>Produtos</title></head><body>");
        writer.println("<h1>Produtos Disponíveis</h1>");

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

        // Tabela de produtos
        writer.println("<table border='1'>");
        writer.println("<tr><th>Nome</th><th>Descrição</th><th>Preço</th><th>Estoque</th></tr>");
        produtos.forEach(p -> {
            writer.println("<tr>");
            writer.println("<td>" + p.getNome() + "</td>");
            writer.println("<td>" + p.getDescricao() + "</td>");
            writer.println("<td>R$ " + p.getPreco() + "</td>");
            writer.println("<td>" + p.getQuantidade() + "</td>");
            writer.println("</tr>");
        });
        writer.println("</table>");

        // Link para cadastrar novo produto
        writer.println("<br><a href='/lojista/novo-produto'>Cadastrar Novo Produto</a>");
        writer.println("<br><a href='/lojista/home'>Voltar</a>");
        writer.println("</body></html>");
    }

    // Formulário para cadastrar produto (versão lojista)
    @GetMapping("/lojista/novo-produto")
    public void formularioNovoProdutoLojista(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || !"lojista".equals(session.getAttribute("tipo"))) {
            response.sendRedirect("/login");
            return;
        }

        response.setContentType("text/html");
        var writer = response.getWriter();
        writer.println("<html><head><title>Novo Produto</title></head><body>");
        writer.println("<h1>Cadastrar Novo Produto</h1>");

        if ("1".equals(request.getParameter("erro"))) {
            writer.println("<p style='color:red;'>Preencha todos os campos obrigatórios!</p>");
        }

        writer.println("<form action='/lojista/produtos' method='POST'>");
        writer.println("Nome: <input type='text' name='nome' required><br>");
        writer.println("Descrição: <input type='text' name='descricao'><br>");
        writer.println("Preço: <input type='number' step='0.01' name='preco' required><br>");
        writer.println("Estoque: <input type='number' name='quantidade' required><br>");
        writer.println("<button type='submit'>Salvar</button>");
        writer.println("</form>");
        writer.println("<a href='/lojista/produtos'>Voltar</a>");
        writer.println("</body></html>");
    }

    // Processar cadastro de produto (versão lojista)
    @PostMapping("/lojista/produtos")
    public void cadastrarProdutoLojista(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
            response.sendRedirect("/lojista/novo-produto?erro=1");
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

                response.sendRedirect("/lojista/produtos");
            } catch (SQLException e) {
                e.printStackTrace();
                response.sendRedirect("/lojista/novo-produto?erro=1");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect("/lojista/novo-produto?erro=1");
        }
    }
}