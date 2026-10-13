# CT1: cadastrarCliente

| Linha | Valor |
| :--- | :--- |
| **Fronteira** | HTTP |
| **Quem chama** | Frontend |
| **Quem responde** | Backend 1 |
| **Atende** | E1 |
| **Como chamar** | `POST /clientes` |

## Entrada
| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| nome | texto | sim | João da Silva |
| telefone | texto | sim | +5519999990000 |
| email | texto | não | joao@email.com |

## Saída
| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| id | inteiro | sim | 42 |
| nome | texto | sim | João da Silva |
| criadoEm | data e hora | sim | 2026-10-13T19:20:00 |

## Erros
| Quando | O que volta |
| :--- | :--- |
| Telefone já cadastrado | Erro `TELEFONE_DUPLICADO`|
| Email já cadastrado | Erro `EMAIL_DUPLICADO`|
