package br.servihub;

import com.sun.net.httpserver.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.type.TypeReference;
import java.net.*;
import java.io.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import de.mkammerer.argon2.*;

public class Main {

  static ObjectMapper json = new ObjectMapper();
  static Map<String, Instant> sessions = new java.util.concurrent.ConcurrentHashMap<>();

  static String env(String k, String d) {
    return System.getenv().getOrDefault(k, d);
  }

  static Connection db() throws Exception {
    return DriverManager.getConnection(
      env("DB_URL", "jdbc:mysql://localhost:3306/servihub_pdf"),
      env("DB_USER", "servihub"),
      env("DB_PASSWORD", "servihub_local")
    );
  }

  static Map<String, Object> body(HttpExchange x) throws Exception {
    byte[] b = x.getRequestBody().readAllBytes();
    return b.length == 0
      ? new HashMap<>()
      : json.readValue(b, new TypeReference<Map<String, Object>>() {});
  }

  static String s(Map<String, ?> m, String k) {
    return Objects.toString(m.get(k), "").trim();
  }

  static long n(Object v) {
    return Long.parseLong(Objects.toString(v));
  }

  static void send(HttpExchange x, int code, Object value) throws Exception {
    byte[] b = json.writeValueAsBytes(value);
    x.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
    x.sendResponseHeaders(code, b.length);
    try (OutputStream o = x.getResponseBody()) {
      o.write(b);
    }
  }

  static Map<String, Object> err(String code) {
    return Map.of("erro", code);
  }

  static boolean has(Connection c, String table, long id) throws Exception {
    try (PreparedStatement q = c.prepareStatement("SELECT id FROM " + table + " WHERE id=?")) {
      q.setLong(1, id);
      return q.executeQuery().next();
    }
  }

  static List<Map<String, Object>> query(Connection c, String sql, Object... args)
    throws Exception {
    try (PreparedStatement q = c.prepareStatement(sql)) {
      for (int i = 0; i < args.length; i++) q.setObject(i + 1, args[i]);
      try (ResultSet r = q.executeQuery()) {
        List<Map<String, Object>> out = new ArrayList<>();
        ResultSetMetaData md = r.getMetaData();
        while (r.next()) {
          Map<String, Object> row = new LinkedHashMap<>();
          for (int i = 1; i <= md.getColumnCount(); i++) {
            Object v = r.getObject(i);
            row.put(
              md.getColumnLabel(i),
              v instanceof java.sql.Timestamp ||
                v instanceof java.sql.Date ||
                v instanceof java.time.temporal.TemporalAccessor
                ? v.toString()
                : v
            );
          }
          out.add(row);
        }
        return out;
      }
    }
  }

  static Map<String, Object> insert(Connection c, String table, Map<String, ?> m, String... fields)
    throws Exception {
    String cols = String.join(",", fields),
      qs = String.join(",", Collections.nCopies(fields.length, "?"));
    try (
      PreparedStatement q = c.prepareStatement(
        "INSERT INTO " + table + " (" + cols + ") VALUES (" + qs + ")",
        Statement.RETURN_GENERATED_KEYS
      )
    ) {
      for (int i = 0; i < fields.length; i++) {
        Object v = m.get(fields[i]);
        q.setObject(i + 1, v == null || v.toString().isBlank() ? null : v);
      }
      q.executeUpdate();
      try (ResultSet r = q.getGeneratedKeys()) {
        r.next();
        return Map.of("id", r.getLong(1));
      }
    }
  }

  static void init() throws Exception {
    for (int i = 0; i < 40; i++) {
      try (Connection c = db()) {
        migrate(c);
        String email = env("ADMIN_EMAIL", "");
        String pass = env("ADMIN_PASSWORD", "");
        if (email.isBlank() || pass.isBlank()) throw new IllegalStateException(
          "Defina ADMIN_EMAIL e ADMIN_PASSWORD"
        );
        if (query(c, "SELECT id FROM conta WHERE email=?", email).isEmpty()) {
          Argon2 a = Argon2Factory.create();
          String hash = a.hash(3, 65536, 1, pass.toCharArray());
          try (
            PreparedStatement q = c.prepareStatement(
              "INSERT INTO conta(email,senha_hash) VALUES (?,?)"
            )
          ) {
            q.setString(1, email);
            q.setString(2, hash);
            q.executeUpdate();
          }
        }
        return;
      } catch (SQLException ex) {
        Thread.sleep(2000);
      }
    }
    throw new Exception("Banco indisponivel");
  }

