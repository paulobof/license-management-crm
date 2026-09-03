# Plano — Fechamento dos 11 gaps do PRD v1

**Data:** 2026-09-02
**Base:** análise comparativa PRD.md × código em `main` (78de35d)
**Execução:** 4 agentes em paralelo, com fronteiras estritas de propriedade de arquivos.

---

## Convenções compartilhadas (contratos entre agentes)

### Papéis de acesso (item 3) — política única

| Operação | Papel |
|---|---|
| Qualquer `GET` | `hasAnyRole('ADMIN','USUARIO')` |
| `POST`/`PUT` de clientes, documentos, contratos, cobranças | `hasAnyRole('ADMIN','USUARIO')` |
| `POST /documentos/upload` | `hasAnyRole('ADMIN','USUARIO')` |
| `PATCH /cobrancas/{id}/pagar` | `hasAnyRole('ADMIN','USUARIO')` |
| `PATCH /clientes/{id}/toggle-status` | `hasAnyRole('ADMIN','USUARIO')` |
| `POST /contratos/{id}/gerar-cobrancas` | `hasAnyRole('ADMIN','USUARIO')` |
| Alertas: snooze, enviar-manual | `hasAnyRole('ADMIN','USUARIO')` |
| Qualquer `DELETE` | `hasRole('ADMIN')` |
| `/api/v1/users/**` | `hasRole('ADMIN')` |
| `PUT /alertas/config` | `hasRole('ADMIN')` |

Authorities são geradas como `ROLE_ADMIN` / `ROLE_USUARIO` (`UserDetailsServiceImpl:33`).
Cada agente aplica a política **apenas nos controllers que possui**.

### Componente compartilhado de exportação

Criado pelo **Agente 4**, consumido pelos Agentes 1 e 3:

```tsx
// frontend/src/components/ui/ExportButton.tsx
interface ExportButtonProps {
  endpoint: string;                    // '/api/v1/export/documentos'
  params?: Record<string, unknown>;    // filtros ativos da tela
  filename: string;                    // 'documentos.csv'
  label?: string;                      // default: 'Exportar'
}
```

### Numeração de migrations (pré-alocada, evita colisão)

- `V7__*` → Agente 2
- `V8__*` → Agente 3
- `V9__*` → Agente 1
- Agente 4 → sem migration

### Nomenclatura de pasta no Drive

`String.format("%04d - %s", cliente.getId(), nomeFantasia != null && !isBlank ? nomeFantasia : razaoSocial)`
(a entidade `Cliente` não possui campo `codigo`; o `id` faz esse papel)

### Regra de build
Nenhum agente roda `mvn` ou `npm run build` — contenção no `target/`. Build e testes são executados na integração final.

---

## Agente 1 — Documentos & Google Drive
**Itens 1, 2, 10, 11** + política de papéis em `ClienteController` e `DocumentoController`

1. **Upload real na UI:** `DocumentoForm.tsx` ganha `<input type="file">` (limite 30 MB, validação client-side), envia `multipart/form-data` para `POST /api/v1/documentos/upload` via novo `uploadDocumento()` em `api/documentos.ts`.
2. **Prévia:** `DocumentoList.tsx` e `ClienteDetail.tsx` exibem link/ícone abrindo `googleDriveUrl` em nova aba quando presente.
3. **Pasta por cliente:** `ClienteService.create()` chama `GoogleDriveService.createFolder()` e persiste `googleDriveFolderId`; falha do Drive não bloqueia o cadastro (log de warn).
4. **Upload na pasta do cliente:** `DocumentoController.upload()` resolve o `parentFolderId` do cliente (criando a pasta sob demanda se ausente).
5. **Nomenclatura:** `AAAA.MM.DD_NomeArquivo_RevXX` a partir de `dataEmissao` + `nome` + `revisao`, preservando a extensão original.

