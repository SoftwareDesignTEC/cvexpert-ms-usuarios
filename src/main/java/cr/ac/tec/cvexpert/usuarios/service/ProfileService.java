package cr.ac.tec.cvexpert.usuarios.service;

import cr.ac.tec.cvexpert.usuarios.config.AppProperties;
import cr.ac.tec.cvexpert.usuarios.dto.DataExportResponse;
import cr.ac.tec.cvexpert.usuarios.dto.PageResponse;
import cr.ac.tec.cvexpert.usuarios.dto.SessionResponse;
import cr.ac.tec.cvexpert.usuarios.dto.UpdateProfileRequest;
import cr.ac.tec.cvexpert.usuarios.dto.UserProfileResponse;
import cr.ac.tec.cvexpert.usuarios.exception.BadRequestException;
import cr.ac.tec.cvexpert.usuarios.exception.ForbiddenException;
import cr.ac.tec.cvexpert.usuarios.exception.NotFoundException;
import cr.ac.tec.cvexpert.usuarios.model.Profile;
import cr.ac.tec.cvexpert.usuarios.model.Role;
import cr.ac.tec.cvexpert.usuarios.model.Session;
import cr.ac.tec.cvexpert.usuarios.repository.ProfileRepository;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

/** Reglas del perfil: dueño, creación con rol, actualización parcial, sesiones y exportación. */
@Service
public class ProfileService {

    static final String EXPORT_VERSION = "2.0";

    private final ProfileRepository repository;
    private final SessionService sessions;
    private final AppProperties properties;
    private final Clock clock;

    public ProfileService(ProfileRepository repository, SessionService sessions,
                          AppProperties properties, Clock clock) {
        this.repository = repository;
        this.sessions = sessions;
        this.properties = properties;
        this.clock = clock;
    }

    /** Devuelve el perfil y, si no existe, lo crea. También registra la sesión del dispositivo. */
    public UserProfileResponse getOrCreate(String pathId, CurrentUser user, ClientInfo client) {
        assertOwner(pathId, user);
        Profile profile = repository.findById(user.userId())
                .map(p -> promoteIfAdmin(p, user))
                .orElseGet(() -> repository.save(newProfile(user)));
        sessions.touch(user.userId(), client);
        return UserProfileResponse.from(profile);
    }

    public UserProfileResponse update(String pathId, CurrentUser user, UpdateProfileRequest req) {
        assertOwner(pathId, user);
        Profile p = existing(user);
        if (req.fullName() != null) p.setFullName(req.fullName().trim());
        if (req.phone() != null) p.setPhone(req.phone().trim());
        if (req.address() != null) p.setAddress(req.address().trim());
        if (req.photoUrl() != null) p.setPhotoUrl(req.photoUrl().trim());
        if (req.bio() != null) p.setBio(req.bio());
        if (req.types() != null) p.setTypes(new ArrayList<>(req.types()));
        if (req.modalities() != null) p.setModalities(new ArrayList<>(req.modalities()));
        if (req.sectors() != null) p.setSectors(new ArrayList<>(req.sectors()));
        p.setUpdatedAt(clock.millis());
        return UserProfileResponse.from(repository.save(p));
    }

    public PageResponse<SessionResponse> sessions(String pathId, CurrentUser user, ClientInfo client,
                                                  int page, int pageSize) {
        if (page < 1 || pageSize < 1 || pageSize > 100) {
            throw new BadRequestException("page debe ser >= 1 y pageSize debe estar entre 1 y 100.");
        }
        assertOwner(pathId, user);
        existing(user);
        String currentId = sessions.sessionId(user.userId(), client);
        Page<Session> result = sessions.page(user.userId(), page, pageSize);
        List<SessionResponse> items = result.getContent().stream()
                .map(s -> SessionResponse.from(s, currentId))
                .toList();
        return new PageResponse<>(items, result.getTotalElements(), page, pageSize);
    }

    public DataExportResponse export(String pathId, CurrentUser user, ClientInfo client) {
        assertOwner(pathId, user);
        Profile p = existing(user);
        String currentId = sessions.sessionId(user.userId(), client);
        List<SessionResponse> all = sessions.all(user.userId()).stream()
                .map(s -> SessionResponse.from(s, currentId))
                .toList();
        return new DataExportResponse(EXPORT_VERSION, clock.millis(), UserProfileResponse.from(p), all);
    }

    /**
     * El {id} de la ruta puede ser el sub (web) o el correo (Android, Fase I),
     * pero siempre tiene que ser del usuario que APIM autenticó.
     */
    void assertOwner(String pathId, CurrentUser user) {
        if (isBlank(user.userId()) || isBlank(user.email())) {
            throw new ForbiddenException("Faltan los encabezados de identidad que agrega APIM.");
        }
        boolean sameId = user.userId().equals(pathId);
        boolean sameEmail = user.email().equalsIgnoreCase(pathId);
        if (!sameId && !sameEmail) {
            throw new ForbiddenException("El perfil solicitado no corresponde al usuario autenticado.");
        }
    }

    private Profile existing(CurrentUser user) {
        return repository.findById(user.userId())
                .orElseThrow(() -> new NotFoundException(
                        "El perfil no existe todavía. Se crea con GET /profiles/{id}."));
    }

    private Profile newProfile(CurrentUser user) {
        long now = clock.millis();
        Profile p = new Profile();
        p.setId(user.userId());
        p.setEmail(user.email().toLowerCase(Locale.ROOT));
        p.setRole(isAdminEmail(user.email()) ? Role.ADMIN : Role.USER);
        p.setCreatedAt(now);
        p.setUpdatedAt(now);
        return p;
    }

    /** Si el correo se agregó a ADMIN_EMAILS después de crear el perfil, se asciende al leerlo. */
    private Profile promoteIfAdmin(Profile p, CurrentUser user) {
        if (p.getRole() != Role.ADMIN && isAdminEmail(user.email())) {
            p.setRole(Role.ADMIN);
            p.setUpdatedAt(clock.millis());
            return repository.save(p);
        }
        return p;
    }

    private boolean isAdminEmail(String email) {
        return properties.adminEmailSet().contains(email.toLowerCase(Locale.ROOT));
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
