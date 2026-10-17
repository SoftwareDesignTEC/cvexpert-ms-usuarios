package cr.ac.tec.cvexpert.usuarios.config;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración propia del servicio (prefijo {@code app} en application.yml).
 *
 * @param adminEmails correos separados por coma que reciben el rol de administrador
 * @param seedEnabled si se cargan los textos legales y de ayuda iniciales
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(String adminEmails, boolean seedEnabled) {

    public Set<String> adminEmailSet() {
        if (adminEmails == null || adminEmails.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(adminEmails.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }
}
