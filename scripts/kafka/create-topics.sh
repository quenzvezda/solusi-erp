#!/usr/bin/env bash
set -euo pipefail

# Idempotent Kafka topic bootstrap for ERP integration events.
# Skips when KAFKA_BOOTSTRAP_SERVERS is empty so production deploys remain safe
# until Kafka is intentionally configured.

BOOTSTRAP_SERVERS="${KAFKA_BOOTSTRAP_SERVERS:-}"
TOPICS="${KAFKA_TOPICS:-erp.approval.events.v1}"
PARTITIONS="${KAFKA_TOPIC_PARTITIONS:-1}"
REPLICATION_FACTOR="${KAFKA_TOPIC_REPLICATION_FACTOR:-1}"
RETENTION_MS="${KAFKA_TOPIC_RETENTION_MS:-}"
KAFKA_TOPICS_CMD="${KAFKA_TOPICS_CMD:-}"
KAFKA_DOCKER_CONTAINER="${KAFKA_DOCKER_CONTAINER:-}"

if [ -z "$BOOTSTRAP_SERVERS" ]; then
  echo "KAFKA_BOOTSTRAP_SERVERS is empty; skipping Kafka topic bootstrap."
  exit 0
fi

if [ -n "$KAFKA_TOPICS_CMD" ]; then
  TOPIC_CMD=($KAFKA_TOPICS_CMD)
elif command -v kafka-topics.sh >/dev/null 2>&1; then
  TOPIC_CMD=(kafka-topics.sh)
elif [ -n "$KAFKA_DOCKER_CONTAINER" ] && command -v docker >/dev/null 2>&1; then
  TOPIC_CMD=(env MSYS_NO_PATHCONV=1 docker exec "$KAFKA_DOCKER_CONTAINER" /opt/bitnami/kafka/bin/kafka-topics.sh)
elif command -v docker >/dev/null 2>&1 && docker ps --format '{{.Names}}' | grep -qx 'kafka-erp'; then
  TOPIC_CMD=(env MSYS_NO_PATHCONV=1 docker exec kafka-erp /opt/bitnami/kafka/bin/kafka-topics.sh)
else
  echo "Cannot find kafka-topics.sh."
  echo "Set KAFKA_TOPICS_CMD or KAFKA_DOCKER_CONTAINER, or install kafka-topics.sh on PATH."
  exit 1
fi

CONFIG_ARGS=()
if [ -n "$RETENTION_MS" ]; then
  CONFIG_ARGS+=(--config "retention.ms=$RETENTION_MS")
fi

IFS=', ' read -r -a TOPIC_LIST <<< "$TOPICS"

for topic in "${TOPIC_LIST[@]}"; do
  if [ -z "$topic" ]; then
    continue
  fi

  echo "Ensuring Kafka topic exists: $topic"
  "${TOPIC_CMD[@]}" \
    --bootstrap-server "$BOOTSTRAP_SERVERS" \
    --create \
    --if-not-exists \
    --topic "$topic" \
    --partitions "$PARTITIONS" \
    --replication-factor "$REPLICATION_FACTOR" \
    "${CONFIG_ARGS[@]}"
done

echo "Kafka topic bootstrap completed."
