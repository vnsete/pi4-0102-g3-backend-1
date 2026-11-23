
# CT19 cliente

| Linha | Valor |
| --- | --- |
| ID e nome | CT19 cliente |
| Fronteira | Banco de dados |
| Quem chama | Backend 1 |
| Quem responde | MySQL 9.2 |
| Atende | E1, E2, E3, E4, E6, E7, E8, E10 |
| Como chamar | Tabela cliente, via JDBC |

## Estrutura da tabela

| Campo | Tipo SQL | Restrição | Descrição |
| --- | --- | --- | --- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Identificador do cliente |
| nome | VARCHAR(160) | NOT NULL | Nome do cliente |
| telefone | VARCHAR(40) | NOT NULL | Telefone do cliente |
| email | VARCHAR(255) | NULL permitido | E-mail opcional |
| criado_em | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Data e hora do cadastro |

## Regras

A tabela `cliente` armazena os dados básicos dos clientes cadastrados no ServiHUB.

O campo `id` é gerado automaticamente pelo banco de dados e identifica cada registro de forma única.

Os campos `nome` e `telefone` são obrigatórios. O campo `email` é opcional.

O Backend 1 valida que nome e telefone não estejam vazios e exige que o telefone contenha 10 ou 11 dígitos numéricos, incluindo o DDD.

O campo `criado_em` é preenchido automaticamente pelo MySQL quando o registro é criado.

A tabela é criada pelo script `db/schema.sql` do repositório principal.

O cadastro utiliza `POST /clientes`, retornando HTTP 201 com o identificador `id` criado.

A consulta utiliza `GET /clientes`, retornando os campos `id`, `nome`, `telefone`, `email` e `criado_em`, ordenados pelo nome.

A tabela `cliente` é referenciada por chaves estrangeiras em `endereco`, `servico` e `orcamento`.

Um cliente pode possuir múltiplos endereços, serviços e orçamentos (relacionamentos 1:N).

Não há restrição UNIQUE para nome, telefone ou e-mail nessa tabela.

## Erros e validações

| Situação | Comportamento |
| --- | --- |
| Nome ou telefone vazio | HTTP 400 — DADOS_OBRIGATORIOS |
| Telefone fora do padrão de 10 ou 11 dígitos | HTTP 400 — mensagem de telefone inválido |
| Falha de persistência ou exceção | HTTP 400 — REQUISICAO_INVALIDA, conforme tratamento geral atual |
| Referência a cliente inexistente no cadastro de endereço ou orçamento | HTTP 404 — CLIENTE_NAO_ENCONTRADO |

## Persistência

Os dados são armazenados no banco relacional MySQL, utilizando JDBC no Backend 1.

A definição da tabela está no arquivo `db/schema.sql` do repositório principal.
