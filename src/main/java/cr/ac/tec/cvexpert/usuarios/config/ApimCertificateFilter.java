package cr.ac.tec.cvexpert.usuarios.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import cr.ac.tec.cvexpert.usuarios.dto.ApiError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Clock;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Solo deja pasar llamadas que traen el certificado de cliente de APIM.
 *
 * <p>En App Service el TLS termina antes de la aplicación: con el modo de certificados de cliente
 * activado, App Service reenvía el certificado en el encabezado {@code X-ARR-ClientCert} (DER en
 * base64). Aquí se compara su huella SHA-1 con {@code APIM_CERT_THUMBPRINTS} (varias separadas por
 * coma, para poder rotarlo) y se revisa que esté vigente.
 *
 * <p>Si la variable está vacía el filtro no hace nada: así se corre en local y en las pruebas.
 */
@Component
public class ApimCertificateFilter extends OncePerRequestFilter {

    static final String CERT_HEADER = "X-ARR-ClientCert";
    private static final String HEALTH_PATH = "/actuator/health";

    private final Set<String> thumbprints;
    private final Clock clock;
    private final ObjectMapper mapper;

    public ApimCertificateFilter(
            @Value("${apim.cert-thumbprints:}") String thumbprints, Clock clock, ObjectMapper mapper) {
        this.thumbprints = parse(thumbprints);
        this.clock = clock;
        this.mapper = mapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return thumbprints.isEmpty() || request.getRequestURI().startsWith(HEALTH_PATH);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (isTrusted(request.getHeader(CERT_HEADER))) {
            chain.doFilter(request, response);
            return;
        }
        ApiError body = new ApiError(HttpStatus.FORBIDDEN.value(),
                "La llamada no trae el certificado de cliente de APIM.", request.getRequestURI(), clock.millis());
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getOutputStream(), body);
    }

    private boolean isTrusted(String header) {
        if (header == null || header.isBlank()) {
            return false;
        }
        try {
            byte[] der = Base64.getMimeDecoder().decode(header);
            X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509")
                    .generateCertificate(new ByteArrayInputStream(der));
            cert.checkValidity(Date.from(clock.instant()));
            return thumbprints.contains(sha1(cert.getEncoded()));
        } catch (IllegalArgumentException | CertificateException e) {
            return false;
        }
    }

    private static String sha1(byte[] data) {
        try {
            return HexFormat.of().withUpperCase().formatHex(MessageDigest.getInstance("SHA-1").digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 no está disponible en esta JVM", e);
        }
    }

    private static Set<String> parse(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(value.split(","))
                .map(s -> s.replace(":", "").replace(" ", "").trim().toUpperCase(Locale.ROOT))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }
}
