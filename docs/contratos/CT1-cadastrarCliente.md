# CT1 cadastrarCliente

| Linha | Valor |
| --- | --- |
| ID e nome | CT1 cadastrarCliente |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend 1 |
| Atende | E1 |
| Como chamar | POST /clientes |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| nome | texto | sim | Carlos Mendes |
| telefone | texto | sim | 13996937266 |
| email | texto | não | carlos@example.com |

## Saída

Sucesso: HTTP 201.

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
| Origin | texto | sim | https://servihub.example.com |
| Content-Type | texto | sim | application/json |

## Regras

Nome: 1 a 120 caracteres após remover espaços das pontas. Telefone: 10 a 15 dígitos, sem máscara. E-mail opcional: até 254 caracteres e formato válido. O backend gera clienteId e criadoEm. Nomes repetidos são permitidos. O Frontend bloqueia o botão de envio até a resposta e não repete POST automaticamente. Se a conexão cair após enviar, a tela informa resultado não confirmado e consulta a lista antes de permitir outra gravação; não afirma que a operação falhou. Regras de sessão, cabeçalhos, origem, timeout e limites seguem a visão geral. O corpo JSON não contém o token de sessão. Resposta contém Content-Type: application/json e Cache-Control: no-store.

Padrões de campos, datas, valores, rede e erros seguem docs/fluxos/visao-geral.md no Frontend. Campo recebido fora do contrato é rejeitado com 400 DADOS_INVALIDOS.

## Erros

| Quando | O que volta |
| --- | --- |
| Nome, telefone ou e-mail inválidos | 400 DADOS_INVALIDOS |
| Cookie ausente, sessão expirada ou invalidada | 401 SESSAO_INVALIDA |
| Origem ausente ou não autorizada | 403 ORIGEM_NAO_AUTORIZADA |
| Corpo maior que 64 KiB | 413 CORPO_MUITO_GRANDE |
| Tipo de conteúdo diferente de application/json | 415 TIPO_DE_CONTEUDO_INVALIDO |
| ID, campo obrigatório, tipo ou JSON inválido | 400 DADOS_INVALIDOS |
| Banco indisponível ou transação falha | 503 BANCO_INDISPONIVEL |
| Falha inesperada | 500 ERRO_INTERNO |

### Objeto Erro

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| codigo | texto | sim | DADOS_INVALIDOS |
| mensagem | texto | sim | Confira os campos informados. |

