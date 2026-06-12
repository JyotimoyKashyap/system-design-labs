# Stage 1: Build Frontend Assets and Docs
FROM node:26-slim AS builder

WORKDIR /app

# Install global dependencies for docs generation
RUN npm install -g marked

# Copy package configurations
COPY package.json package-lock.json ./
# Copy all source code
COPY . .

# Delete package-lock.json so npm generates a fresh dependency tree for Linux
RUN rm -f package-lock.json

# Install dependencies fresh for the Linux container architecture
RUN npm install

# Build Visualizers
# Ensure --base is set so assets are loaded relative to their subdirectory
RUN cd leader-election-web && npx vite build --base=./
RUN cd bloom-filter-web && npx vite build --base=./
RUN cd rabbitmq-lab/rabbitmq-web && npx vite build --base=./
RUN cd apache-kafka/kafka-web && npx vite build --base=./
RUN cd consistent-hashing/consistent-hashing-web && npx vite build --base=./

# Generate Documentation HTML
RUN ./scripts/build-docs.sh

# Stage 2: Serve with Nginx
FROM nginx:alpine

# Copy the Hub landing page
COPY --from=builder /app/deploy-hub /usr/share/nginx/html

# Copy the built visualizers into the Nginx web root
COPY --from=builder /app/leader-election-web/dist /usr/share/nginx/html/leader-election-web
COPY --from=builder /app/bloom-filter-web/dist /usr/share/nginx/html/bloom-filter-web
COPY --from=builder /app/rabbitmq-lab/rabbitmq-web/dist /usr/share/nginx/html/rabbitmq-web
COPY --from=builder /app/apache-kafka/kafka-web/dist /usr/share/nginx/html/kafka-web
COPY --from=builder /app/consistent-hashing/consistent-hashing-web/dist /usr/share/nginx/html/consistent-hashing-web

# Expose port 80 (Nginx default)
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
