
# CT23 orcamento

| Linha | Valor |
| --- | --- |
| ID e nome | CT23 orcamento |
| Fronteira | Banco de dados |
| Quem chama | Backend 1 |
| Quem responde | MySQL 9.2 |
| Atende | E8 |
| Como chamar | Tabela orcamento, via JDBC |

## Estrutura da tabela

| Campo | Tipo SQL | Restrição | Descrição |
| --- | --- | --- | --- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Identificador do orçamento |
| cliente_id | BIGINT | NOT NULL, FOREIGN KEY | Cliente vinculado |
| servico_id | BIGINT | NULL, FOREIGN KEY | Referência opcional a um serviço |
| descricao | TEXT | NOT NULL | Descrição do orçamento |
| valor | DECIMAL(12,2) | NOT NULL | Valor do orçamento |
| catalogo_id | BIGINT | NULL permitido | Item do catálogo utilizado |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'pendente' | Situação da aprovação |
| servico_aprovado_id | BIGINT | NULL permitido | Identificador do serviço criado na aprovação |

## Regras

A tabela `orcamento` armazena as propostas comerciais vinculadas aos clientes do ServiHUB.

O campo `id` é gerado automaticamente pelo MySQL.

O campo `cliente_id` é obrigatório e possui chave estrangeira para `cliente.id`.

O campo `servico_id` é opcional e possui chave estrangeira para `servico.id`.

As colunas `catalogo_id`, `status` e `servico_aprovado_id` são adicionadas pelo método `Main.migrate()` do Backend 1. O script inicial `db/schema.sql` cria as demais colunas.

O campo `catalogo_id` registra a referência ao item do catálogo utilizado na criação do orçamento. Não existe uma restrição FOREIGN KEY declarada para essa coluna no esquema atual.

O campo `status` utiliza os valores `pendente` e `aprovado` nas operações implementadas.

O campo `servico_aprovado_id` armazena o identificador do serviço criado na aprovação. Também não possui restrição FOREIGN KEY declarada no esquema atual.

O cadastro utiliza `POST /orcamentos`, recebendo `cliente_id`, `catalogo_id` e, opcionalmente, `valor`.

O Backend 1 verifica se o cliente e o item do catálogo existem.

A descrição é preenchida automaticamente a partir do catálogo. Se o valor não for informado, utiliza-se o valor do catálogo. Valores negativos são rejeitados.

Após o cadastro, a API retorna HTTP 201 com o identificador `id` criado.

A consulta utiliza `GET /orcamentos`, listando os orçamentos com o nome do cliente, ordenados por ID decrescente.

A aprovação utiliza `PUT /orcamentos/{id}/aprovar`.

Ao aprovar um orçamento, o Backend 1 cria um registro na tabela `servico` e um registro na tabela `pagamento`, com situação `pendente`.

A aprovação ocorre dentro de uma transação. A coluna `status` passa a `aprovado`, e `servico_aprovado_id` recebe o identificador do serviço gerado.

O código verifica se o orçamento já está aprovado e possui serviço vinculado, evitando repetir a criação nessa condição.

## Erros e validações

| Situação | Comportamento |
| --- | --- |
| Cliente inexistente na criação | HTTP 404 — CLIENTE_NAO_ENCONTRADO |
| Item do catálogo inexistente | HTTP 404 — SERVICO_DO_CATALOGO_NAO_ENCONTRADO |
| Valor negativo | HTTP 400 — VALOR_INVALIDO |
| Orçamento inexistente na aprovação | HTTP 404 — ORCAMENTO_NAO_ENCONTRADO |
| Sessão inválida, sem autorização interna | HTTP 401 — SESSAO_INVALIDA |
| Exceção durante o processamento | HTTP 400 — REQUISICAO_INVALIDA, conforme tratamento geral atual |

## Persistência

A tabela é criada no MySQL pelo arquivo `db/schema.sql` do repositório principal.

As colunas adicionais são criadas pelo método `Main.migrate()` do Backend 1.

As operações de persistência utilizam JDBC. Na aprovação, o Backend 1 utiliza uma transação para criar o serviço, registrar o pagamento pendente e atualizar o orçamento.

