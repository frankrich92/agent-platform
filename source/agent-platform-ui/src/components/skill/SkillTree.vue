<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { Modal, message } from 'ant-design-vue'
import {
  CheckOutlined,
  DeleteOutlined,
  EditOutlined,
  FolderAddOutlined,
  FolderFilled,
  FolderOpenFilled,
  PlusOutlined,
  UploadOutlined,
} from '@ant-design/icons-vue'
import FileIcon from '@/components/workspace/FileIcon.vue'
import type { SkillFileTreeNode } from '@/types'
import * as skillApi from '@/api/skill'

const props = defineProps<{
  skillId: string
  treeData: SkillFileTreeNode[]
}>()

const emit = defineEmits<{
  select: [node: SkillFileTreeNode]
  refresh: []
  fileDeleted: [path: string]
}>()

const selectedKeys = ref<string[]>([])
const expandedKeys = ref<string[]>([])
const uploadInputRef = ref<HTMLInputElement | null>(null)
const uploadParentPath = ref('')

const contextMenu = reactive({
  visible: false,
  x: 0,
  y: 0,
  node: null as SkillFileTreeNode | null,
})

const createModal = reactive({
  visible: false,
  mode: 'file' as 'file' | 'dir',
  parentPath: '',
  name: '',
})

const folderPresets = ['scripts', 'references', 'examples']

onMounted(() => {
  expandAll(props.treeData)
})

watch(
  () => props.treeData,
  (newVal) => expandAll(newVal || []),
)

function expandAll(nodes: SkillFileTreeNode[]) {
  const keys: string[] = []
  const collect = (node: SkillFileTreeNode) => {
    if (node.directory) {
      keys.push(getNodeKey(node))
    }
    node.children?.forEach(collect)
  }
  nodes.forEach(collect)
  expandedKeys.value = keys
}

function getNodeKey(node: SkillFileTreeNode): string {
  return node.directory ? `dir:${node.path}` : `file:${node.path}`
}

function handleSelect(keys: string[], { node }: any) {
  const raw = node.rawNode as SkillFileTreeNode
  const key = keys[0]
  if (!key) return
  if (raw.directory) {
    selectedKeys.value = []
    const index = expandedKeys.value.indexOf(key)
    if (index >= 0) {
      expandedKeys.value.splice(index, 1)
    } else {
      expandedKeys.value.push(key)
    }
    return
  }
  emit('select', raw)
}

function handleContextMenu(e: MouseEvent, node: SkillFileTreeNode) {
  if (node.name === 'SKILL.md') {
    return
  }
  e.preventDefault()
  e.stopPropagation()
  contextMenu.visible = true
  contextMenu.x = e.clientX
  contextMenu.y = e.clientY
  contextMenu.node = node
}

function handleTreeContextMenu(e: MouseEvent) {
  const target = e.target as HTMLElement
  if (target.closest('.ant-tree-node-content-wrapper')) return
  e.preventDefault()
  contextMenu.visible = true
  contextMenu.x = e.clientX
  contextMenu.y = e.clientY
  contextMenu.node = null
}

function closeContextMenu() {
  contextMenu.visible = false
  contextMenu.node = null
}

function showCreateFile(parentNode?: SkillFileTreeNode) {
  createModal.mode = 'file'
  createModal.parentPath = parentNode?.path || ''
  createModal.name = ''
  createModal.visible = true
  closeContextMenu()
}

function showCreateDir(parentNode?: SkillFileTreeNode) {
  createModal.mode = 'dir'
  createModal.parentPath = parentNode?.path || ''
  createModal.name = ''
  createModal.visible = true
  closeContextMenu()
}

async function handleCreateConfirm() {
  if (!createModal.name.trim()) {
    message.warning('请输入名称')
    return
  }

  if (createModal.mode === 'dir') {
    await skillApi.createDirectory(props.skillId, {
      parentPath: createModal.parentPath,
      dirName: createModal.name.trim(),
    })
    message.success('文件夹创建成功')
  } else {
    const extRes = await skillApi.getAllowedExtensions()
    const extensions = extRes.data.data || []
    const dotIndex = createModal.name.lastIndexOf('.')
    if (dotIndex > 0) {
      const ext = createModal.name.substring(dotIndex + 1).toLowerCase()
      if (!extensions.includes(ext)) {
        message.error(`不允许的文件类型: .${ext}，请使用白名单内的扩展名`)
        return
      }
    }
    await skillApi.createFile(props.skillId, {
      parentPath: createModal.parentPath,
      fileName: createModal.name.trim(),
      content: '',
    })
    message.success('文件创建成功')
  }

  createModal.visible = false
  emit('refresh')
}

