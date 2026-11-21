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

## FLNR - New Request Endpoints
Page code: `FLNR` (section `FLEET_REQUEST`)

- `POST /api/v1/fleet/request-create/reference-data`
- `POST /api/v1/fleet/request-create/filter-list`
- `POST /api/v1/fleet/request-create/add`
- `POST /api/v1/fleet/request-create/view`
- `POST /api/v1/fleet/request-create/update`
- `POST /api/v1/fleet/request-create/submit`

Notes:
- Creates `fleet_transport_requests` via `ddl-auto=update`.
- `add` creates status `DRAFT`; `submit` moves `DRAFT` → `SUBMITTED` (visible later in Request Queue).
- `view` / `update` / `filter-list` / `submit` are scoped to the requester (`username`).
- Only `DRAFT` can be updated or submitted.
- Statuses: `DRAFT`, `SUBMITTED`, `CANCELLED`.

## FLMR - My Requests Endpoints
Page code: `FLMR` (section `FLEET_REQUEST`)

- `POST /api/v1/fleet/my-requests/reference-data`
- `POST /api/v1/fleet/my-requests/filter-list`
- `POST /api/v1/fleet/my-requests/view`
- `POST /api/v1/fleet/my-requests/cancel`

Notes:
- Own requests only (`username` = requester).
- Cancel allowed for `DRAFT` or `SUBMITTED` → `CANCELLED`.
- Tasks: VIEW, SEARCH, CANCEL.

## FLRQ - Request Queue Endpoints
Page code: `FLRQ` (section `FLEET_REQUEST`)

- `POST /api/v1/fleet/request-queue/reference-data`
- `POST /api/v1/fleet/request-queue/filter-list`
- `POST /api/v1/fleet/request-queue/view`
- `POST /api/v1/fleet/request-queue/update`

Notes:
- Company-wide queue (not requester-scoped).
- `filter-list` defaults to `SUBMITTED` when `search.status` is empty.
- `update` sets `dispatcherNotes` on `SUBMITTED` only (vehicle/driver assign is Scheduling).
- Tasks: VIEW, SEARCH, UPDATE.

## FLAT - Assign Trip Endpoints
Page code: `FLAT` (section `FLEET_SCHEDULING`)

- `POST /api/v1/fleet/assign-trip/reference-data`
- `POST /api/v1/fleet/assign-trip/filter-list` (SUBMITTED requests only)
- `POST /api/v1/fleet/assign-trip/view-request`
- `POST /api/v1/fleet/assign-trip/add`
- `POST /api/v1/fleet/assign-trip/view`
- `POST /api/v1/fleet/assign-trip/update`

Notes:
- Creates `fleet_trips` via `ddl-auto=update`.
- `add` assigns ACTIVE vehicle + driver, creates trip `ASSIGNED`, sets request status `ASSIGNED`.
- One trip per transport request.
- Vehicle capacity must be >= request passenger count.
- `update` reassigns vehicle/driver while trip is still `ASSIGNED`.
- Tasks: ADD, VIEW, SEARCH, UPDATE.

## FLTS - Today's Schedule Endpoints
Page code: `FLTS` (section `FLEET_SCHEDULING`)

- `POST /api/v1/fleet/today-schedule/reference-data`
- `POST /api/v1/fleet/today-schedule/filter-list`
- `POST /api/v1/fleet/today-schedule/view`
- `POST /api/v1/fleet/today-schedule/update`

Notes:
- Lists trips by `scheduledPickupAt` for `search.scheduleDate` (defaults to today).
- `update` reassigns vehicle/driver while trip is `ASSIGNED`.
- Tasks: VIEW, SEARCH, UPDATE.

## FLDA - My Assignments Endpoints
Page code: `FLDA` (section `FLEET_DRIVER_OPS`)

- `POST /api/v1/fleet/my-assignments/reference-data`
- `POST /api/v1/fleet/my-assignments/filter-list`
- `POST /api/v1/fleet/my-assignments/view`

Notes:
- Resolves driver via `fleet_drivers.linked_username` = request `username`.
- Lists trips for that driver on `search.scheduleDate` (defaults to today).
- Trip statuses: `ASSIGNED`, `STARTED`, `COMPLETED`, `CANCELLED`.
- Tasks: VIEW, SEARCH.

## FLTSU - Trip Status Update Endpoints
Page code: `FLTSU` (section `FLEET_DRIVER_OPS`)

- `POST /api/v1/fleet/trip-status/reference-data`
- `POST /api/v1/fleet/trip-status/filter-list`
- `POST /api/v1/fleet/trip-status/view`
- `POST /api/v1/fleet/trip-status/status-update`

Notes:
- Same driver link as FLDA (`linkedUsername`).
- `filter-list` defaults to actionable statuses `ASSIGNED` + `STARTED` for the day.
- Transitions: `ASSIGNED` → `STARTED` (sets `startedAt`), `STARTED` → `COMPLETED` (sets `completedAt`).
- Tasks: VIEW, SEARCH, UPDATE.

