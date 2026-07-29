# Backend Architecture

## Runtime Flow

- HTTP requests flow from React to `app-api:5000`, then to MongoDB.
- Chat messages flow from React to `app-realtime-chat:8001`, through gRPC on `app-api:5001`, then to MongoDB.
- Notification-producing actions are stored by `app-api`, sent over gRPC to port `8090`, and delivered to connected browsers over WebSocket port `8088`.
- MongoDB remains the source of truth. Realtime delivery is best effort and does not queue a second copy for disconnected users.

## Modules

- `contracts-proto`: canonical protobuf definitions and generated Java/gRPC classes.
- `shared-compat`: shared constants and client-contract support.
- `app-api`: REST controllers, MongoDB persistence, JWT authentication, and chat gRPC.
- `app-realtime-chat`: raw WebSocket chat and online-presence handling.
- `app-realtime-notification`: raw notification WebSocket and notification gRPC.
- `contract-tests`: endpoint inventory and cross-module wire-contract guards.

## HTTP Surface

| Method | Path |
|---|---|
| POST | `/user/signup` |
| POST | `/user/signin` |
| GET | `/user/getUser/{id}` |
| PATCH | `/user/Update/{id}` |
| PATCH | `/user/{id}/following` |
| GET | `/user/getSug` |
| DELETE | `/user/delete/{id}` |
| GET | `/posts` |
| GET | `/posts/search` |
| GET | `/posts/{id}` |
| POST | `/posts` |
| PATCH | `/posts/{id}` |
| PATCH | `/posts/{id}/likePost` |
| DELETE | `/posts/{id}` |
| POST | `/posts/{id}/commentPost` |
| DELETE | `/posts/{id}/deleteComment` |
| POST | `/chat/sendmessage` |
| GET | `/chat/getmsgsbynums` |
| GET | `/chat/get-user-unreadedmsg` |
| GET | `/chat/mark-msg-asreaded` |
| GET | `/notification/{userid}` |
| GET | `/notification/mark-notification-asreaded` |

## Client Contracts

- Authentication state is stored in `localStorage.profile`; Axios and both WebSocket clients derive the JWT and user ID from it.
- Existing field spellings such as `recever`, `deatils`, `isreded`, and related `*Readed` names are part of the persisted and frontend contracts.
- Chat history uses pages of eight messages and returns `{msgs, hasMore}`.
- Chat WebSocket persistence happens exactly once through gRPC. The REST send route is the frontend fallback when WebSocket sending is unavailable.
- Chat presence is process-local and supports one active session per user; the newest duplicate connection wins.
- Notifications are persisted before best-effort realtime delivery.
- CORS accepts the React development origin, credentials, authorization headers, and browser preflights.
- JWTs use HS256 with `user_id` and the existing `expires` epoch claim.
- The real MongoDB collection names are case-sensitive: `User`, `Post`, `Comment`, `Message`, `UnReadedMsg`, and `Notification`.
