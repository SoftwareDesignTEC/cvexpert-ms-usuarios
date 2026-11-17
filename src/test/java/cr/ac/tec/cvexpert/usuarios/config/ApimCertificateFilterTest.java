package cr.ac.tec.cvexpert.usuarios.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class ApimCertificateFilterTest {

    // Parte pública del certificado de cliente de APIM (vigente del 2026-11-01 al 2027-11-01).
    private static final String THUMBPRINT = "FDF119D5BE797DD62E6CC87002C183CC6E70274B";
    private static final Clock VIGENTE = Clock.fixed(Instant.parse("2027-01-15T12:00:00Z"), ZoneOffset.UTC);
    private static final Clock VENCIDO = Clock.fixed(Instant.parse("2028-01-15T12:00:00Z"), ZoneOffset.UTC);

    private static String certBase64;

    @BeforeAll
    static void cargarCertificado() throws Exception {
        try (InputStream in = ApimCertificateFilterTest.class.getResourceAsStream("/apim/cvexpert-apim-client.cer")) {
            certBase64 = Base64.getEncoder().encodeToString(in.readAllBytes());
        }
    }

    @Test
    void sinHuellasConfiguradasNoExigeNada() throws Exception {
        MockFilterChain chain = new MockFilterChain();

        filtro("", VIGENTE).doFilter(peticion("/profiles/sub-1", null), new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void certificadoDeApimVigentePasa() throws Exception {
        MockFilterChain chain = new MockFilterChain();

        filtro(THUMBPRINT, VIGENTE).doFilter(peticion("/profiles/sub-1", certBase64), new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void huellaConDosPuntosYMinusculasTambienSeAcepta() throws Exception {
        MockFilterChain chain = new MockFilterChain();

        filtro("fd:f1:19:d5:be:79:7d:d6:2e:6c:c8:70:02:c1:83:cc:6e:70:27:4b", VIGENTE)
                .doFilter(peticion("/help", certBase64), new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isNotNull();
    }

    @Test
    void sinCertificadoResponde403ConApiError() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filtro(THUMBPRINT, VIGENTE).doFilter(peticion("/profiles/sub-1", null), response, chain);

        assertThat(chain.getRequest()).isNull();
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString())
                .contains("\"status\":403")
                .contains("\"path\":\"/profiles/sub-1\"");
    }

    @Test
    void certificadoConOtraHuellaResponde403() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filtro("0000000000000000000000000000000000000000", VIGENTE)
                .doFilter(peticion("/profiles/sub-1", certBase64), response, chain);

        assertThat(chain.getRequest()).isNull();
        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void certificadoVencidoResponde403() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filtro(THUMBPRINT, VENCIDO).doFilter(peticion("/profiles/sub-1", certBase64), response, chain);

        assertThat(chain.getRequest()).isNull();
        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void encabezadoQueNoEsCertificadoResponde403() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filtro(THUMBPRINT, VIGENTE).doFilter(peticion("/help", "esto-no-es-un-certificado"), response, chain);

        assertThat(chain.getRequest()).isNull();
        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void healthNoExigeCertificado() throws Exception {
        MockFilterChain chain = new MockFilterChain();

        filtro(THUMBPRINT, VIGENTE).doFilter(peticion("/actuator/health", null), new MockHttpServletResponse(), chain);

        assertThat(chain.getRequest()).isNotNull();
    }

    private static ApimCertificateFilter filtro(String thumbprints, Clock clock) {
        return new ApimCertificateFilter(thumbprints, clock, new ObjectMapper());
    }

    private static MockHttpServletRequest peticion(String path, String cert) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        if (cert != null) {
            request.addHeader(ApimCertificateFilter.CERT_HEADER, cert);
        }
        return request;
    }
}
