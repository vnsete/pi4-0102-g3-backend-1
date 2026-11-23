
# CT22 agendamento

| Linha | Valor |
| --- | --- |
| ID e nome | CT22 agendamento |
| Fronteira | Banco de dados |
| Quem chama | Backend 1 |
| Quem responde | MySQL 9.2 |
| Atende | E6, E7 |
| Como chamar | Tabela agendamento, via JDBC |

## Estrutura da tabela

| Campo | Tipo SQL | Restrição | Descrição |
| --- | --- | --- | --- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Identificador do agendamento |
| servico_id | BIGINT | NOT NULL, FOREIGN KEY | Serviço agendado |
| inicio | DATETIME | NOT NULL | Data e hora de início |
| fim | DATETIME | NOT NULL | Data e hora de término |

## Regras

A tabela `agendamento` armazena os horários dos atendimentos associados aos serviços cadastrados no ServiHUB.

O campo `id` é gerado automaticamente pelo MySQL.

O campo `servico_id` referencia `servico.id`, estabelecendo um relacionamento entre serviços e agendamentos.

A tabela é criada pelo script `db/schema.sql` do repositório principal.

O banco não possui uma restrição UNIQUE para `servico_id`. Entretanto, o Backend 1 verifica se o serviço já possui um agendamento antes de permitir outro.

O cadastro utiliza `POST /agendamentos`, recebendo `servico_id` e `inicio`.

O Backend 1 calcula automaticamente o campo `fim`, somando ao horário inicial a duração registrada em `servico.duracao_minutos`.

O horário inicial não pode estar no passado, considerando o fuso `America/Sao_Paulo`.

Serviços com status `concluido`, `cancelado` ou `em_andamento` não podem ser agendados.

Para agendar, o serviço precisa estar vinculado a um orçamento aprovado ou possuir uma data de atendimento previamente cadastrada, que deve coincidir com a data do agendamento.

Antes de inserir, o Backend 1 verifica se o serviço já possui agendamento e se existe sobreposição com outros horários não cancelados.

A verificação de conflitos considera os intervalos de início e fim dos agendamentos existentes.

O método de agendamento utiliza sincronização no Backend 1 e transação no banco de dados, incluindo o bloqueio do registro do serviço por `SELECT ... FOR UPDATE`.

Após o cadastro, o serviço recebe status `agendado`, e sua coluna `data_atendimento` é atualizada.

A operação retorna HTTP 201 com o identificador `id` do agendamento criado.

A consulta da agenda utiliza `GET /agenda`, enquanto o Backend 2 obtém visitas por meio de `GET /interno/visitas` no Backend 1.

## Erros e validações

| Situação | Comportamento |
| --- | --- |
| Horário inicial no passado | HTTP 400 — Não é permitido agendar no passado |
| Serviço inexistente | HTTP 404 — Serviço não encontrado |
| Serviço indisponível para agendamento | HTTP 409 — Este serviço não está disponível para agendamento |
| Serviço sem orçamento aprovado e sem data correspondente | HTTP 400 — Use a data cadastrada no serviço ou aprove um orçamento para agendar |
| Serviço já agendado | HTTP 409 — Este atendimento já está agendado |
| Sobreposição de horários | HTTP 409 — Esse horário já está ocupado. Escolha outro horário |
| Sessão inválida, sem autorização interna | HTTP 401 — SESSAO_INVALIDA |
| Exceção durante o processamento | HTTP 400 — REQUISICAO_INVALIDA, conforme tratamento geral atual |

## Persistência

Os dados são armazenados no MySQL por meio de JDBC.

A definição da tabela está no arquivo `db/schema.sql` do repositório principal.

A coluna `servico_id` possui chave estrangeira para `servico.id`. A prevenção de agendamentos duplicados e conflitos de horários é realizada pelo Backend 1, não por uma restrição UNIQUE no banco.
