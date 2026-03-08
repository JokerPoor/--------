<template>
  <el-popover
    :visible="visible"
    placement="bottom-end"
    :width="400"
    trigger="click"
    popper-class="message-notification-popover"
    @show="handleShow"
    @hide="handleHide"
  >
    <template #reference>
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="message-badge">
        <el-button text @click="togglePopover">
          <el-icon :size="20">
            <Bell />
          </el-icon>
        </el-button>
      </el-badge>
    </template>

    <div class="message-notification">
      <!-- 头部 -->
      <div class="message-header">
        <div class="flex items-center justify-between">
          <span class="text-base font-semibold">消息通知</span>
          <el-button
            v-if="unreadCount > 0"
            text
            size="small"
            @click="handleMarkAllRead"
          >
            全部已读
          </el-button>
        </div>
        <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="mt-2">
          <el-tab-pane label="全部" name="all" />
          <el-tab-pane label="未读" name="unread">
            <template #label>
              未读
              <el-badge v-if="unreadCount > 0" :value="unreadCount" :max="99" class="ml-1" />
            </template>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 消息列表 -->
      <div class="message-list" v-loading="loading">
        <el-empty v-if="!loading && messages.length === 0" description="暂无消息" :image-size="80" />
        
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="message-item"
          :class="{ 'unread': msg.readStatus === 0 }"
          @click="handleMessageClick(msg)"
        >
          <div class="message-icon">
            <el-icon :size="20" :color="getMessageIconColor(msg.messageType)">
              <component :is="getMessageIcon(msg.messageType)" />
            </el-icon>
          </div>
          <div class="message-content">
            <div class="message-title">
              <span class="message-type-tag">{{ getMessageTypeText(msg.messageType) }}</span>
              <span v-if="msg.readStatus === 0" class="unread-dot"></span>
            </div>
            <div class="message-text">{{ msg.content }}</div>
            <div class="message-time">{{ formatTime(msg.createTime) }}</div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="message-footer" v-if="total > pageSize">
        <el-pagination
          small
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </el-popover>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { Bell, ShoppingCart, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import messageService, { type Message } from '../services/message'
import { useRouter } from 'vue-router'
import auth from '../services/auth'

const router = useRouter()

const visible = ref(false)
const loading = ref(false)
const messages = ref<Message[]>([])
const unreadCount = ref(0)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const activeTab = ref<'all' | 'unread'>('all')

let pollingTimer: number | null = null

// 获取当前用户ID
const currentUserId = computed(() => auth.state.user?.id || 0)

// 切换弹窗显示
function togglePopover() {
  visible.value = !visible.value
}

// 弹窗显示时
function handleShow() {
  loadMessages()
}

// 弹窗隐藏时
function handleHide() {
  visible.value = false
}

// 获取消息图标
function getMessageIcon(type: string) {
  switch (type) {
    case '0':
      return Refresh // 自动调拨
    case '1':
      return ShoppingCart // 自动购买
    default:
      return Bell
  }
}

// 获取消息图标颜色
function getMessageIconColor(type: string) {
  switch (type) {
    case '0':
      return '#409EFF' // 蓝色 - 自动调拨
    case '1':
      return '#67C23A' // 绿色 - 自动购买
    default:
      return '#909399'
  }
}

// 获取消息类型文本
function getMessageTypeText(type: string) {
  switch (type) {
    case '0':
      return '自动调拨'
    case '1':
      return '自动购买'
    default:
      return '系统通知'
  }
}

// 格式化时间
function formatTime(time: string) {
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  
  if (diff < minute) {
    return '刚刚'
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < 7 * day) {
    return `${Math.floor(diff / day)}天前`
  } else {
    return date.toLocaleDateString()
  }
}

// 加载消息列表
async function loadMessages() {
  if (!currentUserId.value) return
  
  loading.value = true
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      recipientId: currentUserId.value,
      readStatus: activeTab.value === 'unread' ? 0 : undefined
    }
    const res = await messageService.getMessageList(params)
    messages.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error('加载消息失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载未读数量
async function loadUnreadCount() {
  if (!currentUserId.value) return
  
  try {
    unreadCount.value = await messageService.getUnreadCount(currentUserId.value)
  } catch (error) {
    console.error('加载未读数量失败:', error)
  }
}

// 标签切换
function handleTabChange() {
  currentPage.value = 1
  loadMessages()
}

// 分页切换
function handlePageChange(page: number) {
  currentPage.value = page
  loadMessages()
}

// 点击消息
async function handleMessageClick(msg: Message) {
  // 如果未读，标记为已读
  if (msg.readStatus === 0) {
    try {
      await messageService.markAsRead(msg.id)
      msg.readStatus = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
  
  // 根据消息类型跳转到相应页面
  if (msg.relatedId) {
    if (msg.messageType === '0') {
      // 自动调拨 - 跳转到库存管理页面
      router.push('/inventory')
    } else if (msg.messageType === '1') {
      // 自动购买 - 跳转到采购订单页面
      router.push('/purchase/order')
    }
  }
  
  // 关闭弹窗
  visible.value = false
}

// 全部标记为已读
async function handleMarkAllRead() {
  if (messages.value.length === 0) return
  
  try {
    const unreadIds = messages.value
      .filter(msg => msg.readStatus === 0)
      .map(msg => msg.id)
    
    if (unreadIds.length === 0) {
      ElMessage.info('没有未读消息')
      return
    }
    
    await messageService.batchMarkAsRead(unreadIds)
    ElMessage.success('已全部标记为已读')
    unreadCount.value = 0
    loadMessages()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 开始轮询
function startPolling() {
  // 每30秒刷新一次未读数量
  pollingTimer = window.setInterval(() => {
    loadUnreadCount()
  }, 30000)
}

// 停止轮询
function stopPolling() {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
  }
}

onMounted(() => {
  loadUnreadCount()
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped lang="scss">
.message-badge {
  :deep(.el-badge__content) {
    border: none;
  }
}

.message-notification {
  max-height: 600px;
  display: flex;
  flex-direction: column;
}

.message-header {
  padding: 12px 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  
  :deep(.el-tabs__header) {
    margin: 0;
  }
  
  :deep(.el-tabs__nav-wrap::after) {
    display: none;
  }
}

.message-list {
  flex: 1;
  overflow-y: auto;
  max-height: 400px;
  min-height: 200px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
  border-bottom: 1px solid var(--el-border-color-lighter);
  
  &:hover {
    background-color: var(--el-fill-color-light);
  }
  
  &.unread {
    background-color: var(--el-color-primary-light-9);
  }
}

.message-icon {
  flex-shrink: 0;
  margin-right: 12px;
  margin-top: 2px;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.message-type-tag {
  font-size: 12px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.unread-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: var(--el-color-danger);
}

.message-text {
  font-size: 13px;
  color: var(--el-text-color-regular);
  line-height: 1.5;
  margin-bottom: 4px;
  word-break: break-word;
}

.message-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.message-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--el-border-color-lighter);
  display: flex;
  justify-content: center;
}
</style>

<style>
.message-notification-popover {
  padding: 0 !important;
}
</style>
