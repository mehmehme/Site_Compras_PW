package com.example.SiteCompras;

import com.example.SiteCompras.config.DatabaseInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

import javax.annotation.PostConstruct;

@SpringBootApplication
@ServletComponentScan
public class SiteComprasApplication {

	public static void main(String[] args) {
		SpringApplication.run(SiteComprasApplication.class, args);
	}

	@PostConstruct
	public void init() {
		// Inicializa o banco de dados quando a aplicação começar
		DatabaseInitializer.initialize();
	}
}