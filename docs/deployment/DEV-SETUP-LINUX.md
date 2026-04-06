# Development Setup Guide: Linux (Ubuntu/Debian)

> **Target**: Linux developer machine (Ubuntu 20.04+) dengan development tools
> **Outcome**: Complete dev environment with MinIO, MariaDB, Spring Boot for local testing
> **Time**: ~30-45 min

## Current repo flow (Docker Compose)

This project now uses Docker Compose for local services, so the fastest path is:

1. Install **Git**, **Java 21**, and Docker + Compose plugin:
   ```bash
   sudo apt update
   sudo apt install -y git openjdk-21-jdk docker.io docker-compose-plugin
   ```
2. Copy `.env.example` to `.env`
3. Create the storage folders if they do not exist:
   ```bash
   mkdir -p data/minio data/mariadb
   ```
4. Start services:
   ```bash
   docker compose up -d
   ```
5. If you want the Docker MariaDB too:
   ```bash
   docker compose --profile with-db up -d
   ```
6. Run the app:
   ```bash
   mvn spring-boot:run
   ```
7. Open MinIO console: `http://localhost:9001`
   - Login: `minioadmin / minioadmin`
   - API: `http://localhost:9000`

If MinIO returns 401 or storage errors, restart after the `data/minio` folder exists.

## Bucket setup

The app should create the bucket automatically on first startup if it does not exist.

**Canonical bucket name:** `approval-signatures`

If auto-create fails and you need to provision it manually:
```bash
# If mc is not installed yet, download it first:
# curl -LO https://dl.min.io/client/mc/release/linux-amd64/mc
# chmod +x mc && sudo mv mc /usr/local/bin/

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

- [ ] Ubuntu/Debian 20.04 LTS or newer
- [ ] ~20GB free disk space
- [ ] sudo access (or admin user)
- [ ] Internet connectivity
- [ ] Terminal familiarity

---

## Option A: Full Setup (Recommended for Active Development)

### Phase 1: System Preparation

```bash
# Update system
sudo apt update
sudo apt upgrade -y

# Install essential tools
sudo apt install -y \
  build-essential \
  curl \
  wget \
  git \
  unzip \
  net-tools \
  htop \
  jq \
  ca-certificates
```

### Phase 2: Install Java 21

#### 2.1 Install OpenJDK 21

```bash
# Install Java 21
sudo apt install -y openjdk-21-jdk

# Verify installation
java -version
# Expected: openjdk version "21.x.x"

javac -version
# Expected: javac 21.x.x
```

#### 2.2 Set JAVA_HOME (Optional)

```bash
# Find Java installation
update-alternatives --list java

# Add to ~/.bashrc
echo 'export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64' >> ~/.bashrc
source ~/.bashrc

# Verify
echo $JAVA_HOME
```

### Phase 3: Install MariaDB

#### 3.1 Install MariaDB Server

```bash
# Install MariaDB
sudo apt install -y mariadb-server

# Start and enable service
sudo systemctl start mariadb
sudo systemctl enable mariadb

# Verify
sudo systemctl status mariadb
# Expected: Active: active (running)
```

#### 3.2 Secure MariaDB Installation

```bash
# Run security script
sudo mysql_secure_installation

# Prompts (recommended answers):
# - Enter current password: (press Enter)
# - Switch to unix_socket auth: N
# - Change root password: Y
#   New password: [secure_password]
# - Remove anonymous users: Y
# - Disable root login remotely: Y
# - Remove test database: Y
# - Reload privilege tables: Y
```

#### 3.3 Create Application Database

```bash
# Connect as root
sudo mysql -u root

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

### Phase 4: Setup MinIO for Linux Development

#### 4.1 Download MinIO Binary

```bash
# Create directory
mkdir -p ~/minio-dev
cd ~/minio-dev

# Download MinIO
wget https://dl.min.io/server/minio/release/linux-amd64/minio

# Make executable
chmod +x minio

# Verify
./minio --version
# Expected: minio version RELEASE.2026-04-04T...
```

#### 4.2 Create Storage Directory

```bash
# Create storage directory
mkdir -p ~/erp-storage/approval-signatures

# Verify
ls -la ~/erp-storage/
```

#### 4.3 Create MinIO Start Script

```bash
# Create startup script
cat > ~/minio-dev/start-minio.sh << 'EOF'
#!/bin/bash

# MinIO development server

export MINIO_ACCESS_KEY=minioadmin
export MINIO_SECRET_KEY=minioadmin
export MINIO_REGION=us-east-1

cd ~/minio-dev
./minio server ~/erp-storage

EOF

# Make executable
chmod +x ~/minio-dev/start-minio.sh
```

#### 4.4 Start MinIO (Development)

