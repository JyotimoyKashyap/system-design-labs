import * as React from "react"
import { Button, buttonVariants } from "./ui/Button"
import { Badge } from "./ui/Badge"
import { Card, CardHeader, CardTitle, CardContent } from "./ui/Card"
import { colors, shadows, typography } from "../tokens"

export function DesignShowcase() {
  const [clickedButton, setClickedButton] = React.useState<string | null>(null);

  const handleButtonClick = (name: string) => {
    setClickedButton(name);
    setTimeout(() => setClickedButton(null), 1500);
  };

  return (
    <div className="w-full max-w-6xl mx-auto space-y-16 pb-20">
      {/* Header Banner */}
      <section className="bg-white border-3 border-stone-900 p-8 sm:p-12 shadow-[8px_8px_0px_0px_rgba(28,25,23,1)]">
        <div className="flex items-center gap-2 font-mono text-xs font-bold text-orange-600 uppercase tracking-widest mb-3">
          <span>DESIGN SYSTEM</span>
          <span>•</span>
          <span>SINGLE SOURCE OF TRUTH</span>
        </div>
        <h1 className="font-silkscreen not-italic text-3xl sm:text-5xl text-stone-900 mb-4 leading-tight">
          Paper Brutalist Standards
        </h1>
        <p className="text-stone-600 font-sans text-base sm:text-lg max-w-3xl leading-relaxed">
          This living style guide defines the universal tokens, button variants, card geometry, and typography 
          used across the portfolio, blog, and all distributed systems visualizers.
        </p>
      </section>

      {/* 1. Color Palette Tokens */}
      <section className="space-y-6">
        <div className="border-b-3 border-stone-900 pb-3">
          <h2 className="font-silkscreen not-italic text-2xl text-stone-900">
            01. Brand Color Palette
          </h2>
          <p className="text-stone-600 font-sans text-sm mt-1">
            Managed centrally in <code>packages/ui/src/tokens.ts</code>
          </p>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-4">
          <div className="border-2 border-stone-900 p-4 bg-white shadow-[4px_4px_0px_0px_rgba(28,25,23,1)]">
            <div className="h-16 w-full border-2 border-stone-900 mb-3 bg-[#FF6B6B]"></div>
            <h3 className="font-mono font-bold text-xs text-stone-900 uppercase">Primary</h3>
            <p className="font-mono text-[11px] text-stone-500">#FF6B6B</p>
            <p className="font-sans text-xs text-stone-700 mt-1">Launch App / CTAs</p>
          </div>

          <div className="border-2 border-stone-900 p-4 bg-white shadow-[4px_4px_0px_0px_rgba(28,25,23,1)]">
            <div className="h-16 w-full border-2 border-stone-900 mb-3 bg-[#4ECDC4]"></div>
            <h3 className="font-mono font-bold text-xs text-stone-900 uppercase">Secondary</h3>
            <p className="font-mono text-[11px] text-stone-500">#4ECDC4</p>
            <p className="font-sans text-xs text-stone-700 mt-1">Read Docs / Docs</p>
          </div>

          <div className="border-2 border-stone-900 p-4 bg-white shadow-[4px_4px_0px_0px_rgba(28,25,23,1)]">
            <div className="h-16 w-full border-2 border-stone-900 mb-3 bg-[#FBBF24]"></div>
            <h3 className="font-mono font-bold text-xs text-stone-900 uppercase">Accent Amber</h3>
            <p className="font-mono text-[11px] text-stone-500">#FBBF24</p>
            <p className="font-sans text-xs text-stone-700 mt-1">Navbar CTA / Highlight</p>
          </div>

          <div className="border-2 border-stone-900 p-4 bg-white shadow-[4px_4px_0px_0px_rgba(28,25,23,1)]">
            <div className="h-16 w-full border-2 border-stone-900 mb-3 bg-white"></div>
            <h3 className="font-mono font-bold text-xs text-stone-900 uppercase">Tertiary / Paper</h3>
            <p className="font-mono text-[11px] text-stone-500">#FFFFFF</p>
            <p className="font-sans text-xs text-stone-700 mt-1">Cards / Default Actions</p>
          </div>

          <div className="border-2 border-stone-900 p-4 bg-white shadow-[4px_4px_0px_0px_rgba(28,25,23,1)]">
            <div className="h-16 w-full border-2 border-stone-900 mb-3 bg-[#1C1917]"></div>
            <h3 className="font-mono font-bold text-xs text-stone-900 uppercase">Dark Pitch</h3>
            <p className="font-mono text-[11px] text-stone-500">#1C1917</p>
            <p className="font-sans text-xs text-stone-700 mt-1">Borders / High Contrast</p>
          </div>

          <div className="border-2 border-stone-900 p-4 bg-white shadow-[4px_4px_0px_0px_rgba(28,25,23,1)]">
            <div className="h-16 w-full border-2 border-stone-900 mb-3 bg-[#FDFCFB]"></div>
            <h3 className="font-mono font-bold text-xs text-stone-900 uppercase">Canvas</h3>
            <p className="font-mono text-[11px] text-stone-500">#FDFCFB</p>
            <p className="font-sans text-xs text-stone-700 mt-1">Page Grid Background</p>
          </div>
        </div>
      </section>

      {/* 2. Button Component Matrix */}
      <section className="space-y-6">
        <div className="border-b-3 border-stone-900 pb-3 flex flex-wrap items-center justify-between gap-4">
          <div>
            <h2 className="font-silkscreen not-italic text-2xl text-stone-900">
              02. Button Component Matrix
            </h2>
            <p className="text-stone-600 font-sans text-sm mt-1">
              Test hover lift, active press states, and contrast across all variants.
            </p>
          </div>
          {clickedButton && (
            <span className="font-mono text-xs font-bold text-emerald-700 bg-emerald-100 border-2 border-emerald-500 px-3 py-1 animate-pulse">
              Clicked: {clickedButton}
            </span>
          )}
        </div>

        {/* Variants Grid */}
        <div className="bg-white border-3 border-stone-900 p-6 sm:p-8 shadow-[8px_8px_0px_0px_rgba(28,25,23,1)] space-y-6">
          <h3 className="font-mono text-xs font-bold uppercase tracking-wider text-stone-500">Variants (Default Size)</h3>
          <div className="flex flex-wrap items-center gap-4">
            <Button variant="primary" onClick={() => handleButtonClick('Primary')}>
              Launch App (Primary)
            </Button>
            <Button variant="secondary" onClick={() => handleButtonClick('Secondary')}>
              Read Docs (Secondary)
            </Button>
            <Button variant="tertiary" onClick={() => handleButtonClick('Tertiary / Default')}>
              Tertiary / Default
            </Button>
            <Button variant="dark" onClick={() => handleButtonClick('Dark')}>
              Dark Variant
            </Button>
            <Button variant="accent" onClick={() => handleButtonClick('Accent')}>
              Accent Amber
            </Button>
            <Button variant="outline" onClick={() => handleButtonClick('Outline')}>
              Outline
            </Button>
          </div>

          <h3 className="font-mono text-xs font-bold uppercase tracking-wider text-stone-500 pt-6 border-t border-stone-200">
            Sizes (Small &rarr; Default &rarr; Large)
          </h3>
          <div className="flex flex-wrap items-center gap-4">
            <Button size="sm" variant="secondary" onClick={() => handleButtonClick('Small')}>
              Small Button (sm)
            </Button>
            <Button size="default" variant="primary" onClick={() => handleButtonClick('Default')}>
              Default Button (default)
            </Button>
            <Button size="lg" variant="dark" onClick={() => handleButtonClick('Large')}>
              Large Hero Action (lg)
            </Button>
          </div>
        </div>
      </section>

      {/* 3. Badge & Tag Matrix */}
      <section className="space-y-6">
        <div className="border-b-3 border-stone-900 pb-3">
          <h2 className="font-silkscreen not-italic text-2xl text-stone-900">
            03. Badges & Tag System
          </h2>
          <p className="text-stone-600 font-sans text-sm mt-1">
            Sharp Paper Brutalist label tags with 2px solid drop shadows.
          </p>
        </div>

        <div className="bg-white border-3 border-stone-900 p-6 sm:p-8 shadow-[8px_8px_0px_0px_rgba(28,25,23,1)] flex flex-wrap gap-4">
          <Badge variant="primary">Primary Tag</Badge>
          <Badge variant="secondary">Secondary Tag</Badge>
          <Badge variant="accent">Accent Tag</Badge>
          <Badge variant="dark">Dark Tag</Badge>
          <Badge variant="outline">Outline Tag</Badge>
          <Badge variant="success">Active / Success</Badge>
          <Badge variant="warning">Consensus / Warning</Badge>
          <Badge variant="destructive">Dead / Danger</Badge>
        </div>
      </section>

      {/* 4. Typography Specimen */}
      <section className="space-y-6">
        <div className="border-b-3 border-stone-900 pb-3">
          <h2 className="font-silkscreen not-italic text-2xl text-stone-900">
            04. Typography Hierarchy
          </h2>
          <p className="text-stone-600 font-sans text-sm mt-1">
            Strict non-italic rules: Silkscreen display headers, Inter body copy, and Monospace technical specs.
          </p>
        </div>

        <div className="bg-white border-3 border-stone-900 p-6 sm:p-8 shadow-[8px_8px_0px_0px_rgba(28,25,23,1)] space-y-6">
          <div className="pb-4 border-b border-stone-200">
            <span className="font-mono text-[11px] text-stone-500 uppercase">Display Header (Silkscreen, 400, not-italic)</span>
            <h3 className="font-silkscreen not-italic text-2xl sm:text-3xl text-stone-900 mt-1">
              DISTRIBUTED SYSTEMS LABS
            </h3>
          </div>

          <div className="pb-4 border-b border-stone-200">
            <span className="font-mono text-[11px] text-stone-500 uppercase">Card Title (Sans-Serif, Black 900)</span>
            <h4 className="font-sans font-black text-2xl text-stone-900 mt-1">
              Raft Leader Election & Consensus State Machine
            </h4>
          </div>

          <div className="pb-4 border-b border-stone-200">
            <span className="font-mono text-[11px] text-stone-500 uppercase">Body Prose (Inter, Regular 400)</span>
            <p className="font-sans text-stone-700 text-base leading-relaxed mt-1">
              Step inside the algorithms. Test node additions, trigger network partitions, inject false positives, 
              and watch distributed systems reach consensus in real time with zero hand-waving.
            </p>
          </div>

          <div>
            <span className="font-mono text-[11px] text-stone-500 uppercase">Technical Monospace (JetBrains Mono / Space Mono)</span>
            <p className="font-mono text-xs font-bold text-stone-800 mt-1">
              RPC_REQUEST_VOTE [term=4, candidateId=Node-2, lastLogIndex=12] &rarr; GRANTED
            </p>
          </div>
        </div>
      </section>

      {/* 5. Card Geometry & Elevation */}
      <section className="space-y-6">
        <div className="border-b-3 border-stone-900 pb-3">
          <h2 className="font-silkscreen not-italic text-2xl text-stone-900">
            05. Card Geometry & Elevation
          </h2>
          <p className="text-stone-600 font-sans text-sm mt-1">
            Always <code>rounded-none</code>, 2px-3px solid black borders, and hard offset drop shadows.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          {/* Interactive Card Example */}
          <div className="bg-white border-3 border-stone-900 p-6 sm:p-7 shadow-[8px_8px_0px_0px_rgba(28,25,23,1)] hover:-translate-x-1 hover:-translate-y-1 hover:shadow-[12px_12px_0px_0px_rgba(28,25,23,1)] transition-all flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between mb-4">
                <Badge variant="primary">Interactive Simulation</Badge>
                <span className="font-mono text-xs text-stone-500">Hover Me ↗</span>
              </div>
              <h3 className="font-sans font-black text-2xl text-stone-900 mb-3">
                Interactive Card Variant
              </h3>
              <p className="text-stone-700 text-sm leading-relaxed mb-6 font-sans">
                Features dynamic -1px translation and shadow expansion from 8px to 12px on hover. 
                Used across all portfolio cards.
              </p>
            </div>
            <div className="pt-4 border-t-2 border-stone-100 flex gap-3">
              <Button variant="primary">Launch App</Button>
              <Button variant="secondary">Read Docs</Button>
            </div>
          </div>

          {/* Static Panel Example */}
          <div className="bg-white border-2 border-stone-900 p-6 sm:p-7 shadow-[4px_4px_0px_0px_rgba(28,25,23,1)] flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between mb-4">
                <Badge variant="dark">Static Panel</Badge>
                <span className="font-mono text-xs text-stone-400">Fixed Elevation</span>
              </div>
              <h3 className="font-sans font-black text-2xl text-stone-900 mb-3">
                Static Container / Panel
              </h3>
              <p className="text-stone-700 text-sm leading-relaxed mb-6 font-sans">
                Stable Paper Brutalist container with 2px solid border and 4px offset shadow. 
                Used for simulation sidebars and dashboard widgets.
              </p>
            </div>
            <div className="pt-4 border-t-2 border-stone-100 flex gap-3">
              <Button variant="tertiary">Action A</Button>
              <Button variant="dark">Action B</Button>
            </div>
          </div>
        </div>
      </section>

      {/* 6. AI Agent Quick Reference */}
      <section className="bg-amber-50 border-3 border-stone-900 p-6 sm:p-8 shadow-[8px_8px_0px_0px_rgba(28,25,23,1)]">
        <h3 className="font-silkscreen not-italic text-xl text-stone-900 mb-2">
          🤖 Rules for AI Agents & Pair Programmers
        </h3>
        <ul className="list-disc list-inside space-y-2 text-stone-800 font-sans text-sm mt-3">
          <li><strong>Never hardcode colors or button classes:</strong> Import <code>buttonVariants</code> or <code>Button</code> from <code>@repo/ui</code>.</li>
          <li><strong>To update colors:</strong> Modify <code>packages/ui/src/tokens.ts</code>. All components and visualizers inherit from it.</li>
          <li><strong>To update buttons:</strong> Modify <code>packages/ui/src/components/ui/Button.tsx</code>.</li>
          <li><strong>Geometry Rule:</strong> <code>rounded-none</code> is unconditional across all buttons, cards, and panels.</li>
          <li><strong>Preview Live:</strong> Visit <code>http://localhost:4321/design</code> to verify changes across the whole system.</li>
        </ul>
      </section>
    </div>
  );
}
