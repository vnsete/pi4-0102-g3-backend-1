# CT5 consultarHistorico

| Linha | Valor |
| --- | --- |
| ID e nome | CT5 consultarHistorico |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend 1 |
| Atende | E3 |
| Como chamar | GET /clientes/{clienteId}/historico |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| clienteId | inteiro | sim | 1 |

## Saída

Sucesso: HTTP 200.

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| servicos | lista de objeto Servico | sim | ver objeto Servico |

### Objeto Servico

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| servicoId | inteiro | sim | 20 |
| clienteId | inteiro | sim | 1 |
| enderecoId | inteiro | não | 10 |
| descricao | texto | sim | Limpeza da piscina |
| valorPrevisto | decimal | não | 150.00 |
| status | um de: registrado, agendado, em_andamento, concluido, cancelado | sim | registrado |
| criadoEm | data e hora | sim | 2026-10-13T09:02:00-03:00 |
| atualizadoEm | data e hora | sim | 2026-10-13T09:02:00-03:00 |

## Cabeçalhos de entrada

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| Cookie servihubSessao | texto | sim | 64 caracteres hexadecimais do token |

## Regras

Retorna todos os serviços vinculados ao cliente, inclusive registrados, agendados e cancelados, por criadoEm decrescente e servicoId decrescente. Cliente existente sem serviços retorna lista vazia. Não limita o histórico aos concluídos. Regras de sessão, cabeçalhos, origem, timeout e limites seguem a visão geral. O corpo JSON não contém o token de sessão. Resposta contém Content-Type: application/json e Cache-Control: no-store.

Padrões de campos, datas, valores, rede e erros seguem docs/fluxos/visao-geral.md no Frontend. Campo recebido fora do contrato é rejeitado com 400 DADOS_INVALIDOS.

## Erros

| Quando | O que volta |
| --- | --- |
| Cliente não existe | 404 CLIENTE_NAO_ENCONTRADO |
| Cookie ausente, sessão expirada ou invalidada | 401 SESSAO_INVALIDA |
| ID, campo obrigatório, tipo ou JSON inválido | 400 DADOS_INVALIDOS |
| Banco indisponível ou transação falha | 503 BANCO_INDISPONIVEL |
| Falha inesperada | 500 ERRO_INTERNO |

### Objeto Erro

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| codigo | texto | sim | DADOS_INVALIDOS |
| mensagem | texto | sim | Confira os campos informados. |

