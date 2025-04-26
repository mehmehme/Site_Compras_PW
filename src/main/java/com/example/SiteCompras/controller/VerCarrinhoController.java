package com.example.SiteCompras.controller;

import com.example.SiteCompras.model.Produto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@Controller
public class VerCarrinhoController {

    @GetMapping("/verCarrinho")
    public void verCarrinho(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("carrinho") == null) {
            response.sendRedirect("/listaProdutos");
            return;
        }

        response.setContentType("text/html;charset=UTF-8"); // melhor definir o charset
        try (PrintWriter writer = response.getWriter()) { // usa try-with-resources aqui
            writer.println("<html><head><title>Carrinho de Compras</title></head>");
            writer.println("<body>");
            writer.println("<h1>Carrinho de Compras</h1>");

            Object carrinhoObj = session.getAttribute("carrinho");
            if (!(carrinhoObj instanceof List<?> produtosList)) {
                response.sendRedirect("/listaProdutos"); // segurança extra caso não seja uma lista
                return;
            }

            double total = 0.0;

            writer.println("<table border='1' cellpadding='5'>");
            writer.println("<tr><th>Produto</th><th>Preço Unitário</th><th>Ação</th></tr>");

            for (Object obj : produtosList) {
                if (!(obj instanceof Produto produto)) continue;

                writer.println("<tr>");
                writer.println("<td>" + produto.getNome() + "</td>");
                writer.println("<td>R$ " + String.format("%.2f", produto.getPreco()) + "</td>");
                writer.println("<td><a href='/carrinho?comando=remove&id=" + produto.getId() + "'>Remover</a></td>");
                writer.println("</tr>");

                total += produto.getPreco();
            }

            writer.println("</table>");
            writer.println("<p><strong>Total: R$ " + String.format("%.2f", total) + "</strong></p>");

            writer.println("<div style='margin-top: 20px;'>");
            writer.println("<a href='/listaProdutos'>Continuar Comprando</a> | ");
            writer.println("<a href='/finalizarCompra'>Finalizar Compra</a> | ");
            writer.println("<a href='/logout'>Logout</a>"); // Novo botão
            writer.println("</div>");

            writer.println("</body></html>");
        }
    }
}
