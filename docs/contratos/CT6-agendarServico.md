# CT6: agendarServico

| Linha | Valor |
| :--- | :--- |
| **Fronteira** | HTTP |
| **Quem chama** | Frontend |
| **Quem responde** | Backend 1 |
| **Atende** | E6 |
| **Como chamar** | `POST /agendamentos` |

## Entrada
| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| clienteId | inteiro | sim | 42 |
| descricao | texto | sim | Visita de vistoria |
| dataHora | data e hora | sim | 2026-10-15T14:30:00 |

## Saída
| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| id | inteiro | sim | 7 |
| dataHora | data e hora | sim | 2026-10-15T14:30:00 |

## Erros
| Quando | O que volta |
| :--- | :--- |
| Horário indisponível | Erro `HORARIO_INDISPONIVEL` |
