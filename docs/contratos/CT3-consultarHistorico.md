# CT3: consultarHistorico

| Linha | Valor |
| :--- | :--- |
| **Fronteira** | HTTP |
| **Quem chama** | Frontend |
| **Quem responde** | Backend 1 |
| **Atende** | E3 |
| **Como chamar** | `GET /clientes/{clienteId}/servicos` |

## Entrada
| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| clienteId | inteiro | sim | 42 |

## Saída
| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| servicos | lista de (objeto) | sim | lista com os atendimentos do cliente |

## Erros
| Quando | O que volta |
| :--- | :--- |
| Cliente não existe | Erro `CLIENTE_NAO_ENCONTRADO` |
