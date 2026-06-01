<script setup lang="ts">
import { computed, h, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import { Input, Modal, Select, message } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  DeleteOutlined,
  DownloadOutlined,
  FolderAddOutlined,
  PlusOutlined,
} from '@ant-design/icons-vue'
import SkillEditor from '@/components/skill/SkillEditor.vue'
import SkillTree from '@/components/skill/SkillTree.vue'
import { RouteNames } from '@/router/constants'
import type { SkillFileTreeNode, SkillPackageVO } from '@/types'
import * as skillApi from '@/api/skill'

const route = useRoute()
const router = useRouter()

const isNew = computed(() => route.name === RouteNames.SKILL_EDITOR_NEW)
const skillId = computed(() => String(route.params.id || ''))

const skillInfo = reactive({
  id: '' as string | number,
  name: '',
  description: '',
  category: '',
  enabled: true,
  tools: [] as string[],
})

const treeData = ref<SkillFileTreeNode[]>([])
const selectedFile = ref<SkillFileTreeNode | null>(null)
const hasDirty = ref(false)
const editorRef = ref<InstanceType<typeof SkillEditor> | null>(null)
const skillTreeRef = ref<InstanceType<typeof SkillTree> | null>(null)
const categories = ref<string[]>([])
const allowedExtensions = ref<string[]>([])
const leftPanelWidth = ref(280)
const isResizing = ref(false)

const LARGE_FILE_SIZE = 500 * 1024

onMounted(async () => {
  window.addEventListener('beforeunload', handleBeforeUnload)
  await loadCategories()
  await loadExtensions()

  if (isNew.value) {
    showCreateModal()
  } else {
    await loadSkillDetail()
    await refreshTree()
  }
})

watch(
  () => route.params.id,
  async (newId) => {
    if (newId && newId !== 'new') {
      await loadSkillDetail()
      await refreshTree()
    }
  },
)

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
  document.removeEventListener('mousemove', handleResizeMove)
  document.removeEventListener('mouseup', handleResizeEnd)
  document.body.style.userSelect = ''
  document.body.style.cursor = ''
})

onBeforeRouteLeave((_to, _from, next) => {
  if (hasDirty.value) {
    Modal.confirm({
      title: '未保存的更改',
      content: '您有未保存的文件修改，确定要离开吗？',
      okText: '确定离开',
      cancelText: '继续编辑',
      okButtonProps: { danger: true },
      onOk: () => next(),
      onCancel: () => next(false),
    })
  } else {
    next()
  }
})

function handleResizeStart(e: MouseEvent) {
  e.preventDefault()
  isResizing.value = true
  document.body.style.userSelect = 'none'
  document.body.style.cursor = 'col-resize'
  document.addEventListener('mousemove', handleResizeMove)
  document.addEventListener('mouseup', handleResizeEnd)
}

function handleResizeMove(e: MouseEvent) {
  if (!isResizing.value) return
  leftPanelWidth.value = Math.max(240, Math.min(500, e.clientX))
}

function handleResizeEnd() {
  isResizing.value = false
  document.body.style.userSelect = ''
  document.body.style.cursor = ''
  document.removeEventListener('mousemove', handleResizeMove)
  document.removeEventListener('mouseup', handleResizeEnd)
}

async function loadCategories() {
  try {
    const res = await skillApi.listCategories()
    categories.value = res.data.data || []
  } catch {
    categories.value = []
  }
}

async function loadExtensions() {
  try {
    const res = await skillApi.getAllowedExtensions()
    allowedExtensions.value = res.data.data || []
  } catch {
    allowedExtensions.value = []
  }
}

async function loadSkillDetail() {
  try {
    const res = await skillApi.detail(skillId.value)
    const vo = res.data.data as SkillPackageVO
    if (vo) {
      skillInfo.id = vo.id
      skillInfo.name = vo.name
      skillInfo.description = vo.description
      skillInfo.category = vo.category
      skillInfo.enabled = vo.enabled
      skillInfo.tools = vo.tools || []
    }
  } catch {
    message.error('加载技能包详情失败')
  }
}

async function refreshTree() {
  if (!skillId.value || isNew.value) return
  try {
    const res = await skillApi.getTree(skillId.value)
    treeData.value = res.data.data || []
    if (selectedFile.value && !findNodeByPath(treeData.value, selectedFile.value.path)) {
      selectedFile.value = null
    }
    if (!selectedFile.value) {
      selectedFile.value = findNodeByPath(treeData.value, 'SKILL.md')
    }
  } catch {
    message.error('加载文件树失败')
  }
}

