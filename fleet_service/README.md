# fleet_service

Zynolo Fleet microservice scaffold for transport management (mobile-first pilot).

## Tech Stack
- Spring Boot 3.5.7
- Spring Web
- Spring Data JPA
- Spring Validation
- Spring Cloud OpenFeign
- MySQL (`zynolo_nexus` shared database)

## Package
- `com.zynolo_nexus.fleet_service`

## Entry Point
- `com.zynolo_nexus.fleet_service.FleetServiceApplication`

## Default Port
- `8096`

## Docker (local)

From repo root:

```bash
docker compose -f fleet_service/docker-compose.yml build
docker compose -f fleet_service/docker-compose.yml up -d
```

Or from `fleet_service/`:

```bash
docker compose build
docker compose up -d
```

Uses shared MySQL from `application.properties` (same as other domain services).

## API base
- `/api/v1/fleet/**` (routed via `api-gateway`)

## Planned integrations
- `auth_service` for auth/authorization (role page-task privileges)
- `setting_service` for master/reference data integration

## Health
- `POST /api/v1/fleet/health`

## FLVM - Vehicle Master Endpoints
Page code: `FLVM` (section `FLEET_MASTER`)

- `POST /api/v1/fleet/vehicles/reference-data`
- `POST /api/v1/fleet/vehicles/filter-list`
- `POST /api/v1/fleet/vehicles/add`
- `POST /api/v1/fleet/vehicles/view`
- `POST /api/v1/fleet/vehicles/update`
- `POST /api/v1/fleet/vehicles/status-update`

Notes:
- Reference-data privileges: `username` → role lookup (same as PO/cheque).
- Scoped by `X-Company-Id` (falls back to `app.default.company-id`)
- `spring.jpa.hibernate.ddl-auto=update` (same as other domain services; creates `fleet_vehicles` only)
- Duplicate registration rejected within the same company (normalized: ignore spaces/dashes, case-insensitive)
- Status values: `ACTIVE`, `INACTIVE`
