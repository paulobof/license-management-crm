# Plano de Remediação de Segurança — Prediman CRM

**Data:** 2026-03-30
**Baseado em:** Code review com 6 agentes especializados (code quality, security audit, pentest, Java review, TypeScript review, architecture review)

---

## Status das Correções

### CRITICAL (corrigidos)
- [x] JWT secret hardcoded — removido, lido via `${JWT_SECRET}` sem fallback
- [x] Senha admin "admin123" — lido via `${ADMIN_INITIAL_PASSWORD}`, gatilho com @Profile
- [x] Credenciais DB hardcoded em prod — removido fallback
- [x] Autorização ausente em endpoints — @PreAuthorize adicionado

### HIGH (corrigidos)
- [x] Refresh token indistinguível do access token — claim `type` adicionado
- [x] Sem política de senha — @Size(min=8) + @Pattern para CNPJ/CPF
- [x] Sem rate limiting no login — filtro IP-based implementado
- [x] N+1 queries e listas sem limite — JOIN FETCH + paginação + cap size=100
- [x] Docker roda como root — USER não-root adicionado
- [x] Nginx sem security headers — CSP, X-Frame, HSTS adicionados
- [x] Frontend: isRefreshing leak, auth race condition, unsafe error casts

---

## MEDIUM — Pendentes (próximas 2-4 semanas)

### Prioridade 1 — Integridade de Dados
- [ ] **Cobranca status bypass via PUT**: Remover campo `status` do `CobrancaRequest` no endpoint de update. Status só pode mudar via endpoints dedicados (pagar, cancelar).
- [ ] **Snooze sem limite**: Validar `dias` com `@Max(90)` no `AlertaController.snooze()`. Adicionar @PreAuthorize("hasRole('ADMIN')").
- [ ] **Off-by-one no status de documento**: Unificar threshold em constante (30 dias) usada em `Documento.getStatusCalculado()`, `DocumentoService.buildSpecification()`, e `DocumentoService.getDashboardSummary()`.
- [ ] **@Data em entidades sem exclude**: Trocar `@Data` por `@Getter @Setter` em `Usuario.java` e `ConfiguracaoAlerta.java`, ou adicionar `@EqualsAndHashCode(exclude)`.
- [ ] **CascadeType.ALL em Cliente**: Adicionar guard no `ClienteService.delete()` — rejeitar se tiver contratos ativos ou cobranças pagas. Considerar soft-delete.

### Prioridade 2 — Scheduler e Alertas
- [ ] **Cron fixo ignora horarioExecucao**: Implementar `SchedulingConfigurer` dinâmico que lê o horário do `ConfiguracaoAlerta` ou remover o campo da UI.
- [ ] **Scheduler sem idempotência**: Antes de criar `AlertaLog`, verificar se já existe registro com mesmo `documentoId` e `createdAt` de hoje.
- [ ] **Snooze não funciona**: `getAlertasPendentes()` e `processarAlertasDiarios()` devem excluir documentos com snooze ativo (`snoozedAte >= today`).
- [ ] **Scheduler single-threaded**: Configurar `ThreadPoolTaskScheduler` com pool size > 1.

### Prioridade 3 — Frontend
- [ ] **Sem AbortController**: Adicionar cleanup com `AbortController` em todos os `useEffect` que fazem fetch (ClienteList, DocumentoList, ContratoList, FinanceiroPage).
- [ ] **formatDate inconsistente**: Padronizar helper com guard contra ISO datetime em todos os 4 arquivos.
- [ ] **Notificação badge estática**: Polling a cada 5 minutos ou refresh após snooze/enviar.
- [ ] **SummaryCard duplicado**: Extrair para `components/ui/SummaryCard.tsx`.
- [ ] **Modal sem focus trap**: Implementar trap de foco e restauração no `Modal.tsx`.
- [ ] **Input sem id/htmlFor**: Gerar `id` com `useId()` para acessibilidade.
- [ ] **Formulários sem proteção contra double-submit**: Adicionar guard `if (saving) return;` no início de cada handler.

### Prioridade 4 — Infraestrutura
- [ ] **Sem backup do PostgreSQL**: Adicionar pg_dump cron ou container sidecar com backup para S3/storage externo.
- [ ] **Sem health check no backend**: Adicionar Spring Boot Actuator + Docker HEALTHCHECK.
- [ ] **Sem graceful shutdown**: Adicionar `server.shutdown=graceful` e `spring.lifecycle.timeout-per-shutdown-phase=30s`.
- [ ] **RestTemplate sem timeout**: Configurar 3s connect + 5s read no `RestTemplateConfig`.
- [ ] **show-sql no perfil base**: Mover para application-dev.yml apenas.
- [ ] **Java 17 vs 21**: Alinhar pom.xml, Dockerfiles e documentação para Java 21.
- [ ] **Sem API versioning**: Adicionar prefixo `/api/v1/`.

### Prioridade 5 — Qualidade
- [ ] **Zero testes**: Adicionar Testcontainers + testes para JWT, auth flow, CRUD services, e authorization rules.
- [ ] **Sem OpenAPI/Swagger**: Adicionar `springdoc-openapi-starter-webmvc-ui`.
- [ ] **Sem logging estruturado**: Adicionar `logback-spring.xml` com JSON output em prod.
- [ ] **Mapper inconsistente**: Extrair `toResponse()` inline para classes Mapper dedicadas.
- [ ] **CobrancaRepository query duplicada**: `sumValorEsperadoByVencimentoBetween` é idêntica a `sumValorEsperadoByStatusAndVencimentoBetween` — remover duplicata.
- [ ] **FK faltando em alerta_logs.cobranca_id**: Criar V6 migration adicionando constraint.

---

## Referências
- OWASP Top 10 2021: https://owasp.org/Top10/
- Spring Security Best Practices: https://docs.spring.io/spring-security/reference/
- Docker Security: https://docs.docker.com/engine/security/
