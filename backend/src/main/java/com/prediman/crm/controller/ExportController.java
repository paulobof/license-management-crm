package com.prediman.crm.controller;

import com.prediman.crm.model.enums.CategoriaDocumento;
import com.prediman.crm.model.enums.StatusCobranca;
import com.prediman.crm.service.ExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Exportação das listagens para planilha (CSV compatível com Excel pt-BR).
 */
@RestController
@RequestMapping("/api/v1/export")
@RequiredArgsConstructor
@Slf4j
public class ExportController {

    private static final String CSV_CONTENT_TYPE = "text/csv; charset=UTF-8";
    private static final DateTimeFormatter SUFIXO_ARQUIVO = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ExportService exportService;

    /**
     * Relatório geral de documentos. Aceita os mesmos filtros de {@code GET /api/v1/documentos}
     * e ordena pela data de validade ascendente (mais urgentes primeiro).
     */
    @GetMapping("/documentos")
    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    public ResponseEntity<byte[]> exportarDocumentos(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CategoriaDocumento categoria,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long clienteId) {

        log.info("Exportando documentos (search={}, categoria={}, status={}, clienteId={})",
                search, categoria, status, clienteId);

        byte[] csv = exportService.exportarDocumentos(search, categoria, status, clienteId);
        return respostaCsv(csv, "documentos");
    }

    /**
     * Relatório financeiro. Aceita os mesmos filtros de {@code GET /api/v1/cobrancas}
     * e ordena pela data de vencimento ascendente.
     */
    @GetMapping("/cobrancas")
    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    public ResponseEntity<byte[]> exportarCobrancas(
            @RequestParam(required = false) Long contratoId,
            @RequestParam(required = false) StatusCobranca status,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        log.info("Exportando cobranças (contratoId={}, status={}, month={}, year={})",
                contratoId, status, month, year);

        byte[] csv = exportService.exportarCobrancas(contratoId, status, month, year);
        return respostaCsv(csv, "cobrancas");
    }

    private ResponseEntity<byte[]> respostaCsv(byte[] csv, String prefixoArquivo) {
        String nomeArquivo = prefixoArquivo + "_" + LocalDate.now().format(SUFIXO_ARQUIVO) + ".csv";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(CSV_CONTENT_TYPE));
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"");
        headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
        headers.setContentLength(csv.length);

        return ResponseEntity.ok().headers(headers).body(csv);
    }
}
