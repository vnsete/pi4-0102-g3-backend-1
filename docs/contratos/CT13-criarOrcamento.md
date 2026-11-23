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

## Cabeçalhos de entrada

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| Cookie servihubSessao | texto | sim | 64 caracteres hexadecimais do token |
| Origin | texto | sim | https://servihub.example.com |
| Content-Type | texto | sim | application/json |

## Regras


O cliente informado em cliente_id deve existir. O item informado em catalogo_id deve estar cadastrado no catálogo. A descrição do orçamento é obtida automaticamente do catálogo. O campo valor é opcional; quando não informado, utiliza-se o valor cadastrado no catálogo. Valores negativos são rejeitados com HTTP 400 VALOR_INVALIDO. Após a criação, o Backend 1 retorna HTTP 201 com o identificador id do orçamento gerado.


Padrões de campos, datas, valores, rede e erros seguem docs/fluxos/visao-geral.md no Frontend. Campo recebido fora do contrato é rejeitado com 400 DADOS_INVALIDOS.

## Erros

| Quando | O que volta |
| --- | --- |
| Cliente não existe | 404 CLIENTE_NAO_ENCONTRADO |
| Serviço não existe | 404 SERVICO_NAO_ENCONTRADO |
| Serviço pertence a outro cliente | 409 VINCULO_INVALIDO |
| Descrição ou valor inválidos | 400 DADOS_INVALIDOS |
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

