# CT25 autenticarPrestador

| Linha | Valor |
| --- | --- |
| ID e nome | CT25 autenticarPrestador |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend 1 |
| Atende | E0 |
| Como chamar | POST /sessao |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| email | texto | sim | prestador@example.com |
| senha | texto | sim | SenhaDoPrestador123! |

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
| Origin | texto | sim | https://servihub.example.com |
| Content-Type | texto | sim | application/json |

## Cabeçalhos de saída

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| Set-Cookie | texto | sim | servihubSessao=<token>; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=1800 |

## Regras

E-mail: até 254 caracteres, normalizado com trim e minúsculas. Senha: 12 a 128 caracteres, sem alterar espaços ou caixa. Uma conta do prestador é provisionada na configuração do Backend 1; não há cadastro público de contas. O hash usa Argon2id com salt aleatório e parâmetros mínimos m=19456 KiB, t=2, p=1. A senha e o hash nunca são registrados em logs nem retornados pela API. Após validar credenciais, gera token opaco aleatório de 32 bytes, em 64 caracteres hexadecimais, associa contaId e validade de 30 minutos em memória do Backend 1 e devolve o cookie. O JSON contém somente autenticado, contaId e expiraEm. Login substitui e invalida uma sessão anterior apresentada no mesmo navegador. Cinco tentativas inválidas por IP em cinco minutos bloqueiam novos logins nesse IP por cinco minutos. Exemplos são fictícios e não são credenciais provisionadas. Regras de sessão, cabeçalhos, origem, timeout e limites seguem a visão geral. O corpo JSON não contém o token de sessão. Resposta contém Content-Type: application/json e Cache-Control: no-store.

Padrões de campos, datas, valores, rede e erros seguem docs/fluxos/visao-geral.md no Frontend. Campo recebido fora do contrato é rejeitado com 400 DADOS_INVALIDOS.

## Erros

| Quando | O que volta |
| --- | --- |
| E-mail ou senha não conferem | 401 CREDENCIAIS_INVALIDAS |
| Mais tentativas que o limite | 429 LIMITE_DE_TENTATIVAS |
| Conta não foi configurada no ambiente | 503 AUTENTICACAO_INDISPONIVEL |
| Origem ausente ou não autorizada | 403 ORIGEM_NAO_AUTORIZADA |
| Corpo maior que 64 KiB | 413 CORPO_MUITO_GRANDE |
| Tipo de conteúdo diferente de application/json | 415 TIPO_DE_CONTEUDO_INVALIDO |
| ID, campo obrigatório, tipo ou JSON inválido | 400 DADOS_INVALIDOS |
| Falha inesperada | 500 ERRO_INTERNO |

### Objeto Erro

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| codigo | texto | sim | DADOS_INVALIDOS |
| mensagem | texto | sim | Confira os campos informados. |

