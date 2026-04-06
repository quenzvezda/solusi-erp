# Development Setup Guide: Windows (Local Development Environment)

> **Target**: Windows developer machine (Windows 10/11) dengan development tools
> **Outcome**: Complete dev environment with MinIO, MariaDB, Spring Boot for local testing
> **Time**: ~30-45 min

## Current repo flow (Docker Compose)

This project now uses Docker Compose for local services, so the fastest path is:

1. Install **Git**, **Java 21**, and **Docker Desktop**
2. Copy `.env.example` to `.env`
3. Create the storage folders if they do not exist:
   ```powershell
   New-Item -ItemType Directory -Force data\minio, data\mariadb | Out-Null
   ```
4. Start services:
   ```powershell
   docker compose up -d
   ```
5. If you want the Docker MariaDB too:
   ```powershell
   docker compose --profile with-db up -d
   ```
6. Run the app:
   ```powershell
   mvn spring-boot:run
   ```
7. Open MinIO console: `http://localhost:9001`
   - Login: `minioadmin / minioadmin`
   - API: `http://localhost:9000`

If MinIO returns 401 or storage errors, restart after the `data\minio` folder exists.

## Bucket setup

The app should create the bucket automatically on first startup if it does not exist.

**Canonical bucket name:** `approval-signatures`

If auto-create fails and you need to provision it manually:
```powershell
# If mc is not installed yet, download it first:
# Invoke-WebRequest -Uri https://dl.min.io/client/mc/release/windows-amd64/mc.exe -OutFile mc.exe
# .\mc.exe alias set erp-minio http://localhost:9000 minioadmin minioadmin

# Point mc to local MinIO
mc alias set erp-minio http://localhost:9000 minioadmin minioadmin

# Create the bucket expected by the ERP app
mc mb --ignore-existing erp-minio/approval-signatures

# Verify
mc ls erp-minio
```

If you use a different bucket name, update `.env` and Spring Boot config to match exactly.

---

## Prerequisites

- [ ] Windows 10 or Windows 11
- [ ] ~20GB free disk space
- [ ] Administrator access
- [ ] Git installed (or download from git-scm.com)
- [ ] Internet connectivity

---

## Option A: Full Setup (Recommended for Active Development)

### Phase 1: Install Java 21

#### 1.1 Download & Install OpenJDK 21

```
Visit: https://adoptium.net/
1. Select Java 21 (LTS)
2. Select Windows x64
3. Download .msi file
4. Run installer (next, next, finish)
5. Default location: C:\Program Files\Java\jdk-21.x.x
```

#### 1.2 Verify Installation

```bash
# Open PowerShell and run:
java -version
# Expected: openjdk version "21.x.x"

javac -version
# Expected: javac 21.x.x

# If not found, add to PATH:
# Right-click "This PC" → Properties → Advanced System Settings
# → Environment Variables → Add JAVA_HOME
# Value: C:\Program Files\Java\jdk-21.x.x
```

### Phase 2: Install MariaDB (Windows)

#### 2.1 Download MariaDB Installer

```
Visit: https://mariadb.org/download/
1. Select latest stable version (11.x or 10.11)
2. Download Windows MSI installer
3. Run installer
```

#### 2.2 Run MariaDB Setup

```
Installer steps:
1. Accept license
2. Choose setup type: Developer Default
3. Installation directory: C:\Program Files\MariaDB 11.x (default)
4. Configuration:
   - Port: 3306 (default)
   - Database character set: utf8mb4
   - Check "Install as Service"
   - Service name: MariaDB
5. Configure MariaDB Server:
   - Collation: utf8mb4_unicode_ci
   - Port: 3306
   - Enable TCP/IP
6. Set root password (remember this!)
7. Finish
```

#### 2.3 Verify Installation

```bash
# Open PowerShell as Administrator:
mysql -u root -p
# Enter password when prompted

# In MySQL prompt:
CREATE DATABASE solusi_erp_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'erp_user'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON solusi_erp_db.* TO 'erp_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;

# Test connection
mysql -u erp_user -p -e "SELECT DATABASE();"
# Enter password when prompted
```

### Phase 3: Setup MinIO for Windows Development

#### 3.1 Download MinIO Windows Binary

