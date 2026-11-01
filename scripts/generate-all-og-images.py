#!/usr/bin/env python3
"""
Generate high-fidelity, Paper Brutalist Open Graph (OG) preview images (1200x630)
for every LLD, Lab, Note, Learning entry, and directory section.

Incremental by default:
- Automatically skips existing images unless the markdown source file was modified.
- Supports `--force` to re-generate everything.
- Generated images are stored in `apps/web/public/og/` so they are committed
  directly to GitHub as static assets and never need re-generation on CI or Docker.
"""

import argparse
import glob
import json
import os
import re
import sys
from PIL import Image, ImageDraw, ImageFont

ROOT_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
WEB_DIR = os.path.join(ROOT_DIR, "apps", "web")
PUBLIC_OG_DIR = os.path.join(WEB_DIR, "public", "og")
CONTENT_DIR = os.path.join(WEB_DIR, "src", "content")

WIDTH = 1200
HEIGHT = 630

CANVAS_BG = (253, 252, 251)  # #fdfcfb
DOT_COLOR = (214, 211, 209)  # #d6d3d1
INK = (28, 25, 23)           # #1c1917
INK_MUTED = (87, 83, 78)     # #57534e
WHITE = (255, 255, 255)
AMBER = (251, 191, 36)       # #fbbf24
TEAL = (78, 205, 196)        # #4ecdc4
CORAL = (255, 107, 107)      # #ff6b6b
PURPLE = (192, 132, 252)     # #c084fc
ORANGE = (249, 115, 22)      # #f97316

def get_font(path, size, fallback="Helvetica"):
    try:
        return ImageFont.truetype(path, size)
    except Exception:
        try:
            return ImageFont.truetype(f"/System/Library/Fonts/{fallback}.ttc", size)
        except Exception:
            return ImageFont.load_default()

font_brand = get_font("/System/Library/Fonts/Supplemental/Arial Bold.ttf", 22)
font_badge = get_font("/System/Library/Fonts/Menlo.ttc", 13)
font_desc = get_font("/System/Library/Fonts/Helvetica.ttc", 20)
font_tag = get_font("/System/Library/Fonts/Menlo.ttc", 13)
font_footer = get_font("/System/Library/Fonts/Menlo.ttc", 14)
font_logo = get_font("/System/Library/Fonts/Supplemental/Arial Black.ttf", 28)

def wrap_text(text, font, max_width, draw):
    words = text.split()
    lines = []
    current_line = []
    for word in words:
        test_line = " ".join(current_line + [word])
        bbox = draw.textbbox((0, 0), test_line, font=font)
        if (bbox[2] - bbox[0]) <= max_width:
            current_line.append(word)
        else:
            if current_line:
                lines.append(" ".join(current_line))
                current_line = [word]
            else:
                lines.append(word)
                current_line = []
    if current_line:
        lines.append(" ".join(current_line))
    return lines

def parse_frontmatter(text):
    data = {}
    lines = text.strip().split("\n")
    current_key = None
    for line in lines:
        line_clean = line.strip()
        if not line_clean or line_clean.startswith("#"):
            continue
        if ":" in line_clean and not line_clean.startswith("-"):
            parts = line_clean.split(":", 1)
            k = parts[0].strip()
            v = parts[1].strip()
            if v.startswith("[") and v.endswith("]"):
                try:
                    items = json.loads(v)
                except Exception:
                    items = [x.strip().strip("'\"") for x in v[1:-1].split(",") if x.strip()]
                data[k] = items
                current_key = None
            elif (v.startswith('"') and v.endswith('"')) or (v.startswith("'") and v.endswith("'")):
                data[k] = v[1:-1]
                current_key = None
            elif v.lower() == "true":
                data[k] = True
                current_key = None
            elif v.lower() == "false":
                data[k] = False
                current_key = None
            elif v:
                data[k] = v
                current_key = None
            else:
                data[k] = []
                current_key = k
        elif line_clean.startswith("-") and current_key:
            item = line_clean[1:].strip().strip("'\"")
            if isinstance(data.get(current_key), list):
                data[current_key].append(item)
    return data

