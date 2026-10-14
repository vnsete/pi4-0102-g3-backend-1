# CT22 agendamento

| Linha | Valor |
| --- | --- |
| ID e nome | CT22 agendamento |
| Fronteira | Banco |
| Quem chama | Backend 1 |
| Quem responde | Banco |
| Atende | E6, E7 |
| Como chamar | Tabela agendamento; chave id; conexão MySQL |

## Entrada e saída

Os campos gravados são os mesmos campos lidos.

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| id | inteiro | sim | 30 |
| servico_id | inteiro | sim | 20 |
| inicio_em | data e hora | sim | 2026-10-14T10:00:00-03:00 |
| fim_em | data e hora | sim | 2026-10-14T11:00:00-03:00 |
| criado_em | data e hora | sim | 2026-10-13T09:03:00-03:00 |
| atualizado_em | data e hora | sim | 2026-10-13T09:03:00-03:00 |

## Regras

PK id INT AUTO_INCREMENT. FK servico_id para servico.id, com RESTRICT e UNIQUE (um por serviço). Datas DATETIME(6) UTC; fim_em > inicio_em. A criação e a atualização do status do serviço são atômicas. Backend 1 serializa a verificação de sobreposição antes de inserir; UNIQUE sozinho não evita conflitos de horário. Consultas juntam serviço e cliente. A implementação do bloqueio e do teste de sobreposição segue CT9. Índices: servico_id único e inicio_em para consultas de período. As tabelas usam mecanismo transacional InnoDB, charset utf8mb4 e conexão com timezone UTC. Consultas usam parâmetros vinculados, sem concatenar valores vindos da tela. Restrições e índices são criados por scripts SQL versionados junto do Backend 1.

Os campos da entrada e da saída descrevem os registros persistidos/lidos. Em inserções, id e datas geradas são preenchidos pelo banco ou pelo Backend 1; em consultas, o Backend 1 informa filtros e lê somente as colunas necessárias. Colunas obrigatórias são NOT NULL; opcionais aceitam NULL. A conversão entre campos HTTP e colunas segue a visão geral.

## Erros

| Quando | O que volta |
| --- | --- |
| FK inexistente ou referência incompatível | Banco recusa; Backend 1 retorna o erro de vínculo ou não encontrado do contrato HTTP. |
| Valor, nulidade, enumeração ou unicidade viola a regra da tabela | Banco recusa; Backend 1 retorna 400 ou 409 conforme o contrato HTTP. |
| Conexão ou transação falha | Rollback; Backend 1 retorna 503 BANCO_INDISPONIVEL. |

