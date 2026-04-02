# Prediman CRM

Sistema de gestão de licenças e clientes para Prediman Engenharia. Plataforma completa para controlar e gerenciar contratos, documentos, alertas, cobranças e clientes com autenticação segura e integração com diversos serviços.

![Java 21](https://img.shields.io/badge/Java-21-red?logo=java&logoColor=white)
![Spring Boot 3.5](https://img.shields.io/badge/Spring%20Boot-3.5-green?logo=spring-boot&logoColor=white)
![React 19](https://img.shields.io/badge/React-19-blue?logo=react&logoColor=white)
![PostgreSQL 16](https://img.shields.io/badge/PostgreSQL-16-336791?logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)
![License](https://img.shields.io/badge/License-Proprietary-red)

## Visão Geral

O Prediman CRM é um sistema integrado para gerenciar o ciclo de vida completo dos clientes e contratos. Com funcionalidades de gestão de documentos, sistema de alertas automáticos, módulo financeiro e controle de usuários, oferece uma solução robusta para empresas de engenharia e consultoria.

## Características

### Gestão de Clientes
- Cadastro de clientes Pessoa Física (PF) e Pessoa Jurídica (PJ)
- Gerenciamento de contatos por cliente
- Gestão de endereços com integração ViaCEP para preenchimento automático
- Status do cliente (ativo, inativo, suspenso)
- Busca e filtros avançados

### Gestão de Documentos
- Upload e armazenamento de documentos
- Categorização de documentos (contratos, NF, RG, CNPJ, etc.)
- Controle de validade com alertas automáticos
- Integração com Google Drive para armazenamento em nuvem
- Histórico de versões e rastreamento de alterações

### Sistema de Alertas
- Alertas por email (SMTP Gmail)
- Alertas por WhatsApp (Evolution API)
- Configuração de templates de alerta customizáveis
- Agendamento dinâmico com Scheduler
- Histórico completo de alertas enviados
- Função de snooze para adiar notificações

### Módulo Financeiro
- Gestão de contratos com datas de vigência
- Registro de cobranças com status
- Parcelas automáticas com periodicidade configurável
- Rastreamento de pagamentos
- Relatórios financeiros

### Gestão de Usuários
- Autenticação por JWT com refresh tokens
- Dois perfis: ADMIN e USUARIO
- Controle de acesso baseado em roles
- Senhas seguras com hashing
- Rate limiting para proteção contra abuso

## Pré-requisitos

- **Java 21** (JDK ou OpenJDK)
- **Node.js 20+** com npm
- **Docker** e **Docker Compose** (para execução containerizada)
- **PostgreSQL 16** (automático via Docker Compose)
- **Git**

## Início Rápido

### Com Docker Compose (Recomendado)

1. Clone o repositório:
```bash
git clone <repository-url>
cd license-management-crm
```

2. Copie o arquivo de configuração:
```bash
cp .env .env.local
```

3. Configure as variáveis de ambiente (veja seção "Variáveis de Ambiente"):
```bash
nano .env.local
```

4. Inicie os serviços:
```bash
docker-compose up -d
```

5. Aguarde a inicialização (verifique com `docker-compose logs -f`):
- Banco de dados PostgreSQL estará disponível em `localhost:5432`
- Backend estará disponível em `http://localhost:8080`
- Frontend estará disponível em `http://localhost:80` (ou conforme configurado)

6. Acesse a aplicação:
- URL: `http://localhost`
- Email: `admin@prediman.com.br`
- Senha: `admin123` (mude na primeira execução!)

### Desenvolvimento Local

#### Backend (Spring Boot)

1. Navegue até o diretório backend:
```bash
cd backend
```

2. Configure o banco de dados (opcionalmente com Docker):
```bash
# Inicie apenas o PostgreSQL
docker-compose up -d postgres
```

3. Configure variáveis de ambiente (crie `.env` ou `application-dev.yml`):
```bash
export DB_USERNAME=prediman
export DB_PASSWORD=prediman123
export JWT_SECRET=your-secret-key-256-bits-or-longer
export ADMIN_INITIAL_PASSWORD=admin123
```

4. Execute a aplicação:
```bash
# Com Maven Wrapper
./mvnw spring-boot:run

# Ou com Maven instalado globalmente
mvn spring-boot:run
```

5. A API estará disponível em `http://localhost:8080`

#### Frontend (React + Vite)

1. Navegue até o diretório frontend:
```bash
cd frontend
```

2. Instale as dependências:
```bash
npm install
```

3. Configure a URL do backend (em `.env.local` ou variáveis de ambiente):
```bash
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

4. Inicie o servidor de desenvolvimento:
```bash
npm run dev
```

5. A aplicação estará disponível em `http://localhost:5173` (por padrão)

## Variáveis de Ambiente

Todas as configurações são gerenciadas através do arquivo `.env`. Copie `.env` para `.env.local` e configure conforme necessário.

| Variável | Descrição | Exemplo | Obrigatória |
|----------|-----------|---------|-------------|
| `DB_USERNAME` | Usuário do PostgreSQL | `prediman` | Sim |
| `DB_PASSWORD` | Senha do PostgreSQL | `prediman123` | Sim |
| `JWT_SECRET` | Chave secreta para assinar tokens JWT (mín. 256 bits) | `your-secret-key-must-be-at-least-256-bits-long` | Sim |
| `ADMIN_INITIAL_PASSWORD` | Senha inicial do usuário admin (mude na primeira execução) | `admin123` | Sim |
| `MAIL_HOST` | Host do servidor SMTP | `smtp.gmail.com` | Não* |
| `MAIL_PORT` | Porta do SMTP | `587` | Não* |
| `MAIL_USERNAME` | Usuário do email para envio de alertas | `seu-email@gmail.com` | Não* |
| `MAIL_PASSWORD` | Senha ou App Password do email | `sua-app-password` | Não* |
| `MAIL_FROM` | Email remetente dos alertas | `alertas@prediman.com.br` | Não |
| `GOOGLE_DRIVE_CLIENT_ID` | Client ID da OAuth2 do Google Drive | (Obtido no Google Cloud Console) | Não** |
| `GOOGLE_DRIVE_CLIENT_SECRET` | Client Secret da OAuth2 | (Obtido no Google Cloud Console) | Não** |
| `GOOGLE_DRIVE_REDIRECT_URI` | URI de redirecionamento OAuth2 | `http://localhost:8080/api/oauth2/callback/google` | Não** |
| `GOOGLE_DRIVE_ROOT_FOLDER_ID` | ID da pasta raiz no Google Drive | (Obtido no Google Drive) | Não** |
| `EVOLUTION_API_URL` | URL base da Evolution API | `https://api.evolution.com.br` | Não*** |
| `EVOLUTION_API_KEY` | Chave de API da Evolution | (Obtida na plataforma) | Não*** |
| `EVOLUTION_INSTANCE_NAME` | Nome da instância WhatsApp | `prediman` | Não*** |
| `CORS_ALLOWED_ORIGINS` | Origens CORS permitidas (separadas por vírgula) | `https://prediman.paulobof.com.br` | Não |

**Notas:**
- *: Obrigatória apenas se alertas por email forem ativados
- **: Obrigatória apenas se gestão de documentos no Google Drive for ativada
- ***: Obrigatória apenas se alertas por WhatsApp forem ativados

### Configuração do Gmail SMTP

Para enviar alertas por email via Gmail:

1. Ative a autenticação de dois fatores em sua conta Google
2. Crie uma [App Password](https://support.google.com/accounts/answer/185833)
3. Use o email da conta como `MAIL_USERNAME`
4. Use a App Password gerada como `MAIL_PASSWORD`

### Configuração do Google Drive API

Para integração com Google Drive:

1. Acesse [Google Cloud Console](https://console.cloud.google.com/)
2. Crie um novo projeto
3. Ative a Drive API
4. Crie credenciais OAuth2 (tipo: aplicação web)
5. Autorize a URI de redirecionamento
6. Configure as variáveis de ambiente com as credenciais obtidas

## Documentação da API

A API REST segue padrões RESTful com versionamento. Todas as rotas estão prefixadas com `/api/v1/`.

### Acessar Documentação Interativa

A documentação interativa (Swagger UI) está disponível em:

```
http://localhost:8080/swagger-ui.html
```

Você também pode acessar a especificação OpenAPI em:

```
http://localhost:8080/v3/api-docs
```

### Endpoints Principais

#### Autenticação
- `POST /api/v1/auth/login` - Fazer login
- `POST /api/v1/auth/refresh` - Renovar token JWT

#### Clientes
- `GET /api/v1/clientes` - Listar clientes (paginado)
- `POST /api/v1/clientes` - Criar novo cliente
- `GET /api/v1/clientes/{id}` - Obter detalhes de um cliente
- `PUT /api/v1/clientes/{id}` - Atualizar cliente
- `DELETE /api/v1/clientes/{id}` - Deletar cliente

#### Documentos
- `GET /api/v1/documentos` - Listar documentos
- `POST /api/v1/documentos` - Upload de documento
- `GET /api/v1/documentos/{id}` - Obter documento
- `DELETE /api/v1/documentos/{id}` - Deletar documento

#### Contratos
- `GET /api/v1/contratos` - Listar contratos
- `POST /api/v1/contratos` - Criar contrato
- `GET /api/v1/contratos/{id}` - Obter detalhes do contrato
- `PUT /api/v1/contratos/{id}` - Atualizar contrato

#### Cobranças
- `GET /api/v1/cobrancas` - Listar cobranças
- `POST /api/v1/cobrancas` - Criar cobrança
- `PUT /api/v1/cobrancas/{id}` - Atualizar status de cobrança

#### Alertas
- `GET /api/v1/alertas` - Listar alertas pendentes
- `GET /api/v1/alertas/log` - Histórico de alertas
- `POST /api/v1/alertas/snooze/{id}` - Adiar alerta
- `PUT /api/v1/alertas/configuracoes` - Configurar alertas

Para documentação completa de cada endpoint, acesse o Swagger UI.

## Estrutura do Projeto

```
license-management-crm/
├── backend/
│   ├── src/
│   │   ├── main/java/com/prediman/crm/
│   │   │   ├── config/           # Configurações (Security, Scheduler, etc.)
│   │   │   ├── controller/       # Controladores REST
│   │   │   ├── service/          # Lógica de negócio
│   │   │   ├── repository/       # Acesso a dados (JPA)
│   │   │   ├── model/            # Entidades JPA
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── security/         # JWT, autenticação, autorização
│   │   │   ├── exception/        # Tratamento de exceções
│   │   │   └── CrmApplication.java
│   │   ├── test/                 # Testes unitários e integração
│   │   └── resources/
│   │       ├── db/migration/     # Migrations Flyway SQL
│   │       ├── application.yml
│   │       └── application-prod.yml
│   ├── pom.xml                   # Dependências Maven
│   ├── Dockerfile                # Build multi-stage para produção
│   └── mvnw                       # Maven Wrapper
│
├── frontend/
│   ├── src/
│   │   ├── components/           # Componentes React reutilizáveis
│   │   ├── pages/                # Páginas/rotas principais
│   │   ├── services/             # Chamadas à API
│   │   ├── hooks/                # React Hooks customizados
│   │   ├── styles/               # Tailwind CSS
│   │   ├── App.tsx
│   │   └── main.tsx
│   ├── public/                   # Arquivos estáticos
│   ├── package.json              # Dependências npm
│   ├── vite.config.ts            # Configuração Vite
│   ├── tsconfig.json             # Configuração TypeScript
│   ├── Dockerfile                # Build com Node + nginx
│   └── nginx.conf                # Configuração nginx
│
├── docker-compose.yml            # Orquestração de containers
├── .env                          # Variáveis de ambiente (ignorado no git)
├── .env.local                    # Sobrescrita de variáveis locais (ignorado no git)
├── .gitignore
└── README.md                     # Este arquivo
```

## Testes

O projeto inclui cobertura de testes abrangente com 40+ testes unitários e de integração, atingindo 99% de cobertura de código.

### Executar Testes (Backend)

```bash
cd backend

# Executar todos os testes
./mvnw test

# Executar testes com cobertura
./mvnw test jacoco:report

# Executar testes específicos
./mvnw test -Dtest=ClienteServiceTest

# Executar com saída detalhada
./mvnw test -X
```

### Arquivos de Teste

Os testes estão localizados em `backend/src/test/java/com/prediman/crm/` com a mesma estrutura do código principal:

- **Controllers**: `ClienteControllerTest`, `ContratoControllerTest`, `DocumentoControllerTest`, `AlertaControllerTest`, `AuthControllerTest`, `UserControllerTest`, `CobrancaControllerTest`
- **Services**: `ClienteServiceTest`, `AlertaServiceTest`, `CobrancaServiceTest`, `DocumentoServiceTest`, `ContratoServiceTest`, `UserServiceTest`, `AlertaSchedulerTest`, `AuthServiceTest`, `ViaCepServiceTest`
- **Security**: `JwtAuthenticationFilterTest`, `JwtTokenProviderTest`, `UserDetailsServiceImplTest`, `SecurityConfigTest`, `SecurityFilterChainTest`, `RateLimitFilterTest`
- **Models**: `ClienteTest`, `ContratoTest`, `DocumentoTest`, `AlertaLogTest`, `ConfiguracaoAlertaTest`, `CobrancaTest`, `UsuarioTest`
- **Config**: `DataSeederTest`, `SchedulerConfigTest`, `PageableConfigTest`, `RestTemplateConfigTest`
- **Exception**: `GlobalExceptionHandlerTest`

### Testes do Frontend

```bash
cd frontend

# Executar linter
npm run lint

# Build para produção
npm run build

# Preview do build
npm run preview
```

## Deployment

### Com Docker Compose (Ambiente de Produção)

1. Configure todas as variáveis de ambiente no arquivo `.env`:
```bash
# Segurança
DB_PASSWORD=senhaForte123!@#
JWT_SECRET=chaveSecretaMuitoGrandeComPeloMenos256Bits
ADMIN_INITIAL_PASSWORD=senhaAdminForte123!@#

# Email
MAIL_USERNAME=seu-email@gmail.com
MAIL_PASSWORD=sua-app-password

# CORS
CORS_ALLOWED_ORIGINS=https://seu-dominio.com.br
```

2. Inicie os serviços em background:
```bash
docker-compose up -d
```

3. Verifique o status:
```bash
docker-compose ps
docker-compose logs -f backend
```

4. Parar os serviços:
```bash
docker-compose down
```

5. Parar e remover volumes (cuidado!):
```bash
docker-compose down -v
```

### Backup do Banco de Dados

O Docker Compose está configurado para fazer backup automático do PostgreSQL a cada 24 horas. Os backups são armazenados em `pgbackups/` e mantidos por 7 dias.

Para fazer backup manual:
```bash
docker exec prediman-db-backup pg_dump -U prediman prediman_crm > backup_manual_$(date +%Y%m%d_%H%M%S).dump
```

Para restaurar:
```bash
docker exec -i prediman-db psql -U prediman prediman_crm < backup_manual_20240101_120000.dump
```

### Healthchecks

O Docker Compose está configurado com healthchecks para todos os serviços:

- **PostgreSQL**: Verifica disponibilidade com `pg_isready`
- **Backend**: Verifica `/actuator/health` a cada 30 segundos
- **Frontend**: Nginx está sempre ativo

Verifique o status:
```bash
docker-compose ps
```

## Segurança

### Autenticação e Autorização

- Autenticação por JWT com refresh tokens
- Senhas com hashing bcrypt
- Rate limiting em endpoints sensíveis (5 requisições por minuto)
- CORS configurável por ambiente
- Validação de entrada em todos os endpoints

### Boas Práticas

1. **Sempre use HTTPS em produção** (configure reverse proxy nginx com SSL)
2. **Mude a senha admin** na primeira execução
3. **Gere uma JWT_SECRET forte** (mínimo 256 bits)
4. **Nunca commite o arquivo `.env`** (use `.env.local` para overrides locais)
5. **Revise e atualize dependências** regularmente
6. **Configure backups automáticos** (já configurado via docker-compose)

## Monitoramento

### Endpoints de Health Check

```bash
# Status geral da aplicação
curl http://localhost:8080/actuator/health

# Detalhes do sistema
curl http://localhost:8080/actuator/info

# Métricas Prometheus
curl http://localhost:8080/actuator/prometheus
```

### Logs

```bash
# Ver logs do backend
docker-compose logs -f backend

# Ver logs do PostgreSQL
docker-compose logs -f postgres

# Ver logs do frontend
docker-compose logs -f frontend

# Salvar logs em arquivo
docker-compose logs > logs_$(date +%Y%m%d_%H%M%S).txt
```

## Troubleshooting

### Problema: Erro de conexão com o banco de dados

**Solução:**
```bash
# Verifique se o PostgreSQL está rodando
docker-compose ps

# Reinicie o banco de dados
docker-compose restart postgres

# Verifique as credenciais no .env
grep DB_ .env
```

### Problema: Porta já em uso

**Solução:**
```bash
# Encontre qual processo está usando a porta
lsof -i :8080  # Backend
lsof -i :5432  # PostgreSQL
lsof -i :80    # Frontend

# Mate o processo (Linux/Mac)
kill -9 <PID>

# Ou use a porta alternativa no docker-compose.yml
```

### Problema: Token JWT expirado

**Solução:**
Use o endpoint `/api/v1/auth/refresh` com o refresh token para obter um novo access token.

### Problema: CORS error no frontend

**Solução:**
Verifique se a origem do frontend está configurada em `CORS_ALLOWED_ORIGINS` no `.env`.

### Problema: Senha admin não funciona

**Solução:**
A senha padrão é `admin123`. Se não funcionar:
1. Verifique se o usuário admin foi criado no DataSeeder
2. Consulte os logs: `docker-compose logs backend`
3. Restaure o banco de dados se necessário

## Contribuindo

1. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
2. Commit suas mudanças seguindo o padrão (veja abaixo)
3. Push para a branch (`git push origin feature/AmazingFeature`)
4. Abra um Pull Request

### Padrões de Commit

Use commit convencional com uma única linha de subject (sem body/footer):

```bash
# Exemplos válidos
git commit -m "feat: adicionar endpoint GET /clientes"
git commit -m "fix: corrigir validação de email"
git commit -m "docs: atualizar README"
git commit -m "test: adicionar testes para ClienteService"
git commit -m "refactor: simplificar lógica de alertas"
```

### Padrões de Código

- **Backend**: Siga as convenções Java com Clean Code principles
- **Frontend**: Use TypeScript, componentes funcionais com hooks
- **Testes**: Mínimo 80% de cobertura por módulo

## Stack Tecnológico

### Backend
- **Java 21** com Spring Boot 3.5
- **Spring Security** com JWT (JJWT 0.12.6)
- **Spring Data JPA** com Hibernate
- **PostgreSQL 16** como banco de dados
- **Flyway** para migrations
- **Lombok** para reduzir boilerplate
- **Maven** como gerenciador de dependências
- **JUnit 5** e **Testcontainers** para testes

### Frontend
- **React 19** com TypeScript
- **Vite** como bundler (compilação ultra rápida)
- **Tailwind CSS 4** para styling
- **React Router** para navegação
- **Axios** para requisições HTTP
- **Lucide React** para ícones

### Infraestrutura
- **Docker** e **Docker Compose** para containerização
- **PostgreSQL 16** (Alpine) como banco de dados
- **Nginx** (Alpine) para servir o frontend
- **Java 21 JRE** (Alpine) para o backend
- **Backups automáticos** via pg_dump a cada 24h

## Licença

Proprietary - Todos os direitos reservados para Prediman Engenharia.

## Suporte

Para dúvidas ou problemas:

1. Consulte a documentação no Swagger UI: `http://localhost:8080/swagger-ui.html`
2. Verifique os logs: `docker-compose logs -f`
3. Verifique a seção TROUBLESHOOTING neste README
4. Abra uma issue ou entre em contato com o time de desenvolvimento

---

**Versão**: 1.0.0  
**Última atualização**: 2024  
**Mantido por**: Prediman Engenharia
