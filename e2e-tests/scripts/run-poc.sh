#!/bin/bash
set -e

PROJECT_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"

echo "=== Building JAR with e2e profile ==="
cd "$PROJECT_ROOT"
./mvnw -B package -DskipTests -Pe2e -q

JAR=$(ls target/solusi-program-erp-*.jar | head -1)
echo "=== Starting server: $JAR ==="
java -jar "$JAR" --spring.profiles.active=e2e &
SERVER_PID=$!
trap "kill $SERVER_PID 2>/dev/null || true" EXIT

echo "=== Waiting for server (max 60s) ==="
for i in $(seq 1 60); do
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:18080/login | grep -q "200"; then
        echo "Server ready after ${i}s"
        break
    fi
    if [ "$i" -eq 60 ]; then
        echo "Server failed to start within 60s"
        exit 1
    fi
    sleep 1
done

echo "=== Installing Playwright dependencies ==="
cd "$PROJECT_ROOT/e2e-tests"
npm ci --silent
npx playwright install chromium --with-deps

echo "=== Running Playwright tests ==="
npx playwright test
