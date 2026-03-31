package com.prediman.crm.controller;

import com.prediman.crm.dto.CobrancaRequest;
import com.prediman.crm.dto.CobrancaResponse;
import com.prediman.crm.dto.FinanceiroSummaryResponse;
import com.prediman.crm.model.enums.StatusCobranca;
import com.prediman.crm.service.CobrancaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CobrancaController {

    private final CobrancaService cobrancaService;

    @GetMapping("/cobrancas")
    public ResponseEntity<Page<CobrancaResponse>> findAll(
            @RequestParam(required = false) Long contratoId,
            @RequestParam(required = false) StatusCobranca status,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
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
    public ResponseEntity<CobrancaResponse> create(@Valid @RequestBody CobrancaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cobrancaService.create(request));
    }

    @PutMapping("/cobrancas/{id}")
    public ResponseEntity<CobrancaResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody CobrancaRequest request) {
        return ResponseEntity.ok(cobrancaService.update(id, request));
    }

    @PatchMapping("/cobrancas/{id}/pagar")
    public ResponseEntity<CobrancaResponse> registrarPagamento(@PathVariable Long id,
                                                                @RequestBody CobrancaRequest request) {
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
