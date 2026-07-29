#!/usr/bin/env bash

set -Eeuo pipefail
set -m

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SPRING_DIR="$ROOT_DIR/backend/spring"
FRONTEND_DIR="$ROOT_DIR/frontend"
ENV_FILE="$SPRING_DIR/.env"
PIDS=()

if [[ -f "$ENV_FILE" ]]; then
    set -a
    # shellcheck disable=SC1090
    source "$ENV_FILE"
    set +a
fi

: "${JWT_SECRET:?Set JWT_SECRET in backend/spring/.env or the environment}"

export JWT_ALGORITHM="${JWT_ALGORITHM:-HS256}"
export MONGODB_URI="${MONGODB_URI:-mongodb://localhost:27017/social}"
export API_HTTP_PORT="${API_HTTP_PORT:-5000}"
export CHAT_GRPC_PORT="${CHAT_GRPC_PORT:-5001}"
export CHAT_WS_PORT="${CHAT_WS_PORT:-8001}"
export CHAT_GRPC_HOST="${CHAT_GRPC_HOST:-localhost}"
export NOTIFICATION_WS_PORT="${NOTIFICATION_WS_PORT:-8088}"
export NOTIFICATION_GRPC_PORT="${NOTIFICATION_GRPC_PORT:-8090}"
export GRPC_NOTIFY_HOST="${GRPC_NOTIFY_HOST:-localhost}"

require_command() {
    if ! command -v "$1" >/dev/null 2>&1; then
        printf 'Required command not found: %s\n' "$1" >&2
        exit 1
    fi
}

for command in java npm curl lsof; do
    require_command "$command"
done

for port in "$API_HTTP_PORT" "$CHAT_GRPC_PORT" "$CHAT_WS_PORT" \
    "$NOTIFICATION_WS_PORT" "$NOTIFICATION_GRPC_PORT" 3000; do
    if lsof -nP -iTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1; then
        printf 'Port %s is already in use. Stop the existing service and retry.\n' "$port" >&2
        exit 1
    fi
done

cleanup() {
    trap - EXIT INT TERM
    if [[ ${#PIDS[@]} -eq 0 ]]; then
        return
    fi
    printf '\nStopping Spring services and frontend...\n'
    for pid in "${PIDS[@]}"; do
        kill -TERM -- "-$pid" 2>/dev/null || true
    done
    for pid in "${PIDS[@]}"; do
        wait "$pid" 2>/dev/null || true
    done
}

trap cleanup EXIT
trap 'exit 130' INT TERM

if [[ "${BUILD_ON_START:-true}" == "true" ]]; then
    printf 'Building Spring applications and running backend tests...\n'
    "$SPRING_DIR/mvnw" package -f "$SPRING_DIR/pom.xml"
fi

if [[ ! -d "$FRONTEND_DIR/node_modules" ]]; then
    printf 'Installing frontend dependencies...\n'
    npm install --prefix "$FRONTEND_DIR"
fi

printf 'Starting Spring API on HTTP %s and chat gRPC %s...\n' \
    "$API_HTTP_PORT" "$CHAT_GRPC_PORT"
(
    cd "$SPRING_DIR"
    exec java -jar app-api/target/app-api-0.1.0-SNAPSHOT.jar
) &
PIDS+=("$!")

printf 'Starting Spring chat WebSocket on %s...\n' "$CHAT_WS_PORT"
(
    cd "$SPRING_DIR"
    exec java -jar app-realtime-chat/target/app-realtime-chat-0.1.0-SNAPSHOT.jar
) &
PIDS+=("$!")

printf 'Starting Spring notification WebSocket %s and gRPC %s...\n' \
    "$NOTIFICATION_WS_PORT" "$NOTIFICATION_GRPC_PORT"
(
    cd "$SPRING_DIR"
    exec java -jar app-realtime-notification/target/app-realtime-notification-0.1.0-SNAPSHOT.jar
) &
PIDS+=("$!")

printf 'Starting the unchanged frontend on 3000...\n'
(
    cd "$FRONTEND_DIR"
    exec env BROWSER=none npm start
) &
PIDS+=("$!")

wait_for_health() {
    local name="$1"
    local url="$2"
    for _ in {1..60}; do
        if curl --fail --silent --show-error "$url" >/dev/null 2>&1; then
            printf '%s is ready: %s\n' "$name" "$url"
            return
        fi
        sleep 1
    done
    printf '%s did not become healthy: %s\n' "$name" "$url" >&2
    exit 1
}

wait_for_health "Spring API" "http://localhost:$API_HTTP_PORT/actuator/health"
wait_for_health "Chat WebSocket" "http://localhost:$CHAT_WS_PORT/actuator/health"
wait_for_health "Notification WebSocket" \
    "http://localhost:$NOTIFICATION_WS_PORT/actuator/health"
wait_for_health "Frontend" "http://localhost:3000"

printf '\nAll Spring services are running. Open http://localhost:3000\n'
printf 'MongoDB: %s\n' "$MONGODB_URI"
printf 'Press Ctrl+C to stop everything.\n'

wait
