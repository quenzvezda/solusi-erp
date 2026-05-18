#!/bin/bash
set -e

PROJECT_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
SERVER_LOG="$PROJECT_ROOT/target/e2e-server.log"
SERVER_ERR_LOG="$PROJECT_ROOT/target/e2e-server-err.log"

echo "=== Building JAR with e2e profile ==="
cd "$PROJECT_ROOT"
./mvnw -B package -DskipTests -Pe2e -q

JAR=$(ls target/solusi-program-erp-*.jar | head -1)
echo "=== Starting server: $JAR ==="
echo "Spring Boot logs: $SERVER_LOG"
echo "Spring Boot errors: $SERVER_ERR_LOG"
java -jar "$JAR" --spring.profiles.active=e2e > "$SERVER_LOG" 2> "$SERVER_ERR_LOG" &
SERVER_PID=$!
trap "echo '=== Stopping server (PID: '$SERVER_PID') ==='; kill $SERVER_PID 2>/dev/null || true" EXIT

echo "=== Waiting for server (max 60s) ==="
for i in $(seq 1 60); do
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:18080/login | grep -q "200"; then
        echo "Server ready after ${i}s"
        break
    fi
    if [ "$i" -eq 60 ]; then
        echo "Server failed to start within 60s"
        echo "Check Spring Boot logs: $SERVER_LOG"
        echo "Check Spring Boot errors: $SERVER_ERR_LOG"
        exit 1
    fi
    sleep 1
done

echo "=== Installing Playwright dependencies ==="
cd "$PROJECT_ROOT/e2e-tests"
npm ci --silent
if [ "${INSTALL_PLAYWRIGHT_DEPS:-0}" = "1" ]; then
    npx playwright install chromium --with-deps
else
    npx playwright install chromium
fi

echo "=== Running Playwright tests ==="
npx playwright test