  // Acrescenta os campos necessários quando o banco veio de uma versão anterior.
  static void migrate(Connection c) throws Exception {
    try (Statement q = c.createStatement()) {
      q.execute(
        "CREATE TABLE IF NOT EXISTS catalogo(id BIGINT AUTO_INCREMENT PRIMARY KEY,descricao VARCHAR(255) NOT NULL,duracao_minutos INT NOT NULL,valor DECIMAL(12,2) NOT NULL)"
      );
    }
    Map<String, String> columns = Map.of(
      "servico.duracao_minutos",
      "INT NOT NULL DEFAULT 60",
      "servico.data_atendimento",
      "DATE NULL",
      "orcamento.catalogo_id",
      "BIGINT NULL",
      "orcamento.status",
      "VARCHAR(20) NOT NULL DEFAULT 'pendente'",
      "orcamento.servico_aprovado_id",
      "BIGINT NULL"
    );
    for (var entry : columns.entrySet()) {
      String[] parts = entry.getKey().split("\\.");
      if (
        query(
          c,
          "SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=? AND COLUMN_NAME=?",
          parts[0],
          parts[1]
        ).isEmpty()
      ) try (Statement q = c.createStatement()) {
        q.execute("ALTER TABLE " + parts[0] + " ADD COLUMN " + parts[1] + " " + entry.getValue());
      }
    }
  }

  // O horário final vem da duração do serviço. A agenda confere se o intervalo está livre.
  static synchronized void agendar(HttpExchange x, Connection c) throws Exception {
    Map<String, Object> dados = body(x);
    long sid = n(dados.get("servico_id"));
    LocalDateTime inicio = LocalDateTime.parse(s(dados, "inicio"));
    if (
      inicio.isBefore(LocalDateTime.now(ZoneId.of("America/Sao_Paulo")).withSecond(0).withNano(0))
    ) {
      send(x, 400, err("Não é permitido agendar no passado."));
      return;
    }
    c.setAutoCommit(false);
    try {
      List<Map<String, Object>> rows = query(
        c,
        "SELECT duracao_minutos,status,data_atendimento FROM servico WHERE id=? FOR UPDATE",
        sid
      );
      if (rows.isEmpty()) {
        send(x, 404, err("Serviço não encontrado."));
        return;
      }
      Map<String, Object> servico = rows.get(0);
      if (List.of("concluido", "cancelado", "em_andamento").contains(servico.get("status"))) {
        send(x, 409, err("Este serviço não está disponível para agendamento."));
        return;
      }
      boolean aprovado = !query(
        c,
        "SELECT id FROM orcamento WHERE servico_aprovado_id=? AND status='aprovado'",
        sid
      ).isEmpty();
      // Serviços que já tinham uma data antes desse fluxo continuam usando essa data.
      Object diaAnterior = servico.get("data_atendimento");
      if (
        !aprovado &&
        (diaAnterior == null || !inicio.toLocalDate().toString().equals(diaAnterior.toString()))
      ) {
        send(x, 400, err("Use a data cadastrada no serviço ou aprove um orçamento para agendar."));
        return;
      }
      if (!query(c, "SELECT id FROM agendamento WHERE servico_id=?", sid).isEmpty()) {
        send(x, 409, err("Este atendimento já está agendado."));
        return;
      }
      Timestamp ini = Timestamp.valueOf(inicio);
      Timestamp fim = Timestamp.valueOf(inicio.plusMinutes(n(servico.get("duracao_minutos"))));
      if (
        !query(
          c,
          "SELECT a.id FROM agendamento a JOIN servico s ON s.id=a.servico_id WHERE s.status<>'cancelado' AND a.inicio < ? AND a.fim > ?",
          fim,
          ini
        ).isEmpty()
      ) {
        send(x, 409, err("Esse horário já está ocupado. Escolha outro horário."));
        return;
      }
      dados.put("inicio", ini);
      dados.put("fim", fim);
      Map<String, Object> result = insert(c, "agendamento", dados, "servico_id", "inicio", "fim");
      try (
        PreparedStatement q = c.prepareStatement(
          "UPDATE servico SET status='agendado',data_atendimento=? WHERE id=?"
        )
      ) {
        q.setObject(1, inicio.toLocalDate());
        q.setLong(2, sid);
        q.executeUpdate();
      }
      c.commit();
      send(x, 201, result);
    } finally {
      c.rollback();
    }
  }

