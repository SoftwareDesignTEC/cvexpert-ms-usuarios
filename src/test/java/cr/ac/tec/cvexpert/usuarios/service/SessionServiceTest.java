package cr.ac.tec.cvexpert.usuarios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import cr.ac.tec.cvexpert.usuarios.repository.SessionRepository;
import java.time.Clock;
import org.junit.jupiter.api.Test;

class SessionServiceTest {

    private final SessionService service = new SessionService(mock(SessionRepository.class), Clock.systemUTC());

    @Test
    void mismoDispositivoDaElMismoId() {
        ClientInfo a = ClientInfo.of("web", "Mozilla/5.0 Chrome/130");
        assertThat(service.sessionId("u1", a)).isEqualTo(service.sessionId("u1", a));
        assertThat(service.sessionId("u1", a)).isNotEqualTo(service.sessionId("u2", a));
    }

    @Test
    void reconoceNavegadorYPlataforma() {
        String chromeWindows = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/130.0 Safari/537.36";
        assertThat(SessionService.deviceNameOf(chromeWindows, "web")).isEqualTo("Chrome en Windows");
        assertThat(SessionService.deviceNameOf("okhttp/4.12.0", "android")).isEqualTo("App Android");
    }

    @Test
    void clienteDesconocidoSeNormaliza() {
        assertThat(ClientInfo.of("ios", null).client()).isEqualTo("unknown");
        assertThat(ClientInfo.of(" WEB ", null).client()).isEqualTo("web");
    }
}
