import { defineCollection, z } from 'astro:content';
import { glob } from 'astro/loaders';

const notes = defineCollection({
  loader: glob({ pattern: '**/*.{md,mdx}', base: './src/content/notes' }),
  schema: z.object({
    title: z.string(),
    description: z.string(),
    publishedAt: z.string(),
    category: z.string().default('Distributed Systems'),
    tags: z.array(z.string()).default([]),
    readTimeMinutes: z.number().optional(),
    featured: z.boolean().default(false),
  }),
});

const lld = defineCollection({
  loader: glob({ pattern: '**/*.{md,mdx}', base: './src/content/lld' }),
  schema: z.object({
    title: z.string(),
    description: z.string(),
    publishedAt: z.string(),
    designPatterns: z.array(z.string()).default([]),
    language: z.string().default('Java / TypeScript'),
    githubRepo: z.string().optional(),
    difficulty: z.enum(['Medium', 'Hard']).default('Medium'),
  }),
});

const labs = defineCollection({
  loader: glob({ pattern: '**/*.{md,mdx}', base: './src/content/labs' }),
  schema: z.object({
    id: z.string(),
    title: z.string(),
    description: z.string(),
    tag: z.string(),
    tagColor: z.string(),
    tagTextColor: z.string().default('#fff'),
    category: z.string(),
    featured: z.boolean().default(false),
    status: z.enum(['Interactive Simulation', 'Beta', 'Experimental']).default('Interactive Simulation'),
    githubRepo: z.string().optional(),
  }),
});

const learning = defineCollection({
  loader: glob({ pattern: '**/*.{md,mdx}', base: './src/content/learning' }),
  schema: z.object({
    title: z.string(),
    description: z.string(),
    publishedAt: z.string(),
    category: z.enum(['Concept', 'Algorithm', 'Clipping', 'System', 'General']).default('Concept'),
    tags: z.array(z.string()).default([]),
    readTimeMinutes: z.number().optional(),
    sourceUrl: z.string().optional(),
    author: z.string().optional(),
    featured: z.boolean().default(false),
  }),
});

export const collections = { notes, lld, labs, learning };
