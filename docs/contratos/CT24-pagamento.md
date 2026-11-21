# CT24 pagamento

| Linha | Valor |
| --- | --- |
| ID e nome | CT24 pagamento |
| Fronteira | Banco |
| Quem chama | Backend 1 |
| Quem responde | Banco |
| Atende | E9, E10 |
| Como chamar | Tabela pagamento; chave id; conexão MySQL |

## Entrada e saída

Os campos gravados são os mesmos campos lidos.

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| id | inteiro | sim | 50 |
| servico_id | inteiro | sim | 20 |
| valor | decimal | sim | 150.00 |
| situacao | um de: pendente, pago | sim | pendente |
| pago_em | data e hora | não | omitido quando pendente; presente quando pago |
| atualizado_em | data e hora | sim | 2026-10-13T09:05:00-03:00 |

## Regras

PK id INT AUTO_INCREMENT. FK servico_id com RESTRICT e UNIQUE. valor DECIMAL(12,2) positivo; situacao ENUM(pendente,pago); pago_em DATETIME(6) opcional; atualizado_em DATETIME(6) UTC. pago_em só pode existir se situacao=pago. Inserir ou atualizar por servico_id de forma atômica; consultas de pendências filtram situacao. O serviço é bloqueado na mesma conexão/transação antes do upsert. pago_em deve estar presente se situacao=pago e ausente se pendente, sem exceção. Índice por situacao e atualizado_em suporta CT18. As tabelas usam mecanismo transacional InnoDB, charset utf8mb4 e conexão com timezone UTC. Consultas usam parâmetros vinculados, sem concatenar valores vindos da tela. Restrições e índices são criados por scripts SQL versionados junto do Backend 1.

Os campos da entrada e da saída descrevem os registros persistidos/lidos. Em inserções, id e datas geradas são preenchidos pelo banco ou pelo Backend 1; em consultas, o Backend 1 informa filtros e lê somente as colunas necessárias. Colunas obrigatórias são NOT NULL; opcionais aceitam NULL. A conversão entre campos HTTP e colunas segue a visão geral.

## Erros

| Quando | O que volta |
| --- | --- |
| FK inexistente ou referência incompatível | Banco recusa; Backend 1 retorna o erro de vínculo ou não encontrado do contrato HTTP. |
| Valor, nulidade, enumeração ou unicidade viola a regra da tabela | Banco recusa; Backend 1 retorna 400 ou 409 conforme o contrato HTTP. |
| Conexão ou transação falha | Rollback; Backend 1 retorna 503 BANCO_INDISPONIVEL. |

