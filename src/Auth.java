package br.servihub;
import static br.servihub.Common.*;
import com.google.gson.*;
import com.sun.net.httpserver.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.security.*;
import java.nio.charset.StandardCharsets;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

/** One configured account; RAM sessions expire after thirty minutes. */
public final class Auth {
    private static final SecureRandom RANDOM=new SecureRandom();
    private static final Map<String,Session> SESSIONS=new ConcurrentHashMap<>();
    private record Session(int id,String role,Integer clientId,Instant expires) {}
    private static final Map<String,List<Instant>> ATTEMPTS=new ConcurrentHashMap<>();
    
    private static final String EMAIL=env("APP_EMAIL","").trim().toLowerCase(Locale.ROOT);
    public static void init(){
        String password=env("APP_PASSWORD","");
        try(java.sql.Connection c=db()){
            if(one(c,"SELECT id FROM conta WHERE papel='admin' LIMIT 1")!=null)return;
            require(!EMAIL.isEmpty()&&password.matches("[0-9]{6}"),503,"AUTENTICACAO_INDISPONIVEL","Configure APP_EMAIL e APP_PASSWORD com exatamente seis números.");
            create(c,"Administrador",EMAIL,password,"admin",null);
        }catch(Failure e){throw e;}catch(Exception e){throw new IllegalStateException("Não foi possível consultar ou criar o administrador. Verifique conexão e tabela conta.",e);}
    }
    private static byte[] hash(String s,byte[] salt){Argon2BytesGenerator g=new Argon2BytesGenerator();g.init(new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id).withSalt(salt).withMemoryAsKB(19456).withIterations(2).withParallelism(1).build());byte[] out=new byte[32];g.generateBytes(s.getBytes(StandardCharsets.UTF_8),out);return out;}
    private static void setCookie(HttpExchange x,String token,int age){x.getResponseHeaders().set("Set-Cookie","servihubSessao="+token+"; Path=/; HttpOnly; SameSite=Strict; Max-Age="+age+(ORIGIN.startsWith("https://")?"; Secure":""));}
    public static Map<String,Object> valid(HttpExchange x){
        Session s=SESSIONS.get(cookie(x));if(s==null||!s.expires.isAfter(Instant.now())){SESSIONS.remove(cookie(x));throw fail(401,"SESSAO_INVALIDA","Entre novamente para continuar.");}
        Map<String,Object> result=new LinkedHashMap<>();result.put("autenticado",true);result.put("contaId",s.id);result.put("papel",s.role);result.put("clienteId",s.clientId);result.put("expiraEm",s.expires.toString());return result;
    }
    public static void requireStaff(HttpExchange x){String role=(String)valid(x).get("papel");require(role.equals("admin")||role.equals("funcionario"),403,"ACESSO_NEGADO","Acesso reservado à equipe.");}
    public static void requireAdmin(HttpExchange x){require("admin".equals(valid(x).get("papel")),403,"ACESSO_NEGADO","Acesso reservado ao administrador.");}
    public static Map<String,Object> create(String name,String email,String password,String role,Integer clientId)throws Exception {
        return tx(c->create(c,name,email,password,role,clientId));
    }
    static void validateNewPassword(String password){bad(password!=null&&password.matches("[0-9]{6}"),"A senha deve conter exatamente seis números.");}
    static String newPassword(JsonObject j){text(j,"senha",128,false);String password=j.get("senha").getAsString();validateNewPassword(password);return password;}
    static String validateEmail(String email){String value=email.trim().toLowerCase(Locale.ROOT);bad(value.length()<=254&&value.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"),"E-mail inválido.");return value;}
    static Map<String,Object> create(java.sql.Connection c,String name,String email,String password,String role,Integer clientId)throws Exception {
        require(Set.of("admin","funcionario","cliente").contains(role),400,"PAPEL_INVALIDO","Perfil inválido.");
        validateNewPassword(password);String normalizedEmail=validateEmail(email);
        bad(name!=null&&!name.isBlank()&&name.codePointCount(0,name.length())<=120,"Nome inválido.");
        bad(role.equals("cliente")?clientId!=null:clientId==null,"Vínculo de cliente incompatível com o perfil.");
        byte[] salt=new byte[16];RANDOM.nextBytes(salt);
        return one(c,"INSERT INTO conta(nome,email,papel,cliente_id,salt,senha_hash) VALUES(?,?,?,?,?,?) RETURNING id AS \"contaId\"",name.trim(),normalizedEmail,role,clientId,salt,hash(password,salt));
    }
    public static void handle(HttpExchange x)throws Exception {
        String method=x.getRequestMethod();
        if(method.equals("GET")){send(x,200,valid(x));return;}
        origin(x);
        if(method.equals("DELETE")){SESSIONS.remove(cookie(x));setCookie(x,"",0);send(x,200,Map.of("autenticado",false));return;}
        require(method.equals("POST"),405,"METODO_NAO_PERMITIDO","Método não permitido.");
        JsonObject j=body(x);fields(j,"email","senha");String email=text(j,"email",254,false).toLowerCase(Locale.ROOT);
        bad(j.has("senha")&&j.get("senha").isJsonPrimitive()&&j.getAsJsonPrimitive("senha").isString(),"Informe a senha.");
        // Existing hashes/passwords remain valid; only new passwords follow the six-digit rule.
        String password=j.get("senha").getAsString();bad(password.matches("[0-9]{6}")||(password.codePointCount(0,password.length())>=12&&password.codePointCount(0,password.length())<=128),"Use seis números ou a senha anterior da sua conta.");
        String ip=x.getRemoteAddress().getAddress().getHostAddress();Instant now=Instant.now();
        // Periodically discard expired entries and avoid a permanently growing login map.
        SESSIONS.entrySet().removeIf(e->!e.getValue().expires.isAfter(now));
        ATTEMPTS.entrySet().removeIf(e->e.getValue().isEmpty()||e.getValue().get(e.getValue().size()-1).isBefore(now.minusSeconds(300)));
        List<Instant> attempts=ATTEMPTS.computeIfAbsent(ip,k->Collections.synchronizedList(new ArrayList<>()));
        synchronized(attempts){attempts.removeIf(t->t.isBefore(now.minusSeconds(300)));require(attempts.size()<5,429,"LIMITE_DE_TENTATIVAS","Aguarde cinco minutos antes de tentar novamente.");
            Map<String,Object> account;try(java.sql.Connection c=db()){account=one(c,"SELECT id,papel,cliente_id,salt,senha_hash FROM conta WHERE email=? AND ativo=1",email);}catch(Exception e){throw fail(503,"BANCO_INDISPONIVEL","Não foi possível consultar as contas.");}
            boolean match=account!=null&&MessageDigest.isEqual((byte[])account.get("senha_hash"),hash(password,(byte[])account.get("salt")));if(!match){attempts.add(now);throw fail(401,"CREDENCIAIS_INVALIDAS","E-mail ou senha incorretos.");}attempts.clear();
            int accountId=((Number)account.get("id")).intValue();
            Integer clientId=account.get("cliente_id")==null?null:((Number)account.get("cliente_id")).intValue();
            try(java.sql.Connection c=db()){update(c,"UPDATE conta SET ultimo_acesso=CURRENT_TIMESTAMP(6) WHERE id=?",accountId);}
            byte[] tokenBytes=new byte[32];RANDOM.nextBytes(tokenBytes);
            String token=Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
            SESSIONS.remove(cookie(x));
            Session session=new Session(accountId,(String)account.get("papel"),clientId,now.plusSeconds(1800));
            SESSIONS.put(token,session);setCookie(x,token,1800);
            Map<String,Object> result=new LinkedHashMap<>();result.put("autenticado",true);result.put("contaId",session.id);result.put("papel",session.role);result.put("clienteId",session.clientId);result.put("expiraEm",session.expires.toString());
            send(x,200,result);
        }
}
}
