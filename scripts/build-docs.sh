#!/bin/bash
set -e

echo "Generating static documentation..."

mkdir -p deploy-hub/leader-election-web deploy-hub/bloom-filter-web deploy-hub/rabbitmq-web deploy-hub/kafka-web deploy-hub/db-scaling deploy-hub/redis-cache deploy-hub/consistent-hashing-web

cat << 'EOF' > template.html
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/github-markdown-css/5.5.0/github-markdown-light.min.css">
<style>
  body { 
    background-color: #fdfcfb; 
    padding: 40px 20px; 
    font-family: 'Inter', sans-serif;
  }
  .markdown-container {
    box-sizing: border-box;
    max-width: 900px;
    margin: 0 auto;
    padding: 45px;
    background-color: white;
    border: 3px solid #1c1917;
    box-shadow: 8px 8px 0px 0px rgba(28,25,23,1);
  }
  .back-btn {
    display: inline-block;
    padding: 8px 16px;
    margin-bottom: 30px;
    background: #e7e5e4;
    color: #1c1917;
    border: 2px solid #1c1917;
    text-decoration: none;
    font-size: 0.85rem;
    font-weight: 800;
    text-transform: uppercase;
    box-shadow: 4px 4px 0px 0px rgba(28,25,23,1);
    transition: all 0.15s ease;
  }
  .back-btn:hover {
    transform: translate(2px, 2px);
    box-shadow: 2px 2px 0px 0px rgba(28,25,23,1);
  }
</style>
</head>
<body>
  <div class="markdown-container markdown-body">
    <a href="../" class="back-btn">← Back to Hub</a>
EOF

npx marked leader-election-web/README.md | cat template.html - <(echo "  </div></body></html>") > deploy-hub/leader-election-web/docs.html
npx marked bloom-filter-web/README.md | cat template.html - <(echo "  </div></body></html>") > deploy-hub/bloom-filter-web/docs.html
npx marked rabbitmq-lab/rabbitmq-web/README.md | cat template.html - <(echo "  </div></body></html>") > deploy-hub/rabbitmq-web/docs.html
npx marked apache-kafka/kafka-web/README.md | cat template.html - <(echo "  </div></body></html>") > deploy-hub/kafka-web/docs.html
npx marked db-scaling/README.md | cat template.html - <(echo "  </div></body></html>") > deploy-hub/db-scaling/docs.html
npx marked redis-cache/README.md | cat template.html - <(echo "  </div></body></html>") > deploy-hub/redis-cache/docs.html
npx marked consistent-hashing/consistent-hashing-web/README.md | cat template.html - <(echo "  </div></body></html>") > deploy-hub/consistent-hashing-web/docs.html

rm template.html

echo "Documentation generation complete!"
