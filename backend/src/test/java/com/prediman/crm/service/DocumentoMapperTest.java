package com.prediman.crm.service;

import com.prediman.crm.dto.DocumentoResponse;
import com.prediman.crm.model.Cliente;
import com.prediman.crm.model.Documento;
import com.prediman.crm.model.enums.CategoriaDocumento;
import com.prediman.crm.model.enums.StatusDocumento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DocumentoMapper")
class DocumentoMapperTest {

    private DocumentoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DocumentoMapper();
    }

    @Test
    @DisplayName("toResponse mapeia todos os campos do Documento incluindo statusCalculado")
    void toResponse_mapeiaTodasOsCampos() {
        LocalDateTime now = LocalDateTime.of(2024, 5, 10, 9, 0);
        // dataValidade bem no futuro => VALIDO
        LocalDate validade = LocalDate.now().plusDays(60);

        Cliente cliente = Cliente.builder()
                .id(3L)
                .razaoSocial("Empresa Documental Ltda")
                .build();

        Documento documento = Documento.builder()
                .id(50L)
                .nome("Alvara de Funcionamento")
                .categoria(CategoriaDocumento.OUTRO)
                .dataEmissao(LocalDate.of(2023, 1, 1))
                .dataValidade(validade)
                .revisao("v1.0")
                .observacoes("Renovar anualmente")
                .googleDriveFileId("file-abc")
                .googleDriveUrl("https://drive.google.com/file/d/file-abc")
                .tamanhoBytes(204800L)
                .mimeType("application/pdf")
                .createdAt(now)
                .updatedAt(now)
                .cliente(cliente)
                .build();

        DocumentoResponse response = mapper.toResponse(documento);

        assertAll("campos escalares",
                () -> assertEquals(50L, response.getId()),
                () -> assertEquals("Alvara de Funcionamento", response.getNome()),
                () -> assertEquals(CategoriaDocumento.OUTRO, response.getCategoria()),
                () -> assertEquals(LocalDate.of(2023, 1, 1), response.getDataEmissao()),
                () -> assertEquals(validade, response.getDataValidade()),
                () -> assertEquals("v1.0", response.getRevisao()),
                () -> assertEquals("Renovar anualmente", response.getObservacoes()),
                () -> assertEquals("file-abc", response.getGoogleDriveFileId()),
                () -> assertEquals("https://drive.google.com/file/d/file-abc", response.getGoogleDriveUrl()),
                () -> assertEquals(204800L, response.getTamanhoBytes()),
                () -> assertEquals("application/pdf", response.getMimeType()),
                () -> assertEquals(now, response.getCreatedAt()),
                () -> assertEquals(now, response.getUpdatedAt())
        );

        assertAll("referencia ao cliente",
                () -> assertEquals(3L, response.getClienteId()),
                () -> assertEquals("Empresa Documental Ltda", response.getClienteNome())
        );

        assertEquals(StatusDocumento.VALIDO, response.getStatusCalculado(),
                "statusCalculado deve ser VALIDO para validade 60 dias no futuro");
    }

    @Test
    @DisplayName("toResponse propaga statusCalculado A_VENCER")
    void toResponse_statusCalculadoAVencer() {
        // validade em 15 dias => A_VENCER
        LocalDate validade = LocalDate.now().plusDays(15);

        Cliente cliente = Cliente.builder().id(4L).razaoSocial("Empresa AV").build();

        Documento documento = Documento.builder()
                .id(51L)
                .nome("Certidao")
                .categoria(CategoriaDocumento.OUTRO)
                .dataValidade(validade)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .cliente(cliente)
                .build();

        DocumentoResponse response = mapper.toResponse(documento);

        assertEquals(StatusDocumento.A_VENCER, response.getStatusCalculado());
    }

    @Test
    @DisplayName("toResponse propaga statusCalculado VENCIDO")
    void toResponse_statusCalculadoVencido() {
        LocalDate validade = LocalDate.now().minusDays(1);

        Cliente cliente = Cliente.builder().id(5L).razaoSocial("Empresa Vencida").build();

        Documento documento = Documento.builder()
                .id(52L)
                .nome("Licenca Vencida")
                .categoria(CategoriaDocumento.OUTRO)
                .dataValidade(validade)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .cliente(cliente)
                .build();

        DocumentoResponse response = mapper.toResponse(documento);

        assertEquals(StatusDocumento.VENCIDO, response.getStatusCalculado());
    }

    @Test
    @DisplayName("toResponse propaga statusCalculado SEM_VALIDADE quando dataValidade e null")
    void toResponse_statusCalculadoSemValidade() {
        Cliente cliente = Cliente.builder().id(6L).razaoSocial("Empresa SV").build();

        Documento documento = Documento.builder()
                .id(53L)
                .nome("Contrato Permanente")
                .categoria(CategoriaDocumento.OUTRO)
                .dataValidade(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .cliente(cliente)
                .build();

        DocumentoResponse response = mapper.toResponse(documento);

        assertEquals(StatusDocumento.SEM_VALIDADE, response.getStatusCalculado());
    }
}
