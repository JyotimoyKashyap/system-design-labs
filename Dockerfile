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

# Build standalone visualizers for backward compatibility
RUN if [ -d "leader-election-web" ]; then cd leader-election-web && npx vite build --base=./ || true; fi
RUN if [ -d "bloom-filter-web" ]; then cd bloom-filter-web && npx vite build --base=./ || true; fi
RUN if [ -d "rabbitmq-lab/rabbitmq-web" ]; then cd rabbitmq-lab/rabbitmq-web && npx vite build --base=./ || true; fi
RUN if [ -d "apache-kafka/kafka-web" ]; then cd apache-kafka/kafka-web && npx vite build --base=./ || true; fi
RUN if [ -d "consistent-hashing/consistent-hashing-web" ]; then cd consistent-hashing/consistent-hashing-web && npx vite build --base=./ || true; fi
RUN if [ -d "api-rate-limiter/api-rate-limiter-web" ]; then cd api-rate-limiter/api-rate-limiter-web && npx vite build --base=./ || true; fi

# Stage 2: Serve with Nginx
FROM nginx:alpine

# Copy custom Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy the primary modern Astro web build
COPY --from=builder /app/apps/web/dist /usr/share/nginx/html

# Copy standalone visualizer bundles for backward compatibility
COPY --from=builder /app/leader-election-web/dist /usr/share/nginx/html/leader-election-web
COPY --from=builder /app/bloom-filter-web/dist /usr/share/nginx/html/bloom-filter-web
COPY --from=builder /app/rabbitmq-lab/rabbitmq-web/dist /usr/share/nginx/html/rabbitmq-web
COPY --from=builder /app/apache-kafka/kafka-web/dist /usr/share/nginx/html/kafka-web
COPY --from=builder /app/consistent-hashing/consistent-hashing-web/dist /usr/share/nginx/html/consistent-hashing-web
COPY --from=builder /app/api-rate-limiter/api-rate-limiter-web/dist /usr/share/nginx/html/api-rate-limiter-web

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
