# chatSITO Backend

Spring Boot backend for the chatSITO React application. The Maven reactor builds three executable services that share MongoDB and communicate over gRPC.

## Requirements

- Java 21 or newer
- MongoDB on `localhost:27017`
- Node.js and npm for the frontend
- `curl` and `lsof` for launcher health checks

Maven is provided through the checked-in wrapper.

## Local Setup

Create the ignored runtime configuration once:

```bash
cp backend/spring/.env.example backend/spring/.env
```

Set `JWT_SECRET` in `.env`, then start the complete application from the repository root:

```bash
./run_all_microservices.sh
```

The launcher builds and tests the backend, starts all services and the React development server, waits for their health checks, and stops every child process on Ctrl+C. Open `http://localhost:3000` after startup.

## Services

| Module | Interface | Default port |
|---|---|---:|
| `app-api` | REST and Actuator | 5000 |
| `app-api` | Chat gRPC | 5001 |
| `app-realtime-chat` | Chat WebSocket `/ws/{id}` | 8001 |
| `app-realtime-notification` | Notification WebSocket `/ws/{user_id}` | 8088 |
| `app-realtime-notification` | Notification gRPC | 8090 |
| `frontend` | React development server | 3000 |

The REST routes are mounted directly under `/user`, `/posts`, `/chat`, and `/notification`; there is no internal `/api` prefix.

## Configuration

| Variable | Default |
|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/social` |
| `JWT_ALGORITHM` | `HS256` |
| `API_HTTP_PORT` | `5000` |
| `CHAT_GRPC_PORT` | `5001` |
| `CHAT_WS_PORT` | `8001` |
| `CHAT_GRPC_HOST` | `localhost` |
| `NOTIFICATION_WS_PORT` | `8088` |
| `NOTIFICATION_GRPC_PORT` | `8090` |
| `GRPC_NOTIFY_HOST` | `localhost` |

`JWT_SECRET` is required and has no default. The application uses the existing MongoDB collections `User`, `Post`, `Comment`, `Message`, `UnReadedMsg`, and `Notification`.

## Verification

Run the full backend test suite and create all executable JARs:

```bash
cd backend/spring
./mvnw package
```

Run the API against the isolated fixture database on alternate ports:

```bash
cd backend/spring
set -a
source .env
set +a
MONGODB_URI=mongodb://localhost:27017/social_spring_test \
API_HTTP_PORT=15000 \
CHAT_GRPC_PORT=15001 \
java -jar app-api/target/app-api-0.1.0-SNAPSHOT.jar
```

In a second terminal, run the 22-route HTTP smoke suite:

```bash
cd backend/spring
BASE_URL=http://localhost:15000 \
MONGODB_URI=mongodb://localhost:27017/social_spring_test \
scripts/smoke-local-verification.sh
```

The smoke script reloads and restores only `social_spring_test`; it never modifies `social`. It requires `curl`, `jq`, and `mongosh`.

Build the frontend from its package directory:

```bash
cd frontend
npm run build
```

See [ARCHITECTURE.md](ARCHITECTURE.md) for component flow and client-contract details.
