package cr.ac.tec.cvexpert.usuarios.dto;

import java.util.List;

public record DataExportResponse(
        String exportVersion,
        long generatedAt,
        UserProfileResponse profile,
        List<SessionResponse> sessions) {
}
