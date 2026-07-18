# Phase 1: Architecture & UI Design
## Unified Social Media Marketing & Ad Management Dashboard
*Serendia Solutions LLC — Principal Architect Review*

---

## 1. UI Wireframes

### 1.1 Unified Inbox

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│  SIDEBAR (240px fixed)         │  MAIN CONTENT AREA (flex-grow)                 │
│                                │                                                 │
│  ┌──────────────────────────┐  │  ┌─────────── TOOLBAR ──────────────────────┐  │
│  │  [Logo] Serendia         │  │  │  Unified Inbox          [Filter ▾] [⚙]   │  │
│  └──────────────────────────┘  │  │  [All Platforms ▾] [Unread] [Assigned]   │  │
│                                │  └──────────────────────────────────────────┘  │
│  ● Dashboard                  │                                                  │
│  ● Unified Inbox  [24]        │  ┌── COLUMN A: Message List (380px) ──────────┐ │
│  ● Scheduler                  │  │  SEARCH _________________________ [🔍]     │ │
│  ● Ad Campaigns               │  │  ─────────────────────────────────────────  │ │
│  ● Analytics                  │  │  [IG] Sarah K.     "Love this product!" ✦  │ │
│  ● AI Tools                   │  │       Comment · 2m ago           [POSITIVE] │ │
│  ● Settings                   │  │  ─────────────────────────────────────────  │ │
│                                │  │  [TT] @user99      "When does this ship?"  │ │
│  ──────────────────────────   │  │       DM · 5m ago               [QUESTION] │ │
│  WORKSPACES                   │  │  ─────────────────────────────────────────  │ │
│  ▸ Acme Corp                  │  │  [FB] John D.       "This is misleading..."  │ │
│  ▸ My Brand                   │  │       Mention · 12m ago         [NEGATIVE]  │ │
│  [+ Add Workspace]            │  │  ─────────────────────────────────────────  │ │
│                                │  │  [LI] Marketing Pro  "Great case study!"   │ │
│  ──────────────────────────   │  │       Comment · 1h ago          [POSITIVE]  │ │
│  USER AVATAR                  │  │  ─────────────────────────────────────────  │ │
│  Vibodha B. · Agency Plan     │  │  [X]  @techblog     "Interesting take..."   │ │
└────────────────────────────── │  │       Mention · 2h ago          [NEUTRAL]   │ │
                                 │  └────────────────────────────────────────────┘ │
                                 │                                                  │
                                 │  ┌── COLUMN B: Thread Detail (flex) ──────────┐ │
                                 │  │  [IG] Sarah K. · Original Post Thumbnail    │ │
                                 │  │  ──────────────────────────────────────────  │ │
                                 │  │  "Love this product! When can I buy?"       │ │
                                 │  │  [POSITIVE] [LABEL: VIP Customer] [Assign ▾]│ │
                                 │  │                                              │ │
                                 │  │  ┌────────────────────────────────────────┐ │ │
                                 │  │  │  Reply as @AcmeCorp (Instagram)        │ │ │
                                 │  │  │  ┌──────────────────────────────────┐  │ │ │
                                 │  │  │  │ Type your reply...   [AI Assist] │  │ │ │
                                 │  │  │  └──────────────────────────────────┘  │ │ │
                                 │  │  │  [Attach 📎] [Emoji 😊]   [Send ▶]    │ │ │
                                 │  │  └────────────────────────────────────────┘ │ │
                                 │  │                                              │ │
                                 │  │  CUSTOMER PROFILE PANEL                      │ │
                                 │  │  Sarah K. · @sarahk_style                   │ │
                                 │  │  IG Followers: 12,400                        │ │
                                 │  │  Past interactions: 7 · LTV: High            │ │
                                 │  └────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────────┘

COMPONENT HIERARCHY:
<AppShell>
  <Sidebar>
    <Logo />, <NavMenu />, <WorkspaceSwitcher />, <UserProfile />
  </Sidebar>
  <InboxPage>
    <InboxToolbar>
      <PlatformFilter />, <StatusFilter />, <SettingsButton />
    </InboxToolbar>
    <InboxLayout>
      <MessageList>
        <SearchBar />
        <MessageItem platform, sender, preview, sentiment, timestamp />[]
      </MessageList>
      <ThreadDetail>
        <MessageThread />
        <ReplyComposer>
          <TextArea />, <AIAssistButton />, <AttachmentPicker />, <SendButton />
        </ReplyComposer>
        <CustomerProfilePanel />
      </ThreadDetail>
    </InboxLayout>
  </InboxPage>
