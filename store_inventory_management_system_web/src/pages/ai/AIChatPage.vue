<template>
  <div class="space-y-4">
    <div class="text-lg font-medium">AI 助手</div>
    <el-card shadow="never">
      <div class="space-y-3">
        <el-input v-model="prompt" type="textarea" :rows="5" placeholder="输入你的问题" />
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

