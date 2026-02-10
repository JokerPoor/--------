<template>
  <div class="space-y-4">
    <div class="text-lg font-medium">AI 助手</div>
    <el-card shadow="never">
      <div class="space-y-3">
        <el-input v-model="prompt" type="textarea" :rows="5" placeholder="输入你的问题" />
        
        <!-- 常见问题标签 -->
        <div class="flex flex-wrap gap-2">
          <span class="text-sm text-gray-500 self-center">常见问题：</span>
          <el-tag 
            v-for="(q, idx) in commonQuestions" 
            :key="idx" 
            class="cursor-pointer hover:opacity-80" 
            @click="useQuestion(q)"
          >
            {{ q }}
          </el-tag>
        </div>

        <div class="flex justify-end gap-2">
          <el-button @click="clear" :disabled="loading">清空</el-button>
          <el-button type="primary" @click="send" :loading="loading" :disabled="!prompt.trim()">发送</el-button>
        </div>
      </div>
    </el-card>
    <el-card v-if="answer" shadow="never">
      <pre class="whitespace-pre-wrap break-words">{{ answer }}</pre>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import http from '../../services/http'

const prompt = ref('')
const answer = ref('')
const loading = ref(false)

const commonQuestions = [
  '如何创建采购订单？',
  '怎么进行库存入库？',
  '如何添加新商品？',
  '查看最近的销售报表',
  '库存预警怎么设置？',
  '如何给员工分配权限？'
]

function useQuestion(q: string) {
  prompt.value = q
}

function clear() {
  prompt.value = ''
  answer.value = ''
}

async function send() {
  loading.value = true
  try {
    const res = await http.post('/ai/chat/text', { prompt: prompt.value })
    answer.value = res.data || ''
  } finally {
    loading.value = false
  }
}
</script>

