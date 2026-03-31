package com.prediman.crm.controller;

import com.prediman.crm.dto.DashboardSummaryResponse;
import com.prediman.crm.dto.DocumentoRequest;
import com.prediman.crm.dto.DocumentoResponse;
import com.prediman.crm.model.enums.CategoriaDocumento;
import com.prediman.crm.service.DocumentoService;
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
public class DocumentoController {

    private final DocumentoService documentoService;

    @GetMapping("/documentos")
    public ResponseEntity<Page<DocumentoResponse>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CategoriaDocumento categoria,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        size = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.asc("dataValidade").nullsLast()));
        return ResponseEntity.ok(documentoService.findAll(search, categoria, status, clienteId, pageable));
    }

    @GetMapping("/documentos/{id}")
    public ResponseEntity<DocumentoResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(documentoService.findById(id));
    }

    @GetMapping("/clientes/{clienteId}/documentos")
    public ResponseEntity<List<DocumentoResponse>> findByCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(documentoService.findByClienteId(clienteId));
    }

    @PostMapping("/documentos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocumentoResponse> create(@Valid @RequestBody DocumentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentoService.create(request));
    }

    @PutMapping("/documentos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocumentoResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody DocumentoRequest request) {
        return ResponseEntity.ok(documentoService.update(id, request));
    }

    @DeleteMapping("/documentos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        documentoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary() {
        return ResponseEntity.ok(documentoService.getDashboardSummary());
    }
}
