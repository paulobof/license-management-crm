package com.prediman.crm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contatos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "cliente")
@ToString(exclude = "cliente")
public class Contato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @JsonIgnore
    private Cliente cliente;

    @Column(nullable = false)
    private String nome;

    private String cargo;

    private String email;

    private String telefone;

    private String whatsapp;

    @Column(nullable = false)
    @Builder.Default
    private Boolean principal = false;
}
