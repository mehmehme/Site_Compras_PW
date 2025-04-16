package com.example.compras.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Cadastro {
    @PostMapping("/cadastro")
    public ResponseEntity<String> login(@RequestParam String username,
                                        @RequestParam String password,
                                        @RequestParam String email,
                                        HttpServletResponse response){
        if(!(username.equals("admin") &&
                password.equals("1234") &&
                email.equals("admin@gmail.com"))){

            Cookie cookie = new Cookie("username", username);
            cookie.setHttpOnly(true);
            cookie.setPath("/login");
            cookie.setMaxAge(3600);

            //deve aqui adicionar os novos dados ao banco
            response.addCookie(cookie);
            return ResponseEntity.ok("Cadastrado com sucesso");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Algo deu errado");
    }
}

