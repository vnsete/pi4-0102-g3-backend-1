
package br.servihub;

import org.junit.Test;
import java.time.Instant;
import static org.junit.Assert.*;

public class AccountPolicyTest {

    // Verifica se uma sessão válida é reconhecida.
    @Test
    public void deveReconhecerSessaoValida() {
        String token = "teste-valido";
        Main.sessions.put(token, Instant.now().plusSeconds(1800));

        assertTrue(Main.sessions.get(token).isAfter(Instant.now()));
        Main.sessions.remove(token);
    }

    // Verifica se uma sessão expirada é identificada.
    @Test
    public void deveIdentificarSessaoExpirada() {
        String token = "teste-expirado";
        Main.sessions.put(token, Instant.now().minusSeconds(60));

        assertTrue(Main.sessions.get(token).isBefore(Instant.now()));
        Main.sessions.remove(token);
    }
}
