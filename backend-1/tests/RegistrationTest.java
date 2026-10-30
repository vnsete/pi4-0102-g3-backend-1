package br.servihub;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpServer;
import org.junit.*;
import static org.junit.Assert.*;
import static br.servihub.Common.*;
import java.net.*;
import java.net.http.*;
import java.sql.*;
import java.util.*;

/** Tests run exclusively against an explicitly selected, disposable in-memory DB. */
public class RegistrationTest {
 private Connection c;
 @Before public void prepare()throws Exception {
  Assume.assumeTrue("Use DB_URL=jdbc:h2:mem:servihub_tests;MODE=MySQL;DB_CLOSE_DELAY=-1",env("DB_URL","").startsWith("jdbc:h2:mem:servihub_tests;"));
  c=db();
  try(Statement s=c.createStatement()){
   s.execute("DROP ALL OBJECTS");
   s.execute("CREATE TABLE cliente(id INT AUTO_INCREMENT PRIMARY KEY,nome VARCHAR(120),telefone VARCHAR(15),email VARCHAR(254),criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
   s.execute("CREATE TABLE endereco(id INT AUTO_INCREMENT PRIMARY KEY,cliente_id INT REFERENCES cliente(id),logradouro VARCHAR(120),numero VARCHAR(20),complemento VARCHAR(120),bairro VARCHAR(120),cidade VARCHAR(120),uf CHAR(2),cep CHAR(8))");
   s.execute("CREATE TABLE conta(id INT AUTO_INCREMENT PRIMARY KEY,nome VARCHAR(120),email VARCHAR(254) UNIQUE,papel VARCHAR(20),cliente_id INT REFERENCES cliente(id),salt VARBINARY(16),senha_hash VARBINARY(32),ativo BOOLEAN DEFAULT TRUE,ultimo_acesso TIMESTAMP)");
   s.execute("CREATE TABLE servico(id INT AUTO_INCREMENT PRIMARY KEY,cliente_id INT,endereco_id INT,catalogo_id INT,descricao VARCHAR(500),valor_previsto DECIMAL(12,2),status VARCHAR(20),criado_em TIMESTAMP,atualizado_em TIMESTAMP)");
   s.execute("CREATE TABLE agendamento(id INT AUTO_INCREMENT PRIMARY KEY,servico_id INT,inicio_em TIMESTAMP,fim_em TIMESTAMP)");
   s.execute("CREATE TABLE catalogo_servico(id INT AUTO_INCREMENT PRIMARY KEY,nome VARCHAR(120),descricao VARCHAR(500),preco_padrao DECIMAL(12,2),duracao_minutos INT)");
   s.execute("CREATE TABLE configuracao(id INT PRIMARY KEY,nome_empresa VARCHAR(120),usar_clientes BOOLEAN,usar_agenda BOOLEAN,usar_orcamentos BOOLEAN,usar_pagamentos BOOLEAN)");
   s.execute("INSERT INTO configuracao VALUES(1,'ServiHUB',TRUE,TRUE,TRUE,TRUE)");
  }
 }
 @After public void close()throws Exception{if(c!=null)c.close();}
 private JsonObject input(){return JSON.fromJson("{\"nome\":\"Cliente Teste\",\"telefone\":\"11999990000\",\"email\":\"Cliente@Example.test\",\"senha\":\"012345\",\"logradouro\":\"Rua Teste\",\"numero\":\"10\",\"bairro\":\"Centro\",\"cidade\":\"Belo Horizonte\",\"uf\":\"MG\",\"cep\":\"30000000\"}",JsonObject.class);}
 private int count(String table)throws Exception{return ((Number)one(c,"SELECT COUNT(*) total FROM "+table).get("total")).intValue();}
 @Test public void registrationLinksThreeRecordsAndHashesPassword()throws Exception{
  Map<String,Object> result=Registration.create(input());assertNotNull(result);
  assertEquals(1,count("cliente"));assertEquals(1,count("endereco"));assertEquals(1,count("conta"));
  Map<String,Object> account=one(c,"SELECT * FROM conta");assertEquals("cliente",account.get("papel"));assertEquals("cliente@example.test",account.get("email"));
  assertEquals(32,((byte[])account.get("senha_hash")).length);assertEquals(16,((byte[])account.get("salt")).length);
  assertEquals(one(c,"SELECT cliente_id FROM endereco").get("cliente_id"),account.get("cliente_id"));
 }
 @Test public void duplicateEmailRollsBackClientAndAddress()throws Exception{
  Registration.create(input());try{Registration.create(input());fail();}catch(SQLException expected){}
  assertEquals(1,count("cliente"));assertEquals(1,count("endereco"));assertEquals(1,count("conta"));
 }
 @Test public void publicCallerCannotChooseAdminOrEmployee()throws Exception{
  for(String role:new String[]{"admin","funcionario"}){JsonObject j=input();j.addProperty("papel",role);try{Registration.create(j);fail();}catch(Failure expected){assertEquals(400,expected.status);}}
  assertEquals(0,count("conta"));assertEquals(0,count("cliente"));
 }
 @Test public void existingAdministratorIsNeverReplacedDuringStartup()throws Exception{
  Auth.create("Admin","admin@example.test","123456","admin",null);
  byte[] previous=(byte[])one(c,"SELECT senha_hash FROM conta").get("senha_hash");
  Auth.init();assertEquals(1,count("conta"));assertArrayEquals(previous,(byte[])one(c,"SELECT senha_hash FROM conta").get("senha_hash"));
 }
 @Test public void invalidDataNeverCreatesPartialRecords()throws Exception{
  for(String[] invalid:new String[][]{{"senha","12345"},{"senha","abcdef"},{"senha"," 123456 "},{"telefone","123"},{"email","invalido"},{"cep","123"},{"uf","XX"},{"nome"," "}}){JsonObject j=input();j.addProperty(invalid[0],invalid[1]);try{Registration.create(j);fail();}catch(Failure expected){assertEquals(400,expected.status);}}
  assertEquals(0,count("conta"));assertEquals(0,count("cliente"));assertEquals(0,count("endereco"));
 }
 @Test public void rolesAreEnforcedByHttpRoutes()throws Exception{
  HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
  server.createContext("/",x->{try{Backend1.handle(x);}catch(Failure e){send(x,e.status,Map.of("codigo",e.code));}catch(Exception e){send(x,500,Map.of("codigo","ERRO_TESTE"));}finally{x.close();}});server.start();
  String base="http://127.0.0.1:"+server.getAddress().getPort();HttpClient client=HttpClient.newHttpClient();
  try{
   Registration.create(input());Auth.create("Funcionario","funcionario@example.test","123456","funcionario",null);Auth.create("Administrador","admin@example.test","654321","admin",null);
   int clientId=((Number)one(c,"SELECT cliente_id FROM conta WHERE papel='cliente'").get("cliente_id")).intValue();
   int another=((Number)one(c,"INSERT INTO cliente(nome,telefone,email) VALUES('Outro','11999990001','outro@example.test') RETURNING id").get("id")).intValue();
   update(c,"INSERT INTO servico(cliente_id,descricao,status,valor_previsto) VALUES(?,'Meu atendimento','registrado',100)",clientId);
   update(c,"INSERT INTO servico(cliente_id,descricao,status,valor_previsto) VALUES(?,'Atendimento de outro cliente','registrado',200)",another);
   for(String role:new String[]{"cliente","funcionario","admin"}){
    String email=role.equals("cliente")?"cliente@example.test":role+"@example.test";String password=role.equals("cliente")?"012345":role.equals("admin")?"654321":"123456";
    HttpResponse<String> login=client.send(HttpRequest.newBuilder(URI.create(base+"/sessao")).header("Origin",ORIGIN).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString("{\"email\":\""+email+"\",\"senha\":\""+password+"\"}")).build(),HttpResponse.BodyHandlers.ofString());
    assertEquals(login.body(),200,login.statusCode());String cookie=login.headers().firstValue("Set-Cookie").orElseThrow().split(";",2)[0];
    for(String route:new String[]{"/contas","/configuracoes","/clientes","/catalogo-servicos"}){
     HttpResponse<String> response=client.send(HttpRequest.newBuilder(URI.create(base+route)).header("Cookie",cookie).GET().build(),HttpResponse.BodyHandlers.ofString());
     assertEquals(role+route,role.equals("admin")?200:403,response.statusCode());
    }
    for(String route:new String[]{"/minha-conta","/area-funcionario"}){
     boolean allowed=role.equals(route.equals("/minha-conta")?"cliente":"funcionario");
     HttpResponse<String> response=client.send(HttpRequest.newBuilder(URI.create(base+route)).header("Cookie",cookie).GET().build(),HttpResponse.BodyHandlers.ofString());assertEquals(role+route,allowed?200:403,response.statusCode());
     if(allowed&&role.equals("cliente")){assertTrue(response.body().contains("Meu atendimento"));assertFalse(response.body().contains("Atendimento de outro cliente"));}
     if(allowed&&role.equals("funcionario")){assertFalse(response.body().contains("valorPrevisto"));assertFalse(response.body().contains("clienteId"));}
    }
    if(role.equals("admin")){
     HttpRequest create=HttpRequest.newBuilder(URI.create(base+"/contas")).header("Cookie",cookie).header("Origin",ORIGIN).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString("{\"nome\":\"Novo funcionario\",\"email\":\"novo@example.test\",\"senha\":\"012345\",\"papel\":\"funcionario\"}")).build();
     HttpResponse<String> saved=client.send(create,HttpResponse.BodyHandlers.ofString());assertEquals(saved.body(),201,saved.statusCode());assertNotNull(one(c,"SELECT id FROM conta WHERE email='novo@example.test'"));
    }
   }
  }finally{server.stop(0);}
 }
 @Test public void httpRegistrationRequiresOriginAndPersistsOnce()throws Exception{
  HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
  server.createContext("/",x->{try{Backend1.handle(x);}catch(Failure e){send(x,e.status,Map.of("codigo",e.code));}catch(SQLException e){send(x,409,Map.of("codigo","CONFLITO_DADOS"));}catch(Exception e){send(x,500,Map.of("codigo","ERRO_TESTE"));}finally{x.close();}});server.start();
  URI uri=URI.create("http://127.0.0.1:"+server.getAddress().getPort()+"/cadastro");HttpClient client=HttpClient.newHttpClient();
  try{
   HttpRequest noOrigin=HttpRequest.newBuilder(uri).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(input().toString())).build();
   assertEquals(403,client.send(noOrigin,HttpResponse.BodyHandlers.ofString()).statusCode());assertEquals(0,count("conta"));
   HttpRequest request=HttpRequest.newBuilder(uri).header("Origin",ORIGIN).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(input().toString())).build();
   HttpResponse<String> created=client.send(request,HttpResponse.BodyHandlers.ofString());assertEquals(created.body(),201,created.statusCode());
   assertTrue(created.body().contains("contaId"));assertFalse(created.body().contains("senha"));assertEquals(1,count("conta"));
   assertEquals(409,client.send(request,HttpResponse.BodyHandlers.ofString()).statusCode());assertEquals(1,count("cliente"));assertEquals(1,count("endereco"));
  }finally{server.stop(0);}
 }
 @Test public void legacyPasswordStillAuthenticates()throws Exception{
  byte[] salt=new byte[16];Arrays.fill(salt,(byte)7);
  java.lang.reflect.Method hash=Auth.class.getDeclaredMethod("hash",String.class,byte[].class);hash.setAccessible(true);
  byte[] encoded=(byte[])hash.invoke(null,"senha-anterior-teste",salt);
  update(c,"INSERT INTO conta(nome,email,papel,salt,senha_hash) VALUES('Legado','legado@example.test','admin',?,?)",salt,encoded);
  HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
  server.createContext("/",x->{try{Auth.handle(x);}catch(Failure e){send(x,e.status,Map.of("codigo",e.code));}catch(Exception e){send(x,500,Map.of("codigo","ERRO_TESTE"));}finally{x.close();}});server.start();
  try{
   HttpRequest request=HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+server.getAddress().getPort()+"/sessao")).header("Origin",ORIGIN).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString("{\"email\":\"legado@example.test\",\"senha\":\"senha-anterior-teste\"}")).build();
   HttpResponse<String> response=HttpClient.newHttpClient().send(request,HttpResponse.BodyHandlers.ofString());assertEquals(response.body(),200,response.statusCode());assertEquals(1,count("conta"));
  }finally{server.stop(0);}
 }
}