```bash
# Method 1: Foreground (with logs)
~/minio-dev/start-minio.sh

# Method 2: Background
nohup ~/minio-dev/start-minio.sh > ~/minio-dev/minio.log 2>&1 &

# Method 3: Screen or tmux session
screen -S minio ~/minio-dev/start-minio.sh
```

#### 4.5 Verify MinIO Running

```bash
# Check health
curl -s http://localhost:9000/minio/health/live | jq .
# Expected: {"status":"ok"}

# Or simple check
curl -s http://localhost:9000/minio/health/live
```

### Phase 5: Configure Spring Boot Application

#### 5.1 Create application-dev.properties

```bash
# Create in repo: src/main/resources/application-dev.properties
cat > src/main/resources/application-dev.properties << 'EOF'
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
EOF
```

#### 5.2 Update application.yaml (if used)

```yaml
# In src/main/resources/application.yaml
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

### Phase 6: Run Application

#### 6.1 Using Maven Command Line

```bash
# From project root

# Method 1: Spring Boot plugin
./mvnw clean spring-boot:run

# Method 2: With profile explicitly set
./mvnw clean spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Method 3: Build and run JAR
./mvnw clean package -DskipTests
java -jar target/solusi-program-erp-*.jar --spring.profiles.active=dev
```

#### 6.2 Using IDE

**Eclipse:**
```bash
1. Import project as Maven project
2. Right-click → Run As → Spring Boot App
3. Or: Run → Run Configurations → Spring Boot
```

**IntelliJ IDEA:**
```
1. Open project
2. Edit Run Configuration
3. Add Spring Boot configuration
4. Main class: com.solusi.erp.SolusiProgramErpApplication
5. VM options: -Dspring.profiles.active=dev
6. Run or Debug
```

**VS Code:**
```bash
1. Install "Extension Pack for Java"
2. Install "Spring Boot Extension Pack"
3. File → Open Folder → Select project
4. Click "Run" on main class (SolusiProgramErpApplication)
```

### Phase 7: Verify Local Development Setup

```bash
# Test MariaDB
mysql -u erp_user -p solusi_erp_db -e "SELECT 1;"

# Test MinIO running
curl http://localhost:9000/minio/health/live

# Test Spring Boot
curl http://localhost:8080/health
# Expected: {"status":"UP"}

# Check logs
tail -f nohup.out  # If running in background
# Or check IDE console if running in IDE
```

---

## Option B: Using Docker Compose

If you prefer containerized setup:

### Phase 1: Install Docker

```bash
# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Add user to docker group (no sudo needed)
sudo usermod -aG docker $USER
newgrp docker

# Verify
docker --version
```

### Phase 2: Create docker-compose-dev.yml

```bash
# Create in project root
cat > docker-compose-dev.yml << 'EOF'
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
EOF
```

### Phase 3: Start Services

```bash
# Start all services
docker-compose -f docker-compose-dev.yml up -d

# Verify running
docker-compose -f docker-compose-dev.yml ps

# View logs
docker-compose -f docker-compose-dev.yml logs -f

# Stop services
docker-compose -f docker-compose-dev.yml down
```

---

## Option C: Quick Start (Minimal Setup)

For minimal overhead:

```bash
# 1. Install Java 21
sudo apt install -y openjdk-21-jdk

# 2. Install MariaDB locally
sudo apt install -y mariadb-server

# 3. Create database (from Phase 3.3 above)

# 4. Point to VPS MinIO
# Edit application-dev.properties:
storage.minio.endpoint=http://your-vps-ip:9000

# 5. Run application
./mvnw clean spring-boot:run
```

---

## Common Development Tasks

### Starting Development Session (Option A)

```bash
# Terminal 1: Start MariaDB (auto-starts, verify it)
sudo systemctl status mariadb

# Terminal 2: Start MinIO
~/minio-dev/start-minio.sh
# Or in background: nohup ~/minio-dev/start-minio.sh > ~/minio-dev/minio.log 2>&1 &

# Terminal 3: Run application
./mvnw clean spring-boot:run
# Or in IDE: Click Run button
```

### Testing Signature Upload

```bash
# Create test file
dd if=/dev/urandom of=/tmp/test-sig.png bs=50K count=1

# Upload
curl -X POST http://localhost:8080/api/approvals/signatures/upload \
  -F "signature=@/tmp/test-sig.png" \
  -F "approvalRequestId=1"

# Verify in MinIO
ls ~/erp-storage/approval-signatures/
```

### Database Operations

```bash
# Connect to database
mysql -u erp_user -p solusi_erp_db

# List tables
SHOW TABLES;

# Query signatures
SELECT * FROM appr_signatures;

# Describe table
DESCRIBE appr_signatures;

# Backup
mysqldump -u erp_user -p solusi_erp_db > backup.sql

# Restore
mysql -u erp_user -p solusi_erp_db < backup.sql

