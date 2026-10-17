package cr.ac.tec.cvexpert.usuarios.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Cuerpo de PUT /profiles/{id}. Todo es opcional: un campo null no se toca.
 * Campos que no están aquí (email, role, id, fechas) se ignoran al deserializar.
 */
public record UpdateProfileRequest(
        @Size(min = 1, max = 120) String fullName,
        @Size(max = 30) String phone,
        @Size(max = 200) String address,
        @Size(max = 2048) @Pattern(regexp = "^https?://.+", message = "debe ser una URL http(s)") String photoUrl,
        @Size(max = 1000) String bio,
        List<@Pattern(regexp = "full-time|part-time|freelance|internship") String> types,
        List<@Pattern(regexp = "remote|on-site|hybrid") String> modalities,
        @Size(max = 20) List<@Size(max = 60) String> sectors) {
}