def render_card(
    category_badge: str,
    badge_color: tuple,
    sub_badge: str,
    title: str,
    description: str,
    tags: list,
    url_path: str,
    out_path: str,
):
    img = Image.new("RGB", (WIDTH, HEIGHT), CANVAS_BG)
    draw = ImageDraw.Draw(img)

    # 1. Background Grid Dots
    for x in range(12, WIDTH, 24):
        for y in range(12, HEIGHT, 24):
            draw.rectangle([x, y, x + 1, y + 1], fill=DOT_COLOR)

    CARD_X0 = 40
    CARD_Y0 = 36
    CARD_X1 = WIDTH - 40
    CARD_Y1 = HEIGHT - 36
    SHADOW_OFFSET = 12

    # Draw Brutalist Card Shadow
    draw.rectangle(
        [CARD_X0 + SHADOW_OFFSET, CARD_Y0 + SHADOW_OFFSET, CARD_X1 + SHADOW_OFFSET, CARD_Y1 + SHADOW_OFFSET],
        fill=INK,
    )
    # Draw Brutalist Card Body
    draw.rectangle([CARD_X0, CARD_Y0, CARD_X1, CARD_Y1], fill=WHITE, outline=INK, width=4)

    # TOP BAR: Logo Box & Brand
    LOGO_X = CARD_X0 + 44
    LOGO_Y = CARD_Y0 + 36
    LOGO_SIZE = 46
    draw.rectangle([LOGO_X + 3, LOGO_Y + 3, LOGO_X + LOGO_SIZE + 3, LOGO_Y + LOGO_SIZE + 3], fill=(120, 113, 108))
    draw.rectangle([LOGO_X, LOGO_Y, LOGO_X + LOGO_SIZE, LOGO_Y + LOGO_SIZE], fill=INK)
    draw.text((LOGO_X + 12, LOGO_Y + 4), "J", font=font_logo, fill=WHITE)
    draw.text((LOGO_X + LOGO_SIZE + 16, LOGO_Y + 11), "JYOTIMOY KASHYAP", font=font_brand, fill=INK)

    # Top Badges
    def draw_badge(x, y, text, bg_color):
        bbox = draw.textbbox((0, 0), text, font=font_badge)
        tw = bbox[2] - bbox[0]
        th = bbox[3] - bbox[1]
        px, py = 14, 8
        bw, bh = tw + px * 2, th + py * 2
        draw.rectangle([x + 3, y + 3, x + bw + 3, y + bh + 3], fill=INK)
        draw.rectangle([x, y, x + bw, y + bh], fill=bg_color, outline=INK, width=2)
        draw.text((x + px, y + py), text, font=font_badge, fill=INK)
        return bw

    curr_x = CARD_X1 - 44
    if sub_badge:
        bbox_sub = draw.textbbox((0, 0), sub_badge, font=font_badge)
        bw_sub = (bbox_sub[2] - bbox_sub[0]) + 28
        curr_x -= bw_sub
        draw_badge(curr_x, LOGO_Y + 4, sub_badge, WHITE)
        curr_x -= 12

    if category_badge:
        bbox_cat = draw.textbbox((0, 0), category_badge, font=font_badge)
        bw_cat = (bbox_cat[2] - bbox_cat[0]) + 28
        curr_x -= bw_cat
        draw_badge(curr_x, LOGO_Y + 4, category_badge, badge_color)

    # TITLE
    MAX_TEXT_WIDTH = CARD_X1 - CARD_X0 - 88
    font_title_size = 44 if len(title) < 42 else 38
    font_title = get_font("/System/Library/Fonts/Supplemental/Arial Black.ttf", font_title_size)

    title_lines = wrap_text(title, font_title, MAX_TEXT_WIDTH, draw)
    title_y = CARD_Y0 + 125
    line_spacing = font_title_size + 12
    for line in title_lines[:2]:
        draw.text((CARD_X0 + 44, title_y), line, font=font_title, fill=INK)
        title_y += line_spacing

    # DESCRIPTION
    desc_lines = wrap_text(description, font_desc, MAX_TEXT_WIDTH, draw)
    desc_y = title_y + 12
    for line in desc_lines[:2]:
        draw.text((CARD_X0 + 44, desc_y), line, font=font_desc, fill=INK_MUTED)
        desc_y += 30

    # TAGS
    TAGS_Y = CARD_Y1 - 130
    tag_x = CARD_X0 + 44
    for tag_text in tags[:4]:
        tag_label = f"• {tag_text}"
        bbox = draw.textbbox((0, 0), tag_label, font=font_tag)
        tw = bbox[2] - bbox[0]
        th = bbox[3] - bbox[1]
        px, py = 12, 7
        bw, bh = tw + px * 2, th + py * 2
        if tag_x + bw > CARD_X1 - 44:
            break
        draw.rectangle([tag_x + 2, TAGS_Y + 2, tag_x + bw + 2, TAGS_Y + bh + 2], fill=INK)
        draw.rectangle([tag_x, TAGS_Y, tag_x + bw, TAGS_Y + bh], fill=(245, 245, 244), outline=INK, width=2)
        draw.text((tag_x + px, TAGS_Y + py), tag_label, font=font_tag, fill=INK)
        tag_x += bw + 12

    # FOOTER BAR
    FOOTER_LINE_Y = CARD_Y1 - 65
    draw.line([(CARD_X0, FOOTER_LINE_Y), (CARD_X1, FOOTER_LINE_Y)], fill=INK, width=3)

    FOOTER_TEXT_Y = FOOTER_LINE_Y + 20
    full_url = f"jyotimoykashyap.me/{url_path}".rstrip("/")
    draw.text((CARD_X0 + 44, FOOTER_TEXT_Y), full_url, font=font_footer, fill=INK)

    right_txt = "Personal Lab • Always building in the open"
    bbox_rt = draw.textbbox((0, 0), right_txt, font=font_footer)
    draw.text((CARD_X1 - 44 - (bbox_rt[2] - bbox_rt[0]), FOOTER_TEXT_Y), right_txt, font=font_footer, fill=INK_MUTED)

    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    img.save(out_path, format="PNG", optimize=True)