# Exit
EXIT;
```

### Viewing Logs

```bash
# Spring Boot logs (if running in background)
tail -f nohup.out

# Or use journalctl (if installed)
journalctl -u erp -f

# MinIO logs
tail -f ~/minio-dev/minio.log

# MariaDB logs
sudo journalctl -u mariadb -f
```

### Stopping Services

```bash
# Stop MinIO
pkill minio

# Stop MariaDB
sudo systemctl stop mariadb

# Or kill from IDE
# Click Stop button
```

---

## Troubleshooting

### Issue: "Port 3306 already in use"

```bash
# Find process
lsof -i :3306

# Kill process (if safe)
kill -9 <PID>

# Or restart MariaDB
sudo systemctl restart mariadb
```

### Issue: "Cannot connect to database"

```bash
# Verify MariaDB running
sudo systemctl status mariadb

# Check connection
mysql -u erp_user -p -h localhost

# Verify database exists
mysql -u erp_user -p -e "SHOW DATABASES;"
```

### Issue: "MinIO not accessible"

```bash
# Check if running
ps aux | grep minio

# Check port
netstat -tlnp | grep 9000

# Check health
curl http://localhost:9000/minio/health/live

# Check logs
tail ~/minio-dev/minio.log
```

### Issue: "Out of memory"

```bash
# Increase Java heap size in Maven
export MAVEN_OPTS="-Xmx2048m"
./mvnw clean spring-boot:run

# Or in IDE run configuration:
# VM options: -Xmx2048m
```

### Issue: "Permission denied" for MinIO

```bash
# Fix permissions
chmod +x ~/minio-dev/minio
chmod +x ~/minio-dev/start-minio.sh

# Or run with bash
bash ~/minio-dev/start-minio.sh
```

---

## Performance Tips

1. **Use SSD**: Much faster than HDD for development
2. **Close unused apps**: Free up RAM for IDE and databases
3. **Use Debug wisely**: Debugging slower than logging
4. **Clean databases**: Remove test data periodically
5. **Cache dependencies**: First Maven run is slow, subsequent runs faster
6. **Monitor resources**: Use `htop` to watch CPU/memory

---

## IDE Setup Recommendations

### IntelliJ IDEA (Recommended)

```bash
# Plugins to install:
# - Spring Boot Assistant
# - MariaDB
# - Database Navigator
# - REST Client

# Settings:
# - Build: Maven 3.8+
# - Java: 21 LTS
# - Enable annotation processing
# - Code style: Google Style
```

### VS Code

```bash
# Extensions:
# - Extension Pack for Java
# - Spring Boot Extension Pack
# - REST Client
# - Database Client
# - GitLens

# .vscode/launch.json:
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "ERP Dev",
      "request": "launch",
      "mainClass": "com.solusi.erp.SolusiProgramErpApplication",
      "args": "--spring.profiles.active=dev",
      "console": "integratedTerminal"
    }
  ]
}
```

### Eclipse

```bash
# Plugins:
# - Spring Tools 4
# - MariaDB Driver

# Preferences:
# - Java → Installed JREs → Add Java 21
# - Maven → Automatic Module Management
# - Code style: Google Style
```

---

## Quick Reference

```bash
# Java
java -version
javac -version

# MariaDB
mysql -u erp_user -p solusi_erp_db
sudo systemctl status mariadb
sudo systemctl restart mariadb

# MinIO
~/minio-dev/start-minio.sh
curl http://localhost:9000/minio/health/live
pkill minio

# Maven
./mvnw clean compile
./mvnw clean spring-boot:run
./mvnw clean test
./mvnw clean package -DskipTests

# System
df -h          # Disk usage
free -h        # Memory
htop          # System monitor
netstat -tlnp # Open ports
lsof -i :8080 # Check port 8080
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
☐ start-minio.sh created and executable
☐ application-dev.properties created
☐ Spring Boot runs and connects
☐ Can upload signature files

Option B: Docker Setup
☐ Docker installed
☐ docker-compose-dev.yml created
☐ Services running: docker-compose ps
☐ application-dev.properties updated
☐ Spring Boot connects to Docker services

Option C: Quick Start
☐ Java 21 installed
☐ MariaDB installed locally
☐ VPS MinIO endpoint configured
☐ Application running
☐ Ready to develop
```

---

## Next Steps

1. Choose Option A, B, or C
2. Follow setup steps
3. Verify services running
4. Run application
5. Start coding!

---

## Useful Links

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [MariaDB Documentation](https://mariadb.com/kb/en/)
- [MinIO Documentation](https://docs.min.io/)
- [Maven Documentation](https://maven.apache.org/)

**Happy coding! 🚀**

---

*Last Updated: 2026-04-04*
*For: Solusi Program ERP Development*
*Guide: Linux Local Development Environment*
