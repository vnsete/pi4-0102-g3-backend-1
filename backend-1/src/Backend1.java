package br.servihub;
import static br.servihub.Common.*;
import com.google.gson.*;
import com.sun.net.httpserver.*;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.math.BigDecimal;

public final class Backend1 {
    public static void main(String[]args)throws Exception {Auth.init();server(Integer.parseInt(env("PORT","8081")),Backend1::handle);}
    private static boolean method(HttpExchange x,String m){return x.getRequestMethod().equals(m);}
    public static void handle(HttpExchange x)throws Exception {
        String path=x.getRequestURI().getPath();
        if(path.equals("/sessao")){Auth.handle(x);return;}
        if(path.equals("/cadastro")){Registration.handle(x);return;}
        if(path.startsWith("/interno/"))internal(x);
        Auth.valid(x);
        if(path.equals("/interno/sessao")&&method(x,"GET")){send(x,200,Auth.valid(x));return;}
        if(path.equals("/minha-conta")&&method(x,"GET")){Map<String,Object> session=Auth.valid(x);require("cliente".equals(session.get("papel")),403,"ACESSO_NEGADO","Área do cliente.");Integer cid=(Integer)session.get("clienteId");require(cid!=null,403,"CLIENTE_NAO_VINCULADO","Conta sem cadastro de cliente.");try(Connection c=db()){send(x,200,Map.of("servicos",rows(c,"SELECT "+SERVICO+" FROM servico s WHERE s.cliente_id=? ORDER BY s.id DESC",cid),"agenda",rows(c,"SELECT a.id AS \"agendamentoId\",s.descricao,a.inicio_em AS \"inicioEm\",a.fim_em AS \"fimEm\",s.status FROM agendamento a JOIN servico s ON s.id=a.servico_id WHERE s.cliente_id=? ORDER BY a.inicio_em DESC",cid),"catalogo",rows(c,"SELECT "+CATALOGO+" FROM catalogo_servico ORDER BY nome")));}return;}
        if(path.equals("/contas")&&method(x,"GET")){Auth.requireAdmin(x);try(Connection c=db()){send(x,200,Map.of("contas",rows(c,"SELECT id,nome,email,papel,cliente_id AS \"clienteId\",ultimo_acesso AS \"ultimoAcesso\" FROM conta ORDER BY id"),"totalClientes",one(c,"SELECT COUNT(*) AS total FROM cliente").get("total")));}return;}
        if(path.equals("/contas")&&method(x,"POST")){Auth.requireAdmin(x);origin(x);JsonObject j=body(x);fields(j,"nome","email","senha","papel","clienteId");String role=text(j,"papel",20,false);Integer cid=numberId(j,"clienteId",true);if(role.equals("cliente")){require(cid!=null,400,"CLIENTE_OBRIGATORIO","Vincule a conta a um cliente.");try(Connection c=db()){found(c,"cliente",cid,"CLIENTE_NAO_ENCONTRADO");}}else require(cid==null,400,"CLIENTE_INDEVIDO","Vínculo permitido apenas para cliente.");send(x,201,Auth.create(text(j,"nome",120,false),text(j,"email",254,false),Auth.newPassword(j),role,cid));return;}
        if(path.equals("/area-funcionario")&&method(x,"GET")){
            require("funcionario".equals(Auth.valid(x).get("papel")),403,"ACESSO_NEGADO","Área do funcionário.");query(x);
            try(Connection c=db()){send(x,200,Map.of(
                "servicos",rows(c,"SELECT id AS servicoId,descricao,status FROM servico ORDER BY id DESC"),
                "agenda",rows(c,"SELECT a.id AS agendamentoId,s.descricao,a.inicio_em AS inicioEm,a.fim_em AS fimEm,s.status FROM agendamento a JOIN servico s ON s.id=a.servico_id ORDER BY a.inicio_em DESC")
            ));}return;
        }
        Auth.requireAdmin(x);
        if(!method(x,"GET"))origin(x);
        String[] p=path.split("/");
        if(path.equals("/configuracoes")){
            if(method(x,"GET")){query(x);try(Connection c=db()){send(x,200,one(c,"SELECT "+CONFIG+" FROM configuracao WHERE id=1"));}return;}
            if(method(x,"PUT")){Auth.requireAdmin(x);JsonObject j=body(x);fields(j,"nomeEmpresa","usarClientes","usarAgenda","usarOrcamentos","usarPagamentos");String nome=text(j,"nomeEmpresa",120,false);boolean clientes=bool(j,"usarClientes"),agenda=bool(j,"usarAgenda"),orcamentos=bool(j,"usarOrcamentos"),pagamentos=bool(j,"usarPagamentos");send(x,200,tx(c->{update(c,"UPDATE configuracao SET nome_empresa=?,usar_clientes=?,usar_agenda=?,usar_orcamentos=?,usar_pagamentos=? WHERE id=1",nome,clientes,agenda,orcamentos,pagamentos);return one(c,"SELECT "+CONFIG+" FROM configuracao WHERE id=1");}));return;}
        }
        if(path.equals("/catalogo-servicos")&&method(x,"GET")){query(x);try(Connection c=db()){send(x,200,Map.of("catalogo",rows(c,"SELECT "+CATALOGO+" FROM catalogo_servico ORDER BY nome,id")));}return;}
        if((path.equals("/catalogo-servicos")&&method(x,"POST"))||(p.length==3&&p[1].equals("catalogo-servicos")&&method(x,"PUT"))){
            Integer catalogo=path.equals("/catalogo-servicos")?null:id(p[2]);JsonObject j=body(x);fields(j,"nome","descricao","precoPadrao","duracaoMinutos");String nome=text(j,"nome",120,false),desc=text(j,"descricao",500,true);BigDecimal preco=money(j,"precoPadrao",false);int duracao=numberId(j,"duracaoMinutos",false);bad(duracao<=1440,"Duração deve estar entre 1 e 1440 minutos.");
            send(x,catalogo==null?201:200,tx(c->{if(catalogo==null)return one(c,"INSERT INTO catalogo_servico(nome,descricao,preco_padrao,duracao_minutos) VALUES(?,?,?,?) RETURNING "+CATALOGO,nome,desc,preco,duracao);found(c,"catalogo_servico",catalogo,"CATALOGO_NAO_ENCONTRADO");return one(c,"UPDATE catalogo_servico SET nome=?,descricao=?,preco_padrao=?,duracao_minutos=?,atualizado_em=CURRENT_TIMESTAMP(6) WHERE id=? RETURNING "+CATALOGO,nome,desc,preco,duracao,catalogo);}));return;
        }
        if(path.equals("/clientes")){
            if(method(x,"GET")){query(x);try(Connection c=db()){send(x,200,Map.of("clientes",rows(c,"SELECT "+CLIENTE+",(SELECT CONCAT(e.logradouro,', ',e.numero,' · ',e.cidade,'/',e.uf) FROM endereco e WHERE e.cliente_id=cliente.id ORDER BY e.id LIMIT 1) AS \"enderecoResumo\" FROM cliente ORDER BY nome,id")));}return;}
            if(method(x,"POST")){
                JsonObject j=body(x);fields(j,"nome","telefone","email");String nome=text(j,"nome",120,false),tel=text(j,"telefone",15,false),email=text(j,"email",254,true);
                bad(tel.matches("[0-9]{10,15}"),"Telefone deve ter de 10 a 15 dígitos, sem máscara.");bad(email==null||email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"),"E-mail inválido.");
                send(x,201,tx(c->one(c,"INSERT INTO cliente(nome,telefone,email) VALUES(?,?,?) RETURNING "+CLIENTE,nome,tel,email)));return;
            }
        }
        if(p.length==4&&p[1].equals("clientes")){
            int cliente=id(p[2]);
            if(p[3].equals("enderecos")){
                if(method(x,"GET")){query(x);try(Connection c=db()){found(c,"cliente",cliente,"CLIENTE_NAO_ENCONTRADO");send(x,200,Map.of("enderecos",rows(c,"SELECT "+ENDERECO+" FROM endereco WHERE cliente_id=? ORDER BY id",cliente)));}return;}
                if(method(x,"POST")){
                    JsonObject j=body(x);fields(j,"logradouro","numero","complemento","bairro","cidade","uf","cep");
                    String rua=text(j,"logradouro",120,false),num=text(j,"numero",20,false),comp=text(j,"complemento",120,true),bairro=text(j,"bairro",120,false),cidade=text(j,"cidade",120,false),uf=text(j,"uf",2,false),cep=text(j,"cep",8,false);
                    bad(Set.of("AC","AL","AP","AM","BA","CE","DF","ES","GO","MA","MT","MS","MG","PA","PB","PR","PE","PI","RJ","RN","RS","RO","RR","SC","SP","SE","TO").contains(uf),"UF inválida.");bad(cep.matches("[0-9]{8}"),"CEP deve ter oito dígitos.");
                    send(x,201,tx(c->{found(c,"cliente",cliente,"CLIENTE_NAO_ENCONTRADO");return one(c,"INSERT INTO endereco(cliente_id,logradouro,numero,complemento,bairro,cidade,uf,cep) VALUES(?,?,?,?,?,?,?,?) RETURNING "+ENDERECO,cliente,rua,num,comp,bairro,cidade,uf,cep);}));return;
                }
            }
            if(p[3].equals("historico")&&method(x,"GET")){query(x);try(Connection c=db()){found(c,"cliente",cliente,"CLIENTE_NAO_ENCONTRADO");send(x,200,Map.of("servicos",rows(c,"SELECT "+SERVICO+" FROM servico s WHERE s.cliente_id=? ORDER BY s.criado_em DESC,s.id DESC",cliente)));}return;}
        }
        if(path.equals("/servicos")){
            if(method(x,"GET")){Map<String,String> q=query(x,"clienteId");try(Connection c=db()){
                if(q.containsKey("clienteId")){int cliente=id(q.get("clienteId"));found(c,"cliente",cliente,"CLIENTE_NAO_ENCONTRADO");send(x,200,Map.of("servicos",rows(c,"SELECT "+SERVICO+" FROM servico s WHERE s.cliente_id=? ORDER BY s.criado_em DESC,s.id DESC",cliente)));}
                else send(x,200,Map.of("servicos",rows(c,"SELECT "+SERVICO+" FROM servico s ORDER BY s.criado_em DESC,s.id DESC")));}return;}
            if(method(x,"POST")){
                JsonObject j=body(x);fields(j,"clienteId","enderecoId","catalogoId","descricao","valorPrevisto","dataAtendimento");int cliente=numberId(j,"clienteId",false);Integer endereco=numberId(j,"enderecoId",true),catalogo=numberId(j,"catalogoId",true);String desc=text(j,"descricao",500,false);BigDecimal valor=money(j,"valorPrevisto",true);String dataAtendimento=text(j,"dataAtendimento",10,true);if(dataAtendimento!=null){try{LocalDate.parse(dataAtendimento);}catch(Exception ex){throw fail(400,"DATA_INVALIDA","Data do atendimento inválida.");}}
                send(x,201,tx(c->{found(c,"cliente",cliente,"CLIENTE_NAO_ENCONTRADO");if(endereco!=null){Map<String,Object> e=found(c,"endereco",endereco,"ENDERECO_NAO_ENCONTRADO");require(((Number)e.get("cliente_id")).intValue()==cliente,409,"VINCULO_INVALIDO","O endereço pertence a outro cliente.");}if(catalogo!=null)found(c,"catalogo_servico",catalogo,"CATALOGO_NAO_ENCONTRADO");return one(c,"INSERT INTO servico(cliente_id,endereco_id,catalogo_id,descricao,valor_previsto,criado_em) VALUES(?,?,?,?,?,COALESCE(?,CURRENT_TIMESTAMP(6))) RETURNING "+SERVICO.replace("s.",""),cliente,endereco,catalogo,desc,valor,dataAtendimento==null?null:dataAtendimento+" 12:00:00");}));return;
            }
        }
        if(p.length==4&&p[1].equals("servicos")&&p[3].equals("status")&&method(x,"PATCH")){
            int servico=id(p[2]);JsonObject j=body(x);fields(j,"status");String target=text(j,"status",20,false);
            require(Set.of("registrado","agendado","em_andamento","concluido","cancelado").contains(target),400,"STATUS_INVALIDO","Status inválido.");
            send(x,200,tx(c->{lockAgenda(c);Map<String,Object> s=one(c,"SELECT * FROM servico WHERE id=? FOR UPDATE",servico);require(s!=null,404,"SERVICO_NAO_ENCONTRADO","Serviço não encontrado.");String current=(String)s.get("status");
                boolean allowed=(current.equals("registrado")||current.equals("agendado"))&&(target.equals("em_andamento")||target.equals("cancelado"))||current.equals("em_andamento")&&(target.equals("concluido")||target.equals("cancelado"));
                require(allowed,409,"TRANSICAO_INVALIDA","Transição não permitida. Agendado é definido pela criação do agendamento.");
                return one(c,"UPDATE servico SET status=?,atualizado_em=CURRENT_TIMESTAMP WHERE id=? RETURNING "+SERVICO.replace("s.",""),target,servico);}));return;
        }
        if(path.equals("/agendamentos")&&method(x,"POST")){
            JsonObject j=body(x);fields(j,"servicoId","inicioEm");int servico=numberId(j,"servicoId",false);Instant start=instant(text(j,"inicioEm",50,false));
            require(start.isAfter(Instant.now()),400,"HORARIO_INVALIDO","O início deve ser futuro.");
            send(x,201,tx(c->{lockAgenda(c);Map<String,Object> s=one(c,"SELECT * FROM servico WHERE id=? FOR UPDATE",servico);require(s!=null,404,"SERVICO_NAO_ENCONTRADO","Serviço não encontrado.");require("registrado".equals(s.get("status")),409,"SERVICO_NAO_AGENDAVEL","Serviço deve estar registrado e sem agendamento.");
                Map<String,Object> dur=one(c,"SELECT cs.duracao_minutos AS minutos FROM catalogo_servico cs WHERE cs.id=?",s.get("catalogo_id"));require(dur!=null,409,"DURACAO_NAO_DEFINIDA","Vincule o serviço a um item do catálogo com duração.");Instant end=start.plus(Duration.ofMinutes(((Number)dur.get("minutos")).longValue()));require(one(c,"SELECT id FROM agendamento WHERE servico_id=?",servico)==null,409,"SERVICO_JA_AGENDADO","Já existe agendamento para esse serviço.");
                require(one(c,"SELECT a.id FROM agendamento a JOIN servico s ON s.id=a.servico_id WHERE s.status IN ('agendado','em_andamento') AND a.inicio_em<? AND a.fim_em>? LIMIT 1",end,start)==null,409,"CONFLITO_DE_AGENDA","Existe outro atendimento nesse intervalo.");
                Map<String,Object> a=one(c,"INSERT INTO agendamento(servico_id,inicio_em,fim_em) VALUES(?,?,?) RETURNING id AS \"agendamentoId\",servico_id AS \"servicoId\",inicio_em AS \"inicioEm\",fim_em AS \"fimEm\",criado_em AS \"criadoEm\",atualizado_em AS \"atualizadoEm\"",servico,start,end);
                update(c,"UPDATE servico SET status='agendado',atualizado_em=CURRENT_TIMESTAMP WHERE id=?",servico);return a;}));return;
        }
        if((path.equals("/agenda")||path.equals("/interno/visitas"))&&method(x,"GET")){
            Map<String,String> q=query(x,"de","ate");Instant start=instant(q.get("de")),end=instant(q.get("ate"));boolean visits=path.startsWith("/interno");
            require(end.isAfter(start)&&Duration.between(start,end).compareTo(Duration.ofDays(visits?1:31))<=0,400,"INTERVALO_INVALIDO","Informe um intervalo de até "+(visits?1:31)+" dias.");
            String sql="SELECT a.id AS \"agendamentoId\",s.id AS \"servicoId\",s.cliente_id AS \"clienteId\",c.nome AS \"nomeCliente\",s.endereco_id AS \"enderecoId\",s.descricao,a.inicio_em AS \"inicioEm\",a.fim_em AS \"fimEm\",s.status AS \"statusServico\" FROM agendamento a JOIN servico s ON s.id=a.servico_id JOIN cliente c ON c.id=s.cliente_id WHERE a.inicio_em>=? AND a.inicio_em<?"+(visits?" AND s.status='agendado'":"")+" ORDER BY a.inicio_em,a.id";
            try(Connection c=db()){send(x,200,Map.of("atendimentos",rows(c,sql,start,end)));}return;
        }
        throw fail(404,"ROTA_NAO_ENCONTRADA","Operação não encontrada.");
    }
}
