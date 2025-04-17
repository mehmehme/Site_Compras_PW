package com.example.compras.controllers;

import com.example.compras.model.Produto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@SessionAttributes("carrinho")
public class Compras {

    private final JdbcTemplate jdbcTemplate;

    public Compras(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Listar produtos
    @GetMapping("/produtos")
    public List<Produto> listarProdutos() {
        try {
            String sql = "SELECT id, nome, descricao, preco, quantidade FROM produtos";
            return jdbcTemplate.query(sql, new ProdutoRowMapper());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Adicionar produto ao carrinho
    @PostMapping("/carrinho/add")
    public List<Produto> adicionarAoCarrinho(
            @RequestParam int id,
            @SessionAttribute("carrinho") List<Produto> carrinho
    ) {
        try {
            String sql = "SELECT id, nome, descricao, preco, quantidade FROM produtos WHERE id = ?";
            Produto produto = jdbcTemplate.queryForObject(sql, new Object[]{id}, new ProdutoRowMapper());

            if (produto != null && produto.getQuantidade() > 0) {
                carrinho.add(produto);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return carrinho;
    }

    // Remover produto do carrinho
    @PostMapping("/carrinho/remover")
    public List<Produto> removerDoCarrinho(
            @RequestParam int id,
            @SessionAttribute("carrinho") List<Produto> carrinho
    ) {
        carrinho.removeIf(p -> p.getId() == id);
        return carrinho;
    }

    // Finalizar compra (atualiza estoque)
    @PostMapping("/carrinho/finalizar")
    public ResponseEntity<String> finalizarCompra(@SessionAttribute("carrinho") List<Produto> carrinho) {
        try {
            for (Produto produto : carrinho) {
                String sql = "UPDATE produtos SET quantidade = quantidade - 1 WHERE id = ?";
                jdbcTemplate.update(sql, produto.getId());
            }
            carrinho.clear();
            return ResponseEntity.ok("Compra finalizada!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao finalizar compra");
        }
    }

    // Inicializa o carrinho na sessão
    @ModelAttribute("carrinho")
    public List<Produto> carrinho() {
        return new ArrayList<>();
    }

    // Mapeador de resultados SQL para objetos Produto
    private static class ProdutoRowMapper implements RowMapper<Produto> {
        @Override
        public Produto mapRow(ResultSet rs, int rowNum) throws SQLException {
            Produto produto = new Produto();
            produto.setId(rs.getInt("id"));
            produto.setNome(rs.getString("nome"));
            produto.setDescricao(rs.getString("descricao"));
            produto.setPreco(rs.getDouble("preco"));
            produto.setQuantidade(rs.getInt("quantidade"));
            return produto;
        }
    }
}