package cr.ac.tec.cvexpert.usuarios.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cr.ac.tec.cvexpert.usuarios.config.ClockConfig;
import cr.ac.tec.cvexpert.usuarios.dto.UserProfileResponse;
import cr.ac.tec.cvexpert.usuarios.model.Role;
import cr.ac.tec.cvexpert.usuarios.service.ProfileService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProfileController.class)
@Import(ClockConfig.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ProfileService service;

    @Test
    void devuelveElPerfilConRolEnMinusculaYFechasEnEpoch() throws Exception {
        UserProfileResponse p = new UserProfileResponse("sub-1", "Gabo", "gcabrerarojas.25@gmail.com", null, null,
                null, null, List.of(), List.of("remote"), List.of(), Role.ADMIN, 1_793_491_200_000L, 1_793_491_200_000L);
        when(service.getOrCreate(eq("sub-1"), any(), any())).thenReturn(p);

        mvc.perform(get("/profiles/sub-1")
                        .header("X-User-Id", "sub-1")
                        .header("X-User-Email", "gcabrerarojas.25@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("admin"))
                .andExpect(jsonPath("$.createdAt").value(1_793_491_200_000L))
                .andExpect(jsonPath("$.modalities[0]").value("remote"));
    }

    @Test
    void sinEncabezadosDeApimResponde403() throws Exception {
        mvc.perform(get("/profiles/sub-1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void modalidadInvalidaResponde400() throws Exception {
        mvc.perform(put("/profiles/sub-1")
                        .header("X-User-Id", "sub-1")
                        .header("X-User-Email", "a@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"modalities\":[\"presencial\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
