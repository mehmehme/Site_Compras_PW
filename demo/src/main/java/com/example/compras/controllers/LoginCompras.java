package com.example.compras.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class LoginCompras {

    // Novo método para exibir a página HTML
    @GetMapping("/login")
    public String showLoginPage() {
        return "HtmlLogin"; // Retorna o template HtmlLogin.html
    }

    // Mantém o método original de login (apenas muda a anotação para @ResponseBody)
    @PostMapping("/login")
    @ResponseBody
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpServletResponse response) {
        if(username.equals("admin") && password.equals("1234")) {
            Cookie cookie = new Cookie("username", username);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(3600);
            response.addCookie(cookie);
            return "Logado com sucesso";
        }
        return "Tente novamente ou crie uma conta";
    }
}