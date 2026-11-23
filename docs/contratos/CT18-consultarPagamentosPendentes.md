
# CT18 consultarPagamentosPendentes

| Linha | Valor |
| --- | --- |
| ID e nome | CT18 consultarPagamentosPendentes |
| Fronteira | HTTP |
| Quem chama | Backend 2 |
| Quem responde | Backend 1 |
| Atende | E10 |
| Como chamar | GET /pendencias |

## Entrada

Não possui parâmetros obrigatórios nem corpo de requisição.

## Saída

Sucesso: HTTP 200.

A API retorna diretamente um array JSON contendo somente pagamentos com situação `pendente`.

| Campo | Tipo | Descrição |
| --- | --- | --- |
| id | inteiro | Identificador do pagamento |
| servico_id | inteiro | Identificador do serviço |
| valor | decimal | Valor registrado |
| situacao | texto | pendente |
| data_pagamento | data ou nulo | Data registrada no pagamento |
| descricao | texto | Descrição do serviço |
| cliente | texto | Nome do cliente |

## Cabeçalhos de entrada

| Campo | Tipo | Obrigatório |
| --- | --- | --- |
| X-ServiHUB-Internal | texto | Sim, na comunicação realizada pelo Backend 2 |

O Backend 2 envia a chave configurada em `INTERNAL_API_KEY`.

O Backend 1 também aceita uma sessão válida pelo cookie `servihubSessao`, conforme sua regra geral de autenticação.

## Regras

O endpoint `GET /pendencias` consulta os pagamentos pendentes no Backend 1.

A consulta associa as tabelas `pagamento`, `servico` e `cliente`.

São retornados somente os pagamentos cuja coluna `situacao` seja igual a `pendente`.

O resultado inclui os campos da tabela `pagamento`, além da descrição do serviço e do nome do cliente.

Não há ordenação explícita na consulta SQL.

Quando não existem pagamentos pendentes, retorna um array JSON vazio.

O Backend 2 utiliza esse endpoint após validar a sessão do usuário por meio de `GET /sessao`.

Na chamada interna, o Backend 2 utiliza o cabeçalho `X-ServiHUB-Internal`, com a chave compartilhada entre os backends.

O Backend 1 autoriza o acesso quando a sessão é válida ou quando a chave interna corresponde à configuração do servidor.

A resposta utiliza Content-Type `application/json; charset=utf-8`.

## Erros

| Quando | O que volta |
| --- | --- |
| Sessão inválida e ausência de autorização interna válida | HTTP 401 — SESSAO_INVALIDA |
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
