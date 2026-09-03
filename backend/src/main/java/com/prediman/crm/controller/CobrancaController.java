package com.prediman.crm.controller;

import com.prediman.crm.dto.CobrancaRequest;
import com.prediman.crm.dto.CobrancaResponse;
import com.prediman.crm.dto.FinanceiroSummaryResponse;
import com.prediman.crm.model.enums.StatusCobranca;
import com.prediman.crm.service.CobrancaService;
import com.prediman.crm.service.GoogleDriveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class CobrancaController {

    private final CobrancaService cobrancaService;
    private final GoogleDriveService googleDriveService;

    @GetMapping("/cobrancas")
    public ResponseEntity<Page<CobrancaResponse>> findAll(
            @RequestParam(required = false) Long contratoId,
            @RequestParam(required = false) StatusCobranca status,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        size = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.asc("dataVencimento")));
        return ResponseEntity.ok(cobrancaService.findAll(contratoId, status, month, year, pageable));
    }

    @GetMapping("/cobrancas/{id}")
    public ResponseEntity<CobrancaResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(cobrancaService.findById(id));
    }

    @GetMapping("/contratos/{contratoId}/cobrancas")
    public ResponseEntity<List<CobrancaResponse>> findByContrato(@PathVariable Long contratoId) {
        return ResponseEntity.ok(cobrancaService.findByContratoId(contratoId));
    }

    @PostMapping("/cobrancas")
    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    public ResponseEntity<CobrancaResponse> create(@Valid @RequestBody CobrancaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cobrancaService.create(request));
    }

    @PutMapping("/cobrancas/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    public ResponseEntity<CobrancaResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody CobrancaRequest request) {
        return ResponseEntity.ok(cobrancaService.update(id, request));
    }

    /**
     * Registro de pagamento em JSON puro (sem comprovante).
     */
    @PatchMapping(value = "/cobrancas/{id}/pagar", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    public ResponseEntity<CobrancaResponse> registrarPagamento(@PathVariable Long id,
                                                                @RequestBody CobrancaRequest request) {
        return ResponseEntity.ok(cobrancaService.registrarPagamento(id, request));
    }

    /**
     * Registro de pagamento com comprovante opcional (multipart/form-data).
     * A parte "data" traz o JSON do pagamento e a parte "file" o arquivo do comprovante.
     * Quando o Google Drive está habilitado e há arquivo, ele é enviado e o id do
     * arquivo é gravado em comprovanteDriveId.
     */
    @PatchMapping(value = "/cobrancas/{id}/pagar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    public ResponseEntity<CobrancaResponse> registrarPagamentoComComprovante(
            @PathVariable Long id,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart("data") CobrancaRequest request) throws IOException {

        if (file != null && !file.isEmpty()) {
            if (googleDriveService.isEnabled()) {
                GoogleDriveService.GoogleDriveResult result = googleDriveService.upload(
                        file.getOriginalFilename(),
                        file.getContentType(),
                        file.getBytes(),
                        null);

                if (result != null) {
                    request.setComprovanteDriveId(result.getFileId());
                } else {
                    log.warn("Falha ao enviar comprovante da cobrança {} ao Google Drive; pagamento registrado sem comprovante", id);
                }
            } else {
                log.warn("Google Drive desabilitado; pagamento da cobrança {} registrado sem comprovante", id);
            }
        }

        return ResponseEntity.ok(cobrancaService.registrarPagamento(id, request));
    }

    @DeleteMapping("/cobrancas/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cobrancaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/financeiro/summary")
    public ResponseEntity<FinanceiroSummaryResponse> getFinanceiroSummary() {
        return ResponseEntity.ok(cobrancaService.getFinanceiroSummary());
    }
}
