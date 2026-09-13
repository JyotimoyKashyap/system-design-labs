#!/usr/bin/env bash
set -e

echo "🐳 Building System Design Labs inside Docker container..."
docker build -t system-design-labs:latest .

echo "✅ Docker build succeeded! Image: system-design-labs:latest"
echo "🚀 To run locally: docker compose up or docker run -p 3000:80 system-design-labs:latest"
