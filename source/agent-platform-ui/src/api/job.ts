import request from '@/utils/request'
import type { ApiResponse } from '@/types'
import type { JobInfo } from '@/types'

/**
 * 查询所有定时任务
 * GET /job/list
 */
export function list() {
  return request.get<ApiResponse<JobInfo[]>>('/api/job/list')
}

/**
 * 新增定时任务
 * POST /job/add
 */
export function add(jobInfo: JobInfo) {
  return request.post<ApiResponse<boolean>>('/api/job/add', jobInfo)
}

/**
 * 修改定时任务
 * POST /job/update
 */
export function update(jobInfo: JobInfo) {
  return request.post<ApiResponse<boolean>>('/api/job/update', jobInfo)
}

/**
 * 修改定时任务的 cron 表达式
 * PATCH /job/updateCron
 */
export function updateCron(id: string, cron: string) {
  return request.patch<ApiResponse<boolean>>('/api/job/updateCron', null, {
    params: { id, cron }
  })
}

/**
 * 删除定时任务
 * DELETE /job/delete
 */
export function remove(id: string) {
  return request.delete<ApiResponse<boolean>>('/api/job/delete', {
    params: { id }
  })
}

/**
 * 启动定时任务
 * POST /job/start
 */
export function start(id: string) {
  return request.post<ApiResponse<boolean>>('/api/job/start', null, {
    params: { id }
  })
}

/**
 * 停止定时任务
 * POST /job/stop
 */
export function stop(id: string) {
  return request.post<ApiResponse<boolean>>('/api/job/stop', null, {
    params: { id }
  })
}

/**
 * 根据业务ID查询定时任务
 * GET /job/getByBizId
 */
export function getByBizId(bizId: string) {
  return request.get<ApiResponse<JobInfo>>('/api/job/getByBizId', {
    params: { bizId }
  })
}

/**
 * 根据业务ID删除定时任务（解绑）
 * DELETE /job/deleteByBizId
 */
export function deleteByBizId(bizId: string) {
  return request.delete<ApiResponse<boolean>>('/api/job/deleteByBizId', {
    params: { bizId }
  })
}