</AppShell>

USER FLOW:
1. User lands on Inbox → sees aggregated messages sorted by recency
2. Clicks platform filter → filters to single platform (e.g., Instagram only)
3. Clicks message item → thread loads in right panel with full context
4. Types reply → AI Assist suggests tone-appropriate response
5. Clicks Send → system routes via correct platform API → confirmation toast
```

---

### 1.2 Ad Campaign Dashboard

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│  SIDEBAR (same as above)       │  AD CAMPAIGN DASHBOARD                          │
│                                │                                                  │
│  ● Ad Campaigns ←─ ACTIVE     │  ┌── KPI CARDS ROW ────────────────────────────┐│
│                                │  │ ┌──────────┐ ┌──────────┐ ┌──────────────┐ ││
│                                │  │ │ TOTAL    │ │ AVG CPA  │ │ BUDGET USED  │ ││
│                                │  │ │ SPEND    │ │          │ │              │ ││
│                                │  │ │ $14,280  │ │ $3.42    │ │ $14.2K/20K  │ ││
│                                │  │ │ ▲ 8.2%   │ │ ▼ 12% ✓ │ │ ████████░░  │ ││
│                                │  │ └──────────┘ └──────────┘ └──────────────┘ ││
│                                │  │ ┌──────────────────────────────────────┐    ││
│                                │  │ │ AD REALLOCATION ENGINE    [ACTIVE ●] │    ││
│                                │  │ │ Meta CPA $3.21 ← Best    TikTok $4.12│    ││
│                                │  │ │ Shifted $800 → Meta (auto) [Settings]│    ││
│                                │  │ └──────────────────────────────────────┘    ││
│                                │  └─────────────────────────────────────────────┘│
│                                │                                                  │
│                                │  ┌── CAMPAIGN TABLE ───────────────────────────┐│
│                                │  │  [+ New Campaign]  [Bulk Edit]  [Export CSV] ││
│                                │  │  ──────────────────────────────────────────  ││
│                                │  │  Campaign Name     Platform  Status  Budget   ││
│                                │  │  ──────────────────────────────────────────  ││
│                                │  │  Summer Sale 2026  [IG][FB]  ● LIVE  $5,000  ││
│                                │  │  [Pause ⏸] [Edit ✏] [View Analytics →]       ││
│                                │  │  ──────────────────────────────────────────  ││
│                                │  │  Brand Awareness   [TT]      ● LIVE  $3,000  ││
│                                │  │  [Pause ⏸] [Edit ✏] [View Analytics →]       ││
│                                │  │  ──────────────────────────────────────────  ││
│                                │  │  Q3 Retargeting    [LI]      ⏸ PAUSED $2,000 ││
│                                │  │  [Resume ▶] [Edit ✏] [View Analytics →]      ││
│                                │  └─────────────────────────────────────────────┘│
│                                │                                                  │
│                                │  ┌── CREATIVE FATIGUE TRACKER ─────────────────┐│
│                                │  │  ⚠ ALERT: "Summer_Banner_v2.jpg"            ││
│                                │  │     Watch-through dropped 34% in 48h         ││
│                                │  │     [View Creative] [Swap Asset] [Dismiss]   ││
│                                │  └─────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────────────────────┘

COMPONENT HIERARCHY:
<AppShell>
  <Sidebar />
  <AdCampaignPage>
    <KPIRow>
      <KPICard metric, value, delta />[]
    </KPIRow>
    <ReallocationBanner status, sourcePlatform, targetPlatform, amount />
    <CampaignTable>
      <TableToolbar />
      <CampaignRow name, platforms[], status, budget, onPause, onEdit />[]
    </CampaignTable>
    <CreativeFatigueAlert asset, dropPercent, timeWindow />[]
  </AdCampaignPage>
</AppShell>

USER FLOW:
1. User opens Ad Campaigns → sees live KPI cards and reallocation status
2. Reviews table → pauses underperforming campaign inline
3. Receives fatigue alert → navigates to creative swap workflow
4. Clicks "+ New Campaign" → multi-step wizard: Platform → Audience → Creative → Budget → Review
5. Campaign launches → status badge turns LIVE, metrics begin populating
```

