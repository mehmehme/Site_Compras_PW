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
import java.util.ArrayList;
import java.util.List;

@Controller
public class CarrinhoController {

    @GetMapping("/carrinho")
    public void gerenciarCarrinho(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        List<Produto> carrinho = (List<Produto>) session.getAttribute("carrinho");
        if (carrinho == null) {
            carrinho = new ArrayList<>();
        }

        String comando = request.getParameter("comando");
        int idProduto = Integer.parseInt(request.getParameter("id"));

        try (Connection conn = DatabaseConfig.getConnection()) {
            Produto produto = buscarProdutoNoBanco(conn, idProduto);

            if (produto == null) {
                response.sendRedirect("/listaProdutos?erro=produto");
                return;
            }

            if ("add".equals(comando)) {
                int quantidadeNoCarrinho = (int) carrinho.stream()
                        .filter(p -> p.getId() == idProduto)
                        .count();
                if (quantidadeNoCarrinho >= produto.getQuantidade()) {
                    response.sendRedirect("/listaProdutos?erro=estoque");
                    return;
                }
                carrinho.add(new Produto(
                        produto.getId(),
                        produto.getNome(),
                        produto.getDescricao(),
                        produto.getPreco(),
                        1
                ));
            } else if ("remove".equals(comando)) {
                carrinho.removeIf(p -> p.getId() == idProduto);
            }

            session.setAttribute("carrinho", carrinho);
            response.sendRedirect("/listaProdutos");
        } catch (SQLException e) {
            response.sendRedirect("/listaProdutos?erro=banco");
        }
    }

    private Produto buscarProdutoNoBanco(Connection conn, int idProduto) throws SQLException {
        String sql = "SELECT * FROM produtos WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idProduto);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Produto(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("descricao"),
                            rs.getDouble("preco"),
                            rs.getInt("quantidade")
                    );
                }
            }
        }
        return null;
    }
}