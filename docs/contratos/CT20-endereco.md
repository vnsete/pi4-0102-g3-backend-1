
# CT20 endereco

| Linha | Valor |
| --- | --- |
| ID e nome | CT20 endereco |
| Fronteira | Banco de dados |
| Quem chama | Backend 1 |
| Quem responde | MySQL 9.2 |
| Atende | E2, E4 |
| Como chamar | Tabela endereco, via JDBC |

## Estrutura da tabela

| Campo | Tipo SQL | Restrição | Descrição |
| --- | --- | --- | --- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Identificador do endereço |
| cliente_id | BIGINT | NOT NULL, FOREIGN KEY | Cliente proprietário do endereço |
| logradouro | VARCHAR(200) | NOT NULL | Nome da rua ou avenida |
| numero | VARCHAR(30) | NULL permitido | Número do imóvel |
| complemento | VARCHAR(120) | NULL permitido | Complemento |
| bairro | VARCHAR(120) | NULL permitido | Bairro |
| cidade | VARCHAR(120) | NULL permitido | Cidade |
| estado | VARCHAR(2) | NULL permitido | Sigla do estado |
| cep | VARCHAR(12) | NULL permitido | CEP |

## Regras

A tabela `endereco` armazena os endereços vinculados aos clientes cadastrados no ServiHUB.

O campo `id` é gerado automaticamente pelo MySQL.

O campo `cliente_id` é uma chave estrangeira que referencia `cliente.id`.

Um cliente pode possuir vários endereços, estabelecendo um relacionamento 1:N entre `cliente` e `endereco`.

Os campos obrigatórios no banco são `cliente_id` e `logradouro`. Os demais campos de endereço permitem valores nulos.

O Backend 1 verifica se o cliente existe antes de cadastrar ou consultar seus endereços.

No cadastro, o Backend 1 exige que o campo `logradouro` não esteja vazio.

O cadastro utiliza `POST /clientes/{id}/enderecos`, com o identificador do cliente informado na rota.

O identificador `cliente_id` é preenchido pelo Backend 1 com base no parâmetro da rota, independentemente de um valor enviado no corpo da requisição.

Após cadastrar o endereço, a API retorna HTTP 201 com o identificador `id` criado.

A consulta utiliza `GET /clientes/{id}/enderecos`, retornando os endereços vinculados ao cliente.

A tabela `servico` pode referenciar um endereço por meio da chave estrangeira `endereco_id`.

A tabela é criada pelo script `db/schema.sql` do repositório principal.

## Erros e validações

| Situação | Comportamento |
| --- | --- |
| Cliente inexistente | HTTP 404 — CLIENTE_NAO_ENCONTRADO |
| Logradouro vazio | HTTP 400 — LOGRADOURO_OBRIGATORIO |
| Sessão inválida, sem autorização interna | HTTP 401 — SESSAO_INVALIDA |
| Falha de persistência ou exceção | HTTP 400 — REQUISICAO_INVALIDA, conforme tratamento geral atual |

## Persistência

Os registros são armazenados no MySQL por meio de JDBC, utilizado pelo Backend 1.

A definição da tabela está no arquivo `db/schema.sql` do repositório principal.
