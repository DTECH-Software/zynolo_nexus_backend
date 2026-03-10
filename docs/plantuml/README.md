# Zynolo System Flow (Brick Diagrams)

These diagrams split the full backend flow into small, readable blocks.

## Files

- `01-context.puml`
- `02-auth-login-reset.puml` (overview)
- `02a-auth-login.puml`
- `02b-auth-forgot-password.puml`
- `02c-auth-verify-reset-otp.puml`
- `02d-auth-reset-password.puml`
- `02e-auth-change-password.puml`
- `03-company-switch-context.puml`
- `04-dashboard-subscription.puml`
- `05-user-management.puml` (overview)
- `05a-user-reference-data.puml`
- `05b-user-add.puml`
- `05c-user-view.puml`
- `05d-user-update-status.puml`
- `05e-user-filter-list.puml`
- `05f-user-profile-image.puml`
- `06-role-access.puml` (overview)
- `06a-role-modules-part1.puml`
- `06b-role-modules-part2.puml`
- `06c-role-page-tasks-part1.puml`
- `06d-role-page-tasks-part2.puml`
- `07-master-management.puml` (overview)
- `07a-company-management.puml`
- `07b-module-management.puml`
- `07c-section-management.puml`
- `07d-page-management.puml`
- `07e-task-management.puml`
- `07f-role-and-policy-management.puml`
- `07g-user-company-management.puml`
- `07h-company-module-subscription.puml`
- `08-security-audit.puml`
- `09-cheque-service.puml` (overview)
- `09a-cheque-company-management.puml`
- `09b-cheque-customer-management.puml`
- `09c-cheque-bank-management.puml`
- `09d-cheque-voucher-management.puml`
- `09e-cheque-print-reprint.puml`
- `09f-cheque-reprint-approval.puml`

## Render

Use any PlantUML renderer:

```bash
plantuml docs/plantuml/*.puml
```