function handleFileDeleted(deletedPath: string) {
  if (selectedFile.value?.path === deletedPath) {
    selectedFile.value = null
  }
}

function handleFileSelect(node: SkillFileTreeNode) {
  const ext = (node.extension || '').toLowerCase()
  if (ext && allowedExtensions.value.length > 0 && !allowedExtensions.value.includes(ext)) {
    Modal.confirm({
      title: '不支持的文件类型',
      content: `文件类型 .${ext} 不支持在线预览，请下载到本地进行查看。`,
      okText: '下载',
      cancelText: '取消',
      onOk: () => triggerFileDownload(node.name, node.path),
    })
    return
  }

  if (node.fileSize > LARGE_FILE_SIZE) {
    Modal.confirm({
      title: '文件过大',
      content: `文件过大（${formatFileSize(node.fileSize)}），请下载到本地进行查看。`,
      okText: '下载',
      cancelText: '取消',
      onOk: () => triggerFileDownload(node.name, node.path),
    })
    return
  }

  if (selectedFile.value && hasDirty.value && editorRef.value?.isDirty()) {
    Modal.confirm({
      title: '未保存的更改',
      content: '当前文件有未保存的修改，是否放弃更改？',
      okText: '放弃更改',
      cancelText: '继续编辑',
      okButtonProps: { danger: true },
      onOk: () => {
        selectedFile.value = node
        hasDirty.value = false
      },
    })
  } else {
    selectedFile.value = node
  }
}

async function triggerFileDownload(fileName: string, filePath: string) {
  try {
    const res = await skillApi.downloadFile(skillId.value, filePath)
    downloadBlob(res.data as unknown as Blob, fileName)
  } catch {
    message.error('下载失败')
  }
}

async function handleDownloadZip() {
  try {
    const res = await skillApi.downloadZip(skillId.value)
    downloadBlob(res.data as unknown as Blob, `${skillInfo.name || 'skill'}.zip`)
  } catch {
    message.error('下载失败')
  }
}

function downloadBlob(blob: Blob, fileName: string) {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = fileName
  a.click()
  URL.revokeObjectURL(url)
}

function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function findNodeByPath(nodes: SkillFileTreeNode[], targetPath: string): SkillFileTreeNode | null {
  for (const node of nodes) {
    if (node.path === targetPath) return node
    const found = node.children ? findNodeByPath(node.children, targetPath) : null
    if (found) return found
  }
  return null
}

function handleDirtyChange(dirty: boolean) {
  hasDirty.value = dirty
}

function handleGoBack() {
  if (hasDirty.value) {
    Modal.confirm({
      title: '未保存的更改',
      content: '您有未保存的文件修改，确定要离开吗？',
      okText: '确定离开',
      cancelText: '继续编辑',
      okButtonProps: { danger: true },
      onOk: () => router.push({ name: RouteNames.SKILL }),
    })
  } else {
    router.push({ name: RouteNames.SKILL })
  }
}

function handleNewFile() {
  skillTreeRef.value?.showCreateFile()
}

function handleNewFolder() {
  skillTreeRef.value?.showCreateDir()
}

