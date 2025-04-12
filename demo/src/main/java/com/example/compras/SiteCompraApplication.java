package com.example.compras;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class SiteCompraApplication {

	public static void main(String[] args) {
		SpringApplication.run
				(SiteCompraApplication.class, args);
	}
}
