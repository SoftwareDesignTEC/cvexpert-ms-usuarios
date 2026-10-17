package cr.ac.tec.cvexpert.usuarios.service;

import cr.ac.tec.cvexpert.usuarios.model.Session;
import cr.ac.tec.cvexpert.usuarios.repository.SessionRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.util.HexFormat;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/** Registra los dispositivos desde los que se usa la cuenta. */
@Service
public class SessionService {

    private final SessionRepository repository;
    private final Clock clock;

    public SessionService(SessionRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    /** Crea o actualiza la sesión del dispositivo actual. */
    public Session touch(String userId, ClientInfo info) {
        String id = sessionId(userId, info);
        Session session = repository.findById(id).orElseGet(Session::new);
        session.setId(id);
        session.setUserId(userId);
        session.setClient(info.client());
        session.setPlatform(platformOf(info.userAgent(), info.client()));
        session.setDeviceName(deviceNameOf(info.userAgent(), info.client()));
        session.setLastActiveAt(clock.millis());
        return repository.save(session);
    }

    public Page<Session> page(String userId, int page, int pageSize) {
        return repository.findByUserIdOrderByLastActiveAtDesc(userId, PageRequest.of(page - 1, pageSize));
    }

    public List<Session> all(String userId) {
        return repository.findByUserIdOrderByLastActiveAtDesc(userId);
    }

    /** Identificador estable: el mismo usuario, cliente y User-Agent dan el mismo id. */
    public String sessionId(String userId, ClientInfo info) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha.digest((userId + "|" + info.client() + "|" + info.userAgent())
                    .getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash, 0, 12);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    static String platformOf(String ua, String client) {
        String u = ua.toLowerCase();
        if (u.contains("android") || client.equals("android")) return "Android";
        if (u.contains("iphone") || u.contains("ipad")) return "iOS";
        if (u.contains("windows")) return "Windows";
        if (u.contains("mac os")) return "macOS";
        if (u.contains("linux")) return "Linux";
        return "Desconocida";
    }

    static String deviceNameOf(String ua, String client) {
        String u = ua.toLowerCase();
        if (client.equals("android") && !u.contains("mozilla")) return "App Android";
        String browser;
        if (u.contains("edg/")) browser = "Edge";
        else if (u.contains("opr/")) browser = "Opera";
        else if (u.contains("firefox/")) browser = "Firefox";
        else if (u.contains("chrome/")) browser = "Chrome";
        else if (u.contains("safari/")) browser = "Safari";
        else browser = "Navegador";
        return browser + " en " + platformOf(ua, client);
    }
}
