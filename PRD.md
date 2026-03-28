# PRD — Sistema de Gestão de Clientes (Prediman CRM)

**Versão:** 1.0
**Data:** 2026-03-28
**Status:** Rascunho
**Autor:** Prediman Engenharia
**Baseado em:** Prediman_CRM_Escopo_v1.docx

---

## 1. Visão Geral do Produto

### 1.1 Problema

A Prediman Engenharia gerencia manualmente documentos, licenças, contratos e cobranças de seus clientes, usando planilhas e pastas dispersas. Isso resulta em:

- Documentos vencidos sem aviso prévio
- Cobranças atrasadas sem acompanhamento
- Informações de clientes descentralizadas
- Impossibilidade de acesso remoto às informações

### 1.2 Solução

Aplicação web responsiva para gestão centralizada de clientes, com controle de documentos, alertas automáticos de vencimento e acompanhamento financeiro de contratos. Acesso via navegador com login individual, permitindo consulta e atualização de qualquer dispositivo.

### 1.3 Proposta de Valor

- **Centralização:** todas as informações de clientes em um único lugar
- **Automação:** alertas de vencimento por e-mail e WhatsApp sem intervenção manual
- **Controle financeiro:** visão clara de recebíveis, pagamentos e inadimplência
- **Acessibilidade:** funciona em computador e celular via navegador

---

## 2. Personas

### 2.1 Administrador (Admin)

- **Perfil:** Gestor ou dono da Prediman Engenharia
- **Necessidades:** Visão completa do negócio, controle de usuários, configuração de alertas
- **Permissões:** Acesso total — criação de usuários, exclusão de clientes, configurações do sistema

### 2.2 Usuário Padrão

- **Perfil:** Funcionário da equipe comercial ou administrativa
- **Necessidades:** Cadastrar/editar clientes, fazer upload de documentos, registrar pagamentos
- **Permissões:** Consulta e edição, sem excluir clientes ou acessar configurações sensíveis

---

## 3. Módulos e Funcionalidades

### 3.1 Módulo 1 — Cadastro de Clientes

| Funcionalidade | Descrição |
|---|---|
| Dados da empresa | Razão social, nome fantasia, CNPJ (alfanumérico), IE, segmento, data de fundação e data de início como cliente |
| Múltiplos contatos | Cada empresa pode ter N contatos com nome, cargo, e-mails e telefones (incluindo WhatsApp) |
| Múltiplos endereços | Cobrança, entrega, filial, etc. Preenchimento automático pelo CEP via API ViaCEP |
| Status | Controle de clientes ativos e inativos, com filtro na listagem |
| Busca e filtros | Pesquisa por nome, CNPJ ou status na lista de clientes |

### 3.2 Módulo 2 — Gestão de Documentos

| Funcionalidade | Descrição |
|---|---|
| Upload de arquivos | PDF, imagens, Word, Excel — até 30 MB por arquivo |
| Organização no Drive | Cada cliente tem sua própria pasta no Google Drive, nomeada com código + nome (ex: `0042 - Petrobras SP`) |
| Nome do arquivo | Gerado automaticamente: `AAAA.MM.DD_NomeArquivo_RevXX` usando data de emissão e revisão |
| Campo de revisão | Texto livre: A, Rev.01, FINAL, 0 — sem restrição de formato |
| Metadados | Nome, categoria (Contrato, Alvará, Certificado, Licença, NF, Outro), data de emissão, data de validade (opcional), observações |
| Visualização | Prévia do documento no navegador, sem necessidade de download |
| Status automático | Válido / A Vencer (≤ 30 dias) / Vencido / Sem validade — calculado pela data de validade |
| Relatório geral | Tela dedicada listando documentos de todos os clientes, ordenada por vencimento (mais urgentes primeiro), com filtros e exportação para planilha |

### 3.3 Módulo 3 — Alertas de Vencimento

| Funcionalidade | Descrição |
|---|---|
| Alertas internos | Sino na barra superior com contagem de documentos próximos ao vencimento. Lista mostra cliente, documento, dias restantes e status |
| Envio automático | Job diário (horário configurável) envia alertas por e-mail e/ou WhatsApp |
| Canal: e-mail | Envio via Gmail (SMTP). Template editável com variáveis (nome do cliente, documento, dias restantes etc.) |
| Canal: WhatsApp | Envio via Evolution API para contato principal com WhatsApp. Template editável |
| Configuração | Admin define: dias de antecedência (ex: 30, 15, 7, 1 dia), templates de mensagem, ligar/desligar cada canal |
| Envio manual | Reenvio individual ou em lote de qualquer alerta |
| Snooze | Adiar alerta por 7 dias sem excluí-lo |

