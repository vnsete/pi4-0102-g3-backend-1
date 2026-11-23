
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
| email | texto | sim | admin@servihub.local |
| senha | texto | sim | SenhaExemplo123 |

Os dados são enviados no corpo da requisição em formato JSON.

## Saída

Sucesso: HTTP 200.

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| autenticado | booleano | sim | true |
| contaId | inteiro | sim | 1 |
| expiraEm | texto (ISO 8601) | sim | 2026-11-23T18:30:00Z |

A resposta contém o estado da autenticação, o identificador da conta e a data de expiração da sessão.

## Cabeçalhos de entrada

| Campo | Tipo | Obrigatório |
| --- | --- | --- |
| Content-Type | texto | Recomendado: application/json |

## Cabeçalhos de saída

| Campo | Tipo | Descrição |
| --- | --- | --- |
| Set-Cookie | texto | Cookie de autenticação |
| Content-Type | texto | application/json; charset=utf-8 |

Formato do cookie gerado:

`servihubSessao=<token>; HttpOnly; SameSite=Strict; Path=/; Max-Age=1800`

O atributo Secure não é definido explicitamente na implementação atual.

## Regras

O endpoint `POST /sessao` realiza a autenticação do prestador utilizando e-mail e senha.

As credenciais iniciais são configuradas pelas variáveis de ambiente `ADMIN_EMAIL` e `ADMIN_PASSWORD`.

Na inicialização do Backend 1, caso a conta configurada ainda não exista, o sistema cria seu registro na tabela `conta`.

A senha é armazenada como hash gerado pela biblioteca Argon2. Na criação da conta, o código utiliza os parâmetros de hash `3, 65536, 1`.

Na autenticação, o Backend 1 procura a conta pelo e-mail informado e verifica a senha utilizando `Argon2.verify()`.

O e-mail informado é tratado com remoção de espaços nas extremidades, mas não há conversão explícita para letras minúsculas.

A senha também é lida com remoção de espaços nas extremidades, conforme o método `s()` utilizado no código.

Quando as credenciais são válidas, o Backend 1 gera um token utilizando `UUID.randomUUID()`.

O token é armazenado em memória no Backend 1, associado ao instante de expiração da sessão.

A sessão possui validade de 1800 segundos, equivalentes a 30 minutos.

O navegador recebe o token pelo cookie `servihubSessao`, configurado com os atributos `HttpOnly`, `SameSite=Strict`, `Path=/` e `Max-Age=1800`.

O token não é incluído no corpo JSON da resposta.

A implementação atual não possui limitação de tentativas de login por IP.

Também não invalida automaticamente sessões anteriores quando um novo login é realizado.

A validação das sessões é realizada pelo Backend 1. O encerramento utiliza `DELETE /sessao`, conforme CT27.

## Erros

| Quando | O que volta |
| --- | --- |
| E-mail não encontrado | HTTP 401 — CREDENCIAIS_INVALIDAS |
| Senha incorreta | HTTP 401 — CREDENCIAIS_INVALIDAS |
| JSON inválido ou exceção durante o processamento | HTTP 400 — REQUISICAO_INVALIDA |

Se as credenciais administrativas não forem configuradas na inicialização, o Backend 1 não conclui normalmente sua inicialização. Essa situação não corresponde a uma resposta HTTP 503 do endpoint de login.

## Objeto de erro

Os erros retornam um objeto JSON com o campo `erro`.

Exemplo:

```json
{
  "erro": "CREDENCIAIS_INVALIDAS"
}
```

## Persistência e segurança

As credenciais são armazenadas na tabela `conta` do MySQL.

Os tokens de sessão são mantidos em memória pelo Backend 1, não em uma tabela do banco.

O acesso às rotas protegidas depende da validade da sessão ou da autorização interna, conforme as regras implementadas.
