# Campus Trade Platform (Graduation Project)

Vue3 + Java full-stack campus second-hand marketplace with Qiankun micro frontends and AI assistant.

## Tech Stack

- Frontend: Vue3, JavaScript, Arco Design, Vite, Qiankun
- Shared module: Module Federation remote (`mf-shared`)
- Backend: Spring Boot, Sa-Token, MySQL (schema/data scripts included)
- AI: OpenAI-compatible Chat Completions API

## Monorepo Structure

- `apps/shell`: main app shell (auth, role routing, micro app mount, AI drawer)
- `apps/portal`: buyer/seller portal sub-app
- `apps/ops`: operations center sub-app
- `apps/mf-shared`: shared auth/http/theme/status-tag/ai-sdk remote module
- `backend`: Java API for auth, items, review flow, orders, AI

## CommonEngine-style Micro-frontend Mode

The shell app follows a `dframes`-like architecture:

- dynamic app registration from backend (`GET /api/frame/registers`)
- dynamic route injection by role (`GET /api/frame/routes`)
- qiankun startup after registration data is ready
- cross-app communication via a message manager
- shell router proxy object passed to sub-apps via qiankun `props`

Key shell modules:

- `apps/shell/src/minFrame/index.js`
- `apps/shell/src/minFrame/pinia/framePinia.js`
- `apps/shell/src/minFrame/Message.js`
- `apps/shell/src/minFrame/ShellRouter.js`

## Required Local Environment

1. Node.js 20+
2. pnpm 9+
3. JDK 17+
4. Maven 3.9+
5. MySQL 8.x

## Frontend Install and Run

```bash
pnpm install
pnpm dev:front
```

`pnpm dev:front` builds `mf-shared` first, then starts `commonprovide`, `shell`, `portal`, and `ops`. This keeps `http://localhost:7199/assets/remoteEntry.js` available before the micro apps load it.

Frontend URLs:

- shell: `http://localhost:7100`
- portal: `http://localhost:7101`
- ops: `http://localhost:7102`
- mf-shared/commonprovide: `http://localhost:7199`

## Backend Run

```bash
cd backend
mvn spring-boot:run
```

Backend URL:

- API base: `http://localhost:8080/api`

## Demo Accounts

- buyer / 123456
- seller / 123456
- ops / 123456

## Implemented APIs

- `POST /api/auth/login`
- `POST /api/items`
- `GET /api/items`
- `GET /api/items/{id}`
- `GET /api/ops/reviews`
- `POST /api/ops/reviews/{itemId}/approve`
- `POST /api/ops/reviews/{itemId}/reject`
- `POST /api/orders`
- `GET /api/orders/mine`
- `POST /api/ai/chat`
- `GET /api/ai/sessions/recent`
- `GET /api/ai/sessions/{sessionId}/messages`
- `GET /api/ai/presets`
- `GET /api/frame/registers`
- `GET /api/frame/routes`

## One-click Start (Windows PowerShell)

```powershell
./scripts/start-all.ps1
```

## Notes

- Current backend skeleton uses in-memory seed store for quick local demo flow.
- `schema.sql` and `data.sql` are prepared for MySQL migration.
- AI assistant is read-only by design (Q&A and process guidance only).
