# VPS Deployment Guide: Fresh Ubuntu Setup (From Scratch)

> **Target**: Fresh Ubuntu instance (like oracle cloud) dengan 0 dependencies
> **Outcome**: Production-ready ERP + MinIO dengan backup strategy
> **Time**: ~1 hour

---

## Prerequisites Checklist

Before starting, verify:
- [ ] Ubuntu 20.04 LTS or newer (run `lsb_release -a`)
- [ ] SSH access to VPS
- [ ] sudo privileges
- [ ] ~50GB disk space available
- [ ] Internet connectivity

---

## Phase 1: System Preparation (15 min)

### 1.1 Update System Packages

```bash
# Connect to VPS
ssh ubuntu@your-vps-ip

# Update package lists
sudo apt update
sudo apt upgrade -y

# Install essential build tools
sudo apt install -y \
  build-essential \
  curl \
  wget \
  git \
  unzip \
  net-tools \
  htop \
  ca-certificates

# Verify
echo "✓ System updated"
```

### 1.2 Create Application Directory Structure

```bash
# Create directory structure
mkdir -p ~/app
mkdir -p ~/backups
mkdir -p ~/minio-app
mkdir -p ~/scripts

# Create storage directory
sudo mkdir -p /var/erp/storage/approval-signatures
sudo chown -R ubuntu:ubuntu /var/erp/storage
sudo chmod 755 /var/erp/storage

# Verify directories
tree ~/ -L 2 2>/dev/null || ls -la ~/ && ls -la /var/erp/storage/

echo "✓ Directory structure created"
```

---

## Phase 2: Install Java 21 (10 min)

### 2.1 Install OpenJDK 21

```bash
# Install Java 21 (Temurin distribution, same as GitHub Actions)
sudo apt install -y openjdk-21-jdk

# Verify installation
java -version
# Expected: openjdk version "21.x.x" ...

javac -version
# Expected: javac 21.x.x

echo "✓ Java 21 installed"
```

### 2.2 Set JAVA_HOME (Optional but recommended)

```bash
# Find Java installation path
update-alternatives --list java
# Example: /usr/lib/jvm/java-21-openjdk-amd64/bin/java

# Add to .bashrc (optional)
echo 'export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64' >> ~/.bashrc
source ~/.bashrc

# Verify
echo $JAVA_HOME
```

---

## Phase 3: Install MariaDB (10 min)

### 3.1 Install MariaDB Server

```bash
# Install MariaDB
sudo apt install -y mariadb-server

# Start service
sudo systemctl start mariadb
sudo systemctl enable mariadb

# Verify
sudo systemctl status mariadb | grep "Active"
# Expected: Active: active (running)

echo "✓ MariaDB installed and running"
```

### 3.2 Secure MariaDB Installation

```bash
# Run security script
sudo mysql_secure_installation

# Prompts (recommended answers):
# - Enter current password: (press Enter)
# - Switch to unix_socket authentication: N
# - Change root password: Y
#   New password: [YOUR_SECURE_PASSWORD]
#   Confirm password: [YOUR_SECURE_PASSWORD]
# - Remove anonymous user: Y
# - Disable root login remotely: Y
# - Remove test database: Y
# - Reload privilege tables: Y

echo "✓ MariaDB secured"
```

### 3.3 Create Application Database

```bash
# Connect to MariaDB as root
mysql -u root -p
# (Enter the password you set above)

# Create database and user
CREATE DATABASE solusi_erp_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'erp_user'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON solusi_erp_db.* TO 'erp_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;

# Verify
mysql -u erp_user -p -e "SELECT DATABASE();"
# When prompted, enter the password for erp_user

echo "✓ Database created"
```

---

## Phase 4: Setup Spring Boot Application (10 min)

### 4.1 Configure Application Properties

```bash
# Create application.properties in app directory
cat > ~/app/application.properties << 'EOF'
# Database Configuration
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
logging.file.name=/home/ubuntu/app/erp.log
logging.file.max-size=10MB
logging.file.max-history=30

# MinIO Storage
storage.provider=minio
storage.minio.endpoint=http://localhost:9000
storage.minio.access-key=minioadmin
storage.minio.secret-key=minioadmin
storage.minio.bucket=approval-signatures
storage.minio.region=us-east-1
storage.minio.secure=false
storage.minio.max-file-size=5242880
storage.minio.expiration-minutes=15
storage.minio.retention-days=730
EOF

echo "✓ Application properties configured"
```

### 4.2 Create SystemD Service for ERP

