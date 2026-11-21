# CT26 consultarSessao

| Linha | Valor |
| --- | --- |
| ID e nome | CT26 consultarSessao |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend 1 |
| Atende | E0 |
| Como chamar | GET /sessao |

## Entrada

Não tem.

## Saída

Sucesso: HTTP 200.

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| autenticado | booleano | sim | true |
| contaId | inteiro | sim | 1 |
| expiraEm | data e hora | sim | 2026-10-13T18:30:00-03:00 |

## Cabeçalhos de entrada

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| Cookie servihubSessao | texto | sim | 64 caracteres hexadecimais do token |

## Regras

Valida o cookie servihubSessao na memória do Backend 1 e devolve contaId e expiraEm. Usado ao reabrir ou atualizar a aplicação para decidir entre a tela de login e a navegação autenticada. Não prolonga a validade. Sessão ausente, expirada ou invalidada retorna 401 SESSAO_INVALIDA, e não autenticado=false com HTTP 200. Regras de sessão, cabeçalhos, origem, timeout e limites seguem a visão geral. O corpo JSON não contém o token de sessão. Resposta contém Content-Type: application/json e Cache-Control: no-store.

Padrões de campos, datas, valores, rede e erros seguem docs/fluxos/visao-geral.md no Frontend. Campo recebido fora do contrato é rejeitado com 400 DADOS_INVALIDOS.

## Erros

| Quando | O que volta |
| --- | --- |
| Sessão ausente, expirada ou invalidada | 401 SESSAO_INVALIDA |
| ID, campo obrigatório, tipo ou JSON inválido | 400 DADOS_INVALIDOS |
| Falha inesperada | 500 ERRO_INTERNO |

### Objeto Erro

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| codigo | texto | sim | DADOS_INVALIDOS |
| mensagem | texto | sim | Confira os campos informados. |

