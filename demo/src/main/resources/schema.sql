-- Criação das tabelas se não existirem
CREATE TABLE IF NOT EXISTS usuarios (
                                        id SERIAL PRIMARY KEY,
                                        nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(100) NOT NULL,
    tipo VARCHAR(20) NOT NULL
    );

CREATE TABLE IF NOT EXISTS produtos (
                                        id SERIAL PRIMARY KEY,
                                        nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    preco DECIMAL(10, 2) NOT NULL,
    quantidade INT NOT NULL
    );

-- Inserir dados iniciais apenas se as tabelas estiverem vazias
DO $$
BEGIN
    -- Verifica se a tabela usuarios está vazia
    IF NOT EXISTS (SELECT 1 FROM usuarios LIMIT 1) THEN
        INSERT INTO usuarios (nome, email, senha, tipo) VALUES
        ('João Pedro', 'jp2017@uol.com.br', '12345jaum', 'cliente'),
        ('Amara Silva', 'amarasil@bol.com.br', 'amara82', 'cliente'),
        ('Maria Pereira', 'mariape@terra.com.br', '145aektm', 'cliente'),
        ('Taniro Rodrigues', 'tanirocr@gmail.com', '123456abc', 'vendedor'),
        ('Lorena Silva', 'lore_sil@yahoo.com.br', '12uhuuu@', 'vendedor');
END IF;

    -- Verifica se a tabela produtos está vazia
    IF NOT EXISTS (SELECT 1 FROM produtos LIMIT 1) THEN
        INSERT INTO produtos (nome, descricao, preco, quantidade) VALUES
        ('Mesa', 'Uma mesa de computador', 500.00, 10),
        ('Lápis', 'Lápis B2 grafite', 2.00, 50),
        ('Computador', 'Computador I5 16Gb de RAM', 1500.00, 2);
END IF;
END $$;