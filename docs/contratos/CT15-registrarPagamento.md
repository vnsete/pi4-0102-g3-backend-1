# CT15 registrarPagamento

| Linha | Valor |
| --- | --- |
| ID e nome | CT15 registrarPagamento |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend 1 |
| Atende | E9 |
| Como chamar | PUT /servicos/{servicoId}/pagamento |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| servicoId | inteiro | sim | 20 |
| valor | decimal | sim | 150.00 |
| situacao | um de: pendente, pago | sim | pendente |

## Saída

Sucesso: HTTP 200.

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| pagamentoId | inteiro | sim | 50 |
| servicoId | inteiro | sim | 20 |
| valor | decimal | sim | 150.00 |
| situacao | um de: pendente, pago | sim | pendente |
| pagoEm | data e hora | não | omitido quando pendente; presente quando pago |
| atualizadoEm | data e hora | sim | 2026-10-13T09:05:00-03:00 |

## Cabeçalhos de entrada

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| Cookie servihubSessao | texto | sim | 64 caracteres hexadecimais do token |
| Origin | texto | sim | https://servihub.example.com |
| Content-Type | texto | sim | application/json |

## Regras

Um registro de pagamento por serviço. Cria ou atualiza por servicoId e sempre retorna HTTP 200. Valor positivo com até duas casas decimais representa o valor total controlado manualmente. Ao mudar para pago define pagoEm=agora; ao mudar para pendente remove pagoEm. Repetição do mesmo valor e situação conserva as datas. Não processa cobranças, parcelas ou pagamentos parciais. Backend gera pagamentoId e atualizadoEm. A atualização bloqueia a linha do serviço antes de consultar e gravar o pagamento, de modo que a primeira criação concorrente não duplique o registro. Alterar o valor de um pagamento já pago preserva pagoEm; somente a transição de pendente para pago define uma nova data. As gravações manuais concorrentes são serializadas; a última gravação confirmada define o valor e a situação, e a interface deve consultar o registro após salvar. Regras de sessão, cabeçalhos, origem, timeout e limites seguem a visão geral. O corpo JSON não contém o token de sessão. Resposta contém Content-Type: application/json e Cache-Control: no-store.

Padrões de campos, datas, valores, rede e erros seguem docs/fluxos/visao-geral.md no Frontend. Campo recebido fora do contrato é rejeitado com 400 DADOS_INVALIDOS.

## Erros

| Quando | O que volta |
| --- | --- |
| Serviço não existe | 404 SERVICO_NAO_ENCONTRADO |
| Valor ou situação inválidos | 400 DADOS_INVALIDOS |
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

