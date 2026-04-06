# Deployment & Development Setup Guides

Use the platform-specific guide below. Dev setup is now kept inside the Windows/Linux deployment docs, not in a separate `docs/development/` folder.

## Guides

| File | Purpose |
|---|---|
| [VPS-FRESH-UBUNTU-SETUP.md](VPS-FRESH-UBUNTU-SETUP.md) | Fresh Ubuntu VPS deployment |
| [DEV-SETUP-WINDOWS.md](DEV-SETUP-WINDOWS.md) | Windows development setup |
| [DEV-SETUP-LINUX.md](DEV-SETUP-LINUX.md) | Linux development setup |

## Current local flow

1. Copy `.env.example` to `.env`
2. Start services with `docker compose up -d`
3. Run the app with `mvn spring-boot:run` or from your IDE
4. Open MinIO console at `http://localhost:9001`

## Notes

- Use `docker compose --profile with-db up -d` only if you want the Docker MariaDB too.
- If MinIO cannot write storage, create `data\minio` (Windows) or `data/minio` (Linux) and restart the container.
- The canonical signature bucket is `approval-signatures`; the app should auto-create it, but manual fallback must use the same name.
