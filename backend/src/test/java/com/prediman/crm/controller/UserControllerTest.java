package com.prediman.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prediman.crm.dto.UsuarioRequest;
import com.prediman.crm.dto.UsuarioResponse;
import com.prediman.crm.dto.UsuarioUpdateRequest;
import com.prediman.crm.model.enums.Perfil;
import com.prediman.crm.security.JwtAuthenticationFilter;
import com.prediman.crm.security.JwtTokenProvider;
import com.prediman.crm.security.RateLimitFilter;
import com.prediman.crm.security.UserDetailsServiceImpl;
import com.prediman.crm.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserController — testes de unidade")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    private UsuarioResponse buildUsuarioResponse(Long id, String nome) {
        return UsuarioResponse.builder()
                .id(id)
                .nome(nome)
                .email("usuario@example.com")
                .perfil(Perfil.ADMIN)
                .ativo(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/users
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/v1/users — retorna página de usuários")
    void findAll_returns200() throws Exception {
        UsuarioResponse usuario = buildUsuarioResponse(1L, "Admin");
        Page<UsuarioResponse> page = new PageImpl<>(List.of(usuario));

        when(userService.findAll(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Admin"));
    }

    @Test
    @DisplayName("GET /api/v1/users — paginação customizada")
    void findAll_withPagination_returns200() throws Exception {
        Page<UsuarioResponse> page = new PageImpl<>(List.of());

        when(userService.findAll(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/users")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/users
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/v1/users — cria usuário e retorna 201")
    void create_returns201() throws Exception {
        UsuarioRequest request = UsuarioRequest.builder()
                .nome("Novo Admin")
                .email("novo@example.com")
                .senha("Senha1234!")
                .perfil(Perfil.ADMIN)
                .build();
        UsuarioResponse response = buildUsuarioResponse(2L, "Novo Admin");

        when(userService.create(any(UsuarioRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.nome").value("Novo Admin"));
    }

    @Test
    @DisplayName("POST /api/v1/users — nome em branco retorna 400")
    void create_blankNome_returns400() throws Exception {
        UsuarioRequest request = UsuarioRequest.builder()
                .nome("")
                .email("novo@example.com")
                .senha("Senha1234!")
                .perfil(Perfil.ADMIN)
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/users — email inválido retorna 400")
    void create_invalidEmail_returns400() throws Exception {
        UsuarioRequest request = UsuarioRequest.builder()
                .nome("Admin")
                .email("email-invalido")
                .senha("Senha1234!")
                .perfil(Perfil.ADMIN)
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/users — senha muito curta retorna 400")
    void create_shortSenha_returns400() throws Exception {
        UsuarioRequest request = UsuarioRequest.builder()
                .nome("Admin")
                .email("novo@example.com")
                .senha("1234")
                .perfil(Perfil.ADMIN)
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/users — perfil nulo retorna 400")
    void create_nullPerfil_returns400() throws Exception {
        UsuarioRequest request = UsuarioRequest.builder()
                .nome("Admin")
                .email("novo@example.com")
                .senha("Senha1234!")
                .perfil(null)
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // PUT /api/v1/users/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("PUT /api/v1/users/{id} — atualiza usuário e retorna 200")
    void update_returns200() throws Exception {
        UsuarioUpdateRequest request = UsuarioUpdateRequest.builder()
                .nome("Admin Atualizado")
                .email("atualizado@example.com")
                .perfil(Perfil.USUARIO)
                .build();
        UsuarioResponse response = buildUsuarioResponse(1L, "Admin Atualizado");

        when(userService.update(eq(1L), any(UsuarioUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Admin Atualizado"));
    }

    @Test
    @DisplayName("PUT /api/v1/users/{id} — email inválido retorna 400")
    void update_invalidEmail_returns400() throws Exception {
        UsuarioUpdateRequest request = UsuarioUpdateRequest.builder()
                .nome("Admin")
                .email("invalido")
                .perfil(Perfil.ADMIN)
                .build();

        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // PATCH /api/v1/users/{id}/toggle-status
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("PATCH /api/v1/users/{id}/toggle-status — alterna status e retorna 200")
    void toggleStatus_returns200() throws Exception {
        UsuarioResponse response = buildUsuarioResponse(1L, "Admin");
        response.setAtivo(false);

        when(userService.toggleStatus(1L)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/users/1/toggle-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.ativo").value(false));
    }
}
