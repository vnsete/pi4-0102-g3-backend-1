
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

Não possui parâmetros obrigatórios nem corpo de requisição.

O navegador pode enviar o cookie `servihubSessao`, caso exista uma sessão anterior.

## Saída

Sucesso: HTTP 200.

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| autenticado | booleano | sim | false |

Exemplo:

```json
{
  "autenticado": false
}
```

## Cabeçalhos de entrada

| Campo | Tipo | Obrigatório | Descrição |
| --- | --- | --- | --- |
| Cookie | texto | Não | Permite identificar a sessão que será encerrada |

Formato do cookie:

`Cookie: servihubSessao=<token>`

## Cabeçalhos de saída

| Campo | Tipo | Descrição |
| --- | --- | --- |
| Set-Cookie | texto | Invalida o cookie de autenticação |
| Content-Type | texto | application/json; charset=utf-8 |

Formato do cabeçalho de encerramento:

`Set-Cookie: servihubSessao=; HttpOnly; SameSite=Strict; Path=/; Max-Age=0`

O atributo `Secure` não é definido explicitamente na implementação atual.

## Regras

O endpoint `DELETE /sessao` encerra a sessão do usuário no Backend 1.

O Backend 1 extrai o token do cookie `servihubSessao`, quando ele estiver presente.

Em seguida, remove o token do mapa de sessões mantido em memória.

A remoção é realizada por meio da operação `sessions.remove(token)`.

O servidor envia um novo cabeçalho `Set-Cookie` com o mesmo nome de cookie, valor vazio e `Max-Age=0`, solicitando sua exclusão pelo navegador.

A resposta retorna HTTP 200 com `autenticado: false`.

A operação também retorna HTTP 200 quando o token não existe, já expirou ou não foi enviado.

Não é necessária uma sessão válida para executar `DELETE /sessao`.

O endpoint não consulta nem altera registros no banco de dados.

A resposta não contém o token da sessão.

A resposta utiliza Content-Type `application/json; charset=utf-8`.

## Erros

| Quando | O que volta |
| --- | --- |
| Sessão inexistente, expirada ou já encerrada | HTTP 200 — autenticado: false |
| Exceção durante o processamento | HTTP 400 — REQUISICAO_INVALIDA |

A implementação atual não possui validação explícita do cabeçalho Origin nesse endpoint.

## Objeto de erro

Em caso de exceção, o Backend 1 utiliza um objeto JSON com o campo `erro`.

Exemplo:

```json
{
  "erro": "REQUISICAO_INVALIDA"
}
```

## Persistência e segurança

As sessões são mantidas em memória no Backend 1.

O encerramento remove o token desse armazenamento e invalida o cookie enviado ao navegador.

Não há alteração nas tabelas do MySQL.
