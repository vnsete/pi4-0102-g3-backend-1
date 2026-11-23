
# CT24 pagamento

| Linha | Valor |
| --- | --- |
| ID e nome | CT24 pagamento |
| Fronteira | Banco de dados |
| Quem chama | Backend 1 |
| Quem responde | MySQL 9.2 |
| Atende | E9, E10 |
| Como chamar | Tabela pagamento, via JDBC |

## Estrutura da tabela

| Campo | Tipo SQL | Restrição | Descrição |
| --- | --- | --- | --- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Identificador do pagamento |
| servico_id | BIGINT | NOT NULL, UNIQUE, FOREIGN KEY | Serviço vinculado |
| valor | DECIMAL(12,2) | NOT NULL | Valor registrado |
| situacao | VARCHAR(20) | NOT NULL | Situação do pagamento |
| data_pagamento | DATE | NULL permitido | Data informada para o pagamento |

## Regras

A tabela `pagamento` armazena os registros financeiros associados aos serviços do ServiHUB.

O campo `id` é gerado automaticamente pelo MySQL.

O campo `servico_id` referencia `servico.id` e possui restrição UNIQUE, permitindo apenas um registro de pagamento por serviço.

O campo `valor` é obrigatório e utiliza o tipo DECIMAL(12,2).

O campo `situacao` é armazenado como VARCHAR(20), não como ENUM do MySQL. O Backend 1 aceita exclusivamente os valores `pendente` e `pago` nas operações de registro.

O campo `data_pagamento` é opcional e utiliza o tipo DATE. O Backend 1 grava a data informada na requisição, sem preenchê-la automaticamente quando o pagamento é marcado como pago.

A tabela não possui colunas chamadas `pago_em` ou `atualizado_em`.

O registro manual utiliza `POST /pagamentos`, recebendo `servico_id`, `valor`, `situacao` e, opcionalmente, `data_pagamento`.

O Backend 1 verifica se o serviço existe e valida a situação informada.

O registro ou atualização utiliza `INSERT ... ON DUPLICATE KEY UPDATE`, aproveitando a restrição UNIQUE de `servico_id`.

Caso já exista pagamento para o mesmo serviço, a operação atualiza `valor`, `situacao` e `data_pagamento`.

A aprovação de um orçamento também gera automaticamente um pagamento com situação `pendente`, vinculado ao serviço criado.

A consulta utiliza `GET /pagamentos`, retornando os registros associados à descrição do serviço e ao nome do cliente.

A consulta de pendências utiliza `GET /pendencias`, retornando somente os pagamentos com situação `pendente`.

A tabela é criada pelo script `db/schema.sql` do repositório principal.

## Erros e validações

| Situação | Comportamento |
| --- | --- |
| Serviço inexistente no registro | HTTP 404 — SERVICO_NAO_ENCONTRADO |
| Situação diferente de pendente ou pago | HTTP 400 — SITUACAO_INVALIDA |
| Sessão inválida, sem autorização interna | HTTP 401 — SESSAO_INVALIDA |
| Valor inválido ou exceção durante o processamento | HTTP 400 — REQUISICAO_INVALIDA, conforme tratamento geral atual |

## Persistência

Os dados são armazenados no MySQL e acessados pelo Backend 1 por meio de JDBC.

A definição da tabela está em `db/schema.sql`.

A restrição UNIQUE de `servico_id` evita múltiplos registros de pagamento para o mesmo serviço.