---

## 2. C4 Container Diagram

```mermaid
C4Container
    title Container Diagram — Unified Social Media Dashboard

    Person(marketer, "Digital Marketer", "Agency, entrepreneur, or SMB brand manager")

    System_Boundary(platform, "Serendia Dashboard Platform") {

        Container(spa, "React SPA", "React 18, Vite, TanStack Query", "Unified inbox, ad campaigns, scheduler, analytics UI")

        Container(gateway, "API Gateway", "Spring Cloud Gateway / Kong", "Routing, rate limiting, JWT validation, request logging")

        Container(auth_svc, "Auth Service", "Spring Boot, Java 21", "User registration, login, JWT issuance & refresh, RBAC")

        Container(core_svc, "Core Service", "Spring Boot, Java 21", "Workspace mgmt, social account registry, scheduling engine, webhook processor")

        Container(ai_svc, "AI Service", "Spring Boot + Python sidecar", "Content Slicer (LLM calls), sentiment analysis, creative fatigue scoring")

        Container(ad_svc, "Ad Integration Service", "Spring Boot, Java 21", "Meta Ads & TikTok Ads API calls, budget reallocation engine, CPA tracking")

        Container(inbox_svc, "Inbox Service", "Spring Boot, Java 21", "Message aggregation, routing replies, labeling, sentiment tagging")

        ContainerDb(pg_main, "PostgreSQL (Primary)", "RDS / Cloud SQL", "Users, workspaces, social_accounts, campaigns, messages, posts")

        ContainerDb(redis, "Redis Cluster", "ElastiCache / Memorystore", "Session store, API response cache, job queues (BullMQ)")

        ContainerDb(object_store, "Object Storage", "S3 / GCS", "Media uploads, exported reports, AI-generated assets")
    }

    System_Ext(meta, "Meta (Facebook/Instagram)", "Graph API, Ads API, Webhooks")
    System_Ext(tiktok, "TikTok", "TikTok API, Ads API")
    System_Ext(linkedin, "LinkedIn", "Marketing Dev Platform API")
    System_Ext(twitter_x, "X (Twitter)", "API v2")
    System_Ext(llm, "LLM Provider", "OpenAI / Gemini API")

    Rel(marketer, spa, "Uses", "HTTPS")
    Rel(spa, gateway, "API calls", "HTTPS/REST + WebSocket")
    Rel(gateway, auth_svc, "Auth routes", "HTTP/internal")
    Rel(gateway, core_svc, "Core routes", "HTTP/internal")
    Rel(gateway, ai_svc, "AI routes", "HTTP/internal")
    Rel(gateway, ad_svc, "Ad routes", "HTTP/internal")
    Rel(gateway, inbox_svc, "Inbox routes", "HTTP/internal")

    Rel(auth_svc, pg_main, "R/W users, sessions", "JDBC")
    Rel(core_svc, pg_main, "R/W workspaces, posts, accounts", "JDBC")
    Rel(ad_svc, pg_main, "R/W campaigns, budgets", "JDBC")
    Rel(inbox_svc, pg_main, "R/W messages, labels", "JDBC")

    Rel(core_svc, redis, "Cache, job queues", "Redis protocol")
    Rel(inbox_svc, redis, "Message queues", "Redis protocol")
    Rel(ad_svc, redis, "CPA cache, rate limit state", "Redis protocol")

    Rel(ai_svc, llm, "LLM inference", "HTTPS")
    Rel(core_svc, meta, "Publish, poll", "HTTPS + Webhooks")
    Rel(core_svc, tiktok, "Publish, poll", "HTTPS")
    Rel(core_svc, linkedin, "Publish, poll", "HTTPS")
    Rel(core_svc, twitter_x, "Publish, poll", "HTTPS")
    Rel(ad_svc, meta, "Campaign CRUD, metrics", "HTTPS")
    Rel(ad_svc, tiktok, "Campaign CRUD, metrics", "HTTPS")
    Rel(inbox_svc, meta, "Fetch DMs, comments", "HTTPS")
    Rel(inbox_svc, tiktok, "Fetch DMs, comments", "HTTPS")

    Rel(core_svc, object_store, "Upload/download media", "HTTPS SDK")
    Rel(ai_svc, object_store, "Store generated assets", "HTTPS SDK")
```

