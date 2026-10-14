# CT12 consultarVisitas

| Linha | Valor |
| --- | --- |
| ID e nome | CT12 consultarVisitas |
| Fronteira | HTTP |
| Quem chama | Backend 2 |
| Quem responde | Backend 1 |
| Atende | E7 |
| Como chamar | GET /interno/visitas?de={de}&ate={ate} |

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
| X-ServiHUB-Internal | texto | sim | segredo configurado nos backends |

## Regras

Interface interna. de menor que ate; inclui inicioEm no intervalo [de, ate) e apenas serviços em status agendado. Junta agendamento, serviço e cliente. Ordena por inicioEm e agendamentoId. A mesma estrutura Atendimento é usada por CT10. Cookie de sessão recebido do Backend 2 é validado pelo Backend 1 antes da leitura. O intervalo não pode exceder 24 horas. Uma sessão inválida retorna 401, sem consultar os dados. Regras de sessão, cabeçalhos, origem, timeout e limites seguem a visão geral. O corpo JSON não contém o token de sessão. Resposta contém Content-Type: application/json e Cache-Control: no-store.

Padrões de campos, datas, valores, rede e erros seguem docs/fluxos/visao-geral.md no Frontend. Campo recebido fora do contrato é rejeitado com 400 DADOS_INVALIDOS.

## Erros

| Quando | O que volta |
| --- | --- |
| Intervalo inválido | 400 INTERVALO_INVALIDO |
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