### 3.4 Módulo 4 — Financeiro

| Funcionalidade | Descrição |
|---|---|
| Contratos | Cadastro com valor, periodicidade (mensal, trimestral, semestral, anual, avulso), datas e status |
| Cobranças mensais | Para contratos mensais, parcelas geradas automaticamente. Botão manual para gerar mês atual |
| Registro de pagamento | Marcar parcela como paga com data, valor recebido, forma de pagamento e comprovante (upload) |
| Visão geral | Tela financeira com totais: A Receber (mês atual), Recebido, Em Atraso e Vencendo em 7 dias |
| Lembretes de cobrança | Integrado ao sistema de alertas: avisa por e-mail e WhatsApp cobranças próximas do vencimento ou em atraso |
| Filtros e exportação | Filtro por mês, cliente e status. Exportação para planilha |

---

## 4. Acesso e Segurança

| Recurso | Descrição |
|---|---|
| Login | Acesso com e-mail e senha. Sessão com tokens JWT |
| Recuperação de senha | Redefinição via e-mail. Link com expiração de 2 horas |
| Perfis de acesso | Admin (acesso total) e Usuário padrão (consulta/edição, sem exclusão ou configurações sensíveis) |
| Criação de usuários | Admin cria novos usuários. E-mail de boas-vindas com credenciais iniciais enviado automaticamente |

---

## 5. Requisitos Não-Funcionais

### 5.1 Performance

- Tempo de resposta de páginas: < 2 segundos
- Upload de arquivos de até 30 MB sem timeout
- Job de alertas executado diariamente sem impacto na aplicação

### 5.2 Segurança

- Autenticação JWT com refresh token
- Senhas armazenadas com hash bcrypt
- HTTPS obrigatório em produção
- Proteção contra CSRF, XSS e SQL Injection
- Sessões expiram após inatividade configurável

### 5.3 Responsividade

- Interface funcional em desktop (1920px) e mobile (360px+)
- Layout adaptável para tablets

### 5.4 Disponibilidade

- Uptime alvo: 99.5%
- Backups automáticos do banco de dados

### 5.5 Integrações Externas

| Serviço | Finalidade | Protocolo |
|---|---|---|
| Google Drive API | Armazenamento e organização de arquivos | REST API + OAuth2 |
| Gmail (SMTP) | Envio de e-mails de alerta e notificação | SMTP |
| Evolution API | Envio de mensagens WhatsApp | REST API |
| ViaCEP | Preenchimento automático de endereço por CEP | REST API |

---

## 6. Stack Tecnológica

| Camada | Tecnologia | Justificativa |
|---|---|---|
| Backend | Java 21 + Spring Boot 3.5 | Plataforma robusta e amplamente utilizada em sistemas empresariais |
| Frontend | React 18 + Vite | Interface moderna, rápida e responsiva |
| Banco de dados | PostgreSQL | Banco relacional de alta confiabilidade |
| Armazenamento | Google Drive via API | Arquivos no Drive da empresa, com backup automático do Google |
| E-mail | Gmail (SMTP) | Conta Gmail configurada pelo cliente |
| WhatsApp | Evolution API | Plataforma de automação já utilizada pela empresa |
| Autenticação | JWT + Refresh Token | Segurança sem login frequente |

---

## 7. Modelo de Dados (Entidades Principais)

```
Cliente
├── id, razao_social, nome_fantasia, cnpj, ie, segmento
├── data_fundacao, data_inicio_cliente, status, google_drive_folder_id
├── created_at, updated_at
│
├── Contato [1:N]
│   ├── id, nome, cargo, email, telefone, whatsapp, principal
│
├── Endereco [1:N]
│   ├── id, tipo (cobranca/entrega/filial), cep, logradouro, numero
│   ├── complemento, bairro, cidade, estado
│
├── Documento [1:N]
│   ├── id, nome, categoria, data_emissao, data_validade
│   ├── revisao, observacoes, google_drive_file_id, google_drive_url
│   ├── tamanho_bytes, mime_type, status_calculado
│
├── Contrato [1:N]
│   ├── id, descricao, valor, periodicidade, data_inicio, data_fim, status
│   │
│   └── Cobranca [1:N]
│       ├── id, valor_esperado, valor_recebido, data_vencimento
│       ├── data_pagamento, forma_pagamento, comprovante_drive_id, status
│
└── AlertaLog [1:N]
    ├── id, tipo (documento/cobranca), canal (email/whatsapp)
    ├── data_envio, status_envio, snoozed_ate

Usuario
├── id, nome, email, senha_hash, perfil (admin/usuario), ativo
├── created_at, ultimo_login

ConfiguracaoAlerta
├── id, dias_antecedencia (json array), horario_execucao
├── email_ativo, whatsapp_ativo
├── template_email, template_whatsapp
```

