#!/usr/bin/env node
import { stdin } from 'node:process'

const input = JSON.parse(await new Response(stdin).text() || '{}')
const command = (input.command || '').replace(/\s+/g, ' ').trim()
const bak = /^(mv|move|Move-Item)\s+\S+\s+\.\S+\.\d+\.bak$/i
if (bak.test(command)) {
  reply({ permission: 'allow' })
}
if (/\bgit\b/i.test(command) && /\bpush\b/i.test(command)) {
  reply(deny('git push is denied including git -C and other option forms'))
}
if (/\bgit\b/i.test(command) && /\brm\b/i.test(command)) {
  reply(deny('git rm is denied'))
}
if (/\bfind\b/.test(command) && /(^|\s)-delete\b/.test(command)) {
  reply(deny('find -delete is denied'))
}
if (/^\s*(rm|rmdir|unlink|del|erase|rd|ri|Remove-Item|shred)(\.exe)?\b/i.test(command)) {
  reply(deny('file deletion is denied; move to .name.<time>.bak instead'))
}
if (/\b(cmd|cmd.exe|powershell|pwsh)\b/i.test(command) && /\b(del|erase|rmdir|Remove-Item|rm)\b/i.test(command)) {
  reply(deny('file deletion through a shell wrapper is denied'))
}
reply({ permission: 'allow' })

function deny(message) {
  return { permission: 'deny', user_message: message, agent_message: message }
}

function reply(value) {
  process.stdout.write(JSON.stringify(value))
  process.exit(0)
}
