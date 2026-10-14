# CT20 endereco

| Linha | Valor |
| --- | --- |
| ID e nome | CT20 endereco |
| Fronteira | Banco |
| Quem chama | Backend 1 |
| Quem responde | Banco |
| Atende | E2, E4 |
| Como chamar | Tabela endereco; chave id; conexão MySQL |

## Entrada e saída

Os campos gravados são os mesmos campos lidos.

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| id | inteiro | sim | 10 |
| cliente_id | inteiro | sim | 1 |
| logradouro | texto | sim | Rua das Flores |
| numero | texto | sim | 120 |
| complemento | texto | não | Casa 2 |
| bairro | texto | sim | Centro |
| cidade | texto | sim | Campinas |
| uf | texto | sim | SP |
| cep | texto | sim | 13010000 |
| criado_em | data e hora | sim | 2026-10-13T09:01:00-03:00 |

## Regras

PK id INT AUTO_INCREMENT. FK cliente_id para cliente.id. Textos conforme CT3: logradouro/bairro/cidade VARCHAR(120), numero VARCHAR(20), complemento VARCHAR(120) opcional, uf CHAR(2), cep CHAR(8), criado_em DATETIME(6) UTC. FK com RESTRICT; inserir e consultar por cliente_id. As tabelas usam mecanismo transacional InnoDB, charset utf8mb4 e conexão com timezone UTC. Consultas usam parâmetros vinculados, sem concatenar valores vindos da tela. Restrições e índices são criados por scripts SQL versionados junto do Backend 1. O par (id, cliente_id) possui chave única para verificar o proprietário do endereço por FK composta.

Os campos da entrada e da saída descrevem os registros persistidos/lidos. Em inserções, id e datas geradas são preenchidos pelo banco ou pelo Backend 1; em consultas, o Backend 1 informa filtros e lê somente as colunas necessárias. Colunas obrigatórias são NOT NULL; opcionais aceitam NULL. A conversão entre campos HTTP e colunas segue a visão geral.

## Erros

| Quando | O que volta |
| --- | --- |
| FK inexistente ou referência incompatível | Banco recusa; Backend 1 retorna o erro de vínculo ou não encontrado do contrato HTTP. |
| Valor, nulidade, enumeração ou unicidade viola a regra da tabela | Banco recusa; Backend 1 retorna 400 ou 409 conforme o contrato HTTP. |
| Conexão ou transação falha | Rollback; Backend 1 retorna 503 BANCO_INDISPONIVEL. |

