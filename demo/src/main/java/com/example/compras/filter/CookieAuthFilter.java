package com.example.compras.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.Filter;

import java.io.IOException;

@WebFilter(urlPatterns = "/api/*")
public class CookieAuthFilter implements Filter{
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        Cookie[] cookies = req.getCookies();
        boolean autorizar = false;
        String tipoUsuario = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if("username".equals(cookie.getName()) &&
                        "admin".equals(cookie.getValue())) {
                    autorizar = true;
                    break;
                }
                if ("tipo".equals(cookie.getName())) {
                    tipoUsuario = cookie.getValue(); // cliente ou vendedor
                }
            }
            //não autorizado
            if(!autorizar){
                res.sendRedirect(req.getContextPath() + "/login");
                return;
            }
            //autorizado
            String path = req.getRequestURI();
            if (path.equals("/api") || path.equals("/api/")) {
                if ("cliente".equals(tipoUsuario)) {
                    res.sendRedirect(req.getContextPath() + "/compras");
                } else if ("vendedor".equals(tipoUsuario)) {
                    res.sendRedirect(req.getContextPath() + "/vendas");
                } else {
                    res.sendRedirect(req.getContextPath() + "/login"); // caso seja um valor inválido
                }
                return;
            }


            chain.doFilter(request, response);
        }
    }
}
