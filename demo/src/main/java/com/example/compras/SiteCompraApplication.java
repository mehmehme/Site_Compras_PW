package com.example.compras;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import javax.sql.DataSource;
import java.sql.Connection;
import org.springframework.boot.CommandLineRunner;

@SpringBootApplication
@ServletComponentScan
public class SiteCompraApplication {

	public static void main(String[] args) {
		SpringApplication.run(SiteCompraApplication.class, args);
	}

	@Bean
	CommandLineRunner testDatabaseConnection(DataSource dataSource) {
		return args -> {
			try (Connection connection = dataSource.getConnection()) {
				System.out.println("✅ Conexão com o banco de dados estabelecida com sucesso!");
				System.out.println("🔗 URL: " + connection.getMetaData().getURL());
				System.out.println("👤 Usuário: " + connection.getMetaData().getUserName());
			} catch (Exception e) {
				System.err.println("❌ Falha ao conectar ao banco de dados:");
				e.printStackTrace();
			}
		};
	}
}