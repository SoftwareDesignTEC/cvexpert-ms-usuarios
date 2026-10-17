package cr.ac.tec.cvexpert.usuarios.dto;

import cr.ac.tec.cvexpert.usuarios.model.Session;

public record SessionResponse(
        String id,
        String deviceName,
        String platform,
        String client,
        long lastActiveAt,
        boolean current) {

    public static SessionResponse from(Session s, String currentSessionId) {
        return new SessionResponse(s.getId(), s.getDeviceName(), s.getPlatform(), s.getClient(),
                s.getLastActiveAt(), s.getId().equals(currentSessionId));
    }
}
