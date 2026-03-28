# Fase 1 — Fundação: Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the foundation of Prediman CRM — project scaffolding, authentication with JWT, and full client CRUD with contacts, addresses, ViaCEP integration, search and filters.

**Architecture:** Monorepo with `backend/` (Java 21 + Spring Boot 3.5 + PostgreSQL) and `frontend/` (React 18 + Vite + TypeScript + Tailwind CSS). REST API with JWT auth. Docker Compose for local PostgreSQL.

**Tech Stack:** Java 21, Spring Boot 3.5, Spring Security, PostgreSQL 16, React 18, Vite 5, TypeScript, Tailwind CSS, Axios, React Router, Docker Compose.

---

## File Structure

```
license-management-crm/
├── docker-compose.yml                          # PostgreSQL + pgAdmin
├── backend/
│   ├── pom.xml
│   ├── src/main/java/com/prediman/crm/
│   │   ├── PredimanCrmApplication.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java             # Spring Security + JWT filter
│   │   │   ├── CorsConfig.java
│   │   │   └── JwtConfig.java
│   │   ├── security/
│   │   │   ├── JwtTokenProvider.java           # Generate/validate JWT
│   │   │   ├── JwtAuthenticationFilter.java    # Filter for requests
│   │   │   └── UserDetailsServiceImpl.java
│   │   ├── controller/
│   │   │   ├── AuthController.java             # Login, refresh, password reset
│   │   │   ├── UserController.java             # Admin user management
│   │   │   └── ClienteController.java          # Client CRUD + search
│   │   ├── service/
│   │   │   ├── AuthService.java
│   │   │   ├── UserService.java
│   │   │   ├── ClienteService.java
│   │   │   └── ViaCepService.java
│   │   ├── repository/
│   │   │   ├── UsuarioRepository.java
│   │   │   ├── ClienteRepository.java
│   │   │   ├── ContatoRepository.java
│   │   │   └── EnderecoRepository.java
│   │   ├── model/
│   │   │   ├── Usuario.java
│   │   │   ├── Cliente.java
│   │   │   ├── Contato.java
│   │   │   ├── Endereco.java
│   │   │   └── enums/
│   │   │       ├── Perfil.java                 # ADMIN, USUARIO
│   │   │       ├── StatusCliente.java          # ATIVO, INATIVO
│   │   │       └── TipoEndereco.java           # COBRANCA, ENTREGA, FILIAL
│   │   ├── dto/
│   │   │   ├── LoginRequest.java
│   │   │   ├── LoginResponse.java
│   │   │   ├── ClienteRequest.java
│   │   │   ├── ClienteResponse.java
│   │   │   ├── ContatoDTO.java
│   │   │   ├── EnderecoDTO.java
│   │   │   └── ViaCepResponse.java
│   │   └── exception/
│   │       ├── GlobalExceptionHandler.java
│   │       └── ResourceNotFoundException.java
│   └── src/main/resources/
│       ├── application.yml
│       ├── application-dev.yml
│       └── db/migration/
│           └── V1__create_initial_schema.sql
├── frontend/
│   ├── package.json
│   ├── vite.config.ts
│   ├── tailwind.config.js
│   ├── tsconfig.json
│   ├── index.html
│   └── src/
│       ├── main.tsx
│       ├── App.tsx
│       ├── api/
│       │   ├── axios.ts                        # Axios instance + interceptors
│       │   ├── auth.ts                         # Login, refresh, logout
│       │   └── clientes.ts                     # Client API calls
│       ├── contexts/
│       │   └── AuthContext.tsx                  # Auth state + protected routes
│       ├── components/
│       │   ├── Layout.tsx                      # Sidebar + header + outlet
│       │   ├── ProtectedRoute.tsx
│       │   ├── Sidebar.tsx
│       │   └── ui/                             # Reusable components
│       │       ├── Button.tsx
│       │       ├── Input.tsx
│       │       ├── Modal.tsx
│       │       ├── Table.tsx
│       │       └── Badge.tsx
│       ├── pages/
│       │   ├── Login.tsx
│       │   ├── Dashboard.tsx
│       │   ├── clientes/
│       │   │   ├── ClienteList.tsx
│       │   │   ├── ClienteForm.tsx
│       │   │   └── ClienteDetail.tsx
│       │   └── usuarios/
│       │       └── UsuarioList.tsx
│       └── types/
│           └── index.ts                        # All TypeScript interfaces
```

---

## Task 1: Docker + PostgreSQL Setup

**Files:**
- Create: `docker-compose.yml`

- [ ] **Step 1: Create docker-compose.yml**

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:16-alpine
    container_name: prediman-db
    environment:
      POSTGRES_DB: prediman_crm
      POSTGRES_USER: prediman
      POSTGRES_PASSWORD: prediman123
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data

