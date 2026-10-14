# CT27 encerrarSessao

| Linha | Valor |
| --- | --- |
| ID e nome | CT27 encerrarSessao |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend 1 |
| Atende | E0 |
| Como chamar | DELETE /sessao |

## Entrada

Não tem.

## Saída

Sucesso: HTTP 200.

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| autenticado | booleano | sim | false |

## Cabeçalhos de entrada

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| Cookie servihubSessao | texto | não | 64 caracteres hexadecimais do token |
| Origin | texto | sim | https://servihub.example.com |

## Cabeçalhos de saída

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| Set-Cookie | texto | sim | servihubSessao=; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=0 |

## Regras

Remove o token da memória, quando existir, e devolve cookie servihubSessao vazio com Max-Age=0. Operação idempotente: uma sessão já encerrada ou expirada também retorna HTTP 200. O JSON contém autenticado=false. O Frontend limpa dados das telas e volta ao login; o cookie só é removido pela resposta do servidor. Regras de sessão, cabeçalhos, origem, timeout e limites seguem a visão geral. O corpo JSON não contém o token de sessão. Resposta contém Content-Type: application/json e Cache-Control: no-store.

Padrões de campos, datas, valores, rede e erros seguem docs/fluxos/visao-geral.md no Frontend. Campo recebido fora do contrato é rejeitado com 400 DADOS_INVALIDOS.

## Erros

| Quando | O que volta |
| --- | --- |
| Origem ausente ou não autorizada | 403 ORIGEM_NAO_AUTORIZADA |
| ID, campo obrigatório, tipo ou JSON inválido | 400 DADOS_INVALIDOS |
| Falha inesperada | 500 ERRO_INTERNO |

### Objeto Erro

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| codigo | texto | sim | DADOS_INVALIDOS |
| mensagem | texto | sim | Confira os campos informados. |

