# Zynolo Nexus Dev Deployment

This folder contains a simple GitHub Actions + systemd + Nginx setup for dev.

## GitHub Secrets

Set these in your GitHub repo secrets:

HIIII

- `DEPLOY_HOST` = `45.79.125.85`
- `DEPLOY_PORT` = `231`
- `DEPLOY_USER` = `dtech`
- `DEPLOY_SSH_KEY` = your private SSH key (PEM format)
- `DEPLOY_APP_DIR` = `/opt/zynolo-nexus`

## Server setup (one-time)

1. Create folders:

```bash
sudo mkdir -p /opt/zynolo-nexus/auth_service
sudo mkdir -p /opt/zynolo-nexus/setting_service
sudo mkdir -p /opt/zynolo-nexus/api-gateway
sudo chown -R dtech:dtech /opt/zynolo-nexus
```

2. Install systemd services:

```bash
sudo cp deploy/systemd/auth_service.service /etc/systemd/system/auth_service.service
sudo cp deploy/systemd/setting_service.service /etc/systemd/system/setting_service.service
sudo cp deploy/systemd/api-gateway.service /etc/systemd/system/api-gateway.service
sudo systemctl daemon-reload
sudo systemctl enable auth_service setting_service api-gateway
```

3. Nginx config:

```bash
sudo cp deploy/nginx/zynolo_nexus_dev.conf /etc/nginx/sites-available/zynolo_nexus_dev.conf
sudo ln -s /etc/nginx/sites-available/zynolo_nexus_dev.conf /etc/nginx/sites-enabled/zynolo_nexus_dev.conf
sudo nginx -t
sudo systemctl reload nginx
```

## Deploy

Push to `main` and GitHub Actions will:
- build the jars
- upload to server
- restart systemd services

## Notes

- Change `User=dtech` in systemd units if your server user differs.
- Ensure the ports match your `application.properties`.
