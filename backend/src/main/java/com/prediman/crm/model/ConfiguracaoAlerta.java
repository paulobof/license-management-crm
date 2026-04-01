package com.prediman.crm.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "configuracao_alerta")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ConfiguracaoAlerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * Comma-separated days ahead for alerts, e.g. "30,15,7,1".
     */
    @Column(name = "dias_antecedencia", nullable = false, length = 50)
    private String diasAntecedencia;

    @Column(name = "horario_execucao", nullable = false)
    private LocalTime horarioExecucao;

    @Column(name = "email_ativo", nullable = false)
    @Builder.Default
    private Boolean emailAtivo = Boolean.TRUE;

    @Column(name = "whatsapp_ativo", nullable = false)
    @Builder.Default
    private Boolean whatsappAtivo = Boolean.FALSE;

    @Column(name = "template_email", columnDefinition = "TEXT")
    private String templateEmail;

    @Column(name = "template_whatsapp", columnDefinition = "TEXT")
    private String templateWhatsapp;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Parses diasAntecedencia string into a list of integers.
     */
    public List<Integer> getDiasAntecedenciaList() {
        if (diasAntecedencia == null || diasAntecedencia.isBlank()) {
            return List.of(30, 15, 7, 1);
        }
        return Arrays.stream(diasAntecedencia.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (emailAtivo == null) {
            emailAtivo = Boolean.TRUE;
        }
        if (whatsappAtivo == null) {
            whatsappAtivo = Boolean.FALSE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
