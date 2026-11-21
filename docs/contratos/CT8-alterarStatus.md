# CT8 alterarStatus

| Linha | Valor |
| --- | --- |
| ID e nome | CT8 alterarStatus |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend 1 |
| Atende | E5 |
| Como chamar | PATCH /servicos/{servicoId}/status |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| servicoId | inteiro | sim | 20 |
| status | um de: registrado, agendado, em_andamento, concluido, cancelado | sim | em_andamento |

## Saída

Sucesso: HTTP 200.

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| servicoId | inteiro | sim | 20 |
| clienteId | inteiro | sim | 1 |
| enderecoId | inteiro | não | 10 |
| descricao | texto | sim | Limpeza da piscina |
| valorPrevisto | decimal | não | 150.00 |
| status | um de: registrado, agendado, em_andamento, concluido, cancelado | sim | em_andamento |
| criadoEm | data e hora | sim | 2026-10-13T09:02:00-03:00 |
| atualizadoEm | data e hora | sim | 2026-10-13T09:06:00-03:00 |

## Cabeçalhos de entrada

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| Cookie servihubSessao | texto | sim | 64 caracteres hexadecimais do token |
| Origin | texto | sim | https://servihub.example.com |
| Content-Type | texto | sim | application/json |

## Regras

servicoId vem do caminho; status vem do corpo. Transições permitidas: registrado para em_andamento ou cancelado; agendado para em_andamento ou cancelado; em_andamento para concluido ou cancelado. Somente CT9 cria a transição registrado para agendado, junto da gravação da visita. Concluido e cancelado são finais. Repetir o status atual devolve o registro sem mudar a data. A transação bloqueia a linha do serviço para leitura e atualização; duas alterações concorrentes são reavaliadas em sequência contra o status atual. Não aceita retorno a uma etapa anterior. Regras de sessão, cabeçalhos, origem, timeout e limites seguem a visão geral. O corpo JSON não contém o token de sessão. Resposta contém Content-Type: application/json e Cache-Control: no-store.

Padrões de campos, datas, valores, rede e erros seguem docs/fluxos/visao-geral.md no Frontend. Campo recebido fora do contrato é rejeitado com 400 DADOS_INVALIDOS.

## Erros

| Quando | O que volta |
| --- | --- |
| Serviço não existe | 404 SERVICO_NAO_ENCONTRADO |
| Valor fora da enumeração | 400 STATUS_INVALIDO |
| Transição não permitida; agendado só pode ser definido por CT9 | 409 TRANSICAO_INVALIDA |
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

