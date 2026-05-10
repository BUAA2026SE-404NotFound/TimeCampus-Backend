#!/usr/bin/env node

import { performance } from 'node:perf_hooks'

const BASE_URL = trimTrailingSlash(process.env.BASE_URL || 'http://127.0.0.1:8080')
const SCENARIO = process.env.SCENARIO || 'public-read'
const CONCURRENCY = positiveInt(process.env.CONCURRENCY, 50)
const DURATION_SECONDS = positiveInt(process.env.DURATION_SECONDS, 60)
const ADMIN_TOKEN = process.env.ADMIN_TOKEN || ''
const USER_TOKEN = process.env.USER_TOKEN || ''

const scenarios = {
  health: [
    { method: 'GET', path: '/api/v1/health', weight: 1 }
  ],
  'public-read': [
    { method: 'GET', path: '/api/v1/health', weight: 2 },
    { method: 'GET', path: '/api/v1/pois', weight: 2 },
    { method: 'GET', path: '/api/v1/map/home', weight: 2 },
    { method: 'GET', path: '/api/v1/comments/poi/1', weight: 1 }
  ],
  admin: [
    { method: 'GET', path: '/api/v1/admin/comments?status=pending', weight: 2, token: 'admin' },
    { method: 'GET', path: '/api/v1/admin/logs?limit=50', weight: 1, token: 'admin' },
    { method: 'GET', path: '/api/v1/admin/map/overview?limit=50', weight: 1, token: 'admin' }
  ],
  user: [
    { method: 'GET', path: '/api/v1/me', weight: 1, token: 'user' },
    { method: 'GET', path: '/api/v1/comments/mine', weight: 1, token: 'user' },
    { method: 'GET', path: '/api/v1/favorites', weight: 1, token: 'user' }
  ]
}

const requests = expandWeighted(scenarios[SCENARIO])
if (!requests.length) {
  console.error(`Unknown or empty scenario: ${SCENARIO}`)
  console.error(`Available scenarios: ${Object.keys(scenarios).join(', ')}`)
  process.exit(2)
}

const deadline = Date.now() + DURATION_SECONDS * 1000
const results = []
let nextIndex = 0

console.log(JSON.stringify({
  event: 'start',
  baseUrl: BASE_URL,
  scenario: SCENARIO,
  concurrency: CONCURRENCY,
  durationSeconds: DURATION_SECONDS
}))

await Promise.all(Array.from({ length: CONCURRENCY }, (_, workerId) => runWorker(workerId)))

const summary = summarize(results)
console.log(JSON.stringify({ event: 'summary', ...summary }, null, 2))

if (summary.errorRate > 0.01) {
  process.exitCode = 1
}

async function runWorker() {
  while (Date.now() < deadline) {
    const request = requests[nextIndex++ % requests.length]
    const started = performance.now()
    let status = 0
    let ok = false
    let code = null
    try {
      const response = await fetch(`${BASE_URL}${request.path}`, {
        method: request.method,
        headers: buildHeaders(request),
        body: request.body ? JSON.stringify(request.body) : undefined
      })
      status = response.status
      const text = await response.text()
      const body = tryJson(text)
      code = body?.code ?? null
      ok = response.ok && (code === null || code === 0)
    } catch (error) {
      ok = false
    } finally {
      results.push({
        path: request.path,
        status,
        code,
        ok,
        durationMs: performance.now() - started
      })
    }
  }
}

function buildHeaders(request) {
  const headers = { Accept: 'application/json' }
  if (request.body) {
    headers['Content-Type'] = 'application/json'
  }
  if (request.token === 'admin' && ADMIN_TOKEN) {
    headers.Authorization = `Bearer ${ADMIN_TOKEN}`
  }
  if (request.token === 'user' && USER_TOKEN) {
    headers.Authorization = `Bearer ${USER_TOKEN}`
  }
  return headers
}

function summarize(items) {
  const durations = items.map((item) => item.durationMs).sort((a, b) => a - b)
  const success = items.filter((item) => item.ok).length
  const failed = items.length - success
  const byPath = new Map()
  for (const item of items) {
    const key = item.path
    const current = byPath.get(key) || { total: 0, failed: 0 }
    current.total += 1
    if (!item.ok) {
      current.failed += 1
    }
    byPath.set(key, current)
  }
  return {
    total: items.length,
    success,
    failed,
    errorRate: items.length ? Number((failed / items.length).toFixed(4)) : 0,
    rps: Number((items.length / DURATION_SECONDS).toFixed(2)),
    minMs: round(percentile(durations, 0)),
    p50Ms: round(percentile(durations, 0.5)),
    p90Ms: round(percentile(durations, 0.9)),
    p95Ms: round(percentile(durations, 0.95)),
    p99Ms: round(percentile(durations, 0.99)),
    maxMs: round(percentile(durations, 1)),
    byPath: Object.fromEntries(byPath)
  }
}

function percentile(values, p) {
  if (!values.length) {
    return 0
  }
  const index = Math.min(values.length - 1, Math.max(0, Math.ceil(values.length * p) - 1))
  return values[index]
}

function round(value) {
  return Number(value.toFixed(2))
}

function expandWeighted(definitions = []) {
  return definitions.flatMap((definition) => Array.from({ length: definition.weight || 1 }, () => definition))
}

function trimTrailingSlash(value) {
  return value.endsWith('/') ? value.slice(0, -1) : value
}

function positiveInt(value, fallback) {
  const parsed = Number.parseInt(value, 10)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback
}

function tryJson(text) {
  try {
    return JSON.parse(text)
  } catch {
    return null
  }
}
