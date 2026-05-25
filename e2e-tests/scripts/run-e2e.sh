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
        echo ""
        echo "=== Last 50 lines of $SERVER_LOG ==="
        tail -50 "$SERVER_LOG" 2>/dev/null || echo "(file not found)"
        echo ""
        echo "=== Last 50 lines of $SERVER_ERR_LOG ==="
        tail -50 "$SERVER_ERR_LOG" 2>/dev/null || echo "(file not found)"
        exit 1
    fi
    sleep 1
done

echo "=== Pre-warming JVM (login + key endpoints) ==="
# Spring Boot lazy-initialises beans on first request per controller path.
# Without warmup, the first hit by Playwright on each /create or /list
# endpoint can take 10-25s, racing past page.goto's 30s timeout.
# Hit the same endpoints the test suite uses so JIT + bean graphs are warm
# before Playwright starts.
COOKIE_JAR="$(mktemp)"
trap "rm -f \"$COOKIE_JAR\"; echo '=== Stopping server (PID: '$SERVER_PID') ==='; kill $SERVER_PID 2>/dev/null || true" EXIT
LOGIN_HTML=$(curl -s -c "$COOKIE_JAR" -b "$COOKIE_JAR" --max-time 30 http://localhost:18080/login || true)
CSRF_TOKEN=$(echo "$LOGIN_HTML" | grep -oE 'name="_csrf"[^>]*value="[^"]+"|value="[^"]+"[^>]*name="_csrf"' | grep -oE 'value="[^"]+"' | head -1 | sed 's/value="//; s/"//')
if [ -n "$CSRF_TOKEN" ]; then
    curl -s -o /dev/null -c "$COOKIE_JAR" -b "$COOKIE_JAR" --max-time 30 -L \
        -d "username=admin&password=admin123&_csrf=$CSRF_TOKEN" \
        http://localhost:18080/login || true
else
    curl -s -o /dev/null -c "$COOKIE_JAR" -b "$COOKIE_JAR" --max-time 30 -L \
        -d "username=admin&password=admin123" \
        http://localhost:18080/login || true
fi

WARMUP_URLS=(
    "/dashboard"
    "/inventory/adjustments"
    "/inventory/adjustments/create"
    "/inventory/brands"
    "/inventory/brands/create"
    "/inventory/product-categories"
    "/inventory/product-categories/create"
    "/inventory/products"
    "/inventory/products/create"
    "/inventory/uoms"
    "/inventory/uoms/create"
    "/purchasing/purchase-requisitions"
    "/purchasing/purchase-requisitions/create"
    "/purchasing/supplier-price-lists"
)
for u in "${WARMUP_URLS[@]}"; do
    curl -s -o /dev/null -b "$COOKIE_JAR" --max-time 30 -L "http://localhost:18080$u" \
        || echo "warmup miss $u"
done
echo "=== Warmup complete ==="

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
