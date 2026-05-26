import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { useAgentClient } from '@/composables/useAgentClient'
import { usePlanTracking } from '@/composables/chat/usePlanTracking'
import type {ChatMessageVO, RawEvent, UploadedFileItem} from '@/types'
import { useAccountStore } from '@/stores'

interface ChatAttachmentRef {
  id: string
  name?: string
  extension?: string
  size?: string
}

interface ChatForwardedProps {
  agentId: string
  agentCode?: string
  fileIds: string[]
  fileAttachments: ChatAttachmentRef[]
  memoryActive: boolean
  planActive: boolean
  toolProcessActive: boolean
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  userInfo: any
}

export function useChatStream(
  agentId: import('vue').Ref<string>,
  agentDetail: import('vue').Ref<any>,
  currentSessionId: import('vue').Ref<string | null>,
  fileIds?: import('vue').Ref<string[]>,
  memoryActive?: import('vue').Ref<boolean>,
  planActive?: import('vue').Ref<boolean>,
  toolProcessActive?: import('vue').Ref<boolean>,
  onMessagesChanged?: () => void | Promise<void>) {

  const { userInfo } = useAccountStore()

  // 计划追踪
  const {
    currentPlan,
    hasPlan,
    onToolStart: onPlanToolStart,
    onToolArgs: onPlanToolArgs,
    onToolResult: onPlanToolResult,
    resetPlan
  } = usePlanTracking()

  const normalizeAttachments = (files?: UploadedFileItem[]): ChatAttachmentRef[] =>
    (files ?? [])
      .filter((file) => file && !file.uploading)
      .map((file) => ({
        id: file.id,
        name: file.name,
        extension: file.extension,
        size: file.size
      }))

  const getForwardedProps = (): ChatForwardedProps => ({
    agentId: agentId.value,
    agentCode: agentDetail.value?.agentCode,
    fileIds: fileIds?.value ?? [],
    fileAttachments: [],
    memoryActive: memoryActive?.value ?? false,
    planActive: planActive?.value ?? false,
    toolProcessActive: toolProcessActive?.value ?? true,
    userInfo: userInfo
  })

  const newRunIds = () => {
    const suffix = `${Date.now()}_${Math.random().toString(36).slice(2, 11)}`
    return {
      runId: `run_${suffix}`,
      traceId: `trace_${suffix}`
    }
  }

  // 流式内容
  const agentHasResult = ref(true)
  const streamingContent = ref('')
  const streamingMessageId = ref<string | null>(null)

  // 推理流式内容
  const reasoningContent = ref('')
  const reasoningMessageId = ref<string | null>(null)

  // 工具调用进度
  const toolCallsInProgress = ref<
    Array<{ id: string; name: string; args: string; result?: string; startTime: number; elapsed?: number, needConfirm?: boolean }>
  >([])

  // 使用原有的 useAgentClient
  const { messages, isRunning, run, abort, addUserMessage, client } = useAgentClient({
    handlers: {
      onRunStarted: () => {
        toolCallsInProgress.value = []
        reasoningContent.value = ''
        reasoningMessageId.value = null
      },
      onTextMessageStart: (e) => {
        streamingMessageId.value = e.messageId
        streamingContent.value = ''
      },
      onTextMessageContent: (_e, currentText) => {
        agentHasResult.value = true
        streamingContent.value = currentText
      },
      onTextMessageEnd: () => {
        reasoningContent.value = ''
      },
      onReasoningMessageStart: (e) => {
        reasoningMessageId.value = e.messageId
        reasoningContent.value = ''
      },
      onReasoningMessageContent: (_e, currentText) => {
        reasoningContent.value = currentText
      },
      onReasoningMessageEnd: () => {},
      onToolCallStart: (e) => {
        // 计划追踪：记录工具调用名称
        onPlanToolStart(e.toolCallId, e.toolCallName)

        agentHasResult.value = true
        toolCallsInProgress.value = [
          ...toolCallsInProgress.value,
          { id: e.toolCallId, name: e.toolCallName, args: '', startTime: Date.now() }
        ]

      },
      onToolCallArgs: (_e, partialArgs) => {
        // 计划追踪：累积工具参数
        onPlanToolArgs(_e.toolCallId, partialArgs)

        const arr = [...toolCallsInProgress.value]
        const last = arr[arr.length - 1]
        if (last) last.args = partialArgs
        toolCallsInProgress.value = arr
      },
      onToolCallResult: (e) => {
        // 计划追踪：处理工具结果
        onPlanToolResult(e.toolCallId)

        try {
          // 判断是否开启了显示工具调用
          if (!(toolProcessActive?.value ?? true)) {
            return
          }
          // 更新工具调用结果和耗时
          toolCallsInProgress.value = toolCallsInProgress.value.map((t) =>
            t.id === e.toolCallId ? { ...t, result: e.content, elapsed: Date.now() - t.startTime } : t
          )

        } finally {
          // 清空进行中的工具调用（可根据需要保留，此处清空）
          toolCallsInProgress.value = []
        }
      },
      onRunFinished: (_e) => {
        agentHasResult.value = true
        if (toolCallsInProgress.value.length > 0) {
          toolCallsInProgress.value.forEach(item => item.needConfirm = true)
        }
        Promise.resolve(onMessagesChanged?.()).finally(() => {
          streamingContent.value = ''
          streamingMessageId.value = null
          reasoningContent.value = ''
          reasoningMessageId.value = null
        })
      },
      onRaw: (event) => {
        const e = event as RawEvent
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        const rawEvent: any = e.rawEvent
        if(rawEvent.error) {
          streamingMessageId.value = new Date().getTime() + '' + Math.floor(Math.random() * 90000) + 10000
          streamingContent.value = rawEvent.error
        }
     }
    }
  })

  // 发送消息
  const sendToolContent = async (value: any) => {
    const {id, content } = value
    client.messages = [{
      id,
      role: 'tool',
      content: JSON.stringify(content),
      toolCallId: content[0].id,
    }]

    toolCallsInProgress.value = toolCallsInProgress.value.filter(item => item.id != id)

    const ids = newRunIds()
    await run({
      threadId: currentSessionId.value || undefined,
      runId: ids.runId,
      forwardedProps: {
        ...getForwardedProps(),
        traceId: ids.traceId
      }
    })
  }

  // 中止运行
  const abortRun = async  () => {
    await abort()
    agentHasResult.value = true

    // 重置计划状态
    resetPlan()

    const sid = currentSessionId.value
    if (sid) {
      toolCallsInProgress.value = []
      streamingContent.value = ''
      streamingMessageId.value = null
      reasoningContent.value = ''
      reasoningMessageId.value = null
      isRunning.value = false

    }

  }

  // 发送消息（可选传入 fileIds 覆盖，用于发送时已清空输入框的场景）
  const sendMessage = async (
    inputText: string,
    messagesList: ChatMessageVO[],
    overrideFileIds?: string[],
    overrideFiles?: UploadedFileItem[]
  ) => {
    const effectiveFileIds = overrideFileIds ?? fileIds?.value ?? []
    if (!agentId.value) return
    if (!inputText.trim() && !effectiveFileIds.length) return
    if (isRunning.value) return
    if (!agentDetail.value?.agentCode) {
      message.error('智能体信息未加载完成，请稍后再试')
      return
    }

    // 构建 client 需要的消息格式
    client.messages = messagesList
      .filter((m) => !['system', 'tool'].includes(m.role))
      .map((m) => ({
        id: String(m.id),
        role: m.role as any,
        content: (m.content || '') as string
      }))

    const forwardedProps = getForwardedProps()
    if (overrideFileIds !== undefined) {
      forwardedProps.fileIds = overrideFileIds
    }
    if (overrideFiles !== undefined) {
      forwardedProps.fileAttachments = normalizeAttachments(overrideFiles)
    }

    agentHasResult.value = false
    const ids = newRunIds()
    await run({
      threadId: currentSessionId.value || undefined,
      runId: ids.runId,
      forwardedProps: {
        ...forwardedProps,
        traceId: ids.traceId
      }
    })
  }

  return {
    agentHasResult,
    streamingContent,
    streamingMessageId,
    reasoningContent,
    reasoningMessageId,
    toolCallsInProgress,
    isRunning,
    currentPlan,
    hasPlan,
    abortRun,
    sendMessage,
    sendToolContent,
    client, // 如果需要暴露
  }
}
