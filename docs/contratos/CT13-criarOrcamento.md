
# CT13 criarOrcamento

| Linha | Valor |
| --- | --- |
| ID e nome | CT13 criarOrcamento |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend 1 |
| Atende | E8 |
| Como chamar | POST /orcamentos |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| cliente_id | inteiro | sim | 1 |
| catalogo_id | inteiro | sim | 20 |
| valor | decimal | não | 150.00 |

## Saída

Sucesso: HTTP 201.

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| id | inteiro | sim | 40 |

A resposta é um objeto JSON contendo o identificador do orçamento criado.

## Cabeçalhos de entrada

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| Cookie | texto | sim | servihubSessao=token |
| Content-Type | texto | recomendado | application/json |

O token de sessão é enviado pelo navegador no cookie e não no corpo JSON.

## Regras

O cliente informado em `cliente_id` deve existir no banco de dados.

O item informado em `catalogo_id` deve existir na tabela `catalogo`.

A descrição do orçamento é obtida automaticamente do item do catálogo selecionado.

O campo `valor` é opcional. Quando não informado ou enviado em branco, o Backend 1 utiliza o valor cadastrado no catálogo.

Valores negativos são rejeitados com HTTP 400 e código `VALOR_INVALIDO`. O código permite valor igual a zero.

Após a criação, o Backend 1 registra o orçamento no banco e retorna HTTP 201 com o identificador `id`.

A aprovação do orçamento é uma operação separada, realizada por `PUT /orcamentos/{id}/aprovar`. Quando aprovado, o sistema cria o serviço e o pagamento pendente, vinculados ao orçamento.

O endpoint exige uma sessão válida, identificada pelo cookie `servihubSessao`, ou autorização por chave interna conforme a implementação do Backend 1.

## Erros

| Quando | O que volta |
| --- | --- |
| Cliente não existe | HTTP 404 — CLIENTE_NAO_ENCONTRADO |
| Item do catálogo não existe | HTTP 404 — SERVICO_DO_CATALOGO_NAO_ENCONTRADO |
| Valor negativo | HTTP 400 — VALOR_INVALIDO |
| Sessão inválida ou expirada, sem autorização interna | HTTP 401 — SESSAO_INVALIDA |
| Dados inválidos ou exceção durante o processamento | HTTP 400 — REQUISICAO_INVALIDA |

## Objeto de erro

A implementação retorna os erros em formato JSON:

```json
{
  "erro": "CLIENTE_NAO_ENCONTRADO"
}
```

O campo `erro` contém o código correspondente à falha.
