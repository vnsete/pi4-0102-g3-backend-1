# CT23 orcamento

| Linha | Valor |
| --- | --- |
| ID e nome | CT23 orcamento |
| Fronteira | Banco |
| Quem chama | Backend 1 |
| Quem responde | Banco |
| Atende | E8 |
| Como chamar | Tabela orcamento; chave id; conexão MySQL |

## Entrada e saída

Os campos gravados são os mesmos campos lidos.

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| id | inteiro | sim | 40 |
| cliente_id | inteiro | sim | 1 |
| servico_id | inteiro | não | 20 |
| descricao | texto | sim | Limpeza da piscina e materiais |
| valor_total | decimal | sim | 150.00 |
| criado_em | data e hora | sim | 2026-10-13T09:04:00-03:00 |

## Regras

PK id INT AUTO_INCREMENT. FK cliente_id obrigatória e servico_id opcional, com RESTRICT. descricao VARCHAR(500), valor_total DECIMAL(12,2) positivo, criado_em DATETIME(6) UTC. Backend 1 valida que serviço e cliente correspondem. Inserir e consultar por cliente_id. As tabelas usam mecanismo transacional InnoDB, charset utf8mb4 e conexão com timezone UTC. Consultas usam parâmetros vinculados, sem concatenar valores vindos da tela. Restrições e índices são criados por scripts SQL versionados junto do Backend 1. A FK composta (servico_id, cliente_id) referencia servico(id, cliente_id), impedindo orçamento vinculado ao cliente errado.

Os campos da entrada e da saída descrevem os registros persistidos/lidos. Em inserções, id e datas geradas são preenchidos pelo banco ou pelo Backend 1; em consultas, o Backend 1 informa filtros e lê somente as colunas necessárias. Colunas obrigatórias são NOT NULL; opcionais aceitam NULL. A conversão entre campos HTTP e colunas segue a visão geral.

## Erros

| Quando | O que volta |
| --- | --- |
| FK inexistente ou referência incompatível | Banco recusa; Backend 1 retorna o erro de vínculo ou não encontrado do contrato HTTP. |
| Valor, nulidade, enumeração ou unicidade viola a regra da tabela | Banco recusa; Backend 1 retorna 400 ou 409 conforme o contrato HTTP. |
| Conexão ou transação falha | Rollback; Backend 1 retorna 503 BANCO_INDISPONIVEL. |

