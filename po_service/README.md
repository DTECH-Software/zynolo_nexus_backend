# po_service

Independent microservice scaffold for purchase order module.

## Tech Stack
- Spring Boot 3.5.7
- Spring Web
- Spring Data JPA
- Spring Validation
- Spring Cloud OpenFeign
- MySQL

## Package
- `com.zynolo_nexus.po_service`

## Entry Point
- `com.zynolo_nexus.po_service.PoServiceApplication`

## Default Port
- `8094`

## Starter Endpoints
- `POST /api/v1/po/health`
- `POST /api/v1/po/request-create/reference-data`
- `POST /api/v1/po/request-create`
- `POST /api/v1/po/request-create/view`
- `POST /api/v1/po/request-create/update`
- `POST /api/v1/po/request-create/submit`
- `POST /api/v1/po/request-create/filter-list`

## Planned integrations
- `auth_service` for auth/authorization
- `setting_service` for shared master data
- `api-gateway` for routing
