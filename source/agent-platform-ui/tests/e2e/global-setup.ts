import { spawn } from 'node:child_process'
import { writeFile } from 'node:fs/promises'
import { resolve } from 'node:path'

const PID_FILE = resolve('.playwright-dev-server.pid')

export default async function globalSetup() {
  const child = spawn('pnpm', ['dev', '--host', '127.0.0.1', '--port', '41731', '--strictPort'], {
    cwd: process.cwd(),
    detached: true,
    env: process.env,
    stdio: ['ignore', 'pipe', 'pipe'],
  })

  await writeFile(PID_FILE, String(child.pid), 'utf8')

  await new Promise<void>((resolveReady, reject) => {
    const timeout = setTimeout(() => {
      reject(new Error('Timed out waiting for Vite dev server to start'))
    }, 60_000)

    const onData = (buffer: Buffer) => {
      const text = buffer.toString()
      if (text.includes('Local:') || text.includes('ready in')) {
        clearTimeout(timeout)
        resolveReady()
      }
    }

    child.stdout?.on('data', onData)
    child.stderr?.on('data', onData)
    child.once('exit', (code) => {
      clearTimeout(timeout)
      reject(new Error(`Vite dev server exited before becoming ready, code=${code}`))
    })
  })

  child.unref()
  child.stdout?.unref()
  child.stderr?.unref()
}
