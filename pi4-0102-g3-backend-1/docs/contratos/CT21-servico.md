# CT21 servico

| Linha | Valor |
| --- | --- |
| ID e nome | CT21 servico |
| Fronteira | Banco |
| Quem chama | Backend 1 |
| Quem responde | Banco |
| Atende | E3, E4, E5, E6, E7, E8, E9, E10 |
| Como chamar | Tabela servico; chave id; conexão MySQL |

## Entrada e saída

Os campos gravados são os mesmos campos lidos.

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| id | inteiro | sim | 20 |
| cliente_id | inteiro | sim | 1 |
| endereco_id | inteiro | não | 10 |
| descricao | texto | sim | Limpeza da piscina |
| valor_previsto | decimal | não | 150.00 |
| status | um de: registrado, agendado, em_andamento, concluido, cancelado | sim | registrado |
| criado_em | data e hora | sim | 2026-10-13T09:02:00-03:00 |
| atualizado_em | data e hora | sim | 2026-10-13T09:02:00-03:00 |

## Regras

PK id INT AUTO_INCREMENT. FKs cliente_id e endereco_id (opcional), com RESTRICT. descricao VARCHAR(500); valor_previsto DECIMAL(12,2) opcional e positivo; status ENUM conforme tabela; datas DATETIME(6) UTC. Validação do endereço pertencer ao cliente ocorre no Backend 1. Inserir, consultar e alterar status conforme CT8; atualização não modifica criado_em. Alterações de status usam bloqueio de linha e validam CT8 sobre o registro atual. As tabelas usam mecanismo transacional InnoDB, charset utf8mb4 e conexão com timezone UTC. Consultas usam parâmetros vinculados, sem concatenar valores vindos da tela. Restrições e índices são criados por scripts SQL versionados junto do Backend 1. A FK composta (endereco_id, cliente_id) referencia endereco(id, cliente_id); o par (id, cliente_id) do serviço é único.

Os campos da entrada e da saída descrevem os registros persistidos/lidos. Em inserções, id e datas geradas são preenchidos pelo banco ou pelo Backend 1; em consultas, o Backend 1 informa filtros e lê somente as colunas necessárias. Colunas obrigatórias são NOT NULL; opcionais aceitam NULL. A conversão entre campos HTTP e colunas segue a visão geral.

## Erros

| Quando | O que volta |
| --- | --- |
| FK inexistente ou referência incompatível | Banco recusa; Backend 1 retorna o erro de vínculo ou não encontrado do contrato HTTP. |
| Valor, nulidade, enumeração ou unicidade viola a regra da tabela | Banco recusa; Backend 1 retorna 400 ou 409 conforme o contrato HTTP. |
| Conexão ou transação falha | Rollback; Backend 1 retorna 503 BANCO_INDISPONIVEL. |

