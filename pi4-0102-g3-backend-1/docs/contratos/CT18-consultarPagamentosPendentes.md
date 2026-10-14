# CT18 consultarPagamentosPendentes

| Linha | Valor |
| --- | --- |
| ID e nome | CT18 consultarPagamentosPendentes |
| Fronteira | HTTP |
| Quem chama | Backend 2 |
| Quem responde | Backend 1 |
| Atende | E10 |
| Como chamar | GET /interno/pagamentos-pendentes |

## Entrada

Não tem.

## Saída

Sucesso: HTTP 200.

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| pendencias | lista de objeto Pendencia | sim | ver objeto Pendencia |

### Objeto Pendencia

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| pagamentoId | inteiro | sim | 50 |
| servicoId | inteiro | sim | 20 |
| clienteId | inteiro | sim | 1 |
| nomeCliente | texto | sim | Carlos Mendes |
| descricao | texto | sim | Limpeza da piscina |
| valor | decimal | sim | 150.00 |
| situacao | um de: pendente, pago | sim | pendente |
| atualizadoEm | data e hora | sim | 2026-10-13T09:05:00-03:00 |

## Cabeçalhos de entrada

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| Cookie servihubSessao | texto | sim | 64 caracteres hexadecimais do token |
| X-ServiHUB-Internal | texto | sim | segredo configurado nos backends |

## Regras

Interface interna. Junta pagamento, serviço e cliente; retorna somente pagamentos com situacao=pendente, por atualizadoEm decrescente e pagamentoId decrescente. Sem registros pendentes retorna lista vazia. situacao mantém a enumeração de CT15 e CT16, embora este filtro só devolva pendente. O Backend 1 valida a sessão encaminhada antes da leitura; sessão inválida retorna 401. Regras de sessão, cabeçalhos, origem, timeout e limites seguem a visão geral. O corpo JSON não contém o token de sessão. Resposta contém Content-Type: application/json e Cache-Control: no-store.

Padrões de campos, datas, valores, rede e erros seguem docs/fluxos/visao-geral.md no Frontend. Campo recebido fora do contrato é rejeitado com 400 DADOS_INVALIDOS.

## Erros

| Quando | O que volta |
| --- | --- |
| Cookie ausente, sessão expirada ou invalidada | 401 SESSAO_INVALIDA |
| Segredo interno ausente ou inválido | 403 CHAMADA_INTERNA_NAO_AUTORIZADA |
| ID, campo obrigatório, tipo ou JSON inválido | 400 DADOS_INVALIDOS |
| Banco indisponível ou transação falha | 503 BANCO_INDISPONIVEL |
| Falha inesperada | 500 ERRO_INTERNO |

### Objeto Erro

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| codigo | texto | sim | DADOS_INVALIDOS |
| mensagem | texto | sim | Confira os campos informados. |

