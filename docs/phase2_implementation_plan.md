# Phase 2: Implementation Plan & Checklist
## Unified Social Media Marketing & Ad Management Dashboard
*Serendia Solutions LLC — Approved Architecture: Phase 1 ✅*

> **Stack**: React 18 + Vite (Frontend) · Spring Boot 3 / Java 21 (Backend) · PostgreSQL · Redis · Docker / Kubernetes

---

## Milestone 1: Environment Setup & Infrastructure

### 1.1 Repository & Monorepo Structure
- [ ] Initialize GitHub Organization `serendia-solutions`
- [ ] Create monorepo root `social-dashboard` with `pnpm` workspaces
  - [ ] `/apps/web` — React SPA
  - [ ] `/services/auth-service` — Spring Boot
  - [ ] `/services/core-service` — Spring Boot
  - [ ] `/services/ai-service` — Spring Boot + Python sidecar
  - [ ] `/services/ad-service` — Spring Boot
  - [ ] `/services/inbox-service` — Spring Boot
  - [ ] `/infra` — Terraform / Helm charts
  - [ ] `/shared/api-contracts` — OpenAPI specs
- [ ] Add `.editorconfig`, `.gitignore`, root `README.md`
- [ ] Configure branch protection: require PR + 1 reviewer on `main`

### 1.2 CI/CD Pipelines (GitHub Actions)
- [ ] Create workflow `ci-frontend.yml`
  - [ ] Lint (ESLint + Prettier check)
  - [ ] Unit tests (`vitest`)
  - [ ] Build (`vite build`) — fail on TS errors
- [ ] Create workflow `ci-backend.yml`
  - [ ] Run per-service matrix: `mvn verify` (unit + integration tests)
  - [ ] Static analysis: `SpotBugs`, `Checkstyle`
- [ ] Create workflow `docker-build.yml`
  - [ ] Build and push Docker images to ECR / Artifact Registry on merge to `main`
  - [ ] Tag images with git SHA
- [ ] Create workflow `deploy-staging.yml`
  - [ ] Trigger on tag `v*-rc*`
  - [ ] `helm upgrade --install` to staging K8s namespace
- [ ] Set GitHub secrets: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `DOCKER_REGISTRY_URL`

### 1.3 Container & Kubernetes Infrastructure
- [ ] Write multi-stage `Dockerfile` for each Spring Boot service (JDK 21 slim base)
- [ ] Write `Dockerfile` for React SPA (Nginx Alpine base)
- [ ] Create Helm chart `serendia-platform`
  - [ ] `values.yaml` with per-environment overrides (`staging`, `production`)
  - [ ] Deployments for all 6 containers (5 services + API Gateway)
  - [ ] `HorizontalPodAutoscaler` configs for `ai-service` and `inbox-service`
  - [ ] `ConfigMap` for non-secret environment variables
  - [ ] `Secret` references (mounted from AWS Secrets Manager via External Secrets Operator)
