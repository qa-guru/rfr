#!/usr/bin/env node
import { spawnSync } from 'node:child_process'
import { join, relative } from 'node:path'
import { stdin } from 'node:process'

const input = JSON.parse(await new Response(stdin).text() || '{}')
const file = input.file_path || input.tool_input?.path || input.tool_input?.file_path || ''
const root = input.cwd || process.cwd()
const rel = relative(root, file).replaceAll('\\', '/')
const gw = process.platform === 'win32' ? '.\\gradlew.bat' : './gradlew'
const opts = { encoding: 'utf8', shell: true, timeout: 170000, windowsHide: true }
let parts
if (rel.startsWith('rfr-auth/') && rel.endsWith('.java')) {
  parts = [run(`${gw} -q :rfr-auth:compileJava`, root, 'rfr-auth', rel)]
} else if (rel.startsWith('rfr-api/') && rel.endsWith('.java')) {
  parts = [run(`${gw} -q :rfr-api:compileJava`, root, 'rfr-api', rel)]
} else if (rel.startsWith('rfr-e2e/') && rel.endsWith('.java')) {
  parts = [run(`${gw} -q :rfr-e2e:compileTestJava`, root, 'rfr-e2e', rel)]
} else if (rel.startsWith('rfr-gql-client/src/') && /\.tsx?$/.test(rel)) {
  const frontFile = rel.slice('rfr-gql-client/'.length)
  const frontDir = join(root, 'rfr-gql-client')
  parts = [
    run('npx tsc --noEmit -p tsconfig.json', frontDir, 'rfr-gql-client tsc', rel),
    run(`npx eslint ${JSON.stringify(frontFile)} --report-unused-disable-directives --max-warnings 0`, frontDir, 'rfr-gql-client eslint', rel)
  ]
} else {
  process.stdout.write('{}')
  process.exit(0)
}
process.stdout.write(JSON.stringify({ additional_context: parts.join('\n') }))

function run(cmd, cwd, label, edited) {
  const result = spawnSync(cmd, { ...opts, cwd })
  const log = [result.stdout, result.stderr].filter(Boolean).join('\n').trim()
  const status = result.status === 0 ? 'passed' : 'failed'
  const head = `${label} ${status} after editing ${edited}`
  return result.status === 0 ? head : `${head}:\n${log}`
}