---

## 8. Roadmap de Fases

### Fase 1 — Fundação (Semanas 1-3)

- Setup do projeto (Spring Boot + React + PostgreSQL)
- Autenticação (login, JWT, perfis, recuperação de senha)
- CRUD de clientes com contatos e endereços
- Integração ViaCEP para preenchimento automático
- Listagem com busca e filtros

### Fase 2 — Documentos (Semanas 4-6)

- Upload de arquivos com metadados
- Integração Google Drive (criação de pastas, upload, organização)
- Nomenclatura automática de arquivos
- Visualização/prévia de documentos no navegador
- Status automático de validade
- Relatório geral de documentos com exportação

### Fase 3 — Alertas (Semanas 7-8)

- Sistema de alertas internos (sino + lista)
- Job agendado para verificação diária de vencimentos
- Integração Gmail (SMTP) para envio de alertas por e-mail
- Integração Evolution API para envio por WhatsApp
- Configuração de templates e dias de antecedência
- Envio manual e snooze

### Fase 4 — Financeiro (Semanas 9-11)

- CRUD de contratos com periodicidade
- Geração automática de cobranças mensais
- Registro de pagamentos com upload de comprovante
- Visão geral financeira (A Receber, Recebido, Em Atraso, Vencendo)
- Lembretes de cobrança integrados ao sistema de alertas
- Filtros e exportação para planilha

### Fase 5 — Polimento (Semana 12)

- Testes de integração e E2E
- Ajustes de responsividade mobile
- Aplicação da identidade visual da Prediman
- Deploy em ambiente de produção
- Treinamento do administrador inicial

---

## 9. Fora do Escopo (v1)

Os itens abaixo **não** estão incluídos nesta versão:

- Emissão de boletos ou notas fiscais
- Integração com sistemas ERP ou contábeis
- Aplicativo nativo mobile (iOS/Android)
- Relatórios gráficos avançados ou BI
- Controle de projetos ou ordens de serviço

---

## 10. Critérios de Aceitação

### Cadastro de Clientes
- [ ] Criar, editar e listar clientes com todos os campos obrigatórios
- [ ] Adicionar/remover múltiplos contatos e endereços
- [ ] Preenchimento automático de endereço via CEP
- [ ] Filtrar por nome, CNPJ e status (ativo/inativo)

### Gestão de Documentos
- [ ] Upload de arquivos até 30 MB
- [ ] Pasta criada automaticamente no Google Drive ao cadastrar cliente
- [ ] Nome do arquivo gerado no formato `AAAA.MM.DD_Nome_RevXX`
- [ ] Status calculado automaticamente (Válido, A Vencer, Vencido, Sem validade)
- [ ] Relatório geral com ordenação por vencimento e exportação

### Alertas de Vencimento
- [ ] Sino com contagem de alertas pendentes na barra superior
- [ ] Envio automático diário por e-mail e WhatsApp
- [ ] Templates editáveis com variáveis dinâmicas
- [ ] Configuração de dias de antecedência pelo admin
- [ ] Envio manual e snooze funcional

### Financeiro
- [ ] Cadastro de contratos com todas as periodicidades
- [ ] Geração automática de parcelas mensais
- [ ] Registro de pagamento com comprovante
- [ ] Dashboard com totais (A Receber, Recebido, Em Atraso, Vencendo)
- [ ] Lembretes automáticos de cobrança

### Acesso e Segurança
- [ ] Login com e-mail e senha (JWT)
- [ ] Recuperação de senha por e-mail
- [ ] Dois perfis: Admin e Usuário padrão
- [ ] Admin pode criar/gerenciar usuários

---

## 11. Dependências e Pré-requisitos

Antes de iniciar o desenvolvimento, é necessário:

1. **Confirmação do escopo** — validação dos módulos e funcionalidades
2. **Identidade visual** — cores oficiais e logotipo da Prediman
3. **Conta Gmail** — e-mail que será usado para envio de alertas
4. **Evolution API** — instância configurada e URL de acesso
5. **Google Cloud** — projeto configurado com Google Drive API habilitada e credenciais OAuth2
6. **Administrador inicial** — nome e e-mail do primeiro usuário admin
