import { readFile, rm } from 'node:fs/promises'
import { resolve } from 'node:path'

const PID_FILE = resolve('.playwright-dev-server.pid')

export default async function globalTeardown() {
  try {
    const pid = Number((await readFile(PID_FILE, 'utf8')).trim())
    if (Number.isFinite(pid) && pid > 0) {
      try {
        process.kill(-pid, 'SIGTERM')
      } catch {
        process.kill(pid, 'SIGTERM')
      }
    }
  } catch {
    // No dev server pid was recorded.
  } finally {
    await rm(PID_FILE, { force: true })
  }
}
