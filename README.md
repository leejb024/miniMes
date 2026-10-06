# miniMes

A manufacturing execution system for a small shop floor. It covers master data, purchase receiving, inventory, production, and WMS inbound plans in one flow.

## Features

- **Master data**: partners, warehouses, items, processes, equipment, BOM
- **Purchase**: inbound registration
- **Inventory**: stock inquiry, material handover, material receipt
- **Production**: work orders and production results (mixing, packing, work completion)
- **WMS**: inbound plans created after a work order is completed

Completing a work order confirms the production results and sends them to the inbound plan list through a transfer queue.

## Tech stack

| Area | Used |
| --- | --- |
| Backend | Java 17, Spring Boot 4.1, Spring Security, Spring Data JPA |
| Frontend | Vue 3, Vue Router, Vite, Axios |
| Database | PostgreSQL |
| Auth | JWT |
| API docs | springdoc OpenAPI |

## Requirements

- JDK 17
- Node.js 18 or later
- PostgreSQL
- Maven Wrapper is included, so a separate Maven install is not required.

## Run locally

### 1. Database

Connection settings in `src/main/resources/application.yaml`:

- Host: `localhost`
- Port: `5434`
- Database: `sdmes`
- User / Password: `mesuser` / `mesuser`

Start PostgreSQL with the same settings using Docker:

```bash
docker run --name minimes-postgres -e POSTGRES_DB=sdmes -e POSTGRES_USER=mesuser -e POSTGRES_PASSWORD=mesuser -p 5434:5432 -d postgres:16
```

Hibernate `ddl-auto: update` creates and updates tables when the application starts.

### 2. Backend

```bash
./mvnw spring-boot:run
```

On Windows, use `mvnw.cmd`.

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

On first startup, an admin account is created if it does not already exist.

- ID: `admin`
- Password: `admin123`

This account and the JWT secret in `application.yaml` are for local development. Change them before deploying.

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

App: http://localhost:5173

The dev server proxies `/api` to `http://localhost:8080`. Start the backend first, then sign in.

## Project structure

```text
miniMes/
├── frontend/                 Vue UI
├── src/main/java/minimes/
│   ├── auth/                 Sign-in
│   ├── master/               Master data
│   ├── purchase/             Purchase receiving
│   ├── stock/                Inventory
│   ├── production/           Work orders and results
│   ├── wms/                  Inbound plans
│   └── interfaces/           WMS transfer queue
└── src/main/resources/
    └── application.yaml
```

## Deployment

A push to `main` deploys to EC2 through `.github/workflows/deploy.yml`. The repository needs these secrets:

- `EC2_HOST`
- `EC2_USER`
- `EC2_SSH_KEY`

The workflow builds the frontend into Spring Boot static resources, then starts the backend with `java -jar`. Point the EC2 PostgreSQL settings at the server `application.yaml` or the runtime environment.