  static void route(HttpExchange x) {
    try {
      String path = x.getRequestURI().getPath(),
        method = x.getRequestMethod();
      if (method.equals("OPTIONS")) {
        x.sendResponseHeaders(204, -1);
        return;
      }
      if (path.equals("/health")) {
        send(x, 200, Map.of("ok", true));
        return;
      }
      if (path.equals("/sessao") && method.equals("POST")) {
        Map<String, Object> m = body(x);
        try (Connection c = db()) {
          List<Map<String, Object>> rows = query(
            c,
            "SELECT id,senha_hash FROM conta WHERE email=?",
            s(m, "email")
          );
          if (
            rows.isEmpty() ||
            !Argon2Factory.create().verify(
              rows.get(0).get("senha_hash").toString(),
              s(m, "senha").toCharArray()
            )
          ) {
            send(x, 401, err("CREDENCIAIS_INVALIDAS"));
            return;
          }
          String token = UUID.randomUUID().toString();
          Instant exp = Instant.now().plusSeconds(1800);
          sessions.put(token, exp);
          x.getResponseHeaders().add(
            "Set-Cookie",
            "servihubSessao=" + token + "; HttpOnly; SameSite=Strict; Path=/; Max-Age=1800"
          );
          send(
            x,
            200,
            Map.of(
              "autenticado",
              true,
              "contaId",
              rows.get(0).get("id"),
              "expiraEm",
              exp.toString()
            )
          );
          return;
        }
      }
      String cookie = x.getRequestHeaders().getFirst("Cookie"),
        token = "";
      if (cookie != null) for (String part : cookie.split(";"))
        if (part.trim().startsWith("servihubSessao=")) token = part.trim().substring(15);
      boolean internal =
        env("INTERNAL_API_KEY", "").equals(x.getRequestHeaders().getFirst("X-ServiHUB-Internal")) &&
        !env("INTERNAL_API_KEY", "").isBlank();
      boolean valid = sessions.containsKey(token) && sessions.get(token).isAfter(Instant.now());
      if (path.equals("/sessao") && method.equals("DELETE")) {
        sessions.remove(token);
        x.getResponseHeaders().add(
          "Set-Cookie",
          "servihubSessao=; HttpOnly; SameSite=Strict; Path=/; Max-Age=0"
        );
        send(x, 200, Map.of("autenticado", false));
        return;
      }
      if (path.equals("/sessao") && method.equals("GET")) {
        send(x, 200, Map.of("autenticado", valid));
        return;
      }
      if (!valid && !internal) {
        send(x, 401, err("SESSAO_INVALIDA"));
        return;
      }
      if (path.equals("/interno/visitas") && !internal) {
        send(x, 403, err("ACESSO_NEGADO"));
        return;
      }
      try (Connection c = db()) {
        if (path.equals("/catalogo")) {
          if (method.equals("GET")) {
            send(x, 200, query(c, "SELECT * FROM catalogo ORDER BY descricao"));
            return;
          }
          Map<String, Object> m = body(x);
          if (
            s(m, "descricao").isBlank() ||
            n(m.get("duracao_minutos")) <= 0 ||
            new java.math.BigDecimal(s(m, "valor")).signum() < 0
          ) {
            send(x, 400, err("DADOS_DO_SERVICO_INVALIDOS"));
            return;
          }
          send(x, 201, insert(c, "catalogo", m, "descricao", "duracao_minutos", "valor"));
          return;
        }
        if (path.equals("/clientes")) {
          if (method.equals("GET")) {
            send(
              x,
              200,
              query(c, "SELECT id,nome,telefone,email,criado_em FROM cliente ORDER BY nome")
            );
            return;
          }
          Map<String, Object> m = body(x);
          if (s(m, "nome").isBlank() || s(m, "telefone").isBlank()) {
            send(x, 400, err("DADOS_OBRIGATORIOS"));
            return;
          }
          if (!s(m, "telefone").matches("[0-9]{10,11}")) {
            send(x, 400, err("Informe um telefone com DDD e 10 ou 11 números."));
            return;
          }
          send(x, 201, insert(c, "cliente", m, "nome", "telefone", "email"));
          return;
        }
        if (path.matches("/clientes/\\d+/enderecos")) {
          long id = Long.parseLong(path.split("/")[2]);
          if (!has(c, "cliente", id)) {
            send(x, 404, err("CLIENTE_NAO_ENCONTRADO"));
            return;
          }
          if (method.equals("GET")) {
            send(x, 200, query(c, "SELECT * FROM endereco WHERE cliente_id=?", id));
            return;
          }
          Map<String, Object> m = body(x);
          m.put("cliente_id", id);
          if (s(m, "logradouro").isBlank()) {
            send(x, 400, err("LOGRADOURO_OBRIGATORIO"));
            return;
          }
          send(
            x,
            201,
            insert(
              c,
              "endereco",
              m,
              "cliente_id",
              "logradouro",
              "numero",
              "complemento",
              "bairro",
              "cidade",
              "estado",
              "cep"
            )
          );
          return;
        }
        if (path.matches("/clientes/\\d+/historico")) {
          send(
            x,
            200,
            query(
              c,
              "SELECT * FROM servico WHERE cliente_id=? ORDER BY criado_em DESC",
              Long.parseLong(path.split("/")[2])
            )
          );
          return;
        }
        if (path.equals("/servicos")) {
          if (method.equals("GET")) {
            send(
              x,
              200,
              query(
                c,
                "SELECT s.*,c.nome AS cliente,EXISTS(SELECT 1 FROM orcamento o WHERE o.servico_aprovado_id=s.id AND o.status='aprovado') AS orcamento_aprovado FROM servico s JOIN cliente c ON c.id=s.cliente_id ORDER BY s.id DESC"
              )
            );
            return;
          }
          Map<String, Object> m = body(x);
          long cid = n(m.get("cliente_id"));
          if (!has(c, "cliente", cid)) {
            send(x, 404, err("CLIENTE_NAO_ENCONTRADO"));
            return;
          }
          if (
            m.get("endereco_id") != null &&
            !s(m, "endereco_id").isBlank() &&
            query(
              c,
              "SELECT id FROM endereco WHERE id=? AND cliente_id=?",
              n(m.get("endereco_id")),
              cid
            ).isEmpty()
          ) {
            send(x, 409, err("ENDERECO_DE_OUTRO_CLIENTE"));
            return;
          }
          if (s(m, "descricao").isBlank()) {
            send(x, 400, err("DESCRICAO_OBRIGATORIA"));
            return;
          }
          if (!s(m, "data_atendimento").isBlank()) {
            LocalDate date = LocalDate.parse(s(m, "data_atendimento"));
            if (date.isBefore(LocalDate.now(ZoneId.of("America/Sao_Paulo")))) {
              send(x, 400, err("Não é permitido selecionar uma data passada."));
              return;
            }
            m.put("status", "agendado");
          } else m.put("status", "registrado");
          if (s(m, "duracao_minutos").isBlank()) m.put("duracao_minutos", 60);
          if (n(m.get("duracao_minutos")) <= 0) {
            send(x, 400, err("DURACAO_INVALIDA"));
            return;
          }
          send(
            x,
            201,
            insert(
              c,
              "servico",
              m,
              "cliente_id",
              "endereco_id",
              "descricao",
              "valor_previsto",
              "duracao_minutos",
              "data_atendimento",
              "status"
            )
          );
          return;
        }
        if (path.matches("/servicos/\\d+/status") && method.equals("PUT")) {
          long id = Long.parseLong(path.split("/")[2]);
          Map<String, Object> m = body(x);
          String status = s(m, "status");
          List<Map<String, Object>> old = query(c, "SELECT status FROM servico WHERE id=?", id);
          if (old.isEmpty()) {
            send(x, 404, err("SERVICO_NAO_ENCONTRADO"));
            return;
          }
          Map<String, List<String>> trans = Map.of(
            "registrado",
            List.of("agendado", "cancelado"),
            "agendado",
            List.of("em_andamento", "cancelado"),
            "em_andamento",
            List.of("concluido", "cancelado"),
            "concluido",
            List.of(),
            "cancelado",
            List.of()
          );
          if (
            !trans.getOrDefault(old.get(0).get("status").toString(), List.of()).contains(status)
          ) {
            send(x, 409, err("TRANSICAO_INVALIDA"));
            return;
          }
          try (PreparedStatement q = c.prepareStatement("UPDATE servico SET status=? WHERE id=?")) {
            q.setString(1, status);
            q.setLong(2, id);
            q.executeUpdate();
          }
          send(x, 200, Map.of("status", status));
          return;
        }
        if (path.equals("/agenda")) {
          send(
            x,
            200,
            query(
              c,
              "SELECT a.id,a.servico_id,a.inicio,a.fim,s.cliente_id,s.descricao,c.nome AS cliente,0 AS sem_horario FROM agendamento a JOIN servico s ON s.id=a.servico_id JOIN cliente c ON c.id=s.cliente_id WHERE s.status<>'cancelado' UNION ALL SELECT NULL,s.id,TIMESTAMP(s.data_atendimento),TIMESTAMP(s.data_atendimento),s.cliente_id,s.descricao,c.nome,1 FROM servico s JOIN cliente c ON c.id=s.cliente_id WHERE s.data_atendimento IS NOT NULL AND s.status IN ('registrado','agendado','em_andamento') AND NOT EXISTS(SELECT 1 FROM agendamento a WHERE a.servico_id=s.id) ORDER BY inicio"
            )
          );
          return;
        }
        if (path.equals("/interno/visitas")) {
          send(
            x,
            200,
            query(
              c,
              "SELECT a.*,s.cliente_id,s.descricao,c.nome AS cliente FROM agendamento a JOIN servico s ON s.id=a.servico_id JOIN cliente c ON c.id=s.cliente_id WHERE s.status<>'cancelado' ORDER BY a.inicio"
            )
          );
          return;
        }
        if (path.equals("/agendamentos") && method.equals("POST")) {
          agendar(x, c);
          return;
        }
        if (path.equals("/orcamentos")) {
          if (method.equals("GET")) {
            send(
              x,
              200,
              query(
                c,
                "SELECT o.*,c.nome AS cliente FROM orcamento o JOIN cliente c ON c.id=o.cliente_id ORDER BY o.id DESC"
              )
            );
            return;
          }
          Map<String, Object> m = body(x);
          long cid = n(m.get("cliente_id"));
          if (!has(c, "cliente", cid)) {
            send(x, 404, err("CLIENTE_NAO_ENCONTRADO"));
            return;
          }
          List<Map<String, Object>> catalog = query(
            c,
            "SELECT * FROM catalogo WHERE id=?",
            n(m.get("catalogo_id"))
          );
          if (catalog.isEmpty()) {
            send(x, 404, err("SERVICO_DO_CATALOGO_NAO_ENCONTRADO"));
            return;
          }
          m.put("descricao", catalog.get(0).get("descricao"));
          if (s(m, "valor").isBlank()) m.put("valor", catalog.get(0).get("valor"));
          if (new java.math.BigDecimal(s(m, "valor")).signum() < 0) {
            send(x, 400, err("VALOR_INVALIDO"));
            return;
          }
          send(
            x,
            201,
            insert(c, "orcamento", m, "cliente_id", "catalogo_id", "descricao", "valor")
          );
          return;
        }
        // A aprovação cria o serviço e o pagamento pendente na mesma operação.
        if (path.matches("/orcamentos/\\d+/aprovar") && method.equals("PUT")) {
          long id = Long.parseLong(path.split("/")[2]);
          c.setAutoCommit(false);
          try {
            List<Map<String, Object>> rows = query(
              c,
              "SELECT o.*,COALESCE(cat.duracao_minutos,60) AS duracao FROM orcamento o LEFT JOIN catalogo cat ON cat.id=o.catalogo_id WHERE o.id=? FOR UPDATE",
              id
            );
            if (rows.isEmpty()) {
              c.rollback();
              send(x, 404, err("ORCAMENTO_NAO_ENCONTRADO"));
              return;
            }
            Map<String, Object> o = rows.get(0);
            Object sid = o.get("servico_aprovado_id");
            if (sid == null || !"aprovado".equals(o.get("status"))) {
              Map<String, Object> service = new HashMap<>();
              service.put("cliente_id", o.get("cliente_id"));
              service.put("descricao", o.get("descricao"));
              service.put("valor_previsto", o.get("valor"));
              service.put("duracao_minutos", o.get("duracao"));
              sid = insert(
                c,
                "servico",
                service,
                "cliente_id",
                "descricao",
                "valor_previsto",
                "duracao_minutos"
              ).get("id");
              Map<String, Object> payment = new HashMap<>();
              payment.put("servico_id", sid);
              payment.put("valor", o.get("valor"));
              payment.put("situacao", "pendente");
              insert(c, "pagamento", payment, "servico_id", "valor", "situacao");
              try (
                PreparedStatement q = c.prepareStatement(
                  "UPDATE orcamento SET status='aprovado',servico_aprovado_id=? WHERE id=?"
                )
              ) {
                q.setObject(1, sid);
                q.setLong(2, id);
                q.executeUpdate();
              }
            }
            c.commit();
            send(x, 200, Map.of("status", "aprovado", "servico_aprovado_id", sid));
          } catch (Exception e) {
            c.rollback();
            throw e;
          }
          return;
        }

        if (path.equals("/pagamentos")) {
          if (method.equals("GET")) {
            send(
              x,
              200,
              query(
                c,
                "SELECT p.*,s.descricao,c.nome AS cliente FROM pagamento p JOIN servico s ON s.id=p.servico_id JOIN cliente c ON c.id=s.cliente_id"
              )
            );
            return;
          }
          Map<String, Object> m = body(x);
          long sid = n(m.get("servico_id"));
          if (!has(c, "servico", sid)) {
            send(x, 404, err("SERVICO_NAO_ENCONTRADO"));
            return;
          }
          String situ = s(m, "situacao");
          if (!List.of("pago", "pendente").contains(situ)) {
            send(x, 400, err("SITUACAO_INVALIDA"));
            return;
          }
          try (
            PreparedStatement q = c.prepareStatement(
              "INSERT INTO pagamento(servico_id,valor,situacao,data_pagamento) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE valor=VALUES(valor),situacao=VALUES(situacao),data_pagamento=VALUES(data_pagamento)"
            )
          ) {
            q.setLong(1, sid);
            q.setBigDecimal(2, new java.math.BigDecimal(s(m, "valor")));
            q.setString(3, situ);
            q.setObject(4, m.get("data_pagamento"));
            q.executeUpdate();
          }
          send(x, 200, Map.of("situacao", situ));
          return;
        }
        if (path.equals("/pendencias")) {
          send(
            x,
            200,
            query(
              c,
              "SELECT p.*,s.descricao,c.nome AS cliente FROM pagamento p JOIN servico s ON s.id=p.servico_id JOIN cliente c ON c.id=s.cliente_id WHERE p.situacao='pendente'"
            )
          );
          return;
        }
        send(x, 404, err("ROTA_NAO_ENCONTRADA"));
      }
    } catch (Exception e) {
      e.printStackTrace();
      try {
        send(x, 400, err("REQUISICAO_INVALIDA"));
      } catch (Exception ignored) {}
    }
  }

  public static void main(String[] args) throws Exception {
    init();
    HttpServer server = HttpServer.create(
      new InetSocketAddress(Integer.parseInt(env("PORT", "8080"))),
      0
    );
    server.createContext("/", Main::route);
    server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
    server.start();
    System.out.println("Backend 1 pronto");
  }
}
