
# CT15 registrarPagamento

| Linha | Valor |
| --- | --- |
| ID e nome | CT15 registrarPagamento |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend 1 |
| Atende | E9 |
| Como chamar | POST /pagamentos |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| servico_id | inteiro | sim | 20 |
| valor | decimal | sim | 150.00 |
| situacao | pendente ou pago | sim | pago |
| data_pagamento | data (AAAA-MM-DD) ou nulo | não | 2026-11-23 |

## Saída

Sucesso: HTTP 200.

A API retorna um objeto JSON com a situação registrada.

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| situacao | texto | sim | pago |

Exemplo:

```json
{
  "situacao": "pago"
}
```

## Cabeçalhos de entrada

| Campo | Tipo | Obrigatório |
| --- | --- | --- |
| Cookie | texto | Sim, para autenticação por sessão |
| Content-Type | texto | Recomendado: application/json |

O navegador utiliza o cookie `servihubSessao`.

## Regras

O endpoint `POST /pagamentos` registra ou atualiza o pagamento associado a um serviço.

O campo `servico_id` deve identificar um serviço existente. Caso contrário, a API retorna HTTP 404 com o código `SERVICO_NAO_ENCONTRADO`.

O campo `situacao` aceita exclusivamente os valores `pendente` e `pago`. Outros valores retornam HTTP 400 com o código `SITUACAO_INVALIDA`.

O campo `valor` deve ser convertido para um número decimal válido.

O campo opcional `data_pagamento` é gravado conforme o valor enviado pelo cliente. O Backend 1 não preenche essa data automaticamente ao mudar a situação para `pago`.

Cada serviço pode possuir apenas um registro na tabela `pagamento`, pois a coluna `servico_id` possui restrição UNIQUE.

A operação utiliza `INSERT ... ON DUPLICATE KEY UPDATE`. Dessa forma, um pagamento existente para o mesmo serviço é atualizado, em vez de criar outro registro.

A atualização substitui o valor, a situação e a data de pagamento pelos valores informados na requisição.

A API retorna HTTP 200 com o campo `situacao` após a gravação.

O acesso exige sessão válida ou autorização interna, conforme o mecanismo de autenticação do Backend 1.

A resposta utiliza Content-Type `application/json; charset=utf-8`.

## Erros

| Quando | O que volta |
| --- | --- |
| Serviço não existe | HTTP 404 — SERVICO_NAO_ENCONTRADO |
| Situação diferente de pago ou pendente | HTTP 400 — SITUACAO_INVALIDA |
| Sessão inválida ou expirada, sem autorização interna | HTTP 401 — SESSAO_INVALIDA |
| Dados inválidos ou exceção durante o processamento | HTTP 400 — REQUISICAO_INVALIDA |

## Objeto de erro

Os erros retornam um objeto JSON com o campo `erro`.

Exemplo:

```json
{
  "erro": "SITUACAO_INVALIDA"
}
```
