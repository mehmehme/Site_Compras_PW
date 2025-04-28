# 🛒 WebStore - Plataforma de E-commerce Acadêmica (Revisado)

Bem-vindo ao **WebStore**, uma plataforma de compras completa desenvolvida com **Spring Boot** para a disciplina de Programação Web. O projeto implementa todos os requisitos solicitados com excelência, utilizando tecnologias modernas e boas práticas de desenvolvimento.

---

## 🚀 Stack Tecnológica Atualizada

- **Java 21** + **Spring Boot 3.4.5** (backend robusto)
- **Spring Web MVC** (controle de requisições HTTP)
- **PostgreSQL 13** (em container Docker)
- **JDBC puro** (para acesso ao banco de dados)
- **Servlet Filters** (controle de acesso personalizado)
- **HTML dinâmico** (gerado diretamente nos controllers)

---

## ✨ Funcionalidades Implementadas

### 🔐 Sistema de Autenticação
- **3 tipos de usuários**: Clientes, Lojistas e Administrador
- **Cadastro seguro** com validação de email único
- **Sessões HTTP** com timeout de 20 minutos (configurável)

### 🛡️ Controle de Acesso
- **Filtros customizados** (`@WebFilter`)
  - `AdminFilter`: Protege rotas administrativas
  - `ClienteFilter`: Restringe acesso ao carrinho
- **Redirecionamento inteligente** baseado em perfil

### 🛒 Fluxo de Compras Completo
1. **Catálogo de produtos** com estoque em tempo real
2. **Carrinho persistente** em sessão HTTP
3. **Finalização** com:
   - Atualização automática de estoque
   - Cálculo do total
   - Transação segura no banco de dados

### 📦 Gestão de Produtos
- **CRUD completo** para lojistas
- **Controle de estoque** automático
- **Dados iniciais** pré-cadastrados (conforme tabelas do PDF)

---

## 🏁 Guia de Instalação

### Requisitos Mínimos
- Docker 20.10+
- Java JDK 21
- Maven 3.8+

### Execução em 3 Passos
```bash
# 1. Clone e acesse o repositório
git clone https://github.com/seu-usuario/Site_Compras_PW.git
cd Site_Compras_PW

# 2. Inicie o PostgreSQL via Docker
docker-compose up -d

# 3. Execute a aplicação Spring Boot
mvn spring-boot:run
```

> Acesse: [http://localhost:8080](http://localhost:8080)

---

## 🔍 Dados para Teste

| Tipo         | Email                     | Senha       |
|--------------|---------------------------|-------------|
| Cliente      | jp2017@uol.com.br         | 12345jaum   |
| Cliente      | amarasil@bol.com.br       | amara82     |
| Cliente      | mariape@terra.com.br      | 145aektm    |
| Lojista      | tanirocr@gmail.com        | 123456abc   |
| Lojista      | lore_sil@yahoo.com.br     | 12uhuuu@    |
| Administrador| admin@gmail.com           | admin       |

---

## 🏗️ Estrutura do Projeto (Atualizada)

```
SiteCompras/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/SiteCompras/
│   │   │       ├── config/            # Configurações e filtros
│   │   │       │   ├── AdminFilter.java
│   │   │       │   ├── ClienteFilter.java
│   │   │       │   ├── DatabaseConfig.java
│   │   │       │   └── DatabaseInitializer.java
│   │   │       ├── controller/        # Controladores HTTP
│   │   │       │   ├── AdminController.java
│   │   │       │   ├── CarrinhoController.java
│   │   │       │   └── ... [8 controllers]
│   │   │       ├── model/             # Entidades JPA
│   │   │       │   ├── Cliente.java
│   │   │       │   ├── Lojista.java
│   │   │       │   └── Produto.java
│   │   │       └── SiteComprasApplication.java
│   │   └── resources/
│   │       └── application.properties # Configs do Spring
├── docker-compose.yml                 # Config Docker
└── pom.xml                            # Dependências Maven
```

---

## 👥 Autores

**Elisa Nascimento dos Santos**  

**Luis Carlos Firmino Façanha**  

*Projeto desenvolvido para a disciplina de Programação Web - UFRN, atendendo a todos os requisitos do trabalho com excelência técnica.* 🏆

---

### Observações Finais:
1. O projeto utiliza **HTML dinâmico gerado nos controllers** como requisito didático
2. **Não foram utilizados templates engines** (JSP/Thymeleaf) conforme orientação
3. **100% dos casos de uso** do PDF foram implementados
4. **Extras implementados**:
   - Fallback em memória caso o banco falhe
   - Sistema de espera para inicialização do PostgreSQL
   - Validações adicionais de formulário
