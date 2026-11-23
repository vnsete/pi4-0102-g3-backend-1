
# CT21 servico

| Linha | Valor |
| --- | --- |
| ID e nome | CT21 servico |
| Fronteira | Banco de dados |
| Quem chama | Backend 1 |
| Quem responde | MySQL 9.2 |
| Atende | E3, E4, E5, E6, E7, E8, E9, E10 |
| Como chamar | Tabela servico, via JDBC |

## Estrutura da tabela

| Campo | Tipo SQL | Restrição | Descrição |
| --- | --- | --- | --- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Identificador do serviço |
| cliente_id | BIGINT | NOT NULL, FOREIGN KEY | Cliente vinculado |
| endereco_id | BIGINT | NULL, FOREIGN KEY | Endereço opcional |
| descricao | TEXT | NOT NULL | Descrição do serviço |
| valor_previsto | DECIMAL(12,2) | NULL permitido | Valor previsto |
| status | VARCHAR(30) | NOT NULL, DEFAULT 'registrado' | Situação do serviço |
| criado_em | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Data do registro |
| duracao_minutos | INT | NOT NULL, DEFAULT 60 | Duração em minutos |
| data_atendimento | DATE | NULL permitido | Data prevista do atendimento |

## Regras

A tabela `servico` armazena os atendimentos registrados no ServiHUB.

O campo `id` é gerado automaticamente pelo MySQL.

O campo `cliente_id` referencia `cliente.id` e é obrigatório. O campo `endereco_id` referencia `endereco.id` e é opcional.

Um cliente pode possuir vários serviços (1:N). Um endereço também pode estar vinculado a vários serviços.

O Backend 1 verifica se o cliente existe e, quando informado um endereço, se ele pertence ao cliente selecionado.

A descrição é obrigatória.

O campo `status` é armazenado como VARCHAR(30), não como ENUM do MySQL.

Os estados utilizados pela aplicação são: `registrado`, `agendado`, `em_andamento`, `concluido` e `cancelado`.

As transições permitidas são:
- registrado → agendado ou cancelado;
- agendado → em_andamento ou cancelado;
- em_andamento → concluido ou cancelado;
- concluido e cancelado → sem novas transições.

O campo `duracao_minutos` possui valor padrão de 60 minutos e deve ser maior que zero nas operações de cadastro.

O campo `data_atendimento` permite registrar a data prevista. No cadastro, o Backend 1 não permite uma data anterior à data atual.

Quando `data_atendimento` é informada no cadastro, o serviço recebe status `agendado`. Caso contrário, recebe status `registrado`.

A tabela é criada pelo script `db/schema.sql`, e as colunas `duracao_minutos` e `data_atendimento` são acrescentadas pelo método `Main.migrate()` do Backend 1.

O cadastro utiliza `POST /servicos`, retornando HTTP 201 com o campo `id`.

A consulta utiliza `GET /servicos`, retornando os registros associados aos clientes e ordenados por ID decrescente.

A alteração de status utiliza `PUT /servicos/{id}/status`.

O histórico por cliente utiliza `GET /clientes/{id}/historico`.

## Erros e validações

| Situação | Comportamento |
| --- | --- |
| Cliente inexistente no cadastro | HTTP 404 — CLIENTE_NAO_ENCONTRADO |
| Endereço não vinculado ao cliente | HTTP 409 — ENDERECO_DE_OUTRO_CLIENTE |
| Descrição vazia | HTTP 400 — DESCRICAO_OBRIGATORIA |
| Duração igual ou inferior a zero | HTTP 400 — DURACAO_INVALIDA |
| Data de atendimento anterior à atual | HTTP 400 — Não é permitido selecionar uma data passada |
| Serviço inexistente ao alterar status | HTTP 404 — SERVICO_NAO_ENCONTRADO |
| Transição de status não permitida | HTTP 409 — TRANSICAO_INVALIDA |
| Exceção durante o processamento | HTTP 400 — REQUISICAO_INVALIDA, conforme tratamento geral atual |

## Persistência

Os dados são armazenados no MySQL e acessados pelo Backend 1 por meio de JDBC.

As definições da tabela e suas colunas estão em `db/schema.sql` e no método `Main.migrate()`.

A tabela possui chaves estrangeiras individuais para `cliente_id` e `endereco_id`. A verificação de que o endereço pertence ao cliente é realizada pelo Backend 1.
