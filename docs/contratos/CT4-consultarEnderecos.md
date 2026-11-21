# CT4 consultarEnderecos

| Linha | Valor |
| --- | --- |
| ID e nome | CT4 consultarEnderecos |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend 1 |
| Atende | E2 |
| Como chamar | GET /clientes/{clienteId}/enderecos |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| clienteId | inteiro | sim | 1 |

## Saída

Sucesso: HTTP 200.

| Campo | Tipo | Obrigatório | Exemplo |
| --- | --- | --- | --- |
| enderecos | lista de objeto Endereco | sim | ver objeto Endereco |

### Objeto Endereco

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

## Regras

Confere se o cliente existe; retorna seus endereços por enderecoId crescente. Cliente sem endereço retorna lista vazia. Regras de sessão, cabeçalhos, origem, timeout e limites seguem a visão geral. O corpo JSON não contém o token de sessão. Resposta contém Content-Type: application/json e Cache-Control: no-store.

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

