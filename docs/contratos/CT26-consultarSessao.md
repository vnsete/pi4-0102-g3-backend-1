
# CT26 consultarSessao

| Linha | Valor |
| --- | --- |
| ID e nome | CT26 consultarSessao |
| Fronteira | HTTP |
| Quem chama | Frontend e Backend 2 |
| Quem responde | Backend 1 |
| Atende | E0 |
| Como chamar | GET /sessao |

## Entrada

Não possui parâmetros obrigatórios nem corpo de requisição.

A consulta utiliza o cookie `servihubSessao`, quando disponível.

## Saída

Sucesso: HTTP 200.

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| autenticado | booleano | sim | true |

Exemplo de sessão válida:

```json
{
  "autenticado": true
}
```

Exemplo de sessão ausente, expirada ou inválida:

```json
{
  "autenticado": false
}
```

## Cabeçalhos de entrada

| Campo | Tipo | Obrigatório |
| --- | --- | --- |
| Cookie | texto | Não, mas necessário para identificar uma sessão autenticada |

Formato esperado:

`Cookie: servihubSessao=<token>`

## Regras

O endpoint `GET /sessao` consulta o estado da autenticação no Backend 1.

O Backend 1 extrai o token do cookie `servihubSessao` recebido na requisição.

A validade é verificada no mapa de sessões armazenado em memória, considerando a existência do token e seu instante de expiração.

Quando a sessão é válida, a API retorna HTTP 200 com `autenticado: true`.

Quando a sessão está ausente, expirada ou inválida, a API retorna HTTP 200 com `autenticado: false`.

A consulta não prolonga a validade da sessão.

O endpoint não retorna `contaId` nem `expiraEm`. Esses campos fazem parte da resposta de login do CT25.

O Frontend pode utilizar essa consulta para verificar o estado da autenticação.

O Backend 2 também utiliza `GET /sessao` para validar o cookie antes de consultar lembretes e pendências.

A resposta utiliza Content-Type `application/json; charset=utf-8`.

## Erros

| Quando | O que volta |
| --- | --- |
| Sessão ausente, expirada ou inválida | HTTP 200 — autenticado: false |
| Exceção durante o processamento | HTTP 400 — REQUISICAO_INVALIDA |

A ausência de sessão não gera HTTP 401 neste endpoint.

## Objeto de erro

Em caso de exceção, o Backend 1 utiliza um objeto JSON com o campo `erro`.

Exemplo:

```json
{
  "erro": "REQUISICAO_INVALIDA"
}
```
