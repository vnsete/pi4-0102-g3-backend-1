# CT4: cadastrarServico

| Linha | Valor |
| :--- | :--- |
| **Fronteira** | HTTP |
| **Quem chama** | Frontend |
| **Quem responde** | Backend 1 |
| **Atende** | E4 |
| **Como chamar** | `POST /servicos` |

## Entrada
| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| clienteId | inteiro | sim | 42 |
| descricao | texto | sim | Manutenção preventiva de piscina |
| etapa | um de: aguardando_peca, em_execucao, pronto | sim | aguardando_peca |

## Saída
| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| id | inteiro | sim | 10 |
| clienteId | inteiro | sim | 42 |
| descricao | texto | sim | Manutenção preventiva de piscina |
| etapa | um de: aguardando_peca, em_execucao, pronto | sim | aguardando_peca |
| abertaEm | data e hora | sim | 2026-10-13T19:20:00 |

## Erros
| Quando | O que volta |
| :--- | :--- |
| Cliente não encontrado | Erro `CLIENTE_NAO_ENCONTRADO` |
