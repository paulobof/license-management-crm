package com.prediman.crm.controller;

import com.prediman.crm.dto.ClienteRequest;
import com.prediman.crm.dto.ClienteResponse;
import com.prediman.crm.dto.ViaCepResponse;
import com.prediman.crm.model.enums.StatusCliente;
import com.prediman.crm.service.ClienteService;
import com.prediman.crm.service.ViaCepService;
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

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final ViaCepService viaCepService;

    @GetMapping("/clientes")
    public ResponseEntity<Page<ClienteResponse>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) StatusCliente status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        size = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, size, Sort.by("razaoSocial").ascending());
        Page<ClienteResponse> result = clienteService.findAll(search, status, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/clientes/{id}")
    public ResponseEntity<ClienteResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.findById(id));
    }

    @PostMapping("/clientes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClienteResponse> create(@Valid @RequestBody ClienteRequest request) {
        ClienteResponse response = clienteService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/clientes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClienteResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.ok(clienteService.update(id, request));
    }

    @DeleteMapping("/clientes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clienteService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/clientes/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClienteResponse> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.toggleStatus(id));
    }

    @GetMapping("/cep/{cep}")
    public ResponseEntity<ViaCepResponse> buscarCep(@PathVariable String cep) {
        return ResponseEntity.ok(viaCepService.buscarCep(cep));
    }
}
