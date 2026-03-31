package com.prediman.crm.model;

import com.prediman.crm.model.enums.StatusCobranca;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cobrancas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"contrato"})
@ToString(exclude = {"contrato"})
public class Cobranca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

    @Column(name = "valor_esperado", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorEsperado;

    @Column(name = "valor_recebido", precision = 12, scale = 2)
    private BigDecimal valorRecebido;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Column(name = "forma_pagamento", length = 50)
    private String formaPagamento;

    @Column(name = "comprovante_drive_id")
    private String comprovanteDriveId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusCobranca status = StatusCobranca.PENDENTE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Transient
    public StatusCobranca getStatusCalculado() {
        if (dataPagamento != null && valorRecebido != null) {
            return StatusCobranca.PAGO;
        }
        if (dataVencimento != null && dataVencimento.isBefore(LocalDate.now())) {
            return StatusCobranca.VENCIDO;
        }
        return StatusCobranca.PENDENTE;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (status == null) {
            status = StatusCobranca.PENDENTE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
