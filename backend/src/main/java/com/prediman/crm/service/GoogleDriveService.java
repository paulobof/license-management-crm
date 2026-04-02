package com.prediman.crm.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class GoogleDriveService {

    @Value("${google.drive.enabled:false}")
    private boolean enabled;

    @Value("${google.drive.credentials-path:}")
    private String credentialsPath;

    @Value("${google.drive.root-folder-id:}")
    private String rootFolderId;

    private Drive driveService;

    @PostConstruct
    public void init() {
        if (!enabled || credentialsPath == null || credentialsPath.isBlank()) {
            log.info("Google Drive desabilitado (GOOGLE_DRIVE_ENABLED=false ou sem credenciais)");
            return;
        }

        try {
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(new FileInputStream(credentialsPath))
                    .createScoped(Collections.singletonList(DriveScopes.DRIVE_FILE));

            driveService = new Drive.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName("Prediman CRM")
                    .build();

            log.info("Google Drive inicializado com sucesso");
        } catch (IOException | GeneralSecurityException e) {
            log.error("Falha ao inicializar Google Drive: {}", e.getMessage());
            driveService = null;
        }
    }

    public boolean isEnabled() {
        return enabled && driveService != null;
    }

    public GoogleDriveResult upload(String fileName, String mimeType, byte[] content, String parentFolderId) {
        if (!isEnabled()) {
            return null;
        }

        try {
            String parent = (parentFolderId != null && !parentFolderId.isBlank())
                    ? parentFolderId : rootFolderId;

            File fileMetadata = new File();
            fileMetadata.setName(fileName);
            if (parent != null && !parent.isBlank()) {
                fileMetadata.setParents(List.of(parent));
            }

            com.google.api.client.http.ByteArrayContent mediaContent =
                    new com.google.api.client.http.ByteArrayContent(mimeType, content);

            File uploaded = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id, webViewLink")
                    .execute();

            log.info("Arquivo enviado ao Google Drive: id={}, name={}", uploaded.getId(), fileName);
            return new GoogleDriveResult(uploaded.getId(), uploaded.getWebViewLink());
        } catch (IOException e) {
            log.error("Falha ao enviar arquivo ao Google Drive: {}", e.getMessage());
            return null;
        }
    }

    public String createFolder(String folderName, String parentFolderId) {
        if (!isEnabled()) {
            return null;
        }

        try {
            String parent = (parentFolderId != null && !parentFolderId.isBlank())
                    ? parentFolderId : rootFolderId;

            File folderMetadata = new File();
            folderMetadata.setName(folderName);
            folderMetadata.setMimeType("application/vnd.google-apps.folder");
            if (parent != null && !parent.isBlank()) {
                folderMetadata.setParents(List.of(parent));
            }

            File folder = driveService.files().create(folderMetadata)
                    .setFields("id")
                    .execute();

            log.info("Pasta criada no Google Drive: id={}, name={}", folder.getId(), folderName);
            return folder.getId();
        } catch (IOException e) {
            log.error("Falha ao criar pasta no Google Drive: {}", e.getMessage());
            return null;
        }
    }

    public void delete(String fileId) {
        if (!isEnabled() || fileId == null || fileId.isBlank()) {
            return;
        }

        try {
            driveService.files().delete(fileId).execute();
            log.info("Arquivo removido do Google Drive: id={}", fileId);
        } catch (IOException e) {
            log.error("Falha ao remover arquivo do Google Drive: {}", e.getMessage());
        }
    }

    @Getter
    public static class GoogleDriveResult {
        private final String fileId;
        private final String webViewLink;

        public GoogleDriveResult(String fileId, String webViewLink) {
            this.fileId = fileId;
            this.webViewLink = webViewLink;
        }
    }
}