**Arquivos:** `DocumentoController.java`, `DocumentoService.java`, `GoogleDriveService.java`, `ClienteController.java`, `ClienteService.java`, DTOs de documento, `frontend/src/api/documentos.ts`, `frontend/src/pages/documentos/*`, `frontend/src/pages/clientes/ClienteDetail.tsx`, testes correspondentes.

---

## Agente 2 — Autenticação
**Itens 4, 5**

1. **Recuperação de senha:** entidade `PasswordResetToken` (token opaco, expiração 2 h, uso único), migration `V7`, endpoints públicos `POST /api/v1/auth/forgot-password` e `POST /api/v1/auth/reset-password` (já cobertos por `permitAll` e pelo `RateLimitFilter`). Resposta de `forgot-password` é sempre 200 (não vaza existência de e-mail).
2. **Telas:** `ForgotPassword.tsx`, `ResetPassword.tsx`, rotas públicas em `App.tsx`, link "Esqueci minha senha" no `Login.tsx`.
3. **E-mail de boas-vindas:** `UserService.create()` envia credenciais iniciais via `EmailService.enviar()`; falha de e-mail não aborta a criação.

**Arquivos:** `AuthController.java`, `AuthService.java`, `UserService.java`, `UserController.java`, `PasswordResetToken*`, `V7__*.sql`, `SecurityConfig.java`, `RateLimitFilter.java`, `frontend/src/api/auth.ts`, `frontend/src/pages/Login.tsx`, `frontend/src/pages/auth/*`, `frontend/src/App.tsx`, testes correspondentes.

---

## Agente 3 — Financeiro & Alertas de cobrança
**Itens 7, 8, 9** + política de papéis em `ContratoController`, `CobrancaController`, `AlertaController`

1. **Lembretes de cobrança:** `AlertaService.processarAlertasCobranca()` gera `AlertaLog` com `tipo=COBRANCA` para cobranças vencendo (mesmos dias de antecedência) e em atraso; reaproveita snooze, idempotência diária e os dois canais. Chamado pelo `AlertaScheduler`.
2. **Geração automática mensal:** job agendado (dia 1 às 02:00) percorre contratos `MENSAL`/`ATIVO` e gera a parcela do mês, com a mesma checagem de duplicidade do fluxo manual. Botão manual permanece.
3. **Comprovante de pagamento:** `PATCH /cobrancas/{id}/pagar` passa a aceitar `multipart/form-data` opcional, subindo o arquivo ao Drive e gravando `comprovanteDriveId`; UI do financeiro ganha o campo e o link do comprovante.

**Arquivos:** `ContratoController.java`, `ContratoService.java`, `CobrancaController.java`, `CobrancaService.java`, `AlertaController.java`, `AlertaService.java`, `AlertaScheduler.java`, `CobrancaScheduler.java` (novo), `V8__*.sql` (se necessário), `frontend/src/api/cobrancas.ts`, `api/contratos.ts`, `pages/financeiro/*`, `pages/contratos/*`, `pages/alertas/*`, testes correspondentes.

---

## Agente 4 — Exportação para planilha
**Item 6**

1. `ExportController` + `ExportService`: `GET /api/v1/export/documentos` e `GET /api/v1/export/cobrancas`, CSV `;` com BOM UTF-8 (Excel pt-BR), `Content-Disposition: attachment`, aceitando os mesmos filtros das listagens.
2. `ExportButton.tsx` conforme o contrato acima (download via blob usando a instância `api` do axios).
3. `frontend/src/api/export.ts` com o helper de download.

**Arquivos:** `ExportController.java`, `ExportService.java`, `frontend/src/components/ui/ExportButton.tsx`, `frontend/src/api/export.ts`, testes novos.

---

## Integração (executada após os 4 agentes)

1. `mvn -f backend/pom.xml verify` — build + 500 testes + JaCoCo.
2. `npm --prefix frontend run build` — typecheck + bundle.
3. Correção de conflitos de integração e commit único por workstream.