```bash
# Create directory for MinIO
mkdir "C:\minio"
cd C:\minio

# Download MinIO binary (via PowerShell)
$url = "https://dl.min.io/server/minio/release/windows-amd64/minio.exe"
Invoke-WebRequest -Uri $url -OutFile minio.exe

# Verify download
ls minio.exe
```

#### 3.2 Create Storage Directory

```bash
# Create storage directory
mkdir "C:\erp-storage"
mkdir "C:\erp-storage\approval-signatures"

# Verify
ls "C:\erp-storage"
```

#### 3.3 Create MinIO Batch File (Auto-Start)

```batch
# Create file: C:\minio\start-minio.bat

@echo off
REM MinIO Server for Development
cd /d "C:\minio"
setlocal enabledelayedexpansion

REM Set environment variables
set MINIO_ACCESS_KEY=minioadmin
set MINIO_SECRET_KEY=minioadmin
set MINIO_REGION=us-east-1

REM Run MinIO
echo Starting MinIO server...
minio.exe server "C:\erp-storage"

pause
```

#### 3.4 Create Windows Task Scheduler Entry (Optional Auto-Start)

```batch
# In PowerShell (as Administrator):
# This will run MinIO automatically on login

$TaskName = "MinIO-Dev-Server"
$TaskPath = "C:\minio\start-minio.bat"

# Create trigger (at login)
$Trigger = New-ScheduledTaskTrigger -AtLogon

# Create action
$Action = New-ScheduledTaskAction -Execute $TaskPath -WorkingDirectory "C:\minio"

# Register task
Register-ScheduledTask -TaskName $TaskName -Trigger $Trigger -Action $Action -Force

# Verify
Get-ScheduledTask -TaskName $TaskName
```

#### 3.5 Start MinIO Manually (Development)

```batch
# Double-click C:\minio\start-minio.bat
# OR from PowerShell:

cd C:\minio
$env:MINIO_ACCESS_KEY="minioadmin"
$env:MINIO_SECRET_KEY="minioadmin"
.\minio.exe server "C:\erp-storage"

# MinIO will start on http://localhost:9000
```

#### 3.6 Verify MinIO Running

```bash
# In new PowerShell/CMD window:
curl http://localhost:9000/minio/health/live

# Expected: {"status":"ok"} or similar

# Or visit in browser:
http://localhost:9000/minio/health/live
```

### Phase 4: Configure Spring Boot Application

#### 4.1 Create application-dev.properties

```properties
# In repo: src/main/resources/application-dev.properties

# Database Configuration (Local MariaDB)
spring.datasource.url=jdbc:mariadb://localhost:3306/solusi_erp_db
spring.datasource.username=erp_user
spring.datasource.password=your_secure_password
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MariaDBDialect

# Flyway Migrations
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

# Server
server.port=8080
server.servlet.context-path=/

# Logging
logging.level.root=INFO
logging.level.com.solusi.erp=DEBUG
logging.level.org.springframework.web=DEBUG

# MinIO Storage (Local)
storage.provider=minio
storage.minio.endpoint=http://localhost:9000
storage.minio.access-key=minioadmin
storage.minio.secret-key=minioadmin
storage.minio.bucket=approval-signatures
storage.minio.region=us-east-1
storage.minio.secure=false
storage.minio.max-file-size=5242880
storage.minio.expiration-minutes=15
```

#### 4.2 Add to application.yaml (if used)

```yaml
# In src/main/resources/application.yaml
# Or use environment variables to override

spring:
  profiles:
    active: dev

storage:
  provider: minio
  minio:
    endpoint: http://localhost:9000
    access-key: minioadmin
    secret-key: minioadmin
    bucket: approval-signatures
    region: us-east-1
    secure: false
```

### Phase 5: Run Application in IDE

#### 5.1 Using IntelliJ IDEA (Recommended)

```
1. Open project in IntelliJ
2. File → Project Structure → Project
   - SDK: Java 21
   - Language level: 21
3. Run → Edit Configurations
   - Add new "Spring Boot" configuration
   - Main class: com.solusi.erp.SolusiProgramErpApplication
   - VM options: -Dspring.profiles.active=dev
   - Working directory: $PROJECT_DIR$
4. Run → Run 'ERP-Dev'
```

