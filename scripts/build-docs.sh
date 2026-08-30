#!/bin/bash
set -e

echo "Generating static documentation..."

mkdir -p deploy-hub/leader-election-web deploy-hub/bloom-filter-web deploy-hub/rabbitmq-web deploy-hub/kafka-web deploy-hub/db-scaling deploy-hub/redis-cache deploy-hub/consistent-hashing-web deploy-hub/api-rate-limiter-web deploy-hub/cache-lld

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

cat << 'EOF_FOOTER' > footer.html
  </div>
  <!-- Syntax Highlighting -->
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/styles/github.min.css">
  <script src="https://cdnjs.cloudflare.com/ajax/libs/highlight.js/11.9.0/highlight.min.js"></script>
  <script>hljs.highlightAll();</script>
  
  <!-- Mermaid -->
  <script type="module">
    import mermaid from 'https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.esm.min.mjs';
    mermaid.initialize({ startOnLoad: false, theme: 'default' });
    
    document.querySelectorAll('code.language-mermaid').forEach(async (block, index) => {
      const pre = block.parentElement;
      const graphDef = block.textContent;
      try {
        const { svg } = await mermaid.render(`mermaid-graph-${index}`, graphDef);
        const div = document.createElement('div');
        div.style.textAlign = 'center';
        div.style.margin = '20px 0';
        div.innerHTML = svg;
        pre.replaceWith(div);
      } catch (e) {
        console.error('Mermaid parsing error', e);
      }
    });
  </script>
</body>
</html>
EOF_FOOTER

npx marked leader-election-web/README.md | cat template.html - footer.html > deploy-hub/leader-election-web/docs.html
npx marked bloom-filter-web/README.md | cat template.html - footer.html > deploy-hub/bloom-filter-web/docs.html
npx marked rabbitmq-lab/rabbitmq-web/README.md | cat template.html - footer.html > deploy-hub/rabbitmq-web/docs.html
npx marked apache-kafka/kafka-web/README.md | cat template.html - footer.html > deploy-hub/kafka-web/docs.html
npx marked db-scaling/README.md | cat template.html - footer.html > deploy-hub/db-scaling/docs.html
npx marked redis-cache/README.md | cat template.html - footer.html > deploy-hub/redis-cache/docs.html
npx marked consistent-hashing/consistent-hashing-web/README.md | cat template.html - footer.html > deploy-hub/consistent-hashing-web/docs.html
npx marked api-rate-limiter/api-rate-limiter-web/README.md | cat template.html - footer.html > deploy-hub/api-rate-limiter-web/docs.html

npx marked cache-lld/README.md | cat template.html - footer.html > deploy-hub/cache-lld/docs.html

rm template.html footer.html

echo "Documentation generation complete!"
