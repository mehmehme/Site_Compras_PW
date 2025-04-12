package com.example.compras.controllers;

import com.example.compras.model.Produto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@SessionAttributes({"carrinho", "produtos"})
public class Compras {
    private List<Produto> produtos = new ArrayList<>();
    private int idCounter = 4;

    public Compras() {
        produtos.add(new Produto(1, "Camiseta", 29.90,1));
        produtos.add(new Produto(2, "Tênis", 149.90,3));
        produtos.add(new Produto(3, "Boné", 49.90,4));
    }

    @GetMapping("/produtos")
    public List<Produto> listarProdutos() {
        return produtos;
    }

    @PostMapping("/produtos/adicionar")
    public ResponseEntity<?> adicionarProduto(@RequestBody Produto novoProduto) {
        novoProduto.setId(idCounter++);
        produtos.add(novoProduto);
        return ResponseEntity.ok("Produto adicionado");
    }

    @PostMapping("/carrinho/add")
    public List<Produto> adicionarAoCarrinho(@RequestParam int id, @SessionAttribute("carrinho") List<Produto> carrinho) {
        produtos.stream().filter(p -> p.getId() == id).findFirst().ifPresent(carrinho::add);
        return carrinho;
    }

    @PostMapping("/carrinho/remover")
    public List<Produto> removerDoCarrinho(@RequestParam int id, @SessionAttribute("carrinho") List<Produto> carrinho) {
        carrinho.removeIf(p -> p.getId() == id);
        return carrinho;
    }

    @ModelAttribute("carrinho")
    public List<Produto> carrinho() {
        return new ArrayList<>();
    }

    @ModelAttribute("produtos")
    public List<Produto> getProdutos() {
        return produtos;
    }
}
