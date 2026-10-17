package cr.ac.tec.cvexpert.usuarios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cr.ac.tec.cvexpert.usuarios.config.AppProperties;
import cr.ac.tec.cvexpert.usuarios.dto.UpdateProfileRequest;
import cr.ac.tec.cvexpert.usuarios.dto.UserProfileResponse;
import cr.ac.tec.cvexpert.usuarios.exception.BadRequestException;
import cr.ac.tec.cvexpert.usuarios.exception.ForbiddenException;
import cr.ac.tec.cvexpert.usuarios.exception.NotFoundException;
import cr.ac.tec.cvexpert.usuarios.model.Profile;
import cr.ac.tec.cvexpert.usuarios.model.Role;
import cr.ac.tec.cvexpert.usuarios.repository.ProfileRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProfileServiceTest {

    private static final long NOW = 1_793_491_200_000L;
    private static final CurrentUser ANA = new CurrentUser("sub-ana", "ana@example.com");
    private static final CurrentUser GABO = new CurrentUser("sub-gabo", "Gabox9823@gmail.com");
    private static final ClientInfo WEB = ClientInfo.of("web", "Mozilla/5.0 (Windows NT 10.0) Chrome/130.0");

    private ProfileRepository repository;
    private SessionService sessions;
    private ProfileService service;

    @BeforeEach
    void setUp() {
        repository = mock(ProfileRepository.class);
        sessions = mock(SessionService.class);
        when(repository.save(any(Profile.class))).thenAnswer(inv -> inv.getArgument(0));
        Clock clock = Clock.fixed(Instant.ofEpochMilli(NOW), ZoneOffset.UTC);
        AppProperties props = new AppProperties("gabox9823@gmail.com, otro@example.com", false);
        service = new ProfileService(repository, sessions, props, clock);
    }

    @Test
    void primeraLecturaCreaElPerfilComoUsuario() {
        when(repository.findById("sub-ana")).thenReturn(Optional.empty());

        UserProfileResponse p = service.getOrCreate("sub-ana", ANA, WEB);

        assertThat(p.id()).isEqualTo("sub-ana");
        assertThat(p.email()).isEqualTo("ana@example.com");
        assertThat(p.role()).isEqualTo(Role.USER);
        assertThat(p.createdAt()).isEqualTo(NOW);
        assertThat(p.types()).isEmpty();
        verify(sessions).touch("sub-ana", WEB);
    }

    @Test
    void correoDeAdminEmailsQuedaComoAdministradorSinImportarMayusculas() {
        when(repository.findById("sub-gabo")).thenReturn(Optional.empty());

        UserProfileResponse p = service.getOrCreate("sub-gabo", GABO, WEB);

        assertThat(p.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void perfilExistenteSeAsciendeSiSuCorreoSeAgregoDespues() {
        Profile viejo = profile("sub-gabo", "gabox9823@gmail.com");
        when(repository.findById("sub-gabo")).thenReturn(Optional.of(viejo));

        UserProfileResponse p = service.getOrCreate("sub-gabo", GABO, WEB);

        assertThat(p.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void aceptaElCorreoComoIdComoHaceAndroidEnLaFaseI() {
        when(repository.findById("sub-ana")).thenReturn(Optional.of(profile("sub-ana", "ana@example.com")));

        UserProfileResponse p = service.getOrCreate("ANA@example.com", ANA, WEB);

        assertThat(p.id()).isEqualTo("sub-ana");
    }

    @Test
    void rechazaElPerfilDeOtraPersona() {
        assertThatThrownBy(() -> service.getOrCreate("sub-otro", ANA, WEB))
                .isInstanceOf(ForbiddenException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void rechazaSiFaltanLosEncabezadosDeApim() {
        assertThatThrownBy(() -> service.getOrCreate("sub-ana", new CurrentUser("sub-ana", " "), WEB))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void actualizacionParcialSoloCambiaLoQueViene() {
        Profile p = profile("sub-ana", "ana@example.com");
        p.setFullName("Ana");
        p.setPhone("8888-8888");
        when(repository.findById("sub-ana")).thenReturn(Optional.of(p));

        UpdateProfileRequest req = new UpdateProfileRequest(
                "Ana Rojas", null, "San Carlos", null, null, List.of("internship"), null, null);
        UserProfileResponse r = service.update("sub-ana", ANA, req);

        assertThat(r.fullName()).isEqualTo("Ana Rojas");
        assertThat(r.phone()).isEqualTo("8888-8888");
        assertThat(r.address()).isEqualTo("San Carlos");
        assertThat(r.types()).containsExactly("internship");
        assertThat(r.email()).isEqualTo("ana@example.com");
        assertThat(r.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void actualizarSinPerfilDa404() {
        when(repository.findById("sub-ana")).thenReturn(Optional.empty());
        UpdateProfileRequest req = new UpdateProfileRequest("Ana", null, null, null, null, null, null, null);

        assertThatThrownBy(() -> service.update("sub-ana", ANA, req))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void sesionesValidanLaPaginacion() {
        assertThatThrownBy(() -> service.sessions("sub-ana", ANA, WEB, 0, 10))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.sessions("sub-ana", ANA, WEB, 1, 101))
                .isInstanceOf(BadRequestException.class);
    }

    private static Profile profile(String id, String email) {
        Profile p = new Profile();
        p.setId(id);
        p.setEmail(email);
        p.setRole(Role.USER);
        p.setCreatedAt(1L);
        p.setUpdatedAt(1L);
        return p;
    }
}
