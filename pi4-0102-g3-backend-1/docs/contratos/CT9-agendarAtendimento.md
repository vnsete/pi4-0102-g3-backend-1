# CT9 agendarAtendimento

| Linha | Valor |
| --- | --- |
| ID e nome | CT9 agendarAtendimento |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend 1 |
| Atende | E6 |
| Como chamar | POST /agendamentos |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| servicoId | inteiro | sim | 20 |
| inicioEm | data e hora | sim | 2026-10-14T10:00:00-03:00 |
| fimEm | data e hora | sim | 2026-10-14T11:00:00-03:00 |

## Saída

Sucesso: HTTP 201.

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| agendamentoId | inteiro | sim | 30 |
| servicoId | inteiro | sim | 20 |
| inicioEm | data e hora | sim | 2026-10-14T10:00:00-03:00 |
| fimEm | data e hora | sim | 2026-10-14T11:00:00-03:00 |
| criadoEm | data e hora | sim | 2026-10-13T09:03:00-03:00 |
| atualizadoEm | data e hora | sim | 2026-10-13T09:03:00-03:00 |

## Cabeçalhos de entrada

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| Cookie servihubSessao | texto | sim | 64 caracteres hexadecimais do token |
| Origin | texto | sim | https://servihub.example.com |
| Content-Type | texto | sim | application/json |

## Regras

Serviço deve estar registrado e sem agendamento. Início deve ser igual ou posterior ao instante atual; fim deve ser posterior ao início. A instalação atende um prestador e aceita um agendamento por serviço. Antes da transação, o Backend 1 obtém no MySQL o bloqueio nomeado servihub_agenda com espera máxima de cinco segundos, usando a mesma conexão até liberá-lo. Depois abre a transação, bloqueia o serviço para atualização e verifica conflitos com serviços agendado ou em_andamento. Há conflito quando existente.inicio_em < novo.fimEm e existente.fim_em > novo.inicioEm; horários encostados não conflitam. Cria o agendamento e atualiza o serviço para agendado na mesma transação. Commit ou rollback precede a liberação do bloqueio, sempre em finally. Todos os processos do Backend 1 usam o mesmo nome de bloqueio. Concluídos e cancelados não ocupam a agenda. Reagendamento e múltiplas visitas do mesmo serviço ficam fora do MVP. O Frontend bloqueia o botão de envio até a resposta e não repete POST automaticamente. Se a conexão cair após enviar, a tela informa resultado não confirmado e consulta a lista antes de permitir outra gravação; não afirma que a operação falhou. Regras de sessão, cabeçalhos, origem, timeout e limites seguem a visão geral. O corpo JSON não contém o token de sessão. Resposta contém Content-Type: application/json e Cache-Control: no-store.

Padrões de campos, datas, valores, rede e erros seguem docs/fluxos/visao-geral.md no Frontend. Campo recebido fora do contrato é rejeitado com 400 DADOS_INVALIDOS.

## Erros

| Quando | O que volta |
| --- | --- |
| Serviço não existe | 404 SERVICO_NAO_ENCONTRADO |
| Data inválida, início passado ou fim não posterior | 400 HORARIO_INVALIDO |
| Serviço já agendado ou status incompatível | 409 SERVICO_NAO_AGENDAVEL |
| Outro atendimento ativo ocupa parte do intervalo | 409 CONFLITO_DE_AGENDA |
| Bloqueio da agenda não foi obtido em cinco segundos | 503 AGENDA_TEMPORARIAMENTE_INDISPONIVEL |
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

