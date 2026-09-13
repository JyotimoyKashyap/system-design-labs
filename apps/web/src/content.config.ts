import { defineCollection, z } from 'astro:content';
import { glob } from 'astro/loaders';

const blogs = defineCollection({
  loader: glob({ pattern: '**/*.{md,mdx}', base: './src/content/blogs' }),
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
  loader: glob({ pattern: '**/*.json', base: './src/content/labs' }),
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
  }),
});

export const collections = { blogs, lld, labs };
