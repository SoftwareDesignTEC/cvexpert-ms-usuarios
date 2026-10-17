package cr.ac.tec.cvexpert.usuarios.dto;

import cr.ac.tec.cvexpert.usuarios.model.Profile;
import cr.ac.tec.cvexpert.usuarios.model.Role;
import java.util.List;

/** Perfil tal como sale por la API. Mantiene los nombres de la Fase I (fullName, address). */
public record UserProfileResponse(
        String id,
        String fullName,
        String email,
        String phone,
        String address,
        String photoUrl,
        String bio,
        List<String> types,
        List<String> modalities,
        List<String> sectors,
        Role role,
        long createdAt,
        long updatedAt) {

    public static UserProfileResponse from(Profile p) {
        return new UserProfileResponse(
                p.getId(), p.getFullName(), p.getEmail(), p.getPhone(), p.getAddress(),
                p.getPhotoUrl(), p.getBio(),
                nullSafe(p.getTypes()), nullSafe(p.getModalities()), nullSafe(p.getSectors()),
                p.getRole(), p.getCreatedAt(), p.getUpdatedAt());
    }

    private static List<String> nullSafe(List<String> list) {
        return list == null ? List.of() : List.copyOf(list);
    }
}
