import test from "node:test";
import assert from "node:assert/strict";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const rootDir = path.resolve(__dirname, "..");

test("Docker Pipeline: Dockerfile defines resilient multi-stage build", () => {
  const dockerfilePath = path.join(rootDir, "Dockerfile");
  assert.ok(fs.existsSync(dockerfilePath), "Dockerfile must exist at repository root");

  const content = fs.readFileSync(dockerfilePath, "utf8");
  assert.match(content, /FROM node:20/i, "Stage 1 must use Node 20 environment");
  assert.match(content, /npm run build:web/i, "Must execute npm run build:web inside container");
  assert.match(content, /FROM nginx:alpine/i, "Stage 2 must use nginx:alpine");
  assert.match(content, /COPY --from=builder \/app\/apps\/web\/dist \/usr\/share\/nginx\/html/i, "Must copy Astro build into Nginx html root");
  assert.match(content, /EXPOSE 80/i, "Must expose port 80");
});

test("Docker Pipeline: Nginx configuration supports clean URLs and dual-stack IPv4/IPv6", () => {
  const nginxConfPath = path.join(rootDir, "nginx.conf");
  assert.ok(fs.existsSync(nginxConfPath), "nginx.conf must exist at repository root");

  const content = fs.readFileSync(nginxConfPath, "utf8");
  assert.match(content, /listen 80;/i, "Nginx must listen on IPv4 port 80");
  assert.match(content, /listen \[::\]:80;/i, "Nginx must listen on IPv6 [::]:80");
  assert.match(content, /try_files\s+\$uri\s+\$uri\/\s+\$uri\.html\s+\/index\.html\s+=404;/i, "Nginx must support clean Astro URL routing");
  assert.match(content, /gzip on;/i, "Nginx must enable gzip compression");
});

test("Docker Pipeline: docker-compose defines web service on port 3000", () => {
  const composePath = path.join(rootDir, "docker-compose.yml");
  assert.ok(fs.existsSync(composePath), "docker-compose.yml must exist at repository root");

  const content = fs.readFileSync(composePath, "utf8");
  assert.match(content, /services:/i, "docker-compose must define services");
  assert.match(content, /3000:80/i, "docker-compose must map host port 3000 to container port 80");
  assert.match(content, /container_name:\s*system-design-labs-web/i, "Container must be named system-design-labs-web");
});