function handleDelete(node: SkillFileTreeNode) {
  Modal.confirm({
    title: '确认删除',
    content: node.directory && node.children?.length
      ? `目录 "${node.name}" 下包含文件，确定要递归删除吗？`
      : `确定要删除 "${node.name}" 吗？`,
    okText: '确定',
    cancelText: '取消',
    okButtonProps: { danger: true },
    onOk: () => doDelete(node),
  })
  closeContextMenu()
}

async function doDelete(node: SkillFileTreeNode) {
  if (node.fileId) {
    await skillApi.deleteDbFile(node.fileId)
  } else {
    await skillApi.deleteFileSystemNode(props.skillId, {
      path: node.path,
      directory: node.directory,
    })
  }
  message.success('删除成功')
  emit('fileDeleted', node.path)
  emit('refresh')
}

function showUpload(parentNode?: SkillFileTreeNode) {
  uploadParentPath.value = parentNode?.path || ''
  closeContextMenu()
  uploadInputRef.value?.click()
}

async function handleUpload(e: Event) {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  try {
    await skillApi.uploadFile(props.skillId, uploadParentPath.value, file)
    message.success('上传成功')
    emit('refresh')
  } finally {
    target.value = ''
  }
}

function getStatusIcon(node: SkillFileTreeNode) {
  if (!node.fileId || node.directory) return null
  return node.dirty ? EditOutlined : CheckOutlined
}

function getStatusIconColor(node: SkillFileTreeNode): string {
  return node.dirty ? '#faad14' : '#bfbfbf'
}

function getContextMenuItems(): any[] {
  const node = contextMenu.node
  if (!node) {
    return [
      { key: 'new-file', icon: PlusOutlined, label: '新建文件' },
      { key: 'new-dir', icon: FolderAddOutlined, label: '新建文件夹' },
    ]
  }

  if (node.directory) {
    return [
      { key: 'new-file', icon: PlusOutlined, label: '新建文件' },
      { key: 'new-dir', icon: FolderAddOutlined, label: '新建文件夹' },
      { key: 'upload', icon: UploadOutlined, label: '上传文件' },
      { key: 'delete', icon: DeleteOutlined, label: '删除', danger: true },
    ]
  }

  return [{ key: 'delete', icon: DeleteOutlined, label: '删除', danger: true }]
}

function handleMenuClick(key: string) {
  const node = contextMenu.node
  const parentNode = node?.directory ? node : undefined
  switch (key) {
    case 'new-file':
      showCreateFile(parentNode)
      break
    case 'new-dir':
      showCreateDir(parentNode)
      break
    case 'upload':
      showUpload(parentNode)
      break
    case 'delete':
      if (node) handleDelete(node)
      break
  }
  closeContextMenu()
}

function buildTreeData(nodes: SkillFileTreeNode[]): any[] {
  return nodes.map((node) => ({
    key: getNodeKey(node),
    title: node.name,
    isLeaf: !node.directory,
    directory: node.directory,
    fileName: node.name,
    rawNode: node,
    children: node.children ? buildTreeData(node.children) : undefined,
  }))
}

defineExpose({
  showCreateFile,
  showCreateDir,
})
</script>

