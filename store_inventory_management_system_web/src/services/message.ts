import http from './http'

export interface Message {
  id: number
  messageType: string // 0-自动调拨, 1-自动购买
  content: string
  relatedId?: number
  productId?: number
  recipientId: number
  readStatus: number // 0-未读, 1-已读
  createTime: string
  readTime?: string
}

export interface MessageListParams {
  pageNum: number
  pageSize: number
  recipientId?: number
  messageType?: string
  readStatus?: number // 0-未读, 1-已读, 不传则查询全部
}

export interface MessageListResponse {
  records: Message[]
  total: number
  current: number
  size: number
}

// 获取消息列表
export async function getMessageList(params: MessageListParams): Promise<MessageListResponse> {
  const res = await http.get('/sys-message/page', { params })
  return res.data
}

// 获取未读消息数量
export async function getUnreadCount(recipientId: number): Promise<number> {
  const res = await http.get('/sys-message/unread-count', { params: { recipientId } })
  return res.data || 0
}

// 标记消息为已读
export async function markAsRead(id: number): Promise<void> {
  await http.put(`/sys-message/mark-read/${id}`)
}

// 批量标记消息为已读
export async function batchMarkAsRead(messageIds: number[]): Promise<void> {
  await http.put('/sys-message/batch-mark-read', messageIds)
}

export default {
  getMessageList,
  getUnreadCount,
  markAsRead,
  batchMarkAsRead
}
