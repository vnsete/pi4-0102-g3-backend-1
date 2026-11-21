
package br.servihub;

import com.sun.net.httpserver.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.Assert.*;

public class RegistrationTest {

    private HttpServer servidor;
    private HttpClient cliente;
    private String enderecoBase;

    @Before
    public void preparar() throws Exception {
        servidor = HttpServer.create(
            new InetSocketAddress("127.0.0.1", 0), 0
        );
        servidor.createContext("/", Main::route);
        servidor.start();

        enderecoBase = "http://127.0.0.1:" + servidor.getAddress().getPort();
        cliente = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    }

    @After
    public void finalizar() {
        if (servidor != null) servidor.stop(0);
    }

    private HttpResponse<String> requisitar(
        String caminho, String metodo, String json
    ) throws Exception {

        HttpRequest.Builder requisicao = HttpRequest.newBuilder()
            .uri(URI.create(enderecoBase + caminho))
            .timeout(Duration.ofSeconds(5));

        if ("POST".equals(metodo)) {
            requisicao.header("Content-Type", "application/json");
            requisicao.POST(HttpRequest.BodyPublishers.ofString(
                json, StandardCharsets.UTF_8
            ));
        } else if ("DELETE".equals(metodo)) {
            requisicao.DELETE();
        } else {
            requisicao.GET();
        }

        return cliente.send(
            requisicao.build(),
            HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );
    }

    // TE-01: Verifica se o servidor está funcionando.
    @Test
    public void deveResponderVerificacaoDeSaude() throws Exception {
        HttpResponse<String> resposta = requisitar("/health", "GET", null);

        assertEquals(200, resposta.statusCode());
        assertTrue(resposta.body().contains("\"ok\":true"));
    }

    // TE-02: Verifica uma sessão não autenticada.
    @Test
    public void deveInformarSessaoNaoAutenticada() throws Exception {
        HttpResponse<String> resposta = requisitar("/sessao", "GET", null);

        assertEquals(200, resposta.statusCode());
        assertTrue(resposta.body().contains("\"autenticado\":false"));
    }

    // TE-03: Bloqueia cadastro sem autenticação.
    @Test
    public void deveBloquearCadastroSemAutenticacao() throws Exception {
        String dados = """
            {
              "nome": "Cliente de Teste",
              "telefone": "19999999999",
              "email": "teste@example.com"
            }
            """;

        HttpResponse<String> resposta = requisitar(
            "/clientes", "POST", dados
        );

        assertEquals(401, resposta.statusCode());
        assertTrue(resposta.body().contains("SESSAO_INVALIDA"));
    }

    // TE-04: Bloqueia consulta de clientes sem sessão.
    @Test
    public void deveBloquearConsultaDeClientesSemSessao() throws Exception {
        HttpResponse<String> resposta = requisitar("/clientes", "GET", null);

        assertEquals(401, resposta.statusCode());
        assertTrue(resposta.body().contains("SESSAO_INVALIDA"));
    }

    // TE-05: Verifica o encerramento de sessão.
    @Test
    public void deveEncerrarSessao() throws Exception {
        HttpResponse<String> resposta = requisitar("/sessao", "DELETE", null);

        assertEquals(200, resposta.statusCode());
        assertTrue(resposta.body().contains("\"autenticado\":false"));
    }

    // TE-06: Rejeita um token de sessão inexistente.
    @Test
    public void deveRejeitarTokenDeSessaoInexistente() throws Exception {
        HttpRequest requisicao = HttpRequest.newBuilder()
            .uri(URI.create(enderecoBase + "/clientes"))
            .header("Cookie", "servihubSessao=token-inexistente")
            .GET()
            .build();

        HttpResponse<String> resposta = cliente.send(
            requisicao,
            HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(401, resposta.statusCode());
        assertTrue(resposta.body().contains("SESSAO_INVALIDA"));
    }
}

