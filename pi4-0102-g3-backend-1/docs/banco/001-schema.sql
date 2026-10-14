-- ServiHUB: modelo de dados do MVP, contratos CT19 a CT24.
-- Executar em um banco vazio criado para a aplicação.
-- Requer MySQL com InnoDB e CHECK efetivamente aplicado.
-- A aplicação deve manter timezone UTC em toda conexão.
SET time_zone = '+00:00';

CREATE TABLE cliente (
 id INT NOT NULL AUTO_INCREMENT,
 nome VARCHAR(120) NOT NULL,
 telefone VARCHAR(15) NOT NULL,
 email VARCHAR(254) NULL,
 criado_em DATETIME(6) NOT NULL,
 PRIMARY KEY (id),
 CONSTRAINT ck_cliente_nome CHECK (CHAR_LENGTH(TRIM(nome)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE endereco (
 id INT NOT NULL AUTO_INCREMENT,
 cliente_id INT NOT NULL,
 logradouro VARCHAR(120) NOT NULL,
 numero VARCHAR(20) NOT NULL,
 complemento VARCHAR(120) NULL,
 bairro VARCHAR(120) NOT NULL,
 cidade VARCHAR(120) NOT NULL,
 uf CHAR(2) NOT NULL,
 cep CHAR(8) NOT NULL,
 criado_em DATETIME(6) NOT NULL,
 PRIMARY KEY (id),
 UNIQUE KEY uk_endereco_cliente (id, cliente_id),
 CONSTRAINT fk_endereco_cliente FOREIGN KEY (cliente_id)
  REFERENCES cliente(id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE servico (
 id INT NOT NULL AUTO_INCREMENT,
 cliente_id INT NOT NULL,
 endereco_id INT NULL,
 descricao VARCHAR(500) NOT NULL,
 valor_previsto DECIMAL(12,2) NULL,
 status ENUM('registrado','agendado','em_andamento','concluido','cancelado') NOT NULL,
 criado_em DATETIME(6) NOT NULL,
 atualizado_em DATETIME(6) NOT NULL,
 PRIMARY KEY (id),
 UNIQUE KEY uk_servico_cliente (id, cliente_id),
 KEY ix_servico_historico (cliente_id, criado_em, id),
 CONSTRAINT fk_servico_cliente FOREIGN KEY (cliente_id)
  REFERENCES cliente(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 CONSTRAINT fk_servico_endereco_cliente FOREIGN KEY (endereco_id, cliente_id)
  REFERENCES endereco(id, cliente_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 CONSTRAINT ck_servico_descricao CHECK (CHAR_LENGTH(TRIM(descricao)) > 0),
 CONSTRAINT ck_servico_valor CHECK (valor_previsto IS NULL OR valor_previsto > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE agendamento (
 id INT NOT NULL AUTO_INCREMENT,
 servico_id INT NOT NULL,
 inicio_em DATETIME(6) NOT NULL,
 fim_em DATETIME(6) NOT NULL,
 criado_em DATETIME(6) NOT NULL,
 atualizado_em DATETIME(6) NOT NULL,
 PRIMARY KEY (id),
 UNIQUE KEY uk_agendamento_servico (servico_id),
 KEY ix_agendamento_inicio (inicio_em, id),
 CONSTRAINT fk_agendamento_servico FOREIGN KEY (servico_id)
  REFERENCES servico(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 CONSTRAINT ck_agendamento_intervalo CHECK (fim_em > inicio_em)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE orcamento (
 id INT NOT NULL AUTO_INCREMENT,
 cliente_id INT NOT NULL,
 servico_id INT NULL,
 descricao VARCHAR(500) NOT NULL,
 valor_total DECIMAL(12,2) NOT NULL,
 criado_em DATETIME(6) NOT NULL,
 PRIMARY KEY (id),
 KEY ix_orcamento_cliente (cliente_id, criado_em, id),
 CONSTRAINT fk_orcamento_cliente FOREIGN KEY (cliente_id)
  REFERENCES cliente(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 CONSTRAINT fk_orcamento_servico_cliente FOREIGN KEY (servico_id, cliente_id)
  REFERENCES servico(id, cliente_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 CONSTRAINT ck_orcamento_descricao CHECK (CHAR_LENGTH(TRIM(descricao)) > 0),
 CONSTRAINT ck_orcamento_valor CHECK (valor_total > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE pagamento (
 id INT NOT NULL AUTO_INCREMENT,
 servico_id INT NOT NULL,
 valor DECIMAL(12,2) NOT NULL,
 situacao ENUM('pendente','pago') NOT NULL,
 pago_em DATETIME(6) NULL,
 atualizado_em DATETIME(6) NOT NULL,
 PRIMARY KEY (id),
 UNIQUE KEY uk_pagamento_servico (servico_id),
 KEY ix_pagamento_pendencia (situacao, atualizado_em, id),
 CONSTRAINT fk_pagamento_servico FOREIGN KEY (servico_id)
  REFERENCES servico(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
 CONSTRAINT ck_pagamento_valor CHECK (valor > 0),
 CONSTRAINT ck_pagamento_data CHECK (
  (situacao = 'pago' AND pago_em IS NOT NULL) OR
  (situacao = 'pendente' AND pago_em IS NULL)
 )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- CHECK e FKs não substituem CT9: a API deve serializar o teste de
-- sobreposição de horários e gravar agendamento/status na mesma transação.
