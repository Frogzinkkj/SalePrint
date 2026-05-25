# Gerenciador de Impressoras

Sistema para cadastro, movimentação e histórico de impressoras, com integração opcional ao GLPI.

## Stack

- **Backend:** Java 21, Spring Boot 3.4, PostgreSQL, OpenCSV, SpringDoc (Swagger)
- **Frontend:** Angular 21, Angular Material, Standalone Components
- **Infra:** Docker Compose (PostgreSQL + pgAdmin)

## Estrutura

```
GerenciadorImpressora/
├── backend/           # API REST (porta 8080, context /api)
├── frontend/          # SPA Angular (porta 4200)
├── dados/             # CSV de exemplo
├── docker-compose.yml
└── AI.txt             # Especificação do projeto
```

## Pré-requisitos

- Java 21 (JDK)
- Node.js 20+ e Angular CLI
- Docker Desktop (para PostgreSQL)

## Início rápido

### 1. Banco de dados

```powershell
docker compose up -d
```

- PostgreSQL: `localhost:5432` (db: `gerenciador_impressoras`, user/pass: `postgres`)
- pgAdmin: http://localhost:5050 (admin@admin.com / admin)

### 2. Backend

```powershell
cd backend
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"
# Compilar (se mvnw.cmd falhar, use o comando java abaixo ou instale Maven)
.\mvnw.cmd clean package -DskipTests

# Ou diretamente:
# java -classpath ".mvn\wrapper\maven-wrapper.jar" "-Dmaven.multiModuleProjectDirectory=." org.apache.maven.wrapper.MavenWrapperMain clean package -DskipTests

java -jar target\impressora-1.0.0.jar
```

- API: http://localhost:8080/api
- Swagger: http://localhost:8080/api/swagger-ui.html
- Health: http://localhost:8080/api/health

### 3. Frontend

```powershell
cd frontend
npm install
ng serve
```

- App: http://localhost:4200

## Endpoints principais

| Recurso | Path |
|---------|------|
| Localidades | `/api/localidades` |
| Setores | `/api/setores` |
| Impressoras | `/api/impressoras` |
| Dashboard | `/api/impressoras/dashboard` |
| Movimentar | `POST /api/impressoras/movimentar` |
| Histórico | `/api/movimentacoes/historico/{id}` |
| Import CSV | `POST /api/importacao/csv` |

## Importação CSV

### Formato planilha (export Excel / lista operacional)

Colunas na ordem:

`Localidade | Setor/Local | Marca | Modelo | IP | S/N | Status | Data Status | Observações`

Exemplo:

```csv
,Setor/Local,Marca,Modelo,IP,S/N,Status,Data Status,Observações
Dom Bosco,Gestora Pedagogica,CANON,G6010,10.100.13.249,KMFL07996,Ativa,24/03/2026,
Dom Bosco,BACKUP,EPSON,M3180,10.100.14.49,X5UL001736F,Defeito,24/03/2026,NA TI
```

- **Localidades** devem existir no seed: `Dom Bosco`, `Camaçari`
- **Setores** são criados automaticamente se não existirem (ex.: Gestora Pedagogica, BLA SOE 1º andar)
- **Status** mapeados: Ativa, Defeito, Manutenção, Provisória, Substituida/Retirada, Backup
- **IP vazio** → `SEM-IP-<numeroSerie>` (evita conflito entre impressoras sem IP)

### Formato simples (legado)

```csv
marca,modelo,numeroSerie,ip,setor,localidade,observacao
HP,LaserJet Pro M404n,CN12345ABC,192.168.1.50,Tesouraria,Dom Bosco,Impressora principal
```

Arquivo exemplo: `dados/impressoras-exemplo.csv`

## Regras de negócio

- Número de série único globalmente
- IP único apenas entre impressoras com status **ATIVA**
- Alteração de setor/status gera registro em `movimentacoes`
- Status `COM_DEFEITO_INTERNO` dispara chamado GLPI (se `glpi.enabled=true`)

## GLPI (opcional)

Em `backend/src/main/resources/application.properties`:

```properties
glpi.enabled=true
glpi.url=https://seu-glpi/apirest.php/Ticket
```

## Checklist de verificação

1. [ ] `docker compose up -d` — Postgres healthy
2. [ ] Backend inicia sem erro de conexão JDBC
3. [ ] `GET /api/localidades` retorna Dom Bosco e Camaçari
4. [ ] `POST /api/impressoras` cadastra impressora
5. [ ] `POST /api/impressoras/movimentar` altera setor/status e cria histórico
6. [ ] `GET /api/movimentacoes/historico/1` lista movimentações
7. [ ] `GET /api/impressoras/dashboard` retorna totais
8. [ ] `POST /api/importacao/csv` importa arquivo de exemplo
9. [ ] Frontend dashboard exibe cards
10. [ ] Lista com busca/filtros funciona
11. [ ] Modal de movimentação atualiza impressora
12. [ ] Timeline exibe histórico vertical
13. [ ] Importação CSV na UI reporta erros por linha
14. [ ] Duplicar IP ativo retorna erro 409
15. [ ] CORS permite localhost:4200

## Licença

Uso interno.
# Gerenciador-Impressoras
# Gerenciador-Impressoras
