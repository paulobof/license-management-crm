package com.prediman.crm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.prediman.crm.model.enums.TipoEndereco;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "enderecos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "cliente")
@ToString(exclude = "cliente")
public class Endereco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @JsonIgnore
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TipoEndereco tipo = TipoEndereco.COBRANCA;

    private String cep;

    private String logradouro;

    private String numero;

    private String complemento;

    private String bairro;

    private String cidade;

    @Column(length = 2)
    private String estado;
}
