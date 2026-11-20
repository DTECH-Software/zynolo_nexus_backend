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

## API base
- `/api/v1/fleet/**` (routed via `api-gateway`)

## Planned integrations
- `auth_service` for auth/authorization (role page-task privileges)
- `setting_service` for master/reference data integration

## Health
- `POST /api/v1/fleet/health`