volumes:
  pgdata:
```

- [ ] **Step 2: Start PostgreSQL**

Run: `docker-compose up -d`
Expected: PostgreSQL running on port 5432

- [ ] **Step 3: Commit**

```bash
git add docker-compose.yml
git commit -m "feat: add Docker Compose with PostgreSQL 16"
```

---

## Task 2: Spring Boot Backend Scaffolding

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/src/main/java/com/prediman/crm/PredimanCrmApplication.java`
- Create: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/resources/application-dev.yml`

- [ ] **Step 1: Create pom.xml** with dependencies: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-security, spring-boot-starter-validation, postgresql, jjwt (0.12.x), flyway-core, lombok, spring-boot-starter-test

- [ ] **Step 2: Create main application class**

- [ ] **Step 3: Create application.yml** with Spring profiles, JPA config (ddl-auto=validate, show-sql=true), server port 8080

- [ ] **Step 4: Create application-dev.yml** with PostgreSQL connection (jdbc:postgresql://localhost:5432/prediman_crm)

- [ ] **Step 5: Create Flyway migration V1__create_initial_schema.sql** with all tables: usuarios, clientes, contatos, enderecos

- [ ] **Step 6: Build and verify**

Run: `cd backend && mvn clean compile`

- [ ] **Step 7: Commit**

---

## Task 3: JPA Entities + Enums

**Files:**
- Create: All model classes and enums

- [ ] **Step 1: Create enums** — Perfil (ADMIN, USUARIO), StatusCliente (ATIVO, INATIVO), TipoEndereco (COBRANCA, ENTREGA, FILIAL, OUTRO)

- [ ] **Step 2: Create Usuario entity** — id, nome, email, senhaHash, perfil, ativo, createdAt, ultimoLogin

- [ ] **Step 3: Create Cliente entity** — all fields from PRD, @OneToMany for contatos and enderecos (cascade ALL, orphanRemoval)

- [ ] **Step 4: Create Contato entity** — id, nome, cargo, email, telefone, whatsapp, principal, @ManyToOne cliente

- [ ] **Step 5: Create Endereco entity** — id, tipo, cep, logradouro, numero, complemento, bairro, cidade, estado, @ManyToOne cliente

- [ ] **Step 6: Compile and verify**

- [ ] **Step 7: Commit**

---

## Task 4: Authentication (JWT)

**Files:**
- Create: SecurityConfig, JwtTokenProvider, JwtAuthenticationFilter, AuthController, AuthService, DTOs

- [ ] **Step 1: Create JwtTokenProvider** — generateToken, validateToken, getEmailFromToken using jjwt

- [ ] **Step 2: Create JwtAuthenticationFilter** — extends OncePerRequestFilter, extracts and validates JWT from Authorization header

- [ ] **Step 3: Create SecurityConfig** — permits /api/auth/**, secures everything else, stateless session, CORS config

- [ ] **Step 4: Create UserDetailsServiceImpl** — loads user from UsuarioRepository

- [ ] **Step 5: Create AuthController** — POST /api/auth/login, POST /api/auth/refresh

- [ ] **Step 6: Create AuthService** — authenticate, generateTokenPair, refreshToken

- [ ] **Step 7: Create seed for admin user** — CommandLineRunner that creates admin@prediman.com.br / admin123 if not exists

- [ ] **Step 8: Test login via curl/httpie**

- [ ] **Step 9: Commit**

---

## Task 5: Client CRUD (Backend)

**Files:**
- Create: ClienteController, ClienteService, ClienteRepository, DTOs

- [ ] **Step 1: Create ClienteRequest/ClienteResponse DTOs** — nested ContatoDTO and EnderecoDTO lists

- [ ] **Step 2: Create ClienteRepository** — extends JpaRepository, custom query methods for search (findByRazaoSocialContaining, findByCnpj, findByStatus)

- [ ] **Step 3: Create ClienteService** — create, update, findById, findAll (with Specification for filters), delete (admin only), toggleStatus

- [ ] **Step 4: Create ClienteController** — GET/POST/PUT/DELETE /api/clientes, GET /api/clientes/search?q=&status=

- [ ] **Step 5: Create ViaCepService** — calls viacep.com.br/ws/{cep}/json/ and returns EnderecoDTO

- [ ] **Step 6: Add GET /api/cep/{cep} endpoint** to ClienteController

- [ ] **Step 7: Create GlobalExceptionHandler** — handles validation errors, not found, duplicate CNPJ

- [ ] **Step 8: Test all endpoints via curl**

- [ ] **Step 9: Commit**

---

## Task 6: React Frontend Scaffolding

**Files:**
- Create: All frontend scaffolding files

- [ ] **Step 1: Create Vite + React + TypeScript project** — `npm create vite@latest frontend -- --template react-ts`

- [ ] **Step 2: Install dependencies** — tailwindcss, react-router-dom, axios, lucide-react, @headlessui/react

- [ ] **Step 3: Configure Tailwind** with dark-mode class strategy, custom colors for Prediman brand

- [ ] **Step 4: Create types/index.ts** — TypeScript interfaces matching backend DTOs

- [ ] **Step 5: Create api/axios.ts** — base URL, JWT interceptor (attach token, refresh on 401)

- [ ] **Step 6: Create AuthContext.tsx** — login, logout, isAuthenticated, user, token persistence in localStorage

- [ ] **Step 7: Create ProtectedRoute.tsx** — redirect to /login if not authenticated

- [ ] **Step 8: Create App.tsx with routes** — /login, / (protected layout with sidebar)

- [ ] **Step 9: Verify dev server**

Run: `cd frontend && npm run dev`

- [ ] **Step 10: Commit**

---

## Task 7: Login Page (Frontend)

**Files:**
- Create: `frontend/src/pages/Login.tsx`

- [ ] **Step 1: Create Login page** — email + password form, error handling, redirect to / on success, Prediman branding

- [ ] **Step 2: Create api/auth.ts** — login(email, password), refreshToken()

- [ ] **Step 3: Wire to AuthContext** — call login, store tokens, redirect

- [ ] **Step 4: Test login flow in browser**

- [ ] **Step 5: Commit**

---

## Task 8: Layout + Navigation

**Files:**
- Create: Layout.tsx, Sidebar.tsx, Dashboard.tsx

- [ ] **Step 1: Create Sidebar** — navigation items: Dashboard, Clientes, Usuários (admin only). Active state, responsive collapse for mobile

- [ ] **Step 2: Create Layout** — sidebar + header (user name, logout button, notification bell placeholder) + Outlet

- [ ] **Step 3: Create Dashboard** — placeholder with welcome message and summary cards (to be filled in later phases)

- [ ] **Step 4: Commit**

---

## Task 9: Client List Page (Frontend)

**Files:**
- Create: ClienteList.tsx, api/clientes.ts, ui components

- [ ] **Step 1: Create api/clientes.ts** — getAll(params), getById(id), create(data), update(id, data), delete(id), searchCep(cep)

- [ ] **Step 2: Create reusable Table component** — sortable headers, pagination

- [ ] **Step 3: Create ClienteList page** — table with razao_social, cnpj, segmento, status badge, actions (edit, view). Search bar + status filter. "Novo Cliente" button

- [ ] **Step 4: Test list rendering with mock data**

- [ ] **Step 5: Commit**

---

## Task 10: Client Form Page (Frontend)

**Files:**
- Create: ClienteForm.tsx, reusable Input/Modal/Button components

- [ ] **Step 1: Create reusable UI components** — Input (with label, error), Button (variants), Modal, Badge

- [ ] **Step 2: Create ClienteForm page** — tabbed form: Dados da Empresa, Contatos, Endereços. Create and Edit modes.

- [ ] **Step 3: Implement Contatos section** — dynamic list, add/remove contact rows (nome, cargo, email, telefone, whatsapp, principal checkbox)

- [ ] **Step 4: Implement Endereços section** — dynamic list with tipo select, CEP field that auto-fills via ViaCEP API

- [ ] **Step 5: Wire form submission** — POST for create, PUT for edit, redirect to list on success

- [ ] **Step 6: Test full CRUD flow in browser**

- [ ] **Step 7: Commit**

---

## Task 11: User Management (Admin)

**Files:**
- Create: UserController, UserService (backend), UsuarioList.tsx (frontend)

- [ ] **Step 1: Backend — UserController** — GET /api/users (admin only), POST /api/users, PUT /api/users/{id}, PATCH /api/users/{id}/toggle-status

- [ ] **Step 2: Backend — UserService** — create (hash password, send welcome email placeholder), update, toggleActive

- [ ] **Step 3: Frontend — UsuarioList page** — table with nome, email, perfil, ativo. Create user modal. Toggle active status

- [ ] **Step 4: Test admin can create and manage users**

- [ ] **Step 5: Commit**

---

## Parallel Agent Assignment

| Agent | Tasks | Focus |
|-------|-------|-------|
| **Agent A: Backend** | Tasks 1-5 | Docker, Spring Boot, entities, auth, API |
| **Agent B: Frontend** | Tasks 6-10 | React, auth UI, layout, client pages |
| **Agent C: Integration** | Task 11 + wiring | User management, final integration, testing |

Agents A and B run in parallel. Agent C starts after both A and B complete.
