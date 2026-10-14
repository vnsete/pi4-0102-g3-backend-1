# CT14 consultarOrcamentos

| Linha | Valor |
| --- | --- |
| ID e nome | CT14 consultarOrcamentos |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend 1 |
| Atende | E8 |
| Como chamar | GET /clientes/{clienteId}/orcamentos |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| clienteId | inteiro | sim | 1 |

## Saída

Sucesso: HTTP 200.

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| orcamentos | lista de objeto Orcamento | sim | ver objeto Orcamento |

### Objeto Orcamento

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| orcamentoId | inteiro | sim | 40 |
| clienteId | inteiro | sim | 1 |
| servicoId | inteiro | não | 20 |
| descricao | texto | sim | Limpeza da piscina e materiais |
| valorTotal | decimal | sim | 150.00 |
| criadoEm | data e hora | sim | 2026-10-13T09:04:00-03:00 |

## Cabeçalhos de entrada

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| Cookie servihubSessao | texto | sim | 64 caracteres hexadecimais do token |

## Regras

Confere cliente e retorna seus orçamentos por criadoEm decrescente e orcamentoId decrescente. Cliente sem orçamento retorna lista vazia. Regras de sessão, cabeçalhos, origem, timeout e limites seguem a visão geral. O corpo JSON não contém o token de sessão. Resposta contém Content-Type: application/json e Cache-Control: no-store.

Padrões de campos, datas, valores, rede e erros seguem docs/fluxos/visao-geral.md no Frontend. Campo recebido fora do contrato é rejeitado com 400 DADOS_INVALIDOS.

## Erros

| Quando | O que volta |
| --- | --- |
| Cliente não existe | 404 CLIENTE_NAO_ENCONTRADO |
| Cookie ausente, sessão expirada ou invalidada | 401 SESSAO_INVALIDA |
| ID, campo obrigatório, tipo ou JSON inválido | 400 DADOS_INVALIDOS |
| Banco indisponível ou transação falha | 503 BANCO_INDISPONIVEL |
| Falha inesperada | 500 ERRO_INTERNO |

### Objeto Erro

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| codigo | texto | sim | DADOS_INVALIDOS |
| mensagem | texto | sim | Confira os campos informados. |