- [ ] Provision K8s staging cluster (EKS / GKE)
- [ ] Install External Secrets Operator on cluster
- [ ] Configure Nginx Ingress Controller with TLS (cert-manager + Let's Encrypt)
- [ ] Set up API Gateway (Kong or Spring Cloud Gateway) deployment
  - [ ] Define routes to all internal services
  - [ ] Enable rate limiting plugin (100 req/min per user)
  - [ ] Enable request logging plugin

### 1.4 Database Initialization
- [ ] Provision managed PostgreSQL instance (RDS `db.t4g.medium`, Multi-AZ for production)
- [ ] Create databases: `serendia_auth`, `serendia_core`, `serendia_ad`, `serendia_inbox`
- [ ] Provision Redis Cluster (ElastiCache, 2 shards + 1 replica each)
- [ ] Provision S3 bucket `serendia-media-assets` with versioning enabled
  - [ ] Configure CORS policy for SPA origin
  - [ ] Configure lifecycle rules (move to Glacier after 90 days)
- [ ] Create IAM role per service (least-privilege) for DB and S3 access
- [ ] Set up Flyway migration baseline `V0__baseline.sql` for each service DB schema

### 1.5 Observability Stack
- [ ] Deploy Prometheus + Grafana to cluster
- [ ] Configure Spring Boot Actuator `/metrics` endpoint on all services
- [ ] Create Grafana dashboard: service latency, error rate, DB connection pool
- [ ] Configure PagerDuty / Slack alert for error rate > 1% over 5 min
- [ ] Set up distributed tracing (OpenTelemetry agent on all JVMs → Jaeger / Tempo)

---

## Milestone 2: Security & Authentication

### 2.1 Auth Service — Core Setup (Spring Boot)
- [ ] Bootstrap `auth-service` with `spring-boot-starter-web`, `spring-boot-starter-security`, `spring-boot-starter-data-jpa`
- [ ] Add dependencies: `java-jwt` (Auth0), `flyway-core`, `postgresql` driver
- [ ] Create Flyway migration `V1__create_users.sql`
  - [ ] `USERS` table per approved ERD
  - [ ] Index on `email`
- [ ] Implement `UserEntity` JPA entity
- [ ] Implement `UserRepository` (Spring Data JPA)
- [ ] Implement `PasswordHashingService` (BCrypt, cost factor 12)

### 2.2 Registration & Login Endpoints
- [ ] `POST /auth/register`
  - [ ] Validate email format, password strength (min 12 chars, mixed case, digit)
  - [ ] Check email uniqueness — return `409 Conflict` if duplicate
  - [ ] Hash password, persist user, return `201 Created`
- [ ] `POST /auth/login`
  - [ ] Validate credentials
  - [ ] Issue short-lived Access Token (JWT, 15 min expiry, RS256)
  - [ ] Issue HttpOnly Secure Refresh Token cookie (7-day expiry)
  - [ ] Store refresh token hash in Redis (`refresh:{userId}` key)
- [ ] `POST /auth/refresh`
  - [ ] Read refresh token from cookie
  - [ ] Validate against Redis hash
  - [ ] Rotate refresh token (invalidate old, issue new)
  - [ ] Return new access token
- [ ] `POST /auth/logout`
  - [ ] Delete refresh token from Redis
  - [ ] Clear HttpOnly cookie

### 2.3 JWT Validation at API Gateway
- [ ] Configure API Gateway JWT verification filter
  - [ ] Load public key (RS256) from environment/Vault
  - [ ] Validate `exp`, `iss`, `aud` claims
  - [ ] Forward `X-User-Id` and `X-User-Role` headers to downstream services
  - [ ] Return `401` for missing/invalid token, `403` for insufficient role

### 2.4 RBAC Implementation
- [ ] Define role enum: `OWNER`, `ADMIN`, `MEMBER`, `VIEWER`
- [ ] Implement `@PreAuthorize` annotations on service endpoints
- [ ] Create `WorkspaceMembership` check: user must belong to target workspace
- [ ] Create Flyway migration `V2__create_workspace_members.sql`

### 2.5 OAuth 2.0 Integration — Meta (Facebook/Instagram)
- [ ] Register Meta App in Meta Developer Portal
  - [ ] Request scopes: `pages_manage_posts`, `pages_read_engagement`, `ads_management`, `instagram_basic`, `instagram_manage_messages`
  - [ ] Configure redirect URI: `https://api.serendia.io/social/oauth/meta/callback`
- [ ] Implement `OAuthInitController.initMeta()`
  - [ ] Generate 32-byte PKCE `code_verifier` (store in Redis TTL 10 min)
  - [ ] Generate `state` param (CSRF token, store in Redis TTL 10 min)
  - [ ] Build and return Meta authorization URL
- [ ] Implement `OAuthCallbackController.handleMetaCallback()`
  - [ ] Validate `state` from Redis (delete on validation)
  - [ ] Exchange `code` for tokens via `POST https://graph.facebook.com/oauth/access_token`
  - [ ] Encrypt `access_token` and `refresh_token` with AES-256-GCM
  - [ ] Store encryption key in AWS Secrets Manager under key `social/{accountId}/enc-key`
  - [ ] Persist `SOCIAL_ACCOUNTS` record with encrypted tokens
  - [ ] Verify token: `GET /me?fields=id,name`
- [ ] Implement background `TokenRefreshScheduler` (`@Scheduled`, every 6 hours)
  - [ ] Query accounts expiring within 24 hours
  - [ ] Decrypt, refresh, re-encrypt, update DB

### 2.6 OAuth 2.0 Integration — TikTok
- [ ] Register TikTok App in TikTok Developer Portal
  - [ ] Request scopes: `user.info.basic`, `video.list`, `tcm.order.get`, `biz.creator.info`
  - [ ] Configure redirect URI
- [ ] Implement `OAuthInitController.initTikTok()` (same PKCE pattern as Meta)
- [ ] Implement `OAuthCallbackController.handleTikTokCallback()`
  - [ ] Exchange code: `POST https://open.tiktokapis.com/v2/oauth/token/`
  - [ ] TikTok tokens expire in 24 hours — implement hourly refresh job
- [ ] Implement TikTok Ads OAuth separately (different credentials)

### 2.7 Secret Management
- [ ] Create AWS Secrets Manager secrets:
  - [ ] `serendia/jwt-private-key` (RSA 2048 PEM)
  - [ ] `serendia/jwt-public-key`
  - [ ] `serendia/meta-app-secret`
  - [ ] `serendia/tiktok-app-secret`
  - [ ] `serendia/db-credentials/{service}`
- [ ] Configure Spring Boot `aws-secretsmanager-config-server` integration
- [ ] Rotate DB credentials via Secrets Manager rotation lambda (every 30 days)

---

## Milestone 3: Core Backend Microservices

### 3.1 Core Service — Workspace & Account Management
- [ ] Bootstrap `core-service` with JPA, Web, Security starters
- [ ] Flyway migrations:
  - [ ] `V1__workspaces.sql` — `WORKSPACES`, `WORKSPACE_MEMBERS`
  - [ ] `V2__social_accounts.sql` — `SOCIAL_ACCOUNTS`
  - [ ] `V3__posts.sql` — `POSTS`
- [ ] Implement `WorkspaceController`
  - [ ] `POST /workspaces` — create workspace
  - [ ] `GET /workspaces/{id}` — get workspace (membership check)
  - [ ] `POST /workspaces/{id}/members` — invite member (send email via SES)
  - [ ] `DELETE /workspaces/{id}/members/{userId}` — remove member
- [ ] Implement `SocialAccountController`
  - [ ] `GET /workspaces/{id}/social-accounts` — list connected accounts
  - [ ] `DELETE /social-accounts/{id}` — disconnect account (revoke token)

### 3.2 Platform API Adapter Pattern
- [ ] Define `SocialPlatformAdapter` interface:
  ```java
  interface SocialPlatformAdapter {
      PostResult publishPost(PublishRequest req);
      List<InboxMessage> fetchMessages(SocialAccount account, Instant since);
      AccountMetrics fetchMetrics(SocialAccount account, DateRange range);
  }
  ```
- [ ] Implement `MetaAdapter` (Facebook + Instagram) using Meta Graph API client
  - [ ] Handle token decryption before each call
  - [ ] Implement exponential backoff on 429 responses
  - [ ] Map Meta API errors to internal `PlatformException` types
- [ ] Implement `TikTokAdapter`
- [ ] Implement `LinkedInAdapter`
- [ ] Implement `TwitterXAdapter` (polling-only, no webhooks)
- [ ] Register adapters in `PlatformAdapterRegistry` (Map by `PlatformType` enum)
- [ ] Implement circuit breaker per adapter using Resilience4j
  - [ ] `OPEN` after 5 failures in 10 seconds
  - [ ] `HALF_OPEN` after 30 seconds
  - [ ] Emit metric to Prometheus on state change

### 3.3 Publishing & Scheduling Engine
- [ ] Implement `PostSchedulerService`
  - [ ] `schedulePost(PostRequest req)` — persist `POSTS` record, enqueue Redis job
  - [ ] `cancelPost(UUID postId)` — update status to `CANCELLED`, remove from queue
- [ ] Implement Redis BullMQ-compatible job queue (via Lettuce Java client)
  - [ ] Job payload: `{ postId, platformTargets[], scheduledAt }`
  - [ ] Worker: `PostPublisherWorker` (polls queue, calls adapter per platform)
  - [ ] On failure: retry up to 3 times with exponential backoff, then set status `FAILED`
  - [ ] Dead-letter queue for permanently failed jobs
- [ ] Implement `PostController`
  - [ ] `POST /workspaces/{id}/posts` — create and optionally schedule post
  - [ ] `GET /workspaces/{id}/posts` — paginated list, filter by status/date
  - [ ] `PATCH /posts/{id}` — update draft or reschedule
  - [ ] `DELETE /posts/{id}` — cancel scheduled post

### 3.4 Webhook Listener (Meta)
- [ ] Register webhook in Meta App Dashboard
  - [ ] Endpoint: `POST /webhooks/meta`
  - [ ] Verify token: validate `X-Hub-Signature-256` HMAC on all requests
- [ ] Implement `MetaWebhookController`
  - [ ] `GET /webhooks/meta` — hub challenge verification
  - [ ] `POST /webhooks/meta` — parse event, publish to Redis channel `webhook:meta`
- [ ] Implement `WebhookEventProcessor` (async consumer)
  - [ ] Route `messages` events → Inbox Service
  - [ ] Route `feed` events → analytics enrichment queue

### 3.5 Inbox Service
- [ ] Bootstrap `inbox-service`
- [ ] Flyway migration `V1__inbox_messages.sql` per approved ERD
- [ ] Implement `MessageIngestionService`
  - [ ] Subscribe to Redis channel `webhook:meta` for real-time Meta events
  - [ ] Poll non-webhook platforms (X, LinkedIn) every 2 minutes via `@Scheduled`
  - [ ] Deduplicate by `platform_message_id` (upsert with `ON CONFLICT DO NOTHING`)
- [ ] Implement `SentimentTaggingService`
  - [ ] Call AI Service `/ai/sentiment` async after ingestion
  - [ ] Update `sentiment` column on response
- [ ] Implement `InboxController`
  - [ ] `GET /workspaces/{id}/inbox` — paginated, filter by platform/sentiment/label/status
  - [ ] `PATCH /inbox/{id}/read` — mark as read
  - [ ] `PATCH /inbox/{id}/assign` — assign to user
  - [ ] `POST /inbox/{id}/labels` — add label
  - [ ] `POST /inbox/{id}/reply` — route reply to correct platform adapter
- [ ] Implement WebSocket endpoint `ws://api/inbox/stream`
  - [ ] Push new message events to connected SPA clients in real time

### 3.6 Ad Integration Service
- [ ] Bootstrap `ad-service`
- [ ] Flyway migrations `V1__campaigns.sql`, `V2__campaign_platform_configs.sql`
- [ ] Implement `CampaignController`
  - [ ] `POST /workspaces/{id}/campaigns` — create campaign + platform configs
  - [ ] `GET /workspaces/{id}/campaigns` — list with aggregated KPIs
  - [ ] `PATCH /campaigns/{id}/status` — pause / resume (calls native platform API)
  - [ ] `PATCH /campaigns/{id}/budget` — update daily budget
- [ ] Implement `MetaAdsAdapter`
  - [ ] `createCampaign()` → Meta Ads API `POST /act_{adAccountId}/campaigns`
  - [ ] `pauseCampaign()` / `resumeCampaign()` → `POST /{campaignId}` with `status` field
  - [ ] `fetchMetrics()` → Meta Insights API with fields `spend,cpa,impressions`
- [ ] Implement `TikTokAdsAdapter`
  - [ ] `createCampaign()` → TikTok Ads API `/open_api/v1.3/campaign/create/`
  - [ ] `fetchMetrics()` → `/open_api/v1.3/report/integrated/get/`
- [ ] Implement `MetricsSyncScheduler` (`@Scheduled`, every 15 minutes)
  - [ ] For each active `CAMPAIGN_PLATFORM_CONFIG`, call adapter `fetchMetrics()`
  - [ ] Update `spend_usd`, `cpa_usd`, `last_synced_at`
- [ ] Implement `BudgetReallocationEngine`
  - [ ] Evaluate every 1 hour: compare `cpa_usd` across platform configs for same campaign
  - [ ] If CPA delta > 20%: compute shift amount (move up to 30% of budget from highest-CPA to lowest-CPA platform)
  - [ ] Call both platform adapters to update budget
  - [ ] Persist reallocation event to audit log table
  - [ ] Notify SPA via WebSocket push

### 3.7 AI Service
- [ ] Bootstrap `ai-service` with Spring Web + Python FastAPI sidecar
- [ ] Implement `/ai/sentiment` endpoint
  - [ ] Input: `{ text: string }`
  - [ ] Call LLM with few-shot classification prompt (POSITIVE / NEUTRAL / NEGATIVE)
  - [ ] Return `{ sentiment, confidence }`
- [ ] Implement `/ai/content-slicer` endpoint
  - [ ] Input: `{ url: string, targetPlatforms: string[] }`
  - [ ] Fetch URL content (BeautifulSoup / Readability.js)
  - [ ] Prompt LLM: "Generate a [platform]-optimized post from this article..."
  - [ ] Return `{ variants: { instagram: string, tiktok: string, twitter: string } }`
- [ ] Implement `/ai/fatigue-score` endpoint
  - [ ] Input: `{ assetId, metrics: [{ date, watchThroughRate, scrollStopRate }] }`
  - [ ] Apply linear regression to detect slope > –10% over 48h window
  - [ ] Return `{ fatigued: boolean, dropPercent, recommendation }`
- [ ] Implement LLM provider abstraction (swap OpenAI / Gemini via config)
- [ ] Implement prompt versioning: store prompts in DB table `AI_PROMPTS`

---

## Milestone 4: Frontend Scaffolding & State

### 4.1 Project Bootstrap
- [x] Initialize Vite + React 18 project in `/apps/web`
- [x] Configure `tsconfig.json` (strict mode, path aliases `@/` → `src/`)
- [x] Configure `vite.config.ts` (proxy `/api` → gateway in dev, env-based base URL)
- [x] Install and configure ESLint (`@typescript-eslint`, `eslint-plugin-react-hooks`)
- [x] Install and configure Prettier with `.prettierrc`
- [x] Install Husky + lint-staged (pre-commit: lint + format)

### 4.2 Design System & Component Library
- [x] Run `modern-web-guidance search "design system CSS tokens"` for current best practices
- [x] Define CSS custom properties in `src/styles/tokens.css`
- [x] Import Google Font `Inter` (variable weight) in `index.html`
- [x] Add global reset and base styles to `src/styles/global.css`
- [x] Create component stubs in `src/components/ui/` (Button, Badge, Avatar, Modal, Toast, DataTable, PlatformBadge, SentimentBadge, KPICard, Skeleton)

### 4.3 Application Shell & Routing
- [x] Install React Router v6
- [x] Define route structure in `src/router/index.tsx`
- [x] Implement `<ProtectedRoute>` wrapper
- [x] Implement `<AppShell>` layout component
- [x] Implement `<Sidebar>` component

### 4.4 State Management
- [x] Install TanStack Query v5
- [x] Configure `QueryClient` in `src/lib/queryClient.ts`
- [x] Install Zustand for local UI state
- [x] Create Zustand stores (useAuthStore, useWorkspaceStore, useInboxStore)
- [x] Implement API client `src/lib/api.ts` (Axios instance with refresh interceptor)

### 4.5 Authentication UI
- [x] Build `<LoginPage>`
- [x] Build `<RegisterPage>`
- [x] Build `<OAuthCallbackPage>`
- [x] Implement `useAuth()` hook

---

## Milestone 5: Feature Implementation

### 5.1 Unified Inbox — Frontend
- [x] Implement TanStack Query hook `useInboxMessages(workspaceId, filters)`
- [x] Build `<InboxPage>` layout (3-column layout)
- [x] Build `<MessageList>` component with platform and sentiment badges
- [x] Build `<ThreadDetail>` component with label manager, team assignment, and composer
- [x] Implement WebSocket client `src/lib/inboxSocket.ts` to push real-time toast alerts

### 5.2 Unified Inbox — Backend Integration Points
- [x] Validate `POST /inbox/{id}/reply` end-to-end (replies successfully dispatch)

### 5.3 Ad Campaign Dashboard — Frontend
- [x] Implement TanStack Query hooks: `useCampaigns` and mutations
- [x] Build `<CampaignDashboardPage>` with KPIRow, ReallocationBanner, and CampaignTable
- [x] Build `<NewCampaignWizard>` modal wizard

### 5.4 Budget Reallocation Engine — Backend
- [x] Implement `ReallocationEventEntity` + table `reallocation_events`
- [x] Expose `GET /ad/campaigns/{id}/reallocation-history` endpoint
- [x] Implement `BudgetReallocationEngine` with automated CPA delta threshold logic and shifts

### 5.5 AI Content Slicer — Frontend
- [x] Build `<AIToolsPage>` layout
- [x] Build `<ContentSlicerWidget>` (platform checks, loading skeleton, variant copy cards)

### 5.6 Content Scheduler — Frontend
- [x] Build `<SchedulerPage>` with monthly calendar representation
- [x] Build `<NewPostModal>` (multi-platform composer with target platform selections)

### 5.7 Analytics Page — Frontend
- [x] Install `recharts` for data visualization
- [x] Build `<AnalyticsPage>` with area reach trend, audience share pie charts, and top-performing post table
- [x] `[Export PDF]` trigger simulation

---

## Milestone 6: Testing & QA

### 6.1 Backend Unit Tests
- [ ] `AuthServiceTest` — register, login, refresh, logout flows (JUnit 5 + Mockito)
- [ ] `TokenEncryptionServiceTest` — encrypt/decrypt roundtrip, tamper detection
- [ ] `OAuthCallbackServiceTest` — state validation, code exchange mock, token storage
- [ ] `PostSchedulerServiceTest` — schedule, cancel, retry logic
- [ ] `BudgetReallocationEngineTest` — CPA delta threshold, budget shift calculation
- [ ] `MetaAdapterTest` — API call construction, error mapping, rate limit backoff
- [ ] `TikTokAdapterTest` — same pattern
- [ ] `MessageIngestionServiceTest` — deduplication logic
- [ ] `SentimentTaggingServiceTest` — async call, DB update
- [ ] `ContentSlicerServiceTest` — prompt construction, variant parsing
- [ ] Target: **≥ 80% line coverage** per service (`mvn jacoco:report`)

### 6.2 Backend Integration Tests
- [ ] Use Testcontainers for PostgreSQL and Redis in all integration tests
- [ ] `AuthIntegrationTest` — full register → login → refresh → logout via MockMvc
- [ ] `InboxIntegrationTest` — ingest message → fetch via API → mark read → reply
- [ ] `CampaignIntegrationTest` — create campaign → pause → resume → metrics sync
- [ ] `WebhookIntegrationTest` — POST to `/webhooks/meta`, verify event queued in Redis
- [ ] `OAuthIntegrationTest` — mock Meta OAuth server (WireMock), full callback flow

### 6.3 Frontend Unit Tests (Vitest + React Testing Library)
- [ ] `LoginPage.test.tsx` — form validation, submit success, submit error states
- [ ] `MessageItem.test.tsx` — renders platform badge, sentiment badge, truncates text
- [ ] `ReplyComposer.test.tsx` — char count, submit dispatches mutation, clears on success
- [ ] `CampaignTable.test.tsx` — renders rows, pause button calls mutation
- [ ] `KPICard.test.tsx` — renders value, positive/negative delta color
- [ ] `useInboxMessages.test.tsx` — mock server, test infinite scroll, filter params
- [ ] `useAuth.test.tsx` — login sets token, 401 triggers refresh, logout clears store

### 6.4 Visual Regression Testing (Chrome DevTools)
- [ ] Use `chrome-devtools` skill to capture baseline screenshots
- [ ] Create test script `scripts/visual-regression.ts`
  - [ ] Navigate to `/login` → capture screenshot `login-baseline.png`
  - [ ] Navigate to `/inbox` (mocked data) → capture `inbox-baseline.png`
  - [ ] Navigate to `/campaigns` → capture `campaigns-baseline.png`
- [ ] Integrate screenshot diffing with `pixelmatch` in CI
  - [ ] Fail PR if pixel diff > 0.5% on any baseline
- [ ] Add Chrome DevTools performance audit
  - [ ] Run Lighthouse programmatically on `/dashboard`
  - [ ] Fail CI if LCP > 2.5s or TBT > 200ms

### 6.5 End-to-End Tests (Playwright)
- [ ] Install `@playwright/test`
- [ ] Write `e2e/auth.spec.ts` — register → login → see dashboard → logout
- [ ] Write `e2e/inbox.spec.ts` — view inbox, filter by platform, send reply
- [ ] Write `e2e/campaigns.spec.ts` — view campaigns, pause campaign, verify status change
- [ ] Write `e2e/content-slicer.spec.ts` — input URL, select platforms, view generated content
- [ ] Run E2E tests against staging environment in `deploy-staging.yml` workflow

### 6.6 Load Testing (Gatling)
- [ ] Write Gatling simulation `InboxLoadSimulation.scala`
  - [ ] Ramp to 500 concurrent users over 2 minutes
  - [ ] Scenario: login → poll inbox → mark read → reply
  - [ ] Assert: p95 response time < 500ms, error rate < 0.1%
- [ ] Write `CampaignMetricsSimulation.scala`
  - [ ] 200 concurrent users fetching campaign KPIs
  - [ ] Assert: p95 < 300ms (Redis cache hit)
- [ ] Run load tests against staging before every production release

### 6.7 Security Testing
- [ ] Run OWASP ZAP baseline scan against staging API
  - [ ] Assert: no HIGH or CRITICAL findings in CI
- [ ] Test CSRF protection: verify state param rejection on mismatch
- [ ] Test JWT: expired token returns 401, tampered token returns 401
- [ ] Test RBAC: VIEWER role cannot POST /campaigns (verify 403)
- [ ] Test SQL injection: parameterized queries verified via Checkmarx / SonarQube scan
- [ ] Validate `X-Hub-Signature-256` — verify webhook rejected without valid HMAC

---

## Milestone Summary

| # | Milestone | Owner | Dependencies |
|---|---|---|---|
| 1 | Environment Setup & Infrastructure | DevOps | None |
| 2 | Security & Authentication | Backend | M1 |
| 3 | Core Backend Microservices | Backend | M2 |
| 4 | Frontend Scaffolding & State | Frontend | M1 |
| 5 | Feature Implementation | Full Stack | M3 + M4 |
| 6 | Testing & QA | QA + Full Stack | M5 |
| 7 | OAuth 2.0 Social Platform Setup Wizard | Full Stack | M2 + M4 |

---

## Milestone 7: OAuth 2.0 Social Platform Setup Wizard

Adds a first-login onboarding setup screen where users connect their social media accounts via real OAuth 2.0 flows before accessing the dashboard. Extends backend OAuth clients to support LinkedIn and X/Twitter in addition to existing Meta and TikTok support.

### 7.1 Backend — `auth-service`
- [ ] Add Flyway migration `V4__add_onboarding_flag.sql` — `ALTER TABLE users ADD COLUMN onboarding_complete BOOLEAN NOT NULL DEFAULT FALSE;`
- [ ] Update `UserEntity` with `onboardingComplete` field
- [ ] Include `onboardingComplete` in `AuthResponse` DTO returned on login
- [ ] Implement `PATCH /auth/users/{userId}/onboarding-complete` internal endpoint (protected by `X-Internal-Secret` header, not exposed via Nginx)

### 7.2 Backend — `core-service` (New OAuth Clients)
- [ ] Implement `LinkedInOAuthClient.java`
  - [ ] PKCE authorization URL → `https://www.linkedin.com/oauth/v2/authorization`
  - [ ] Scopes: `r_liteprofile`, `r_emailaddress`, `w_member_social`, `rw_organization_admin`
  - [ ] Token exchange: `POST https://www.linkedin.com/oauth/v2/accessToken`
- [ ] Implement `XOAuthClient.java`
  - [ ] PKCE authorization URL → `https://twitter.com/i/oauth2/authorize`
  - [ ] Scopes: `tweet.read`, `tweet.write`, `users.read`, `offline.access`
  - [ ] Token exchange: `POST https://api.twitter.com/2/oauth2/token`
- [ ] Wire new clients into `OAuthService.initOAuth()` and `OAuthService.handleCallback()` switch expressions
- [ ] Add `parsePlatform()` aliases for `"LINKEDIN"` → `SocialPlatform.LINKEDIN` and `"X"` / `"TWITTER"` → `SocialPlatform.X`
- [ ] After successful callback, call `auth-service` internal REST endpoint to set `onboarding_complete = true`
- [ ] Implement `GET /core/workspaces/{workspaceId}/onboarding-status` returning `{ platformsConnected, count }`

### 7.3 Frontend — React SPA
- [ ] Create `src/stores/useOnboardingStore.ts` (Zustand, persisted to `localStorage`)
- [ ] Update `useAuthStore.setAuth()` to also seed `useOnboardingStore` from login response
- [ ] Create `src/pages/SocialSetupPage.tsx` (route: `/setup`)
  - [ ] Welcome header with Serendia branding
  - [ ] Progress bar ("X of 4 platforms connected")
  - [ ] Platform cards for Meta, TikTok, LinkedIn, X — each with status badge and Connect button
  - [ ] "Continue to Dashboard" button (enabled when ≥1 platform connected)
  - [ ] "Skip for now" link (marks onboarding complete, navigates to `/dashboard`)
- [ ] Update `ProtectedRoute.tsx` — redirect to `/setup` when `onboardingComplete === false` (exempt `/setup` route itself)
- [ ] Update `OAuthCallbackPage.tsx` — redirect to `/setup` if `onboardingComplete === false`, else `/settings`
- [ ] Add `/setup` route to `router/index.tsx`
- [ ] Add LinkedIn and X connection cards to `SettingsPage.tsx`

### 7.4 Environment Variables
- [ ] Add `LINKEDIN_CLIENT_ID`, `LINKEDIN_CLIENT_SECRET` to `.env.example`
- [ ] Add `X_CLIENT_ID`, `X_CLIENT_SECRET` to `.env.example`
- [ ] Add `INTERNAL_SERVICE_SECRET` to `.env.example` (used by core-service → auth-service internal calls)

### 7.5 Documentation
- [ ] Create `docs/oauth_setup_guide.md` covering LinkedIn and X developer app registration, env vars, and local testing with ngrok
- [ ] Update `README.md` to reference the new setup guide

---

*Phase 2 Implementation Plan — Serendia Solutions LLC · July 2026*
