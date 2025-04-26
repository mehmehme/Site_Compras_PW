package com.example.SiteCompras.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        // Aguarda o banco estar pronto (especialmente importante com Docker)
        waitForDatabase();

        String createClientesTable = """
            CREATE TABLE IF NOT EXISTS clientes (
                id SERIAL PRIMARY KEY,
                nome VARCHAR(100) NOT NULL,
                email VARCHAR(100) UNIQUE NOT NULL,
                senha VARCHAR(100) NOT NULL
            )""";

        String createLojistasTable = """
            CREATE TABLE IF NOT EXISTS lojistas (
                id SERIAL PRIMARY KEY,
                nome VARCHAR(100) NOT NULL,
                email VARCHAR(100) UNIQUE NOT NULL,
                senha VARCHAR(100) NOT NULL,
                is_admin BOOLEAN DEFAULT FALSE
            )""";

        String createProdutosTable = """
            CREATE TABLE IF NOT EXISTS produtos (
                id SERIAL PRIMARY KEY,
                nome VARCHAR(100) NOT NULL,
                descricao TEXT,
                preco DECIMAL(10,2) NOT NULL,
                quantidade INTEGER NOT NULL
            )""";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            // Cria tabelas
            stmt.execute(createClientesTable);
            stmt.execute(createLojistasTable);
            stmt.execute(createProdutosTable);

            // Insere dados iniciais
            insertInitialData(conn);

            System.out.println("Banco de dados inicializado com sucesso!");

        } catch (SQLException e) {
            System.err.println("Erro ao inicializar banco de dados: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void waitForDatabase() {
        int maxAttempts = 10;
        int attempt = 0;
        while (attempt < maxAttempts) {
            try (Connection conn = DatabaseConfig.getConnection()) {
                System.out.println("Conexão com o banco de dados estabelecida com sucesso!");
                return;
            } catch (SQLException e) {
                attempt++;
                System.out.println("Aguardando banco de dados... Tentativa " + attempt + " de " + maxAttempts);
                try {
                    Thread.sleep(5000); // Espera 5 segundos entre tentativas
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupção durante a espera pelo banco de dados", ie);
                }
            }
        }
        throw new RuntimeException("Não foi possível conectar ao banco de dados após " + maxAttempts + " tentativas");
    }

    private static void insertInitialData(Connection conn) throws SQLException {
        // Verifica se já existem dados para não duplicar
        if (hasData(conn, "clientes")) {
            System.out.println("Dados já existem no banco. Pulando inserção inicial.");
            return;
        }

        System.out.println("Inserindo dados iniciais...");

        // Clientes (Tabela 1 do trabalho)
        String insertClientes = "INSERT INTO clientes (nome, email, senha) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertClientes)) {
            // Cliente 1
            pstmt.setString(1, "João Pedro");
            pstmt.setString(2, "jp2017@uol.com.br");
            pstmt.setString(3, "12345jaum");
            pstmt.executeUpdate();

            // Cliente 2
            pstmt.setString(1, "Amara Silva");
            pstmt.setString(2, "amarasil@bol.com.br");
            pstmt.setString(3, "amara82");
            pstmt.executeUpdate();

            // Cliente 3
            pstmt.setString(1, "Maria Pereira");
            pstmt.setString(2, "mariape@terra.com.br");
            pstmt.setString(3, "145aektm");
            pstmt.executeUpdate();

            System.out.println("Clientes inseridos com sucesso!");
        }

        // Lojistas (Tabela 2 do trabalho)
        String insertLojistas = "INSERT INTO lojistas (nome, email, senha, is_admin) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertLojistas)) {
            // Lojista 1
            pstmt.setString(1, "Taniro Rodrigues");
            pstmt.setString(2, "tanirocr@gmail.com");
            pstmt.setString(3, "123456abc");
            pstmt.setBoolean(4, false);
            pstmt.executeUpdate();

            // Lojista 2
            pstmt.setString(1, "Lorena Silva");
            pstmt.setString(2, "lore_sil@yahoo.com.br");
            pstmt.setString(3, "12uhuuu@");
            pstmt.setBoolean(4, false);
            pstmt.executeUpdate();

            // ADMIN
            pstmt.setString(1, "admin");
            pstmt.setString(2, "admin@gmail.com");
            pstmt.setString(3, "admin");
            pstmt.setBoolean(4, true);
            pstmt.executeUpdate();

            System.out.println("Lojistas inseridos com sucesso!");
        }

        // Produtos (Tabela 3 do trabalho)
        String insertProdutos = "INSERT INTO produtos (nome, descricao, preco, quantidade) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertProdutos)) {
            // Produto 1 - Mesa
            pstmt.setString(1, "Mesa");
            pstmt.setString(2, "Uma mesa de computador");
            pstmt.setDouble(3, 500.0);
            pstmt.setInt(4, 10);
            pstmt.executeUpdate();

            // Produto 2 - Lápis
            pstmt.setString(1, "Lápis");
            pstmt.setString(2, "Lápis B2 grafite");
            pstmt.setDouble(3, 2.0);
            pstmt.setInt(4, 50);
            pstmt.executeUpdate();

            // Produto 3 - Computador
            pstmt.setString(1, "Computador");
            pstmt.setString(2, "Computador I5 16GB de RAM");
            pstmt.setDouble(3, 1500.0);
            pstmt.setInt(4, 2);
            pstmt.executeUpdate();

            System.out.println("Produtos inseridos com sucesso!");
        }
    }

    private static boolean hasData(Connection conn, String table) throws SQLException {
        String query = "SELECT COUNT(*) FROM " + table;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            rs.next();
            return rs.getInt(1) > 0;
        }
    }
}