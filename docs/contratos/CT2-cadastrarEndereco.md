# CT2: cadastrarEndereco

| Linha | Valor |
| :--- | :--- |
| **Fronteira** | HTTP |
| **Quem chama** | Frontend |
| **Quem responde** | Backend 1 |
| **Atende** | E2 |
| **Como chamar** | `POST /clientes/{clienteId}/enderecos` |

## Entrada
| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| cep | texto | sim | 13010100 |
| logradouro | texto | sim | Rua 15 de Novembro |
| numero | texto | sim | 1000 |
| bairro | texto | sim | Centro |
| cidade | texto | sim | Campinas |
| estado | texto | sim | SP |

## Saída
| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| id | inteiro | sim | 15 |
| clienteId | inteiro | sim | 42 |
| logradouro | texto | sim | Rua 15 de Novembro |

## Erros
| Quando | O que volta |
| :--- | :--- |
| Cliente não encontrado | Erro `CLIENTE_NAO_ENCONTRADO` |