def should_generate(source_file: str, out_path: str, force: bool) -> bool:
    if force or not os.path.exists(out_path):
        return True
    if source_file and os.path.exists(source_file):
        return os.path.getmtime(source_file) > os.path.getmtime(out_path)
    return False


def process_content_files(force: bool = False):
    generated_count = 0
    skipped_count = 0

    # 1. LLD Collection
    lld_files = glob.glob(os.path.join(CONTENT_DIR, "lld", "*.md"))
    for f in lld_files:
        slug = os.path.splitext(os.path.basename(f))[0]
        out_path = os.path.join(PUBLIC_OG_DIR, "lld", f"{slug}.png")

        if not should_generate(f, out_path, force):
            skipped_count += 1
            continue

        with open(f, "r", encoding="utf-8") as fp:
            content = fp.read()
        m = re.search(r"^---\s*\n(.*?)\n---", content, re.DOTALL)
        if not m:
            continue
        fm = parse_frontmatter(m.group(1))
        title = fm.get("title", slug.replace("-", " ").title())
        desc = fm.get("description", "Object-oriented Low-Level Design and concurrency patterns.")
        tags = fm.get("designPatterns") or fm.get("tags") or ["Object-Oriented Design", "Concurrency"]

        render_card(
            category_badge="LOW-LEVEL DESIGN",
            badge_color=TEAL,
            sub_badge="OBJECT-ORIENTED ARCHITECTURE",
            title=title,
            description=desc,
            tags=tags,
            url_path=f"lld/{slug}",
            out_path=out_path,
        )
        print(f"  + Generated: lld/{slug}.png")
        generated_count += 1

    # 2. Labs Collection
    lab_files = glob.glob(os.path.join(CONTENT_DIR, "labs", "*.md"))
    for f in lab_files:
        slug = os.path.splitext(os.path.basename(f))[0]
        out_path = os.path.join(PUBLIC_OG_DIR, "labs", f"{slug}.png")

        if not should_generate(f, out_path, force):
            skipped_count += 1
            continue

        with open(f, "r", encoding="utf-8") as fp:
            content = fp.read()
        m = re.search(r"^---\s*\n(.*?)\n---", content, re.DOTALL)
        if not m:
            continue
        fm = parse_frontmatter(m.group(1))
        title = fm.get("title", slug.replace("-", " ").title())
        desc = fm.get("description", "Interactive visual simulation of distributed systems protocols.")
        tag = fm.get("tag", "Simulation")

        render_card(
            category_badge="INTERACTIVE LAB",
            badge_color=CORAL,
            sub_badge="BROWSER SIMULATION",
            title=title,
            description=desc,
            tags=[tag, "Distributed Systems", "Visual Simulation"],
            url_path=f"labs/{slug}",
            out_path=out_path,
        )
        print(f"  + Generated: labs/{slug}.png")
        generated_count += 1

    # 3. Notes Collection
    note_files = glob.glob(os.path.join(CONTENT_DIR, "notes", "*.md"))
    for f in note_files:
        slug = os.path.splitext(os.path.basename(f))[0]
        out_path = os.path.join(PUBLIC_OG_DIR, "notes", f"{slug}.png")

        if not should_generate(f, out_path, force):
            skipped_count += 1
            continue

        with open(f, "r", encoding="utf-8") as fp:
            content = fp.read()
        m = re.search(r"^---\s*\n(.*?)\n---", content, re.DOTALL)
        if not m:
            continue
        fm = parse_frontmatter(m.group(1))
        title = fm.get("title", slug.replace("-", " ").title())
        desc = fm.get("description", "Deep dive engineering note on architecture and systems thinking.")
        tags = fm.get("tags") or ["Distributed Systems", "Architecture"]

        render_card(
            category_badge="ENGINEERING NOTE",
            badge_color=AMBER,
            sub_badge="DISTRIBUTED SYSTEMS",
            title=title,
            description=desc,
            tags=tags,
            url_path=f"notes/{slug}",
            out_path=out_path,
        )
        print(f"  + Generated: notes/{slug}.png")
        generated_count += 1

    # 4. Learning Collection
    learning_files = glob.glob(os.path.join(CONTENT_DIR, "learning", "*.md"))
    for f in learning_files:
        slug = os.path.splitext(os.path.basename(f))[0]
        out_path = os.path.join(PUBLIC_OG_DIR, "learning", f"{slug}.png")

        if not should_generate(f, out_path, force):
            skipped_count += 1
            continue

        with open(f, "r", encoding="utf-8") as fp:
            content = fp.read()
        m = re.search(r"^---\s*\n(.*?)\n---", content, re.DOTALL)
        if not m:
            continue
        fm = parse_frontmatter(m.group(1))
        title = fm.get("title", slug.replace("-", " ").title())
        desc = fm.get("description", "Curated mental models, algorithms, and engineering clippings.")
        cat = fm.get("category", "Concept").upper()
        tags = fm.get("tags") or [cat.title()]

        render_card(
            category_badge="LEARNING VAULT",
            badge_color=PURPLE,
            sub_badge=cat,
            title=title,
            description=desc,
            tags=tags,
            url_path=f"learning/{slug}",
            out_path=out_path,
        )
        print(f"  + Generated: learning/{slug}.png")
        generated_count += 1

    # 5. Section Hub Banners
    sections = [
        ("lld", "LOW-LEVEL DESIGN", TEAL, "OBJECT-ORIENTED ARCHITECTURE", "Low-Level Design (LLD) Archive", "Clean code, extensible domain models, and high-concurrency patterns modeled from first principles in Java and TypeScript.", ["Design Patterns", "Concurrency", "Caches", "Event-Driven"]),
        ("labs", "INTERACTIVE LABS", CORAL, "BROWSER SIMULATIONS", "System Design Labs & Visualizers", "Explore complex distributed algorithms through interactive simulations: Raft consensus, consistent hashing, append-only logs, and bloom filters.", ["Raft", "Consistent Hashing", "Kafka", "RabbitMQ"]),
        ("notes", "ENGINEERING NOTES", AMBER, "DISTRIBUTED SYSTEMS", "Engineering Notes & Architectures", "In-depth notes exploring high-throughput systems, database scaling, caching topologies, and trade-offs.", ["Scalability", "Databases", "Distributed Systems"]),
        ("learning", "LEARNING VAULT", PURPLE, "CONCEPTS & CLIPPINGS", "Learning Vault & Knowledge Base", "Curated mental models, algorithm analyses, system designs, and notable engineering clippings from industry leaders.", ["Algorithms", "Mental Models", "Engineering Incidents"]),
    ]

    for sec_slug, badge_text, color, sub_badge, title, desc, tags in sections:
        out_path = os.path.join(PUBLIC_OG_DIR, "sections", f"{sec_slug}.png")
        if not should_generate(None, out_path, force):
            skipped_count += 1
            continue

        render_card(
            category_badge=badge_text,
            badge_color=color,
            sub_badge=sub_badge,
            title=title,
            description=desc,
            tags=tags,
            url_path=sec_slug,
            out_path=out_path,
        )
        print(f"  + Generated: sections/{sec_slug}.png")
        generated_count += 1

    # 6. Master Home Banner
    root_og_path = os.path.join(WEB_DIR, "public", "og-image.png")
    if should_generate(None, root_og_path, force):
        render_card(
            category_badge="SYSTEM DESIGN LABS",
            badge_color=AMBER,
            sub_badge="INTERACTIVE VISUALIZERS",
            title="DISTRIBUTED SYSTEMS & LLD",
            description="Interactive mental models, low-level design patterns, and high-performance backend architecture built from first principles.",
            tags=["⚡ Raft Consensus", "🔄 Consistent Hashing", "🎯 Bloom Filters", "📨 Kafka & RabbitMQ", "🔒 Lock Striping Cache"],
            url_path="",
            out_path=root_og_path,
        )
        print("  + Generated: public/og-image.png")
        generated_count += 1
    else:
        skipped_count += 1

    if generated_count > 0:
        print(f"✅ Generated {generated_count} new/updated image(s). (Skipped {skipped_count} up-to-date image(s))")
    else:
        print(f"⚡ All {skipped_count} Open Graph images are already up-to-date in git assets! (Nothing to generate)")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Generate Open Graph preview images.")
    parser.add_argument("--force", action="store_true", help="Force regenerate all images even if they already exist.")
    args = parser.parse_args()
    process_content_files(force=args.force)