<template>
  <div class="skill-tree" @click="closeContextMenu" @contextmenu="handleTreeContextMenu">
    <input ref="uploadInputRef" type="file" style="display: none" @change="handleUpload" />

    <ATree
      v-model:selectedKeys="selectedKeys"
      v-model:expandedKeys="expandedKeys"
      :tree-data="buildTreeData(treeData)"
      :show-icon="true"
      :block-node="true"
      @select="handleSelect"
    >
      <template #icon="{ directory, fileName, key }: any">
        <span v-if="directory" class="folder-icon">
          <FolderOpenFilled v-if="expandedKeys.includes(key)" />
          <FolderFilled v-else />
        </span>
        <FileIcon v-else :file-name="fileName" :width="16" />
      </template>
      <template #title="{ title, rawNode: node }: any">
        <span class="tree-node-title" @contextmenu="(e: MouseEvent) => handleContextMenu(e, node)">
          <span class="node-name">{{ title }}</span>
          <span v-if="node.fileId && !node.directory" class="node-status">
            <component
              :is="getStatusIcon(node)"
              :style="{ color: getStatusIconColor(node), fontSize: '12px', marginLeft: '6px' }"
            />
          </span>
        </span>
      </template>
    </ATree>

    <Teleport to="body">
      <div
        v-if="contextMenu.visible"
        class="context-menu-overlay"
        @click="closeContextMenu"
        @contextmenu.prevent="closeContextMenu"
      >
        <div class="context-menu-panel" :style="{ left: `${contextMenu.x}px`, top: `${contextMenu.y}px` }">
          <div
            v-for="item in getContextMenuItems()"
            :key="item.key"
            class="context-menu-item"
            :class="{ 'context-menu-item--danger': item.danger }"
            @click.stop="handleMenuClick(item.key)"
          >
            <component :is="item.icon" class="context-menu-icon" />
            <span>{{ item.label }}</span>
          </div>
        </div>
      </div>
    </Teleport>

    <AModal
      v-model:open="createModal.visible"
      :title="createModal.mode === 'file' ? '新建文件' : '新建文件夹'"
      ok-text="确定"
      cancel-text="取消"
      @ok="handleCreateConfirm"
    >
      <AForm layout="vertical">
        <AFormItem label="父目录">
          <AInput :value="createModal.parentPath || '(根目录)'" disabled />
        </AFormItem>
        <AFormItem :label="createModal.mode === 'file' ? '文件名' : '文件夹名'">
          <div v-if="createModal.mode === 'dir'" class="folder-presets">
            <span class="folder-presets-label">快捷选择：</span>
            <span
              v-for="preset in folderPresets"
              :key="preset"
              class="folder-preset"
              @click="createModal.name = preset"
            >
              {{ preset }}
            </span>
          </div>
          <AInput
            v-model:value="createModal.name"
            :placeholder="createModal.mode === 'file' ? '例如: helper.py' : '例如: utils'"
            @pressEnter="handleCreateConfirm"
          />
        </AFormItem>
      </AForm>
    </AModal>
  </div>
</template>

<style scoped>
.skill-tree {
  height: 100%;
  overflow: auto;
  padding: 8px;
}

.folder-icon {
  color: #faad14;
  font-size: 20px;
  display: inline-flex;
  align-items: center;
}

.tree-node-title {
  display: flex;
  align-items: center;
  width: 100%;
  font-size: 16px;
  padding: 2px 0;
  cursor: default;
}

.node-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.node-status {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
}

.folder-presets {
  margin-bottom: 6px;
}

.folder-presets-label {
  font-size: 12px;
  color: #999;
  margin-right: 4px;
}

.folder-preset {
  cursor: pointer;
  margin-right: 10px;
}
</style>

<style>
.context-menu-overlay {
  position: fixed;
  inset: 0;
  z-index: 1050;
}

.context-menu-panel {
  position: fixed;
  z-index: 1051;
  min-width: 140px;
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 4px 0;
}

.context-menu-item {
  display: flex;
  align-items: center;
  padding: 6px 12px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;
}

.context-menu-item:hover {
  background: #f5f5f5;
}

.context-menu-item--danger {
  color: #ff4d4f;
}

.context-menu-item--danger:hover {
  background: #fff2f0;
}

.context-menu-icon {
  font-size: 16px;
  margin-right: 8px;
}

.skill-tree .ant-tree-node-content-wrapper {
  display: flex !important;
  align-items: center !important;
}

.skill-tree .ant-tree-iconEle {
  display: inline-flex !important;
  align-items: center !important;
}

.skill-tree .ant-tree-title {
  display: flex !important;
  align-items: center !important;
  flex: 1;
}

.skill-tree .ant-tree-switcher {
  display: none !important;
}
</style>
