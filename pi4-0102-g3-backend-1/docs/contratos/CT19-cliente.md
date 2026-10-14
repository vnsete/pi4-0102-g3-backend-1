# CT19 cliente

| Linha | Valor |
| --- | --- |
| ID e nome | CT19 cliente |
| Fronteira | Banco |
| Quem chama | Backend 1 |
| Quem responde | Banco |
| Atende | E1, E2, E3, E4, E6, E7, E8, E10 |
| Como chamar | Tabela cliente; chave id; conexão MySQL |

## Entrada e saída

Os campos gravados são os mesmos campos lidos.

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| id | inteiro | sim | 1 |
| nome | texto | sim | Carlos Mendes |
| telefone | texto | sim | 13996937266 |
| email | texto | não | carlos@example.com |
| criado_em | data e hora | sim | 2026-10-13T09:00:00-03:00 |

## Regras

PK id INT AUTO_INCREMENT. nome VARCHAR(120), telefone VARCHAR(15), email VARCHAR(254) opcional, criado_em DATETIME(6) UTC. Campos obrigatórios são NOT NULL. Nome não é único. Usada para inserir/listar clientes e verificar vínculos; não há exclusão no MVP. As tabelas usam mecanismo transacional InnoDB, charset utf8mb4 e conexão com timezone UTC. Consultas usam parâmetros vinculados, sem concatenar valores vindos da tela. Restrições e índices são criados por scripts SQL versionados junto do Backend 1.

Os campos da entrada e da saída descrevem os registros persistidos/lidos. Em inserções, id e datas geradas são preenchidos pelo banco ou pelo Backend 1; em consultas, o Backend 1 informa filtros e lê somente as colunas necessárias. Colunas obrigatórias são NOT NULL; opcionais aceitam NULL. A conversão entre campos HTTP e colunas segue a visão geral.

## Erros

| Quando | O que volta |
| --- | --- |
| FK inexistente ou referência incompatível | Banco recusa; Backend 1 retorna o erro de vínculo ou não encontrado do contrato HTTP. |
| Valor, nulidade, enumeração ou unicidade viola a regra da tabela | Banco recusa; Backend 1 retorna 400 ou 409 conforme o contrato HTTP. |
| Conexão ou transação falha | Rollback; Backend 1 retorna 503 BANCO_INDISPONIVEL. |

