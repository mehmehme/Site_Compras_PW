package com.example.SiteCompras.controller;

import com.example.SiteCompras.config.DatabaseConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ProdutoController {

    @GetMapping("/listaProdutos")
    public void listarProdutos(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        var writer = response.getWriter();
        writer.println("<html><head><title>Lista de Produtos</title></head><body>");
        writer.println("<h1>Produtos Disponíveis</h1>");

        // Mensagem de erro de estoque
        if ("estoque".equals(request.getParameter("erro"))) {
            writer.println("<p style='color: red;'>Produto sem estoque disponível!</p>");
        }

        writer.println("<table border='1'>");
        writer.println("<tr><th>Nome</th><th>Descrição</th><th>Preço</th><th>Estoque</th><th>Ação</th></tr>");

        // Lista todos os produtos do banco de dados via JDBC
        List<Produto> produtos = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM produtos");
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Produto produto = new Produto();
                produto.setId(rs.getInt("id"));
                produto.setNome(rs.getString("nome"));
                produto.setDescricao(rs.getString("descricao"));
                produto.setPreco(rs.getDouble("preco"));
                produto.setQuantidade(rs.getInt("quantidade"));
                produtos.add(produto);
            }
        } catch (SQLException e) {
            writer.println("<p style='color: red;'>Erro ao carregar produtos</p>");
            e.printStackTrace();
        }

        // Exibe os produtos na tabela
        for (Produto produto : produtos) {
            writer.println("<tr>");
            writer.println("<td>" + produto.getNome() + "</td>");
            writer.println("<td>" + produto.getDescricao() + "</td>");
            writer.println("<td>R$ " + produto.getPreco() + "</td>");
            writer.println("<td>" + produto.getQuantidade() + "</td>");

            if (produto.getQuantidade() > 0) {
                writer.println("<td><a href='/carrinho?comando=add&id=" + produto.getId() + "'>Adicionar</a></td>");
            } else {
                writer.println("<td style='color: gray;'>Esgotado</td>");
            }

            writer.println("</tr>");
        }

        writer.println("</table>");

        // Mostra quantidade de itens no carrinho
        HttpSession session = request.getSession(false);
        int itensNoCarrinho = 0;
        if (session != null && session.getAttribute("carrinho") != null) {
            itensNoCarrinho = ((List<?>) session.getAttribute("carrinho")).size();
        }
        writer.println("<p><a href='/verCarrinho'>Ver Carrinho (" + itensNoCarrinho + " itens)</a></p>");

        // Link para área administrativa (se for lojista)
        if (session != null && "lojista".equals(session.getAttribute("tipo"))) {
            writer.println("<p><a href='/admin/produtos'>Área Administrativa</a></p>");
        }

        writer.println("<div style='margin-top: 20px;'>");
        writer.println("<a href='/logout'>Logout</a>"); // Botão de logout
        writer.println("</div>");

        writer.println("</body></html>");
    }

    // Classe interna para representar o produto (pode ser movida para model se preferir)
    private static class Produto {
        private int id;
        private String nome;
        private String descricao;
        private double preco;
        private int quantidade;

        // Getters e Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }
        public double getPreco() { return preco; }
        public void setPreco(double preco) { this.preco = preco; }
        public int getQuantidade() { return quantidade; }
        public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    }
}