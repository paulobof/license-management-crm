package com.prediman.crm.controller;

import com.prediman.crm.dto.ContratoRequest;
import com.prediman.crm.dto.ContratoResponse;
import com.prediman.crm.model.enums.Periodicidade;
import com.prediman.crm.model.enums.StatusContrato;
import com.prediman.crm.service.ContratoService;
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
public class ContratoController {

    private final ContratoService contratoService;

    @GetMapping("/contratos")
    public ResponseEntity<Page<ContratoResponse>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) StatusContrato status,
            @RequestParam(required = false) Periodicidade periodicidade,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(contratoService.findAll(search, clienteId, status, periodicidade, pageable));
    }

    @GetMapping("/contratos/{id}")
    public ResponseEntity<ContratoResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(contratoService.findById(id));
    }

    @GetMapping("/clientes/{clienteId}/contratos")
    public ResponseEntity<List<ContratoResponse>> findByCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(contratoService.findByClienteId(clienteId));
    }

    @PostMapping("/contratos")
    public ResponseEntity<ContratoResponse> create(@Valid @RequestBody ContratoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contratoService.create(request));
    }

    @PutMapping("/contratos/{id}")
    public ResponseEntity<ContratoResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody ContratoRequest request) {
        return ResponseEntity.ok(contratoService.update(id, request));
    }

    @DeleteMapping("/contratos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        contratoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/contratos/{id}/gerar-cobrancas")
    public ResponseEntity<ContratoResponse> gerarCobrancas(@PathVariable Long id) {
        return ResponseEntity.ok(contratoService.gerarCobrancasMensais(id));
    }
}
