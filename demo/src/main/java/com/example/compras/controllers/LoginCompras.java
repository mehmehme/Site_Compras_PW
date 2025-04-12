package com.example.compras.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginCompras {
    @PostMapping("/login")
    public ResponseEntity <String> login(@RequestParam String username,
                                         @RequestParam String password,
                                         HttpServletResponse response){
        if(username.equals("admin") && password.equals("1234")){
            //adicione banco de dados
            Cookie cookie = new Cookie("username", username);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(3600);

            response.addCookie(cookie);
            return ResponseEntity.ok("Logado com sucesso");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Tente novamente ou crie uma conta");
    }
}
