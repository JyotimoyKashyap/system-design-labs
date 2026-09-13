# Stage 1: Build Frontend Assets with Node 20
FROM node:20-slim AS builder

WORKDIR /app

# Copy all source files
COPY . .

# Remove host package-lock to avoid platform-specific binary mismatches
RUN rm -f package-lock.json

# Install dependencies cleanly inside Linux container
RUN npm install

# Build the Web Application (triggers prebuild -> scripts/auto-discover-content.mjs automatically)
RUN npm run build:web

# Stage 2: Serve with Nginx
FROM nginx:alpine

# Copy custom Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy the Astro web build output
COPY --from=builder /app/apps/web/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
