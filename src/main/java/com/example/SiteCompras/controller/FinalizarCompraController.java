package com.example.SiteCompras.controller;

import com.example.SiteCompras.config.DatabaseConfig;
import com.example.SiteCompras.model.Produto;
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
import java.util.List;

@Controller
public class FinalizarCompraController {

    @GetMapping("/finalizarCompra")
    public void finalizarCompra(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);

        // Verifica se há sessão e carrinho
        if (session == null || session.getAttribute("carrinho") == null) {
            response.sendRedirect("/listaProdutos");
            return;
        }

        List<Produto> carrinho = (List<Produto>) session.getAttribute("carrinho");

        double total = 0;
        Connection conn = null;

        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false); // Inicia transação

            // 1. Verificação de estoque e cálculo do total
            for (Produto produto : carrinho) {
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT preco, quantidade FROM produtos WHERE id = ? FOR UPDATE")) {
                    pstmt.setInt(1, produto.getId());
                    ResultSet rs = pstmt.executeQuery();

                    if (rs.next()) {
                        if (rs.getInt("quantidade") <= 0) {
                            conn.rollback();
                            response.sendRedirect("/verCarrinho?erro=estoque");
                            return;
                        }
                        total += rs.getDouble("preco");
                    } else {
                        conn.rollback();
                        response.sendRedirect("/verCarrinho?erro=produto");
                        return;
                    }
                }
            }

            // 2. Atualização de estoque
            for (Produto produto : carrinho) {
                try (PreparedStatement pstmt = conn.prepareStatement(
                        "UPDATE produtos SET quantidade = quantidade - 1 WHERE id = ?")) {
                    pstmt.setInt(1, produto.getId());
                    int affectedRows = pstmt.executeUpdate();

                    if (affectedRows == 0) {
                        conn.rollback();
                        response.sendRedirect("/verCarrinho?erro=atualizacao");
                        return;
                    }
                }
            }


            conn.commit(); // Confirma a transação
            session.removeAttribute("carrinho");

            // 3. Gera resposta HTML
            response.setContentType("text/html");
            var writer = response.getWriter();
            writer.println("<html><head><title>Compra Finalizada</title></head>");
            writer.println("<body>");
            writer.println("<h1>Compra Finalizada com Sucesso!</h1>");
            writer.println("<p>Total: R$ " + String.format("%.2f", total) + "</p>");
            writer.println("<a href='/listaProdutos'>Voltar aos Produtos</a>");
            writer.println("<div style='margin-top: 20px;'>");
            writer.println("<a href='/logout'>Logout</a>"); // Botão de logout
            writer.println("</div>");

            writer.println("</body></html>");
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            response.sendRedirect("/verCarrinho?erro=banco");
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}