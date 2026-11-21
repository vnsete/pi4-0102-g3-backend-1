# CT10 consultarAgenda

| Linha | Valor |
| --- | --- |
| ID e nome | CT10 consultarAgenda |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend 1 |
| Atende | E6 |
| Como chamar | GET /agenda?de={de}&ate={ate} |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| de | data e hora | sim | 2026-10-14T00:00:00-03:00 |
| ate | data e hora | sim | 2026-10-15T00:00:00-03:00 |

## Saída

Sucesso: HTTP 200.

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| atendimentos | lista de objeto Atendimento | sim | ver objeto Atendimento |

### Objeto Atendimento

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| agendamentoId | inteiro | sim | 30 |
| servicoId | inteiro | sim | 20 |
| clienteId | inteiro | sim | 1 |
| nomeCliente | texto | sim | Carlos Mendes |
| enderecoId | inteiro | não | 10 |
| descricao | texto | sim | Limpeza da piscina |
| inicioEm | data e hora | sim | 2026-10-14T10:00:00-03:00 |
| fimEm | data e hora | sim | 2026-10-14T11:00:00-03:00 |
| statusServico | um de: registrado, agendado, em_andamento, concluido, cancelado | sim | agendado |

## Cabeçalhos de entrada

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| Cookie servihubSessao | texto | sim | 64 caracteres hexadecimais do token |

## Regras

Parâmetros na query; de deve ser menor que ate. Retorna os agendamentos com inicioEm maior ou igual a de e menor que ate, ordenados por inicioEm e agendamentoId. Inclui cancelados, identificados por statusServico, para preservar a consulta do registro. O Frontend monta de e ate no fuso America/Sao_Paulo e codifica o offset na URL (o sinal + deve ser %2B). O filtro por início é deliberado: uma visita iniciada antes de de aparece na consulta do dia de seu início. Backend rejeita intervalos superiores a 31 dias para limitar a consulta. Regras de sessão, cabeçalhos, origem, timeout e limites seguem a visão geral. O corpo JSON não contém o token de sessão. Resposta contém Content-Type: application/json e Cache-Control: no-store.

Padrões de campos, datas, valores, rede e erros seguem docs/fluxos/visao-geral.md no Frontend. Campo recebido fora do contrato é rejeitado com 400 DADOS_INVALIDOS.

## Erros

| Quando | O que volta |
| --- | --- |
| Intervalo ou formato de data inválido | 400 INTERVALO_INVALIDO |
| Cookie ausente, sessão expirada ou invalidada | 401 SESSAO_INVALIDA |
| ID, campo obrigatório, tipo ou JSON inválido | 400 DADOS_INVALIDOS |
| Banco indisponível ou transação falha | 503 BANCO_INDISPONIVEL |
| Falha inesperada | 500 ERRO_INTERNO |

### Objeto Erro

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| codigo | texto | sim | DADOS_INVALIDOS |
| mensagem | texto | sim | Confira os campos informados. |

