import { expect, test, type Page } from '@playwright/test'

const ok = <T>(data: T) => ({
  code: 200,
  success: true,
  data,
  msg: 'ok',
})

const pageResult = <T>(records: T[]) => ({
  records,
  total: records.length,
  size: 50,
  current: 1,
  pages: 1,
})

const skill = {
  id: '1',
  name: '迁移技能',
  description: '用于验证 Skill 文件粒度迁移',
  skillContent: '# 迁移技能',
  category: '默认',
  references: null,
  examples: null,
  scripts: null,
  enabled: true,
  createdAt: '2026-06-03 10:00:00',
  updatedAt: '2026-06-03 10:00:00',
  createdBy: '1',
  updatedBy: '1',
  used: [],
  tools: [],
}

const provider = {
  id: '11',
  type: 'OPEN_AI',
  name: 'OpenAI',
  description: 'OpenAI compatible provider',
  baseUrl: 'https://api.example.com',
  authType: 'CONFIG',
  apiKey: 'secret',
  envVarName: '',
  enabled: true,
  configMeta: null,
  createdAt: '2026-06-03 10:00:00',
  updatedAt: '2026-06-03 10:00:00',
  createdBy: '1',
  updatedBy: '1',
}

const model = {
  id: '101',
  providerId: '11',
  name: 'gpt-test',
  modelId: 'gpt-test',
  modelType: ['CHAT'],
  description: 'test model',
  streaming: true,
  thinking: false,
  contextWindow: 128000,
  maxTokens: 4096,
  temperature: 0.7,
  topP: 1,
  topK: 0,
  repeatPenalty: 1,
  seed: '',
  extendConfig: null,
  connectivityStatus: 'NOT_CHECKED',
  connectivityMessage: '',
  lastConnectivityCheck: '',
  enabled: true,
  createdAt: '2026-06-03 10:00:00',
  updatedAt: '2026-06-03 10:00:00',
  createdBy: '1',
  updatedBy: '1',
  used: [],
}

test.beforeEach(async ({ page }) => {
  await page.addInitScript(() => {
    const ttl = String(Date.now() + 24 * 60 * 60 * 1000)
    localStorage.setItem('agent-accessToken', JSON.stringify({ value: 'e2e-token', ttl }))
    localStorage.setItem('agent-refreshToken', JSON.stringify({ value: 'e2e-refresh-token', ttl }))
    localStorage.setItem(
      'agent-user',
      JSON.stringify({
        id: '1',
        nickname: 'Admin',
        email: 'admin@example.com',
        username: 'admin',
        enabled: true,
        roles: ['ADMIN'],
      })
    )
  })
})

test('Skill card can update category and refresh filtered list', async ({ page }) => {
  let currentCategory = '默认'
  let updatedPayload: Record<string, unknown> | undefined

  await mockSkillApis(page, () => currentCategory, (payload) => {
    updatedPayload = payload
    currentCategory = String(payload.category)
  })

  const skillPageResponse = page.waitForResponse((response) => response.url().includes('/api/skill/page'))
  await page.goto('/#/skill')
  await skillPageResponse

  const card = page.locator('.skill-card', { hasText: '迁移技能' })
  await expect(card).toBeVisible()

  await card.getByLabel('技能操作菜单').hover()
  await page.getByText('设置分类').click()
  await page.locator('.ant-modal .ant-select-selector').click()
  await page.getByPlaceholder('请输入新分类').fill('迁移验证')
  await page.getByRole('button', { name: '添加' }).click()
  await page.locator('.ant-select-dropdown .ant-select-item-option', { hasText: '迁移验证' }).click()
  await page.locator('.ant-modal').getByRole('button', { name: /确\s*定/ }).click()

  await expect.poll(() => updatedPayload?.category).toBe('迁移验证')
  await expect(card.getByText('迁移验证')).toBeVisible()
})

test('Skill editor opens file tree and loads SKILL.md content', async ({ page }) => {
  await mockSkillApis(page, () => '默认', () => {})

  await page.goto('/#/skill/1/edit')

  await expect(page.getByText('迁移技能')).toBeVisible()
  await expect(page.getByRole('tree').getByText('SKILL.md')).toBeVisible()
  await expect(page.getByText('Skill file content for e2e')).toBeVisible()
})

test('Model config modal can run connectivity check', async ({ page }) => {
  let modelStatus = 'NOT_CHECKED'
  await mockModelApis(page, () => modelStatus, () => {
    modelStatus = 'CONNECTED'
  })

  const providerPageResponse = page.waitForResponse((response) => response.url().includes('/api/model/provider/page'))
  await page.goto('/#/model')
  await providerPageResponse

  const card = page.locator('.provider-card', { hasText: 'OpenAI' })
  await expect(card).toBeVisible()
  await card.getByLabel('模型供应商操作菜单').hover()
  await page.getByRole('menuitem', { name: /模型$/ }).click()

  await expect(page.getByText('OpenAI - 配置模型')).toBeVisible()
  await page.getByRole('button', { name: '测试连接' }).click()

  await expect(page.getByText('已连接')).toBeVisible()
})

async function mockSkillApis(
  page: Page,
  getCategory: () => string,
  onUpdate: (payload: Record<string, unknown>) => void
) {
  await page.route('**/api/skill/get/categories', async (route) => {
    await route.fulfill({ json: ok(['默认', '工具', getCategory()]) })
  })

  await page.route('**/api/skill/page**', async (route) => {
    await route.fulfill({
      json: ok(pageResult([{ ...skill, category: getCategory() }])),
    })
  })

  await page.route('**/api/skill/allowed-extensions', async (route) => {
    await route.fulfill({ json: ok(['md', 'txt', 'json', 'py']) })
  })

  await page.route('**/api/skill/1/tree', async (route) => {
    await route.fulfill({
      json: ok([
        {
          name: 'SKILL.md',
          path: 'SKILL.md',
          directory: false,
          fileId: '501',
          fileType: 'SKILL_MD',
          extension: 'md',
          fileSize: 128,
          children: [],
          content: 'Skill file content for e2e',
        },
      ]),
    })
  })

  await page.route('**/api/skill/1/file-content**', async (route) => {
    await route.fulfill({ json: ok('Skill file content for e2e') })
  })

  await page.route('**/api/skill/1', async (route) => {
    await route.fulfill({ json: ok({ ...skill, category: getCategory() }) })
  })

  await page.route('**/api/skill', async (route) => {
    if (route.request().method() === 'PUT') {
      onUpdate(route.request().postDataJSON() as Record<string, unknown>)
    }
    await route.fulfill({ json: ok(true) })
  })
}

async function mockModelApis(page: Page, getStatus: () => string, onCheck: () => void) {
  await page.route('**/api/model/provider/page**', async (route) => {
    await route.fulfill({ json: ok(pageResult([provider])) })
  })

  await page.route('**/api/model/config/page**', async (route) => {
    await route.fulfill({
      json: ok(pageResult([{ ...model, connectivityStatus: getStatus() }])),
    })
  })

  await page.route('**/api/model/config/check/101', async (route) => {
    onCheck()
    await route.fulfill({
      json: ok({
        success: true,
        message: '连接成功',
        latencyMs: 12,
      }),
    })
  })

  await page.route('**/api/model/config/used-with-agent', async (route) => {
    await route.fulfill({ json: ok([]) })
  })
}
