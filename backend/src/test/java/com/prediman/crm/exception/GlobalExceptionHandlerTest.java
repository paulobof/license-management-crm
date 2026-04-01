package com.prediman.crm.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Mock
    private BindingResult bindingResult;

    // -------------------------------------------------------------------------
    // BusinessException -> 422 Unprocessable Entity
    // -------------------------------------------------------------------------

    @Test
    void handleBusiness_returnsBadRequest() {
        BusinessException ex = new BusinessException("Regra de negócio violada");

        ResponseEntity<ErrorResponse> response = handler.handleBusiness(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(422);
        assertThat(response.getBody().getMessage()).isEqualTo("Regra de negócio violada");
        assertThat(response.getBody().getError()).isEqualTo("Regra de negócio");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleBusiness_preservesCustomMessage() {
        String customMessage = "Cliente não pode ser removido pois possui contratos ativos";
        BusinessException ex = new BusinessException(customMessage);

        ResponseEntity<ErrorResponse> response = handler.handleBusiness(ex);

        assertThat(response.getBody().getMessage()).isEqualTo(customMessage);
    }

    // -------------------------------------------------------------------------
    // ResourceNotFoundException -> 404 Not Found
    // -------------------------------------------------------------------------

    @Test
    void handleNotFound_returnsNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Cliente", 42L);

        ResponseEntity<ErrorResponse> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).contains("42");
        assertThat(response.getBody().getError()).isEqualTo("Recurso não encontrado");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleNotFound_withMessageConstructor_returnsCorrectMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Recurso customizado não localizado");

        ResponseEntity<ErrorResponse> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).isEqualTo("Recurso customizado não localizado");
    }

    // -------------------------------------------------------------------------
    // MethodArgumentNotValidException -> 400 Bad Request with field errors
    // -------------------------------------------------------------------------

    @Test
    void handleValidationErrors_returnsFieldErrors() {
        FieldError razaoSocialError = new FieldError("clienteRequest", "razaoSocial", "não deve estar em branco");
        FieldError cnpjError = new FieldError("clienteRequest", "cnpj", "CNPJ inválido");

        when(bindingResult.getFieldErrors()).thenReturn(List.of(razaoSocialError, cnpjError));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationErrors(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Erro de validação");
        assertThat(response.getBody().getMessage()).isEqualTo("Verifique os campos informados");
        assertThat(response.getBody().getFieldErrors()).hasSize(2);

        ErrorResponse.FieldError first = response.getBody().getFieldErrors().get(0);
        assertThat(first.getField()).isEqualTo("razaoSocial");
        assertThat(first.getMessage()).isEqualTo("não deve estar em branco");

        ErrorResponse.FieldError second = response.getBody().getFieldErrors().get(1);
        assertThat(second.getField()).isEqualTo("cnpj");
        assertThat(second.getMessage()).isEqualTo("CNPJ inválido");
    }

    @Test
    void handleValidationErrors_withNoFieldErrors_returnsEmptyList() {
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationErrors(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getFieldErrors()).isEmpty();
    }

    @Test
    void handleValidationErrors_timestampIsNotNull() {
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleValidationErrors(ex);

        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    // -------------------------------------------------------------------------
    // DataIntegrityViolationException -> 409 Conflict
    // -------------------------------------------------------------------------

    @Test
    void handleDataIntegrity_genericViolation_returnsConflict() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("constraint violation");

        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrity(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("Conflito de dados");
        assertThat(response.getBody().getMessage()).isEqualTo("Violação de integridade de dados");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleDataIntegrity_cnpjViolation_returnsCnpjMessage() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("duplicate key cnpj unique");

        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrity(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).isEqualTo("CNPJ já cadastrado no sistema");
    }

    @Test
    void handleDataIntegrity_emailViolation_returnsEmailMessage() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("duplicate key email unique");

        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrity(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).isEqualTo("E-mail já cadastrado no sistema");
    }

    @Test
    void handleDataIntegrity_nullMessage_returnsGenericMessage() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(null);

        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrity(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).isEqualTo("Violação de integridade de dados");
    }

    // -------------------------------------------------------------------------
    // BadCredentialsException -> 401 Unauthorized
    // -------------------------------------------------------------------------

    @Test
    void handleBadCredentials_returnsUnauthorized() {
        BadCredentialsException ex = new BadCredentialsException("bad credentials");

        ResponseEntity<ErrorResponse> response = handler.handleBadCredentials(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getError()).isEqualTo("Credenciais inválidas");
        assertThat(response.getBody().getMessage()).isEqualTo("E-mail ou senha incorretos");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    // -------------------------------------------------------------------------
    // DisabledException -> 401 Unauthorized
    // -------------------------------------------------------------------------

    @Test
    void handleDisabled_returnsUnauthorized() {
        DisabledException ex = new DisabledException("user is disabled");

        ResponseEntity<ErrorResponse> response = handler.handleDisabled(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getError()).isEqualTo("Acesso negado");
        assertThat(response.getBody().getMessage()).isEqualTo("Usuário inativo");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    // -------------------------------------------------------------------------
    // IllegalArgumentException -> 400 Bad Request
    // -------------------------------------------------------------------------

    @Test
    void handleIllegalArgument_returnsBadRequest() {
        IllegalArgumentException ex = new IllegalArgumentException("argumento inválido");

        ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Requisição inválida");
        assertThat(response.getBody().getMessage()).isEqualTo("argumento inválido");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    // -------------------------------------------------------------------------
    // Exception (generic) -> 500 Internal Server Error
    // -------------------------------------------------------------------------

    @Test
    void handleGeneral_returnsInternalServerError() {
        Exception ex = new RuntimeException("erro inesperado");

        ResponseEntity<ErrorResponse> response = handler.handleGeneral(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Erro interno");
        assertThat(response.getBody().getMessage()).isEqualTo("Ocorreu um erro inesperado. Tente novamente mais tarde.");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleGeneral_withCheckedException_returnsInternalServerError() {
        Exception ex = new Exception("checked exception");

        ResponseEntity<ErrorResponse> response = handler.handleGeneral(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getStatus()).isEqualTo(500);
    }
}