```bash
# Create systemd service file
sudo tee /etc/systemd/system/erp.service > /dev/null << 'EOF'
[Unit]
Description=Solusi ERP Spring Boot App
After=network.target mariadb.service

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/home/ubuntu/app
ExecStart=java -jar /home/ubuntu/app/app.jar
SuccessExitStatus=143
TimeoutStopSec=10
Restart=on-failure
RestartSec=10

# Logging
StandardOutput=append:/home/ubuntu/app/erp.log
StandardError=append:/home/ubuntu/app/erp-error.log

[Install]
WantedBy=multi-user.target
EOF

# Reload systemd
sudo systemctl daemon-reload
sudo systemctl enable erp

echo "✓ SystemD service created"
```

### 4.3 Deploy app.jar

```bash
# Copy app.jar to app directory
# (In real deployment, this comes from GitHub Actions via SCP)
# For now, build locally or copy from somewhere

# If you have the JAR, place it at ~/app/app.jar
ls -lh ~/app/app.jar
# Expected: -rw-r--r-- ubuntu ubuntu [size] app.jar

# Start ERP service
sudo systemctl start erp

# Wait for startup
sleep 10

# Check status
sudo systemctl status erp | grep "Active"
# Expected: Active: active (running)

# Check logs
sudo journalctl -u erp -n 20 --no-pager
# Should show Spring Boot startup messages

echo "✓ ERP service started and running"
```

---

## Phase 5: Install MinIO Standalone (10 min)

### 5.1 Download MinIO Binary

```bash
# Go to minio directory
cd ~/minio-app

# Download MinIO binary
wget https://dl.min.io/server/minio/release/linux-amd64/minio

# Make executable
chmod +x minio

# Verify
./minio --version
# Expected: minio version RELEASE.2026-04-04T...

echo "✓ MinIO binary downloaded"
```

### 5.2 Create MinIO SystemD Service

```bash
# Create MinIO systemd service file
sudo tee /etc/systemd/system/minio.service > /dev/null << 'EOF'
[Unit]
Description=MinIO Object Storage Server
After=network-online.target
Wants=network-online.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/home/ubuntu/minio-app
ExecStart=/home/ubuntu/minio-app/minio server /var/erp/storage

Restart=always
RestartSec=5

Environment="MINIO_ACCESS_KEY=minioadmin"
Environment="MINIO_SECRET_KEY=minioadmin"
Environment="MINIO_REGION=us-east-1"

StandardOutput=append:/home/ubuntu/minio.log
StandardError=append:/home/ubuntu/minio-error.log

[Install]
WantedBy=multi-user.target
EOF

# Reload systemd
sudo systemctl daemon-reload
sudo systemctl enable minio

# Start MinIO
sudo systemctl start minio

# Wait for startup
sleep 3

# Check status
sudo systemctl status minio | grep "Active"
# Expected: Active: active (running)

# Verify MinIO is accessible
curl -s http://localhost:9000/minio/health/live
# Expected: {"status":"ok"} or similar

echo "✓ MinIO installed and running"
```

### 5.3 Bucket provisioning (auto-create first, manual fallback)

The application should create the bucket automatically on first startup if it does not exist. Use this manual step only if provisioning fails or you want to prepare storage before the app starts.

**Canonical bucket name:** `approval-signatures`

```bash
# Install MinIO client if needed
curl -LO https://dl.min.io/client/mc/release/linux-amd64/mc
chmod +x mc
sudo mv mc /usr/local/bin/

# Point mc to local MinIO
mc alias set erp-minio http://localhost:9000 minioadmin minioadmin

# Create the bucket expected by the ERP app
mc mb --ignore-existing erp-minio/approval-signatures

# Verify bucket exists
mc ls erp-minio
```

If you change the bucket name here, also update:
- `MINIO_BUCKET_NAME` in `.env`
- `storage.minio.bucket` in Spring Boot config
- any code that hardcodes the bucket name

---

## Phase 6: Setup Backup Strategy (10 min)

### 6.1 Install rclone (Google Drive Sync)

```bash
# Install rclone
curl https://rclone.org/install.sh | sudo bash

# Verify
rclone version
# Expected: rclone v1.x.x

echo "✓ rclone installed"
```

### 6.2 Configure rclone for Google Drive

```bash
# Interactive setup
rclone config

# Follow prompts:
# 1. new - Create new remote
# 2. Name: gdrive
# 3. Storage type: 17 (Google Drive)
# 4. Follow OAuth flow to authorize
# 5. Share drive? n
# 6. Name of root folder? (leave empty)
# 7. Service account file? (leave empty)
# 8. Edit config? n
# 9. Keep this remote? y

# Verify
rclone listremotes
# Expected: gdrive

# Test
rclone ls gdrive:
# Should list files in your Google Drive

echo "✓ rclone configured for Google Drive"
```

### 6.3 Create Backup Script

