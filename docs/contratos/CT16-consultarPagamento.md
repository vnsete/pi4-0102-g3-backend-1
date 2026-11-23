
# CT16 consultarPagamento

| Linha | Valor |
| --- | --- |
| ID e nome | CT16 consultarPagamento |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend 1 |
| Atende | E9 |
| Como chamar | GET /pagamentos |

## Entrada

Não possui parâmetros obrigatórios nem corpo de requisição.

## Saída

Sucesso: HTTP 200.

A API retorna um array JSON contendo os pagamentos cadastrados.

| Campo | Tipo | Descrição |
| --- | --- | --- |
| id | inteiro | Identificador do pagamento |
| servico_id | inteiro | Identificador do serviço |
| valor | decimal | Valor registrado |
| situacao | texto | pendente ou pago |
| data_pagamento | data ou nulo | Data informada no pagamento |
| descricao | texto | Descrição do serviço |
| cliente | texto | Nome do cliente |

## Cabeçalhos de entrada

| Campo | Tipo | Obrigatório |
| --- | --- | --- |
| Cookie | texto | Sim, para autenticação por sessão |

O navegador utiliza o cookie `servihubSessao`.

## Regras

O endpoint `GET /pagamentos` consulta todos os pagamentos cadastrados no banco de dados.

A consulta utiliza a tabela `pagamento`, associada às tabelas `servico` e `cliente`.

O resultado inclui a descrição do serviço e o nome do cliente associado.

Não existe filtro obrigatório por identificador de serviço.

Quando não existem pagamentos cadastrados, a API retorna um array JSON vazio.

O acesso exige sessão válida ou autorização interna, conforme o mecanismo de autenticação do Backend 1.

A resposta utiliza Content-Type `application/json; charset=utf-8`.

## Erros

| Quando | O que volta |
| --- | --- |
| Sessão inválida ou expirada, sem autorização interna | HTTP 401 — SESSAO_INVALIDA |
| Rota inexistente | HTTP 404 — ROTA_NAO_ENCONTRADA |
| Exceção durante o processamento | HTTP 400 — REQUISICAO_INVALIDA |

## Objeto de erro

Os erros retornam um objeto JSON com o campo `erro`.

Exemplo:

```json
{
  "erro": "SESSAO_INVALIDA"
}
```