## FLDI - Incidents Endpoints
Page code: `FLDI` (section `FLEET_DRIVER_OPS`)

- `POST /api/v1/fleet/incidents/reference-data`
- `POST /api/v1/fleet/incidents/filter-list`
- `POST /api/v1/fleet/incidents/add`
- `POST /api/v1/fleet/incidents/view`

Notes:
- Same driver link as FLDA (`linkedUsername`).
- Report only on own trips in `ASSIGNED`, `STARTED`, or `COMPLETED`.
- Creates `fleet_trip_incidents` via `ddl-auto=update`; new incidents are `OPEN`.
- Types: `BREAKDOWN`, `ACCIDENT`, `DELAY`, `PASSENGER`, `OTHER`.
- Severity: `LOW`, `MEDIUM`, `HIGH`.
- Tasks: ADD, VIEW, SEARCH.

## FLLT - Live Trip Track Endpoints
Page code: `FLLT` (section `FLEET_TRACKING`)

- `POST /api/v1/fleet/live-track/reference-data`
- `POST /api/v1/fleet/live-track/filter-list`
- `POST /api/v1/fleet/live-track/view`

### WebSocket (STOMP) — live push
- Endpoint: `ws://<gateway>:8099/api/v1/fleet/ws` (SockJS: same path)
- Auth: `Authorization: Bearer <jwt>` or query `?access_token=<jwt>`
- Subscribe: `/topic/trips/{tripId}/location`
- Event published on each successful **FLLP** `/add`

Notes:
- Company-wide trackable trips (`STARTED` / `ASSIGNED` by default).
- `view` returns last location + trail (up to 100 points) + `signalLost` (no ping for 30s).
- Prefer WebSocket for the map marker; keep `view` for initial load / reconnect.
- Simple in-memory broker (single fleet instance). Add Redis broker relay when scaling out.
- Tasks: VIEW, SEARCH.

## FLLP - Location Ping Endpoints
Page code: `FLLP` (section `FLEET_TRACKING`)

- `POST /api/v1/fleet/location-ping/reference-data`
- `POST /api/v1/fleet/location-ping/add`

Notes:
- Driver GPS ingest (REST). Requires `linkedUsername`. Trip must be `STARTED`.
- Updates trip last-location fields and appends `fleet_trip_locations` history.
- Stale `recordedAt` (older than last) is stored in history but does not overwrite last location.
- Tasks: ADD, VIEW.
- Next production step: WebSocket push + Redis pub/sub on top of this model.

## FLMT - My Trips Endpoints
Page code: `FLMT` (section `FLEET_PASSENGER`)

- `POST /api/v1/fleet/my-trips/reference-data`
- `POST /api/v1/fleet/my-trips/filter-list`
- `POST /api/v1/fleet/my-trips/view`

Notes:
- Passenger/employee scope: trips linked to transport requests where `requesterUsername` = login `username`.
- `filter-list` search: optional `scheduleDate`, `tripNumber`, `requestNumber`, `status` (no default date — all own trips).
- Default sort: `scheduledPickupAt` DESC.
- Trip statuses: `ASSIGNED`, `STARTED`, `COMPLETED`, `CANCELLED`.
- Tasks: VIEW, SEARCH.

## FLTD - Trip Detail Endpoints
Page code: `FLTD` (section `FLEET_PASSENGER`)

- `POST /api/v1/fleet/trip-detail/reference-data`
- `POST /api/v1/fleet/trip-detail/view`

Notes:
- Same ownership as FLMT (`requesterUsername` = login `username`).
- `view` returns full trip + request fields, last location, trail (default 50 / max 100), `signalLost` (30s), and `websocketTopic`.
- Prefer WebSocket for live marker while trip is `STARTED`; use `view` for initial load / reconnect.
- Tasks: VIEW.

## FLTF - Trip Fare Endpoints
Page code: `FLTF` (section `FLEET_PAYMENT`)

- `POST /api/v1/fleet/trip-fare/reference-data`
- `POST /api/v1/fleet/trip-fare/filter-list`
- `POST /api/v1/fleet/trip-fare/add`
- `POST /api/v1/fleet/trip-fare/view`
- `POST /api/v1/fleet/trip-fare/update`

Notes:
- Creates `fleet_trip_fares` via `ddl-auto=update`.
- `add` only on trip status `COMPLETED`; one fare per trip; generates `FTF-yyyyMMdd-####`.
- Payment methods: `CASH`, `CARD`, `COMPANY`, `OTHER`. Currency: `LKR` (default) or `USD`.
- Statuses: `RECORDED`, `CANCELLED`.
- `update` allowed while `RECORDED` (amount / currency / method / notes; optional `status=CANCELLED`).
- Company-wide (dispatcher / owner). Tasks: ADD, VIEW, SEARCH, UPDATE.