---

## 3. Entity-Relationship Diagram (ERD)

```mermaid
erDiagram

    USERS {
        uuid        id              PK
        varchar     email           UK
        varchar     password_hash
        varchar     full_name
        varchar     role            "OWNER | ADMIN | MEMBER | VIEWER"
        timestamptz created_at
        timestamptz last_login_at
        boolean     is_active
    }

    WORKSPACES {
        uuid        id              PK
        uuid        owner_id        FK
        varchar     name
        varchar     plan            "FREE | STARTER | PRO | AGENCY"
        jsonb       settings
        timestamptz created_at
    }

    WORKSPACE_MEMBERS {
        uuid        workspace_id    FK
        uuid        user_id         FK
        varchar     role            "ADMIN | MEMBER | VIEWER"
        timestamptz joined_at
    }

    SOCIAL_ACCOUNTS {
        uuid        id              PK
        uuid        workspace_id    FK
        varchar     platform        "FACEBOOK | INSTAGRAM | TIKTOK | LINKEDIN | X"
        varchar     platform_user_id UK
        varchar     display_name
        varchar     access_token_enc    "AES-256 encrypted"
        varchar     refresh_token_enc   "AES-256 encrypted"
        timestamptz token_expires_at
        varchar     scopes
        boolean     is_active
        timestamptz connected_at
    }

    CAMPAIGNS {
        uuid        id              PK
        uuid        workspace_id    FK
        varchar     name
        varchar     status          "DRAFT | ACTIVE | PAUSED | ARCHIVED"
        varchar     objective       "AWARENESS | TRAFFIC | CONVERSIONS | RETARGETING"
        decimal     daily_budget_usd
        decimal     total_budget_usd
        date        start_date
        date        end_date
        timestamptz created_at
        timestamptz updated_at
    }

    CAMPAIGN_PLATFORM_CONFIGS {
        uuid        id              PK
        uuid        campaign_id     FK
        uuid        social_account_id FK
        varchar     external_campaign_id    "Native platform campaign ID"
        varchar     status
        decimal     allocated_budget_usd
        decimal     spend_usd
        decimal     cpa_usd
        timestamptz last_synced_at
    }

    POSTS {
        uuid        id              PK
        uuid        workspace_id    FK
        varchar     status          "DRAFT | SCHEDULED | PUBLISHED | FAILED"
        text        base_content
        jsonb       platform_variants   "{ instagram: {caption, media_ids[]}, tiktok: {...} }"
        timestamptz scheduled_at
        timestamptz published_at
        integer     retry_count
    }

    INBOX_MESSAGES {
        uuid        id              PK
        uuid        workspace_id    FK
        uuid        social_account_id FK
        varchar     platform_message_id UK
        varchar     message_type    "DM | COMMENT | MENTION"
        varchar     sender_platform_id
        varchar     sender_name
        text        content
        varchar     sentiment       "POSITIVE | NEUTRAL | NEGATIVE"
        boolean     is_read
        boolean     is_assigned
        uuid        assigned_to     FK "nullable → USERS.id"
        jsonb       labels
        timestamptz platform_created_at
        timestamptz ingested_at
    }

    USERS            ||--o{ WORKSPACE_MEMBERS   : "belongs to"
    WORKSPACES       ||--o{ WORKSPACE_MEMBERS   : "has"
    USERS            ||--o{ WORKSPACES          : "owns"
    WORKSPACES       ||--o{ SOCIAL_ACCOUNTS     : "has"
    WORKSPACES       ||--o{ CAMPAIGNS           : "owns"
    WORKSPACES       ||--o{ POSTS               : "schedules"
    WORKSPACES       ||--o{ INBOX_MESSAGES      : "receives"
    CAMPAIGNS        ||--o{ CAMPAIGN_PLATFORM_CONFIGS : "targets"
    SOCIAL_ACCOUNTS  ||--o{ CAMPAIGN_PLATFORM_CONFIGS : "used by"
    SOCIAL_ACCOUNTS  ||--o{ INBOX_MESSAGES      : "sources"
    USERS            ||--o{ INBOX_MESSAGES      : "assigned"
```

