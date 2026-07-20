# OAuth 2.0 Third-Party Integration Setup Guide

This guide details how to register developer applications, obtain client credentials, and configure environment variables for third-party social integrations in **Serendia Social Dashboard**.

---

## 1. LinkedIn API V2 Integration

### Developer App Registration
1. Go to the [LinkedIn Developer Portal](https://developer.linkedin.com/).
2. Create a new App:
   - **App Name**: `Serendia Dashboard`
   - **Company**: Link your company page.
   - **App Logo**: Upload a logo.
3. Under the **Products** tab, request access to:
   - **Share on LinkedIn** (for publishing organic posts)
   - **Sign In with LinkedIn** (for user profile & email mapping)
4. Under the **Auth** tab:
   - Note your **Client ID** and **Client Secret**.
   - Add Authorized Redirect URL: `http://localhost:8080/oauth/callback`.

### Environment Configuration
Add to your `.env` file:
```env
LINKEDIN_CLIENT_ID=your_linkedin_client_id
LINKEDIN_CLIENT_SECRET=your_linkedin_client_secret
LINKEDIN_REDIRECT_URI=http://localhost:8080/oauth/callback
```

---

## 2. X (Twitter) API V2 Integration

### Developer App Registration
1. Go to the [X Developer Portal](https://developer.x.com/).
2. Create a Project and add an App under it.
3. Go to the App's **Settings** and click **Set up** under **User authentication settings**:
   - **App permissions**: Read and Write (minimum required for posting tweets).
   - **Type of App**: Web App, Automated App or Bot.
   - **App Info**:
     - Callback URI / Redirect URL: `http://localhost:8080/oauth/callback`.
     - Website URL: `http://localhost:8080`.
4. Save authentication settings and note your **OAuth 2.0 Client ID** and **Client Secret**. (Do *not* confuse these with API Key/Secret; OAuth 2.0 PKCE requires the client credentials shown under OAuth 2.0 settings).

### Environment Configuration
Add to your `.env` file:
```env
X_CLIENT_ID=your_x_oauth_client_id
X_CLIENT_SECRET=your_x_oauth_client_secret
X_REDIRECT_URI=http://localhost:8080/oauth/callback
```

---

## 3. Meta (Facebook/Instagram) Integration

### Developer App Registration
1. Go to the [Meta Developer Portal](https://developers.facebook.com/).
2. Create a new App of type **Business**.
3. Under **App Settings > Basic**:
   - Note the **App ID** and **App Secret**.
4. Add the product **Facebook Login for Business**:
   - Go to settings under Facebook Login.
   - Add Valid OAuth Redirect URIs: `http://localhost:8080/oauth/callback`.
5. Under **App Review > Permissions and Features**, request:
   - `pages_manage_posts`
   - `pages_read_engagement`
   - `ads_management`
   - `instagram_basic`
   - `instagram_manage_messages`

### Environment Configuration
Add to your `.env` file:
```env
META_APP_ID=your_meta_app_id
META_APP_SECRET=your_meta_app_secret
META_REDIRECT_URI=http://localhost:8080/oauth/callback
```

---

## 4. TikTok Marketing API Integration

### Developer App Registration
1. Go to the [TikTok Developer Portal](https://developers.tiktok.com/).
2. Register an app and request access to:
   - `user.info.basic`
   - `video.list`
3. Add Redirect URI: `http://localhost:8080/oauth/callback`.

### Environment Configuration
Add to your `.env` file:
```env
TIKTOK_CLIENT_KEY=your_tiktok_client_key
TIKTOK_CLIENT_SECRET=your_tiktok_client_secret
TIKTOK_REDIRECT_URI=http://localhost:8080/oauth/callback
```

---

## 5. Local Tunneling for Webhooks & Redirect Testing

While local development uses `localhost` redirect URIs, testing real webhook event ingestion (e.g., Meta messages) requires a public HTTPS URL.

We recommend using **ngrok**:
```bash
ngrok http 8080
```
Update your registered OAuth redirect URIs and `.env` redirect URIs to use the public ngrok address (e.g. `https://xxxx.ngrok-free.app/oauth/callback`).
