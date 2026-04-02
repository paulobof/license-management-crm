package com.prediman.crm.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoogleDriveService — testes de unidade")
class GoogleDriveServiceTest {

    @Test
    @DisplayName("isEnabled retorna false quando desabilitado")
    void isEnabled_falseQuandoDesabilitado() {
        GoogleDriveService service = new GoogleDriveService();
        ReflectionTestUtils.setField(service, "enabled", false);
        ReflectionTestUtils.setField(service, "credentialsPath", "");
        ReflectionTestUtils.setField(service, "rootFolderId", "");

        assertFalse(service.isEnabled());
    }

    @Test
    @DisplayName("upload retorna null quando desabilitado")
    void upload_retornaNullQuandoDesabilitado() {
        GoogleDriveService service = new GoogleDriveService();
        ReflectionTestUtils.setField(service, "enabled", false);
        ReflectionTestUtils.setField(service, "credentialsPath", "");
        ReflectionTestUtils.setField(service, "rootFolderId", "");

        assertNull(service.upload("file.pdf", "application/pdf", new byte[]{1, 2, 3}, null));
    }

    @Test
    @DisplayName("createFolder retorna null quando desabilitado")
    void createFolder_retornaNullQuandoDesabilitado() {
        GoogleDriveService service = new GoogleDriveService();
        ReflectionTestUtils.setField(service, "enabled", false);
        ReflectionTestUtils.setField(service, "credentialsPath", "");
        ReflectionTestUtils.setField(service, "rootFolderId", "");

        assertNull(service.createFolder("Nova Pasta", null));
    }

    @Test
    @DisplayName("delete nao faz nada quando desabilitado")
    void delete_noopQuandoDesabilitado() {
        GoogleDriveService service = new GoogleDriveService();
        ReflectionTestUtils.setField(service, "enabled", false);
        ReflectionTestUtils.setField(service, "credentialsPath", "");
        ReflectionTestUtils.setField(service, "rootFolderId", "");

        // Não deve lançar exceção
        assertDoesNotThrow(() -> service.delete("fileId123"));
    }

    @Test
    @DisplayName("delete nao faz nada quando fileId vazio")
    void delete_noopQuandoFileIdVazio() {
        GoogleDriveService service = new GoogleDriveService();
        ReflectionTestUtils.setField(service, "enabled", true);
        ReflectionTestUtils.setField(service, "credentialsPath", "");
        ReflectionTestUtils.setField(service, "rootFolderId", "");

        assertDoesNotThrow(() -> service.delete(null));
        assertDoesNotThrow(() -> service.delete(""));
    }

    @Test
    @DisplayName("init sem credenciais nao inicializa driveService")
    void init_semCredenciais() {
        GoogleDriveService service = new GoogleDriveService();
        ReflectionTestUtils.setField(service, "enabled", true);
        ReflectionTestUtils.setField(service, "credentialsPath", "");
        ReflectionTestUtils.setField(service, "rootFolderId", "");

        service.init();

        assertFalse(service.isEnabled());
    }

    @Test
    @DisplayName("init com credenciais invalidas loga erro")
    void init_credenciaisInvalidas() {
        GoogleDriveService service = new GoogleDriveService();
        ReflectionTestUtils.setField(service, "enabled", true);
        ReflectionTestUtils.setField(service, "credentialsPath", "/caminho/inexistente/creds.json");
        ReflectionTestUtils.setField(service, "rootFolderId", "folder123");

        service.init();

        // driveService fica null por causa do erro
        assertFalse(service.isEnabled());
    }

    @Test
    @DisplayName("GoogleDriveResult armazena fileId e webViewLink")
    void googleDriveResult() {
        GoogleDriveService.GoogleDriveResult result = new GoogleDriveService.GoogleDriveResult("id1", "https://link");
        assertEquals("id1", result.getFileId());
        assertEquals("https://link", result.getWebViewLink());
    }
}
