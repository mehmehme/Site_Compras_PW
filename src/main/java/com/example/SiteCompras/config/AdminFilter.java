package com.example.SiteCompras.config;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/admin/*") // Ou rotas específicas como "/admin/lojistas/*"
public class AdminFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpSession session = ((HttpServletRequest) request).getSession(false);

        // Verifica se é um lojista E admin
        if (session == null ||
                !"lojista".equals(session.getAttribute("tipo")) ||
                !Boolean.TRUE.equals(session.getAttribute("is_admin"))) {
            ((HttpServletResponse) response).sendRedirect("/admin/produtos?erro=acesso");
            return;
        }
        chain.doFilter(request, response);
    }
}