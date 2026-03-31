package com.prediman.crm.model;

import com.prediman.crm.model.enums.CategoriaDocumento;
import com.prediman.crm.model.enums.StatusDocumento;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "documentos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"cliente"})
@ToString(exclude = {"cliente"})
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private CategoriaDocumento categoria = CategoriaDocumento.OUTRO;

    @Column(name = "data_emissao")
    private LocalDate dataEmissao;

    @Column(name = "data_validade")
    private LocalDate dataValidade;

    @Column(length = 50)
    private String revisao;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "google_drive_file_id")
    private String googleDriveFileId;

    @Column(name = "google_drive_url", length = 500)
    private String googleDriveUrl;

    @Column(name = "tamanho_bytes")
    private Long tamanhoBytes;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Transient
    public StatusDocumento getStatusCalculado() {
        if (dataValidade == null) {
            return StatusDocumento.SEM_VALIDADE;
        }
        LocalDate today = LocalDate.now();
        if (dataValidade.isBefore(today)) {
            return StatusDocumento.VENCIDO;
        }
        if (dataValidade.isBefore(today.plusDays(31)) || dataValidade.isEqual(today.plusDays(31))) {
            return StatusDocumento.A_VENCER;
        }
        return StatusDocumento.VALIDO;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (categoria == null) {
            categoria = CategoriaDocumento.OUTRO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
