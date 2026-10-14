# CT2 listarClientes

| Linha | Valor |
| --- | --- |
| ID e nome | CT2 listarClientes |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend 1 |
| Atende | E1 |
| Como chamar | GET /clientes |

## Entrada

Não tem.

## Saída

Sucesso: HTTP 200.

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| clientes | lista de objeto Cliente | sim | ver objeto Cliente |

### Objeto Cliente

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| clienteId | inteiro | sim | 1 |
| nome | texto | sim | Carlos Mendes |
| telefone | texto | sim | 13996937266 |
| email | texto | não | carlos@example.com |
| criadoEm | data e hora | sim | 2026-10-13T09:00:00-03:00 |

## Cabeçalhos de entrada

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| Cookie servihubSessao | texto | sim | 64 caracteres hexadecimais do token |

## Regras

Retorna todos os clientes por nome crescente e clienteId como desempate. Sem registros, clientes é uma lista vazia. Regras de sessão, cabeçalhos, origem, timeout e limites seguem a visão geral. O corpo JSON não contém o token de sessão. Resposta contém Content-Type: application/json e Cache-Control: no-store.

Padrões de campos, datas, valores, rede e erros seguem docs/fluxos/visao-geral.md no Frontend. Campo recebido fora do contrato é rejeitado com 400 DADOS_INVALIDOS.

## Erros

| Quando | O que volta |
| --- | --- |
| Cookie ausente, sessão expirada ou invalidada | 401 SESSAO_INVALIDA |
| ID, campo obrigatório, tipo ou JSON inválido | 400 DADOS_INVALIDOS |
| Banco indisponível ou transação falha | 503 BANCO_INDISPONIVEL |
| Falha inesperada | 500 ERRO_INTERNO |

### Objeto Erro

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| codigo | texto | sim | DADOS_INVALIDOS |
| mensagem | texto | sim | Confira os campos informados. |

