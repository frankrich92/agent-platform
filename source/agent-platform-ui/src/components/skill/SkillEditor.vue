<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { Modal } from 'ant-design-vue'
import {
  CheckCircleFilled,
  LoadingOutlined,
  RollbackOutlined,
  SaveOutlined,
} from '@ant-design/icons-vue'
import SmartCodeEditor from '@/components/editor/SmartCodeEditor.vue'
import type { SkillFileTreeNode } from '@/types'
import * as skillApi from '@/api/skill'

const props = defineProps<{
  skillId: string
  file: SkillFileTreeNode | null
}>()

const emit = defineEmits<{
  dirtyChange: [dirty: boolean]
  saved: []
}>()

const editorContent = ref('')
const originalContent = ref('')
const saveStatus = ref<'idle' | 'saving' | 'saved'>('saved')
const loaded = ref(false)
let savedTimer: ReturnType<typeof setTimeout> | null = null

const isDirty = computed(() => editorContent.value !== originalContent.value)
const breadcrumb = computed(() => props.file?.path.split('/').join(' / ') || '')

watch(
  () => props.file,
  async (newFile, oldFile) => {
    if (oldFile && isDirty.value) {
      const confirmed = await showUnsavedConfirm()
      if (!confirmed) {
        return
      }
    }

    clearSavedTimer()

    if (newFile && !newFile.directory) {
      await loadFileContent(newFile)
    } else {
      editorContent.value = ''
      originalContent.value = ''
      loaded.value = false
      saveStatus.value = 'saved'
      emit('dirtyChange', false)
    }
  },
  { immediate: true },
)

async function loadFileContent(node: SkillFileTreeNode) {
  loaded.value = false
  try {
    if (node.content !== undefined) {
      editorContent.value = node.content || ''
    } else {
      const res = await skillApi.getFileContent(props.skillId, node.path)
      editorContent.value = res.data?.data || ''
    }
    originalContent.value = editorContent.value
    saveStatus.value = 'saved'
    emit('dirtyChange', false)
  } catch {
    editorContent.value = ''
    originalContent.value = ''
    saveStatus.value = 'saved'
    emit('dirtyChange', false)
  } finally {
    loaded.value = true
  }
}

function handleContentChange(newContent: string) {
  editorContent.value = newContent
  if (isDirty.value) {
    clearSavedTimer()
    if (saveStatus.value !== 'idle') {
      saveStatus.value = 'idle'
      emit('dirtyChange', true)
    }
  } else {
    emit('dirtyChange', false)
  }
}

async function handleSave() {
  if (!props.file || saveStatus.value === 'saving') return

  saveStatus.value = 'saving'
  try {
    const content = editorContent.value
    if (props.file.fileId) {
      await skillApi.updateFile(props.file.fileId, content)
    } else {
      await skillApi.writeFileSystemFile(props.skillId, {
        path: props.file.path,
        content,
      })
    }
    originalContent.value = content
    saveStatus.value = 'saved'
    emit('dirtyChange', false)
    emit('saved')

    savedTimer = setTimeout(() => {
      saveStatus.value = 'idle'
    }, 3000)
  } catch {
    saveStatus.value = 'idle'
  }
}

function handleRevert() {
  editorContent.value = originalContent.value
  saveStatus.value = 'saved'
  emit('dirtyChange', false)
}

function showUnsavedConfirm(): Promise<boolean> {
  return new Promise((resolve) => {
    Modal.confirm({
      title: '未保存的更改',
      content: '当前文件有未保存的修改，是否放弃更改并切换文件？',
      okText: '放弃更改',
      cancelText: '继续编辑',
      okButtonProps: { danger: true },
      onOk: () => resolve(true),
      onCancel: () => resolve(false),
    })
  })
}

function clearSavedTimer() {
  if (savedTimer) {
    clearTimeout(savedTimer)
    savedTimer = null
  }
}

defineExpose({
  isDirty: () => isDirty.value,
  save: handleSave,
})
</script>

<template>
  <div class="skill-editor">
    <div v-if="!file" class="editor-empty">
      <div class="empty-text">请在左侧选择文件进行编辑</div>
    </div>

    <template v-else-if="loaded">
      <div class="editor-toolbar">
        <span class="breadcrumb">{{ breadcrumb }}</span>
        <div class="toolbar-actions">
          <template v-if="isDirty">
            <AButton type="text" size="small" @click="handleRevert">
              <template #icon><RollbackOutlined /></template>
              放弃更改
            </AButton>
            <AButton
              type="primary"
              size="small"
              :loading="saveStatus === 'saving'"
              @click="handleSave"
            >
              <template v-if="saveStatus !== 'saving'" #icon><SaveOutlined /></template>
              保存
            </AButton>
          </template>
          <span v-else class="saved-indicator">
            <CheckCircleFilled />
            <span>已保存</span>
          </span>
        </div>
      </div>

      <div class="editor-body">
        <SmartCodeEditor
          :model-value="editorContent"
          :show-toolbar="false"
          language="txt"
          theme="light"
          height="calc(100vh - 100px)"
          @update:model-value="handleContentChange"
          @save="handleSave"
        />
      </div>
    </template>

    <div v-else class="editor-loading">
      <LoadingOutlined spin />
    </div>
  </div>
</template>

<style scoped>
.skill-editor {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.editor-empty,
.editor-loading {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #bfbfbf;
  font-size: 14px;
  background: #fff;
}

.editor-loading .anticon {
  font-size: 24px;
  color: #999;
}

.editor-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px;
  border-bottom: 1px solid #f0f0f0;
  background: #fff;
  flex-shrink: 0;
}

.breadcrumb {
  font-size: 15px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.saved-indicator {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #52c41a;
  padding: 2px 0;
}

.saved-indicator .anticon {
  font-size: 13px;
}

.editor-body {
  flex: 1;
  overflow: hidden;
  background: #fff;
}
</style>
