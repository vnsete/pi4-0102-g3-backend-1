# CT5: atualizarStatusServico

| Linha | Valor |
| :--- | :--- |
| **Fronteira** | HTTP |
| **Quem chama** | Frontend |
| **Quem responde** | Backend 1 |
| **Atende** | E5 |
| **Como chamar** | `PATCH /servicos/{id}/etapa` |

## Entrada
| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| etapa | um de: aguardando_peca, em_execucao, pronto | sim | pronto |

## Saída
| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| id | inteiro | sim | 10 |
| etapa | um de: aguardando_peca, em_execucao, pronto | sim | pronto |
| atualizadaEm | data e hora | sim | 2026-10-13T20:00:00 |

## Erros
| Quando | O que volta |
| :--- | :--- |
| Serviço não existe | Erro `SERVICO_NAO_ENCONTRADO` |
