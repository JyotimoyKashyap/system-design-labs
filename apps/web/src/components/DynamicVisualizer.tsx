import React from 'react';
import { visualizerRegistry, hasVisualizer } from '../labs/registry';
import { url } from '../utils/url';

export { hasVisualizer };

interface DynamicVisualizerProps {
  slug: string;
}

export default function DynamicVisualizer({ slug }: DynamicVisualizerProps) {
  const Component = visualizerRegistry[slug];

  if (!Component) {
    return (
      <div className="flex flex-col items-center justify-center h-full p-8 text-center bg-[#fdfcfb]">
        <div className="max-w-md border-3 border-stone-900 bg-white p-6 shadow-[4px_4px_0px_0px_rgba(28,25,23,1)]">
          <span className="text-3xl mb-2 block">🧪</span>
          <h2 className="text-lg font-silkscreen font-bold text-stone-900 mb-2">
            Simulation In Development
          </h2>
          <p className="font-mono text-xs text-stone-600 mb-4">
            Interactive simulation component not found for slug{' '}
            <code className="bg-stone-100 px-1 py-0.5 border border-stone-300 font-bold text-stone-800">
              {slug}
            </code>
            .
          </p>
          <a
            href={url(`/labs/${slug}/docs`)}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 font-mono text-xs font-bold uppercase bg-stone-900 text-white hover:bg-stone-800 border-2 border-stone-900 shadow-[2px_2px_0px_0px_rgba(28,25,23,1)]"
          >
            <span>📖 Read Documentation</span>
          </a>
        </div>
      </div>
    );
  }

  return <Component />;
}
