package br.servihub;

import static br.servihub.Common.*;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Public registration always creates a client, address and client account atomically. */
public final class Registration {
    private static final Map<String,List<Instant>> ATTEMPTS=new ConcurrentHashMap<>();
    public static void handle(HttpExchange x)throws Exception {
        require(x.getRequestMethod().equals("POST"),405,"METODO_NAO_PERMITIDO","Método não permitido.");
        origin(x);
        Instant now=Instant.now();
        ATTEMPTS.entrySet().removeIf(e->{synchronized(e.getValue()){return e.getValue().isEmpty()||e.getValue().get(e.getValue().size()-1).isBefore(now.minusSeconds(300));}});
        List<Instant> attempts=ATTEMPTS.computeIfAbsent(x.getRemoteAddress().getAddress().getHostAddress(),k->new ArrayList<>());
        synchronized(attempts){attempts.removeIf(t->t.isBefore(now.minusSeconds(300)));require(attempts.size()<5,429,"LIMITE_DE_TENTATIVAS","Aguarde cinco minutos antes de cadastrar novamente.");attempts.add(now);}
        send(x,201,create(body(x)));
    }
    static Map<String,Object> create(JsonObject j)throws Exception {
        fields(j,"nome","telefone","email","senha","logradouro","numero","complemento","bairro","cidade","uf","cep");
        String name=text(j,"nome",120,false),phone=text(j,"telefone",15,false),email=Auth.validateEmail(text(j,"email",254,false)),password=Auth.newPassword(j);
        Auth.validateNewPassword(password);bad(phone.matches("[0-9]{10,15}"),"Telefone deve ter de 10 a 15 números.");
        String street=text(j,"logradouro",120,false),number=text(j,"numero",20,false),extra=text(j,"complemento",120,true),district=text(j,"bairro",120,false),city=text(j,"cidade",120,false),uf=text(j,"uf",2,false),cep=text(j,"cep",8,false);
        bad(Set.of("AC","AL","AP","AM","BA","CE","DF","ES","GO","MA","MG","MT","MS","PA","PB","PR","PE","PI","RJ","RN","RS","RO","RR","SC","SP","SE","TO").contains(uf),"UF inválida.");
        bad(cep.matches("[0-9]{8}"),"CEP deve ter oito números.");
        return tx(c->{
            int clientId=((Number)one(c,"INSERT INTO cliente(nome,telefone,email) VALUES(?,?,?) RETURNING id",name,phone,email).get("id")).intValue();
            update(c,"INSERT INTO endereco(cliente_id,logradouro,numero,complemento,bairro,cidade,uf,cep) VALUES(?,?,?,?,?,?,?,?)",clientId,street,number,extra,district,city,uf,cep);
            return Auth.create(c,name,email,password,"cliente",clientId);
        });
    }
    private Registration(){}
}
