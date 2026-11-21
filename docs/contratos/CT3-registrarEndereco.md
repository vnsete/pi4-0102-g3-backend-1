# CT3 registrarEndereco

| Linha | Valor |
| --- | --- |
| ID e nome | CT3 registrarEndereco |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend 1 |
| Atende | E2 |
| Como chamar | POST /clientes/{clienteId}/enderecos |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| clienteId | inteiro | sim | 1 |
| logradouro | texto | sim | Rua das Flores |
| numero | texto | sim | 120 |
| complemento | texto | não | Casa 2 |
| bairro | texto | sim | Centro |
| cidade | texto | sim | Campinas |
| uf | texto | sim | SP |
| cep | texto | sim | 13010000 |

## Saída

Sucesso: HTTP 201.

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| enderecoId | inteiro | sim | 10 |
| clienteId | inteiro | sim | 1 |
| logradouro | texto | sim | Rua das Flores |
| numero | texto | sim | 120 |
| complemento | texto | não | Casa 2 |
| bairro | texto | sim | Centro |
| cidade | texto | sim | Campinas |
| uf | texto | sim | SP |
| cep | texto | sim | 13010000 |
| criadoEm | data e hora | sim | 2026-10-13T09:01:00-03:00 |

## Cabeçalhos de entrada

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| Cookie servihubSessao | texto | sim | 64 caracteres hexadecimais do token |
| Origin | texto | sim | https://servihub.example.com |
| Content-Type | texto | sim | application/json |

## Regras

clienteId vem do caminho; demais campos vêm do corpo JSON. Logradouro, bairro e cidade: 1 a 120 caracteres; número: 1 a 20 (aceita S/N); complemento: até 120; uf: duas letras maiúsculas de uma UF brasileira; cep: oito dígitos. O backend gera enderecoId e criadoEm. O Frontend bloqueia o botão de envio até a resposta e não repete POST automaticamente. Se a conexão cair após enviar, a tela informa resultado não confirmado e consulta a lista antes de permitir outra gravação; não afirma que a operação falhou. Regras de sessão, cabeçalhos, origem, timeout e limites seguem a visão geral. O corpo JSON não contém o token de sessão. Resposta contém Content-Type: application/json e Cache-Control: no-store.

Padrões de campos, datas, valores, rede e erros seguem docs/fluxos/visao-geral.md no Frontend. Campo recebido fora do contrato é rejeitado com 400 DADOS_INVALIDOS.

## Erros

| Quando | O que volta |
| --- | --- |
| Cliente não existe | 404 CLIENTE_NAO_ENCONTRADO |
| Campos do endereço inválidos | 400 DADOS_INVALIDOS |
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

