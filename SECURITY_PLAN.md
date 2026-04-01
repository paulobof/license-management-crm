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

## MEDIUM — Corrigidos (2026-03-31)

### Prioridade 1 — Integridade de Dados
- [x] **Cobranca status bypass via PUT**: Removido campo `status` do `CobrancaRequest`. Status forçado PENDENTE no create, removido set no update.
- [x] **Snooze sem limite**: `@Min(1) @Max(90)` + `@Validated` no AlertaController. `@PreAuthorize("hasRole('ADMIN')")` já existia.
- [x] **Off-by-one no status de documento**: Criada constante `DocumentoConstants.DIAS_ALERTA_VENCIMENTO = 30`, corrigido 31→30 no entity, usada em buildSpecification e getDashboardSummary.
- [x] **@Data em entidades sem exclude**: Trocado `@Data` por `@Getter @Setter` + `@EqualsAndHashCode(onlyExplicitlyIncluded=true)` em `Usuario.java` e `ConfiguracaoAlerta.java`.
- [x] **CascadeType.ALL em Cliente**: Cascade de contratos reduzido para `PERSIST+MERGE`. Guard no delete rejeita se tiver contratos ativos ou cobranças pagas.

### Prioridade 2 — Scheduler e Alertas
- [x] **Cron fixo ignora horarioExecucao**: Implementado `SchedulingConfigurer` dinâmico que lê horário do banco a cada disparo.
- [x] **Scheduler sem idempotência**: Verificação de existência por documentoId + data antes de criar AlertaLog.
- [x] **Snooze não funciona**: `getAlertasPendentes()` e `processarAlertasDiarios()` agora excluem documentos com snooze ativo.
- [x] **Scheduler single-threaded**: `ThreadPoolTaskScheduler` com pool size 4 via `SchedulerConfig`.

### Prioridade 3 — Frontend
- [x] **Sem AbortController**: Cleanup com cancelled flag e AbortController em Dashboard, FinanceiroPage, e fetches com useEffect.
- [x] **formatDate inconsistente**: Helper centralizado em `utils/formatDate.ts` com guard contra ISO datetime, usado em 5 arquivos.
- [x] **Notificação badge estática**: Polling a cada 5 minutos no Layout via setInterval.
- [x] **SummaryCard duplicado**: Extraído para `components/ui/SummaryCard.tsx`, usado em Dashboard e FinanceiroPage.
- [x] **Modal sem focus trap**: Focus trap com Tab/Shift+Tab, foco inicial no primeiro elemento, restauração ao fechar.
- [x] **Input sem id/htmlFor**: `useId()` para gerar id, `htmlFor` no label, `aria-invalid` e `aria-describedby` para errors.
- [x] **Formulários sem proteção contra double-submit**: Guard `if (saving) return;` adicionado em ClienteForm, ContratoForm, DocumentoForm, AlertaConfig, Login, UsuarioList.

### Prioridade 4 — Infraestrutura
- [x] **Sem backup do PostgreSQL**: Container sidecar `pg-backup` com pg_dump diário e retenção de 7 dias.
- [x] **Sem health check no backend**: Spring Boot Actuator + Docker HEALTHCHECK no Dockerfile e docker-compose.
- [x] **Sem graceful shutdown**: `server.shutdown=graceful` + `lifecycle.timeout-per-shutdown-phase=30s`.
- [x] **RestTemplate sem timeout**: 3s connect + 5s read via RestTemplateBuilder.
- [x] **show-sql no perfil base**: Movido para `application-dev.yml` apenas.
- [x] **Java 17 vs 21**: pom.xml `java.version=21`, Dockerfile usando `temurin-21`.
- [x] **Sem API versioning**: Prefixo `/api/v1/` aplicado em 7 controllers, SecurityConfig, RateLimitFilter, 8 módulos frontend API, axios interceptor, e nginx.

### Prioridade 5 — Qualidade
- [x] **Zero testes**: Testcontainers + JwtTokenProviderTest com 8 testes unitários. application-test.yml com PostgreSQL TC.
- [x] **Sem OpenAPI/Swagger**: springdoc-openapi-starter-webmvc-ui adicionado com config no application.yml.
- [x] **Sem logging estruturado**: `logback-spring.xml` com JSON (LogstashEncoder) em prod, console em dev.
- [x] **Mapper inconsistente**: Criados UsuarioMapper, DocumentoMapper, CobrancaMapper, ContratoMapper. Inline toResponse() removidos dos services.
- [x] **CobrancaRepository query duplicada**: Removida `sumValorEsperadoByVencimentoBetween`, caller atualizado.
- [x] **FK faltando em alerta_logs.cobranca_id**: Migration V6 criada.

---

## Referências
- OWASP Top 10 2021: https://owasp.org/Top10/
- Spring Security Best Practices: https://docs.spring.io/spring-security/reference/
- Docker Security: https://docs.docker.com/engine/security/
