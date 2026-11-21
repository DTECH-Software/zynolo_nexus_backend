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

## Swagger UI (live contract)

Use **api-gateway** only (same as the UI and mobile apps):

- **Swagger UI:** `http://<gateway-host>:8099/api/v1/fleet/swagger-ui.html`
- **OpenAPI JSON:** `http://<gateway-host>:8099/api/v1/fleet/v3/api-docs`

Example (dev): `http://45.79.125.85:8099/api/v1/fleet/swagger-ui.html`

Opening the docs is unauthenticated (GET). Next to **Authorize**, use the green **Login** button (calls existing `POST /api/v1/auth/login` via gateway — not a fleet API). Then use FLVM/FLDM **Try it out** (`username` in body must match token; `X-Company-Id` from JWT).

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

## FLDM - Driver Master Endpoints
Page code: `FLDM` (section `FLEET_MASTER`)

- `POST /api/v1/fleet/drivers/reference-data`
- `POST /api/v1/fleet/drivers/filter-list`
- `POST /api/v1/fleet/drivers/add`
- `POST /api/v1/fleet/drivers/view`
- `POST /api/v1/fleet/drivers/update`
- `POST /api/v1/fleet/drivers/status-update`

Notes:
- Reference-data includes active vehicles for `usualVehicleId` and privileges via `username`.
- Duplicate mobile rejected within the same company (normalized: spaces/dashes stripped).
- `usualVehicleId` must belong to the same company when provided.
- Availability: `AVAILABLE`, `UNAVAILABLE`; status: `ACTIVE`, `INACTIVE`.
