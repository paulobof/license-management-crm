package com.prediman.crm.model;

import com.prediman.crm.model.enums.CanalAlerta;
import com.prediman.crm.model.enums.StatusEnvio;
import com.prediman.crm.model.enums.TipoAlerta;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerta_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"documento"})
@ToString(exclude = {"documento"})
public class AlertaLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id")
    private Documento documento;

    /**
     * cobranca_id stored as plain Long to avoid compile dependency on the Cobranca entity
     * being implemented by another agent.
     */
    @Column(name = "cobranca_id")
    private Long cobrancaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoAlerta tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CanalAlerta canal;

    @Column(length = 255)
    private String destinatario;

    @Column(columnDefinition = "TEXT")
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_envio", nullable = false, length = 20)
    @Builder.Default
    private StatusEnvio statusEnvio = StatusEnvio.PENDENTE;

    @Column(name = "data_envio")
    private LocalDateTime dataEnvio;

    @Column(name = "snoozed_ate")
    private LocalDate snoozedAte;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (statusEnvio == null) {
            statusEnvio = StatusEnvio.PENDENTE;
        }
    }
}
