
# CT14 consultarOrcamentos

| Linha | Valor |
| --- | --- |
| ID e nome | CT14 consultarOrcamentos |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend 1 |
| Atende | E8 |
| Como chamar | GET /orcamentos |

## Entrada

Não possui parâmetros obrigatórios nem corpo de requisição.

## Saída

Sucesso: HTTP 200.

A API retorna diretamente um array JSON de orçamentos.

Cada objeto contém os campos da tabela `orcamento` e o nome do cliente associado.

| Campo | Tipo | Descrição |
| --- | --- | --- |
| id | inteiro | Identificador do orçamento |
| cliente_id | inteiro | Identificador do cliente |
| catalogo_id | inteiro | Identificador do item do catálogo |
| servico_id | inteiro ou nulo | Vínculo opcional existente no banco |
| descricao | texto | Descrição do orçamento |
| valor | decimal | Valor do orçamento |
| status | texto | Situação do orçamento |
| servico_aprovado_id | inteiro ou nulo | Serviço gerado após aprovação |
| cliente | texto | Nome do cliente |

## Cabeçalhos de entrada

| Campo | Tipo | Obrigatório |
| --- | --- | --- |
| Cookie | texto | Sim, para autenticação por sessão |

O navegador utiliza o cookie `servihubSessao`.

## Regras

O endpoint `GET /orcamentos` consulta todos os orçamentos cadastrados, independentemente do cliente.

A consulta utiliza a tabela `orcamento`, associada à tabela `cliente` por meio de `cliente_id`.

O resultado é ordenado pelo identificador do orçamento (`id`) em ordem decrescente.

O endpoint não realiza filtragem por cliente.

Quando não existem orçamentos, retorna um array JSON vazio.

O acesso exige uma sessão válida ou autorização interna, conforme o mecanismo de autenticação do Backend 1.

A resposta possui Content-Type `application/json; charset=utf-8`.

## Erros

| Quando | O que volta |
| --- | --- |
| Sessão inválida ou expirada, sem autorização interna | HTTP 401 — SESSAO_INVALIDA |
| Rota inexistente | HTTP 404 — ROTA_NAO_ENCONTRADA |
| Exceção durante o processamento | HTTP 400 — REQUISICAO_INVALIDA |

## Objeto de erro

Os erros utilizam um objeto JSON com o campo `erro`.

Exemplo:

```json
{
  "erro": "SESSAO_INVALIDA"
}
```
