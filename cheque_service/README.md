# cheque_service

Independent microservice scaffold for cheque module.

## Tech Stack
- Spring Boot 3.5.7
- Spring Web
- Spring Data JPA
- Spring Validation
- Spring Cloud OpenFeign
- MySQL

## Package
- `com.zynolo_nexus.cheque_service`

## Entry Point
- `com.zynolo_nexus.cheque_service.ChequeServiceApplication`

## Default Port
- `8093`

## Planned integrations
- `auth_service` for auth/authorization integration
- `setting_service` for master/reference data integration

## CHCM - Cheque Company Management Endpoints
- `POST /api/v1/cheque/companies` (create)
- `POST /api/v1/cheque/companies/view` (view by `id`)
- `POST /api/v1/cheque/companies/update` (update by `id`)
- `POST /api/v1/cheque/companies/status` (status change by `id`)
- `POST /api/v1/cheque/companies/filter-list` (search + paging)
- `POST /api/v1/cheque/companies/reference-data` (default status + privileges)

Note:
- CHCM now uses the shared `companies` table (same company master data used by setting service).

## CHCU - Cheque Customer Management Endpoints
- `POST /api/v1/cheque/customers` (create)
- `POST /api/v1/cheque/customers/view` (view by `id`)
- `POST /api/v1/cheque/customers/update` (update by `id`)
- `POST /api/v1/cheque/customers/status` (status change by `id`)
- `POST /api/v1/cheque/customers/filter-list` (search + paging)
- `POST /api/v1/cheque/customers/reference-data` (default status + privileges)

## CHVM - Cheque Voucher Management Endpoints
- `POST /api/v1/cheque/vouchers` (create)
- `POST /api/v1/cheque/vouchers/view` (view by `id`)
- `POST /api/v1/cheque/vouchers/update` (update by `id`)
- `POST /api/v1/cheque/vouchers/filter-list` (search + paging)
- `POST /api/v1/cheque/vouchers/reference-data` (status + companies + customers + privileges)
- `POST /api/v1/cheque/vouchers/export-pdf` (returns base64 PDF)
- `POST /api/v1/cheque/vouchers/export-pdf-download` (direct PDF file download response)

Note:
- Voucher PDF is now generated using JasperReports template: `reports/cheque-voucher.jrxml`.

### Sample create payload
```json
{
  "channel": "OP",
  "ip": "0.0.0.1",
  "message": "CHEQUE_COMPANY_CREATE",
  "userAgent": "Chrome",
  "username": "superadmin",
  "code": "CHQ_MAIN",
  "description": "Main cheque company",
  "status": "ACTIVE"
}
```