#### 5.2 Using Eclipse

```
1. Import project as Maven project
2. Right-click project → Properties
   - Java Build Path: Set JRE to Java 21
   - Project Facets: Check Java version 21
3. Right-click project → Run As → Spring Boot App
4. Or: Run → Run Configurations → Spring Boot App
   - Main class: com.solusi.erp.SolusiProgramErpApplication
   - Arguments: --spring.profiles.active=dev
```

#### 5.3 Using Maven Command Line

```bash
# From project root
.\mvnw clean spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Or simpler:
.\mvnw clean spring-boot:run

# Maven will auto-detect active profile from application.yaml
```

### Phase 6: Verify Local Development Setup

```bash
# Test database connection
mysql -u erp_user -p solusi_erp_db -e "SELECT 1;"

# Test MinIO running
curl http://localhost:9000/minio/health/live

# Test Spring Boot running
curl http://localhost:8080/health

# Expected: {"status":"UP"}

# Test MariaDB from app
# Check logs in IDE console - should show successful connection
```

---

## Option B: Simplified Setup (Using Docker Desktop)

If you prefer Docker (easier but requires Docker Desktop):

### Phase 1: Install Docker Desktop

```
Visit: https://www.docker.com/products/docker-desktop
1. Download Docker Desktop for Windows
2. Run installer
3. Restart computer
4. Verify: docker --version
```

### Phase 2: Create docker-compose.yml

```yaml
# In project root: docker-compose.dev.yml

version: '3.8'

services:
  mariadb:
    image: mariadb:latest
    container_name: erp-mariadb-dev
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: solusi_erp_db
      MYSQL_USER: erp_user
      MYSQL_PASSWORD: erp_password
    ports:
      - "3306:3306"
    volumes:
      - erp-db:/var/lib/mysql
    networks:
      - erp-dev

  minio:
    image: minio/minio:latest
    container_name: erp-minio-dev
    environment:
      MINIO_ACCESS_KEY: minioadmin
      MINIO_SECRET_KEY: minioadmin
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - erp-storage:/data
    command: server /data --console-address ":9001"
    networks:
      - erp-dev

volumes:
  erp-db:
  erp-storage:

networks:
  erp-dev:
```

### Phase 3: Start Services

```bash
# Start all services
docker-compose -f docker-compose.dev.yml up -d

# Verify running
docker-compose -f docker-compose.dev.yml ps

# View logs
docker-compose -f docker-compose.dev.yml logs -f

# Stop services
docker-compose -f docker-compose.dev.yml down
```

### Phase 4: Connect Application

```properties
# application-dev.properties

# Connect to Docker containers
spring.datasource.url=jdbc:mariadb://localhost:3306/solusi_erp_db
spring.datasource.username=erp_user
spring.datasource.password=erp_password

storage.minio.endpoint=http://localhost:9000
storage.minio.access-key=minioadmin
storage.minio.secret-key=minioadmin
```

---

## Option C: Quick Start (SQL Server + External MinIO)

For minimal setup:

```bash
# 1. Install Java 21 only (from Phase 1 above)
# 2. Install MariaDB only (from Phase 2 above)
# 3. Connect to your VPS MinIO

# application-dev.properties
storage.minio.endpoint=http://your-vps-ip:9000
storage.minio.access-key=minioadmin
storage.minio.secret-key=minioadmin
```

---

## Common Tasks

### Starting Development Session

```bash
# 1. Start MariaDB (if not auto-starting)
net start MariaDB

# 2. Start MinIO (double-click start-minio.bat or run PowerShell command)

# 3. Run application in IDE
# Or: .\mvnw clean spring-boot:run

# 4. Access at http://localhost:8080
```

### Testing Signature Upload

```bash
# 1. Create dummy signature file
PS> Add-Content -Path C:\test-signature.png -Value ""

# 2. Upload via curl
curl -X POST http://localhost:8080/api/approvals/signatures/upload `
  -F "signature=@C:\test-signature.png" `
  -F "approvalRequestId=1"

# 3. Verify in MinIO
ls "C:\erp-storage\approval-signatures"
```

### Database Operations