---

## 4. OAuth 2.0 Sequence Diagram — Meta/TikTok Account Connection

```mermaid
sequenceDiagram
    autonumber
    actor       User
    participant  SPA     as React SPA
    participant  Core    as Core Service<br/>(Spring Boot)
    participant  Auth    as Auth Service<br/>(Spring Boot)
    participant  Vault   as Secret Store<br/>(AWS Secrets Manager)
    participant  Meta    as Meta / TikTok<br/>(OAuth Provider)
    participant  DB      as PostgreSQL

    User->>SPA: Clicks "Connect Instagram Account"
    SPA->>Core: POST /api/social/oauth/init<br/>{ platform: "INSTAGRAM", workspaceId }
    Core->>Core: Generate cryptographically random<br/>state param + PKCE code_verifier
    Core->>DB: Store state → redis (TTL 10min)
    Core-->>SPA: Return { authorizationUrl }

    SPA->>Meta: Browser redirect to<br/>Meta OAuth Authorization URL<br/>(client_id, redirect_uri, scope, state, code_challenge)
    Meta->>User: Show consent screen<br/>(permissions: pages_read, ads_read, etc.)
    User->>Meta: Grants permission
    Meta->>SPA: Redirect to callback URL<br/>?code=AUTH_CODE&state=STATE_PARAM

    SPA->>Core: POST /api/social/oauth/callback<br/>{ code, state, platform }
    Core->>Core: Validate state matches<br/>stored value (CSRF protection)
    Core->>Meta: POST /oauth/access_token<br/>{ code, code_verifier, client_secret }
    Meta-->>Core: { access_token, refresh_token,<br/>expires_in, scopes }

    Core->>Core: AES-256-GCM encrypt<br/>access_token + refresh_token
    Core->>Vault: Store encryption key<br/>keyed by social_account_id
    Core->>DB: INSERT social_accounts<br/>{ platform_user_id, access_token_enc,<br/>refresh_token_enc, token_expires_at, scopes }
    Core->>Meta: GET /me?fields=id,name<br/>(verify token validity)
    Meta-->>Core: { id, name }

    Core-->>SPA: 201 Created<br/>{ socialAccountId, displayName, platform }
    SPA->>User: Show success toast<br/>"Instagram @handle connected!"

    Note over Core, DB: Token Refresh Flow (background job)
    Core->>DB: Query accounts where<br/>token_expires_at < NOW() + 1 day
    Core->>Vault: Retrieve decryption key
    Core->>Core: Decrypt refresh_token
    Core->>Meta: POST /oauth/refresh_token<br/>{ refresh_token }
    Meta-->>Core: { new_access_token, new_expires_in }
    Core->>Core: Re-encrypt new token
    Core->>DB: UPDATE social_accounts<br/>SET access_token_enc, token_expires_at
```

---

## Design Decisions & Architecture Notes

| Decision | Rationale |
|---|---|
| **API Gateway as single ingress** | Centralizes JWT validation, rate limiting, and logging — services remain auth-agnostic |
| **Per-service fault isolation** | X API failure cannot cascade to Meta; each integration service is independently scalable (per SRS NFR) |
| **AES-256-GCM + Vault** | Tokens encrypted at rest per SRS security requirement; keys never stored in DB |
| **PKCE + state param** | Prevents CSRF and authorization code interception attacks on OAuth flows |
| **Redis job queues** | Decouples webhook ingestion from processing; enables retry logic for failed posts (per SRS reliability NFR) |
| **`platform_variants` as JSONB** | Accommodates platform-specific content customization without schema explosion |
| **`CAMPAIGN_PLATFORM_CONFIGS`** | Normalizes multi-platform campaign targeting; enables CPA comparison across platforms for reallocation engine |

---

*Phase 1 Complete — Awaiting approval to proceed to Phase 2 Implementation Checklist*
