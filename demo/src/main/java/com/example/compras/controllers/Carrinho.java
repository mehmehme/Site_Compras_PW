package com.example.compras.controllers;
import com.example.compras.model.Produto;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/carrinho")
public class Carrinho {
    @PostMapping("/adicionar")
    public List<Produto> adicionarProduto(@RequestParam int id, HttpSession session) {
        List<Produto> carrinho = (List<Produto>) session.getAttribute("carrinho");
        if (carrinho == null) {
            carrinho = new ArrayList<>();
        }

        // Simula uma lista de produtos já existente no sistema
        List<Produto> produtos = List.of(
                new Produto(1, "Camiseta", 29.90),
                new Produto(2, "Tênis", 149.90),
                new Produto(3, "Boné", 49.90)
        );

        produtos.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .ifPresent(carrinho::add);

        session.setAttribute("carrinho", carrinho);
        return carrinho;
    }

    @PostMapping("/remover")
    public List<Produto> removerProduto(@RequestParam int id, HttpSession session) {
        List<Produto> carrinho = (List<Produto>) session.getAttribute("carrinho");
        if (carrinho == null) {
            return new ArrayList<>();
        }

        carrinho.removeIf(p -> p.getId() == id);
        session.setAttribute("carrinho", carrinho);
        return carrinho;
    }

    @GetMapping
    public List<Produto> verCarrinho(HttpSession session) {
        List<Produto> carrinho = (List<Produto>) session.getAttribute("carrinho");
        return carrinho != null ? carrinho : new ArrayList<>();
    }

    @PostMapping("/finalizar")
    public String finalizarCompra(HttpSession session) {
        session.removeAttribute("carrinho");
        return "Compra finalizada com sucesso!";
    }
}