```bash
# Login to MySQL
mysql -u erp_user -p solusi_erp_db

# Common queries
SHOW TABLES;
SELECT * FROM appr_signatures;
DESCRIBE appr_signatures;

# Backup
mysqldump -u erp_user -p solusi_erp_db > backup.sql

# Restore
mysql -u erp_user -p solusi_erp_db < backup.sql
```

### Viewing Logs

```bash
# IDE console output

# Or file-based logs (if configured)
Get-Content "logs/application.log" -Tail 50

# Or MinIO logs
Get-Content "C:\minio\minio.log" -Tail 50
```

---

## Troubleshooting

### Issue: "Port 3306 already in use"

```bash
# Find process using port
netstat -ano | findstr :3306

# Kill process (if safe)
taskkill /PID <PID> /F

# Or restart MariaDB service
net stop MariaDB
net start MariaDB
```

### Issue: "Cannot connect to database"

```bash
# Verify MariaDB running
Get-Service MariaDB | Select-Object Status

# Check connection
mysql -u root -p -h localhost

# Verify credentials in application.properties
```

### Issue: "MinIO not accessible"

```bash
# Verify MinIO process running
Get-Process minio* 

# Check if port 9000 available
netstat -ano | findstr :9000

# Restart MinIO
# Kill process and restart start-minio.bat
```

### Issue: "Out of memory"

```bash
# Increase Java heap size
# In IDE run configuration: VM options: -Xmx2048m

# Or in Maven:
.\mvnw -DargLine="-Xmx2048m" spring-boot:run
```

---

## Performance Tips

1. **Use SSD**: Development on SSD significantly faster than HDD
2. **Allocate RAM**: Allocate 2-4GB to MariaDB and MinIO combined
3. **Exclude from Antivirus**: Exclude C:\minio and database folder from antivirus scanning
4. **Clean up databases**: Periodically delete test data to keep database size small
5. **Use IDE debugging**: IntelliJ/Eclipse debugging faster than print logging

---

## IDE Recommendations

### IntelliJ IDEA (Recommended)

```
Plugins to install:
- Spring Boot Assistant
- MariaDB  
- Database Navigator
- REST Client

Settings:
- Build: Maven 3.8+
- Java: 21 LTS
- Compiler: Enable annotation processing
```

### Visual Studio Code

```
Extensions:
- Extension Pack for Java
- Spring Boot Extension Pack
- REST Client
- Database Client

Launch configuration (.vscode/launch.json):
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "ERP-Dev",
      "request": "launch",
      "mainClass": "com.solusi.erp.SolusiProgramErpApplication",
      "args": "--spring.profiles.active=dev"
    }
  ]
}
```

---

## Quick Reference

```bash
# Start services
net start MariaDB              # Start MariaDB
C:\minio\start-minio.bat       # Start MinIO

# Stop services
net stop MariaDB
# Close MinIO window or taskkill /F /IM minio.exe

# Database
mysql -u erp_user -p solusi_erp_db
mysqldump -u erp_user -p solusi_erp_db > backup.sql

# Build & Run
.\mvnw clean compile                                        # Compile
.\mvnw clean spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"  # Run
.\mvnw clean test                                          # Test

# Check ports
netstat -ano | findstr :8080   # Spring Boot
netstat -ano | findstr :9000   # MinIO
netstat -ano | findstr :3306   # MariaDB
```

---

## Checklist

```
Option A: Full Setup
☐ Java 21 installed
☐ MariaDB installed & running
☐ Database created
☐ MinIO downloaded
☐ Storage directory created
☐ start-minio.bat created
☐ application-dev.properties created
☐ Spring Boot runs and connects to services
☐ Can upload signature files

Option B: Docker Setup
☐ Docker Desktop installed
☐ docker-compose.dev.yml created
☐ Services running: docker-compose ps
☐ application-dev.properties updated
☐ Spring Boot connects to Docker containers

Option C: Quick Start
☐ Java 21 installed
☐ MariaDB installed locally
☐ VPS MinIO endpoint configured
☐ Ready to develop
```

---

## Next Steps

1. Choose Option A, B, or C
2. Follow setup steps
3. Verify services running
4. Run application
5. Start coding!

**Happy coding! 🚀**

---

*Last Updated: 2026-04-04*
*For: Solusi Program ERP Development*
*Guide: Windows Local Development Environment*
