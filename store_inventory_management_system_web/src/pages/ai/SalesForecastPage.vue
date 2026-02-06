<template>
  <div class="space-y-4">
    <div class="flex justify-between items-center">
      <div class="text-lg font-medium">销量预测</div>
      <el-button type="primary" :loading="loading" @click="fetch">生成预测</el-button>
    </div>
    <el-card shadow="never">
      <el-form label-width="110px">
        <el-form-item label="商品ID(可选)">
          <el-input v-model="form.productId" placeholder="例如：1001" />
        </el-form-item>
        <el-form-item label="时间范围(可选)">
          <el-date-picker
            v-model="range"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="items.length > 0" shadow="never">
      <el-table :data="items" border>
        <el-table-column prop="productId" label="商品ID" width="120" />
        <el-table-column prop="productName" label="商品名称" min-width="160" />
        <el-table-column prop="suggestedPurchaseMin" label="建议最小补货" width="140" />
        <el-table-column prop="suggestedPurchaseMax" label="建议最大补货" width="140" />
        <el-table-column prop="confidence" label="置信度" width="120" />
        <el-table-column prop="reason" label="原因" min-width="260" />
      </el-table>
    </el-card>

    <el-card v-else-if="raw" shadow="never">
      <pre class="whitespace-pre-wrap break-words">{{ raw }}</pre>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import http from '../../services/http'

type ForecastItem = {
  productId: number | string
  productName: string
  reason: string
  suggestedPurchaseMin: number
  suggestedPurchaseMax: number
  confidence: number
}

const loading = ref(false)
const raw = ref('')
const items = ref<ForecastItem[]>([])

const form = reactive({
  productId: ''
})

const range = ref<[string, string] | null>(null)

async function fetch() {
  loading.value = true
  raw.value = ''
  items.value = []
  try {
    const payload: any = {}
    if (form.productId.trim()) payload.productId = Number(form.productId.trim())
    if (range.value?.[0] && range.value?.[1]) {
      payload.startTime = `${range.value[0]}T00:00:00`
      payload.endTime = `${range.value[1]}T23:59:59`
    }
    const res = await http.post('/ai/forecast/hot-products', Object.keys(payload).length ? payload : null)
    const text = String(res.data ?? '')
    raw.value = text
    try {
      const parsed = JSON.parse(text)
      if (Array.isArray(parsed)) items.value = parsed
    } catch { }
  } finally {
    loading.value = false
  }
}
</script>