```bash
# Create backup script
cat > ~/backup-db.sh << 'EOF'
#!/bin/bash

DATE=$(date +%Y-%m-%d)
BACKUP_DIR="/home/ubuntu/backups"
mkdir -p $BACKUP_DIR

echo "[$(date)] Starting backup..."

# ===== DATABASE BACKUP =====
echo "[$(date)] Backing up database..."
mysqldump -u erp_user -p'your_secure_password' solusi_erp_db > $BACKUP_DIR/erp-$DATE.sql

if [ $? -eq 0 ]; then
    echo "[$(date)] ✓ Database backup successful"
else
    echo "[$(date)] ✗ Database backup FAILED"
    exit 1
fi

# ===== SIGNATURE FILES BACKUP =====
echo "[$(date)] Backing up signature files..."
if [ -d "/var/erp/storage/approval-signatures" ]; then
    tar -czf $BACKUP_DIR/signatures-$DATE.tar.gz \
        /var/erp/storage/approval-signatures/ 2>/dev/null
    
    if [ $? -eq 0 ]; then
        FILESIZE=$(du -h $BACKUP_DIR/signatures-$DATE.tar.gz | cut -f1)
        echo "[$(date)] ✓ Signature backup successful ($FILESIZE)"
    else
        echo "[$(date)] ⚠ Signature backup had issues (continuing)"
    fi
else
    echo "[$(date)] ⚠ Signature directory not found yet"
fi

# ===== GOOGLE DRIVE SYNC =====
echo "[$(date)] Uploading to Google Drive..."
rclone copy $BACKUP_DIR/erp-$DATE.sql gdrive:solusi-erp/ 2>&1 | grep -v "^.*\.sql: *$"
rclone copy $BACKUP_DIR/signatures-$DATE.tar.gz gdrive:solusi-erp/ 2>&1 | grep -v "^.*\.tar\.gz: *$"

if [ $? -eq 0 ]; then
    echo "[$(date)] ✓ Google Drive sync successful"
else
    echo "[$(date)] ⚠ Google Drive sync had issues"
fi

# ===== PRUNING: Keep only last 3 days =====
echo "[$(date)] Pruning old backups (keeping last 3 days)..."
DELETED_DB=$(find $BACKUP_DIR -name "erp-*.sql" -mtime +3 -delete -print | wc -l)
DELETED_SIG=$(find $BACKUP_DIR -name "signatures-*.tar.gz" -mtime +3 -delete -print | wc -l)

if [ $DELETED_DB -gt 0 ] || [ $DELETED_SIG -gt 0 ]; then
    echo "[$(date)] ✓ Deleted $DELETED_DB DB backups and $DELETED_SIG signature backups"
fi

echo "[$(date)] Backup completed!"
EOF

# Make executable
chmod +x ~/backup-db.sh

# Test backup script
~/backup-db.sh

echo "✓ Backup script created and tested"
```

### 6.4 Setup Cron Job

```bash
# Edit crontab
crontab -e

# Add this line (runs daily at 19:00)
0 19 * * * /home/ubuntu/backup-db.sh >> /home/ubuntu/backups/backup.log 2>&1

# Verify
crontab -l
# Should show the backup-db.sh line

echo "✓ Cron job scheduled"
```

---

## Phase 7: Verification & Monitoring (5 min)

### 7.1 System Health Check

```bash
# Check all services running
sudo systemctl status erp
sudo systemctl status minio
sudo systemctl status mariadb

# Expected: all "Active: active (running)"

# Check port availability
ss -tlnp | grep -E "3306|8080|9000"
# Expected:
# - 3306: MariaDB
# - 8080: Spring Boot
# - 9000: MinIO

# Check disk space
df -h
# Expected: plenty of free space

# Check memory usage
free -h
# Expected: reasonable usage

echo "✓ All services verified"
```

### 7.2 Application Verification

```bash
# Test Spring Boot app
curl -s http://localhost:8080/health | jq .
# Expected: {"status":"UP"}

# Test MariaDB connection
mysql -u erp_user -p -e "SELECT 1;" 
# When prompted, enter password for erp_user

# Test MinIO health
curl -s http://localhost:9000/minio/health/live | jq .
# Expected: {"status":"ok"}

echo "✓ Application verified"
```

### 7.3 Logs Location

```bash
# ERP logs
tail -f ~/app/erp.log

# MinIO logs
tail -f ~/minio.log

# MariaDB logs
sudo journalctl -u mariadb -f

# System logs
sudo journalctl -u erp -f
sudo journalctl -u minio -f

echo "✓ Logs accessible"
```

---

## Phase 8: Post-Deployment Configuration (Optional)

### 8.1 Setup Firewall (if needed)

