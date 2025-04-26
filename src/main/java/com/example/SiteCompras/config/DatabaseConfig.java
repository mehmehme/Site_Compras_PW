package com.example.SiteCompras.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    // Configurações para conexão com o container Docker
    private static final String DOCKER_URL = "jdbc:postgresql://sitecompras-db:5432/sitecompras";
    private static final String LOCAL_URL = "jdbc:postgresql://localhost:5432/sitecompras";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    // Tempo máximo de espera por conexão (30 segundos)
    private static final int CONNECTION_TIMEOUT = 30;

    public static Connection getConnection() throws SQLException {
        // Tenta primeiro conectar via Docker (nome do serviço)
        Connection conn = tryConnect(DOCKER_URL);
        if (conn != null) return conn;

        // Se falhar, tenta via localhost (para desenvolvimento sem Docker)
        conn = tryConnect(LOCAL_URL);
        if (conn != null) return conn;

        throw new SQLException("Não foi possível estabelecer conexão com o banco de dados");
    }

    private static Connection tryConnect(String url) {
        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(url, USER, PASSWORD);
            if (conn != null && conn.isValid(CONNECTION_TIMEOUT)) {
                System.out.println("Conexão estabelecida com: " + url);
                return conn;
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC não encontrado: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Falha ao conectar em " + url + ": " + e.getMessage());
        }
        return null;
    }

    // Método auxiliar para testar a conexão
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && conn.isValid(CONNECTION_TIMEOUT);
        } catch (SQLException e) {
            return false;
        }
    }
}