function handleDeleteSkill() {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除技能包 "${skillInfo.name}" 吗？此操作不可恢复。`,
    okText: '确定删除',
    cancelText: '取消',
    okButtonProps: { danger: true },
    async onOk() {
      try {
        await skillApi.remove([String(skillInfo.id)])
        message.success('删除成功')
        router.push({ name: RouteNames.SKILL })
      } catch {
        message.error('删除失败')
      }
    },
  })
}

function showCreateModal() {
  let name = ''
  let description = ''
  let category = ''

  Modal.confirm({
    title: '新建技能包',
    content: () =>
      h('div', { style: 'padding: 12px 0' }, [
        h('div', { style: 'margin-bottom: 12px' }, [
          h('label', { style: 'display: block; margin-bottom: 4px; font-size: 13px' }, '技能包名称'),
          h(Input, {
            placeholder: '请输入技能包名称',
            onChange: (e: Event) => { name = (e.target as HTMLInputElement).value },
          }),
        ]),
        h('div', { style: 'margin-bottom: 12px' }, [
          h('label', { style: 'display: block; margin-bottom: 4px; font-size: 13px' }, '描述'),
          h(Input.TextArea, {
            placeholder: '请输入技能包描述',
            rows: 3,
            onChange: (e: Event) => { description = (e.target as HTMLTextAreaElement).value },
          }),
        ]),
        h('div', [
          h('label', { style: 'display: block; margin-bottom: 4px; font-size: 13px' }, '分类'),
          h(Select, {
            placeholder: '请选择分类',
            style: 'width: 100%',
            options: categories.value.map((item) => ({ value: item, label: item })),
            onChange: (val: string) => { category = val },
          } as any),
        ]),
      ]),
    okText: '创建',
    cancelText: '取消',
    async onOk() {
      if (!name.trim()) {
        message.warning('请输入技能包名称')
        return Promise.reject()
      }
      try {
        const res = await skillApi.save({
          name: name.trim(),
          description,
          category,
          enabled: true,
          skillContent: '',
          references: null,
          examples: null,
          scripts: null,
          tools: [],
        } as any)
        if (res.data.code === 200) {
          const newId = String(res.data.data)
          message.success('创建成功')
          router.replace({ name: RouteNames.SKILL_EDITOR, params: { id: newId } })
        }
      } catch {
        message.error('创建失败')
        return Promise.reject()
      }
    },
    onCancel() {
      router.push({ name: RouteNames.SKILL })
    },
  })
}

function handleBeforeUnload(e: BeforeUnloadEvent) {
  if (hasDirty.value) {
    e.preventDefault()
    e.returnValue = ''
  }
}
</script>

<template>
  <div
    class="skill-editor-view"
    :style="{ height: 'calc(100vh - 56px)', gridTemplateColumns: `${leftPanelWidth}px 4px 1fr` }"
  >
    <div class="left-panel">
      <div class="panel-toolbar">
        <AButton type="text" size="small" @click="handleGoBack">
          <ArrowLeftOutlined />
        </AButton>
        <span class="skill-name">
          <span v-if="!isNew" class="skill-name-text">{{ skillInfo.name }}</span>
          <span v-else>新建技能包</span>
        </span>
        <span class="panel-spacer"></span>
        <AButton v-if="!isNew" type="text" size="small" title="新建文件" @click="handleNewFile">
          <PlusOutlined />
        </AButton>
        <AButton v-if="!isNew" type="text" size="small" title="新建文件夹" @click="handleNewFolder">
          <FolderAddOutlined />
        </AButton>
        <AButton v-if="!isNew" type="text" size="small" title="下载技能包" @click="handleDownloadZip">
          <DownloadOutlined />
        </AButton>
      </div>

      <div v-if="!isNew" class="tree-container">
        <SkillTree
          ref="skillTreeRef"
          :skill-id="skillId"
          :tree-data="treeData"
          @select="handleFileSelect"
          @refresh="refreshTree"
          @file-deleted="handleFileDeleted"
        />
      </div>

      <div v-if="!isNew" class="panel-footer">
        <AButton type="text" danger size="small" @click="handleDeleteSkill">
          <DeleteOutlined />
          <span>删除技能包</span>
        </AButton>
      </div>
    </div>

    <div class="resize-handle" :class="{ 'resize-handle--active': isResizing }" @mousedown="handleResizeStart" />

    <div class="right-panel">
      <div v-if="!isNew" class="editor-container">
        <SkillEditor
          ref="editorRef"
          :skill-id="skillId"
          :file="selectedFile"
          @dirty-change="handleDirtyChange"
          @saved="loadSkillDetail"
        />
      </div>

      <div v-else class="create-placeholder">
        请先填写技能包基本信息
      </div>
    </div>
  </div>
</template>

<style scoped>
.skill-editor-view {
  display: grid;
  overflow: hidden;
}

.left-panel {
  display: flex;
  flex-direction: column;
  background: #fafafa;
  border-right: 1px solid #f0f0f0;
  overflow: hidden;
}

.panel-toolbar {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  gap: 8px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
  background-color: #fff;
}

.skill-name {
  font-size: 14px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.panel-spacer {
  flex: 1;
}

.tree-container {
  flex: 1;
  overflow: auto;
  background-color: #fff;
}

.panel-footer {
  padding: 8px 12px;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
  background-color: #fff;
  text-align: center;
}

.right-panel {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #fff;
}

.editor-container {
  flex: 1;
  overflow: hidden;
}

.create-placeholder {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #bfbfbf;
  font-size: 14px;
}

.resize-handle {
  width: 4px;
  cursor: col-resize;
  background: transparent;
  transition: background 0.2s;
  z-index: 10;
}

.resize-handle:hover,
.resize-handle--active {
  background: #1890ff;
}
</style>