```bash
# Check if UFW is enabled
sudo ufw status

# If not, enable it
sudo ufw enable

# Allow SSH (critical!)
sudo ufw allow 22/tcp

# Allow HTTP/HTTPS for ERP
sudo ufw allow 8080/tcp

# Allow MinIO (internal only, don't expose)
# sudo ufw allow 9000/tcp  # Only if needed externally

# Verify rules
sudo ufw status numbered
```

### 8.2 Setup SSL/TLS (Optional)

```bash
# If you have a domain and want HTTPS:
# Option 1: Use Let's Encrypt with Certbot
sudo apt install -y certbot python3-certbot-nginx

# Option 2: Use Nginx as reverse proxy
# (Out of scope for this guide, but recommended for production)
```

### 8.3 Monitor Disk Space (Optional)

```bash
# Setup disk space alert
df -h /

# If low on space, consider:
# - Reduce backup retention (modify backup script)
# - Archive old signatures to Google Drive
# - Increase VPS disk size
```

---

## Troubleshooting

### Issue: "Address already in use"

```bash
# Find process using port
sudo lsof -i :8080  # or :9000, :3306
kill -9 <PID>
```

### Issue: "Database connection refused"

```bash
# Check MariaDB status
sudo systemctl status mariadb

# Check credentials
mysql -u erp_user -p -h localhost

# Check database exists
mysql -u erp_user -p -e "SHOW DATABASES;"
```

### Issue: "Java not found"

```bash
# Verify Java installed
java -version

# If not, reinstall
sudo apt install -y openjdk-21-jdk
```

### Issue: "Permission denied" for files

```bash
# Fix ownership
sudo chown -R ubuntu:ubuntu ~/app
sudo chown -R ubuntu:ubuntu ~/backups
sudo chown -R ubuntu:ubuntu /var/erp/storage
```

---

## Monitoring & Maintenance

### Daily Checks

```bash
# Check services
sudo systemctl status erp mariadb minio

# Check disk space
df -h /

# Check backup completion
tail /home/ubuntu/backups/backup.log
```

### Weekly Checks

```bash
# Check error logs
grep ERROR ~/app/erp.log

# Verify backups on Google Drive
rclone ls gdrive:solusi-erp/

# Check MinIO bucket
mc ls erp-minio/approval-signatures
```

### Monthly Maintenance

```bash
# Update system packages
sudo apt update && sudo apt upgrade -y

# Update MinIO (optional)
# cd ~/minio-app
# wget https://dl.min.io/server/minio/release/linux-amd64/minio
# sudo systemctl restart minio

# Check disk usage growth
du -sh /var/erp/storage/
du -sh /home/ubuntu/backups/
```

---

## Quick Reference Commands

```bash
# Start/Stop services
sudo systemctl start/stop/restart erp
sudo systemctl start/stop/restart minio
sudo systemctl start/stop/restart mariadb

# View logs
sudo journalctl -u erp -f
sudo journalctl -u minio -f
tail -f ~/app/erp.log

# Backup operations
~/backup-db.sh  # Run backup manually
ls -lh ~/backups/  # List backups

# Database operations
mysql -u erp_user -p solusi_erp_db
mysqldump -u erp_user -p solusi_erp_db > backup.sql

# MinIO operations
curl http://localhost:9000/minio/health/live
ls -la /var/erp/storage/

# System info
df -h  # Disk usage
free -h  # Memory
ss -tlnp  # Open ports
```

---

## Deployment Checklist

```
Phase 1: System Preparation
☐ Update system packages
☐ Create directory structure
☐ Create /var/erp/storage directory

Phase 2: Java 21
☐ Install OpenJDK 21
☐ Verify java -version

Phase 3: MariaDB
☐ Install MariaDB
☐ Run mysql_secure_installation
☐ Create database and user
☐ Verify connection

Phase 4: Spring Boot
☐ Create application.properties
☐ Create systemd service
☐ Deploy app.jar
☐ Start service and verify

Phase 5: MinIO
☐ Download MinIO binary
☐ Create systemd service
☐ Start MinIO
☐ Verify health endpoint

Phase 6: Backup
☐ Install rclone
☐ Configure Google Drive
☐ Create backup script
☐ Setup cron job
☐ Test backup

Phase 7: Verification
☐ Verify all services running
☐ Test application endpoints
☐ Check logs

Phase 8: Done!
☐ Document server IP
☐ Document credentials
☐ Setup monitoring
☐ Plan maintenance schedule
```

---

## Next Steps

Once deployment is complete:
1. Test application at `http://your-vps-ip:8080`
2. Configure DNS if using domain
3. Setup SSL/TLS (recommended for production)
4. Setup monitoring/alerts
5. Test backup restoration process

**All services should be running and accessible!** 🚀

---

*Last Updated: 2026-04-04*
*For: Solusi Program ERP*
*Guide: Fresh Ubuntu VPS Deployment*
