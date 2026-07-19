# =============================================================================
# Multi-stage Dockerfile — React SPA (Vite build → Nginx serve)
# =============================================================================

# --- Stage 1: Build ---
FROM node:22-alpine AS builder
WORKDIR /app

# Cache node_modules layer
COPY package.json package-lock.json* ./
RUN npm ci

# Build the SPA
COPY . .
RUN npm run build

# --- Stage 2: Serve with Nginx ---
FROM nginx:1.27-alpine AS runtime

# Remove default nginx config
RUN rm /etc/nginx/conf.d/default.conf

# Copy custom Nginx config for SPA (handles client-side routing)
COPY nginx.conf /etc/nginx/conf.d/app.conf

# Copy built assets
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
