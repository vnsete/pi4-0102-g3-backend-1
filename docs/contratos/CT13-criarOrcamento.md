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
| clienteId | inteiro | sim | 1 |
| servicoId | inteiro | não | 20 |
| descricao | texto | sim | Limpeza da piscina e materiais |
| valorTotal | decimal | sim | 150.00 |

## Saída

Sucesso: HTTP 201.

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| orcamentoId | inteiro | sim | 40 |
| clienteId | inteiro | sim | 1 |
| servicoId | inteiro | não | 20 |
| descricao | texto | sim | Limpeza da piscina e materiais |
| valorTotal | decimal | sim | 150.00 |
| criadoEm | data e hora | sim | 2026-10-13T09:04:00-03:00 |

## Cabeçalhos de entrada

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| Cookie servihubSessao | texto | sim | 64 caracteres hexadecimais do token |
| Origin | texto | sim | https://servihub.example.com |
| Content-Type | texto | sim | application/json |

## Regras

Cliente deve existir; serviço, se informado, deve existir e pertencer ao cliente. Descrição: 1 a 500 caracteres. valorTotal positivo, até duas casas decimais. Valor é informado pelo prestador; o MVP não calcula impostos nem emite documentos fiscais. Backend gera orcamentoId e criadoEm. Campos opcionais não preenchidos são omitidos; string vazia ou null enviados explicitamente são rejeitados. Os vínculos são verificados dentro da transação de gravação. O Frontend bloqueia o botão de envio até a resposta e não repete POST automaticamente. Se a conexão cair após enviar, a tela informa resultado não confirmado e consulta a lista antes de permitir outra gravação; não afirma que a operação falhou. Regras de sessão, cabeçalhos, origem, timeout e limites seguem a visão geral. O corpo JSON não contém o token de sessão. Resposta contém Content-Type: application/json e Cache-Control: no-store.

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

