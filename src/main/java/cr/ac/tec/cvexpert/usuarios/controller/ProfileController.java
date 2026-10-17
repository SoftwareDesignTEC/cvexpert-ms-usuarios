package cr.ac.tec.cvexpert.usuarios.controller;

import cr.ac.tec.cvexpert.usuarios.dto.DataExportResponse;
import cr.ac.tec.cvexpert.usuarios.dto.PageResponse;
import cr.ac.tec.cvexpert.usuarios.dto.SessionResponse;
import cr.ac.tec.cvexpert.usuarios.dto.UpdateProfileRequest;
import cr.ac.tec.cvexpert.usuarios.dto.UserProfileResponse;
import cr.ac.tec.cvexpert.usuarios.service.ClientInfo;
import cr.ac.tec.cvexpert.usuarios.service.CurrentUser;
import cr.ac.tec.cvexpert.usuarios.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Las 4 operaciones de perfil. X-User-Id y X-User-Email los agrega APIM. */
@RestController
@RequestMapping("/profiles/{id}")
public class ProfileController {

    private final ProfileService service;

    public ProfileController(ProfileService service) {
        this.service = service;
    }

    @GetMapping
    public UserProfileResponse get(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader(value = "X-Client", required = false) String client,
            @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent) {
        return service.getOrCreate(id, new CurrentUser(userId, email), ClientInfo.of(client, userAgent));
    }

    @PutMapping
    public UserProfileResponse update(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Email") String email,
            @Valid @RequestBody UpdateProfileRequest body) {
        return service.update(id, new CurrentUser(userId, email), body);
    }

    @GetMapping("/sessions")
    public PageResponse<SessionResponse> sessions(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader(value = "X-Client", required = false) String client,
            @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return service.sessions(id, new CurrentUser(userId, email), ClientInfo.of(client, userAgent), page, pageSize);
    }

    @GetMapping("/export")
    public DataExportResponse export(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader(value = "X-Client", required = false) String client,
            @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent) {
        return service.export(id, new CurrentUser(userId, email), ClientInfo.of(client, userAgent));
    }
}
