<template>
  <div class="space-y-4">
    <div class="flex justify-between items-center">
      <div class="text-lg font-medium">销售统计</div>
      <el-button type="primary" :loading="loading" @click="fetch">刷新</el-button>
    </div>

    <el-card shadow="never">
      <el-form label-width="110px">
        <el-form-item label="统计时间范围">
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

    <el-card shadow="never">
      <div class="grid grid-cols-1 md:grid-cols-4 gap-3">
        <el-statistic title="销售额" :value="stats.totalRevenue || 0" />
        <el-statistic title="订单数" :value="stats.orderCount || 0" />
        <el-statistic title="销量(件)" :value="stats.unitsSold || 0" />
        <el-statistic title="客单价" :value="stats.avgOrderValue || 0" />
      </div>
    </el-card>

    <el-card shadow="never" v-if="(stats.topProducts || []).length">
      <div class="text-base font-medium mb-3">Top 商品</div>
      <el-table :data="stats.topProducts" border>
        <el-table-column prop="productId" label="商品ID" width="120" />
        <el-table-column prop="productName" label="商品名称" min-width="180" />
        <el-table-column prop="unitsSold" label="销量(件)" width="120" />
        <el-table-column prop="revenue" label="销售额" width="160" />
      </el-table>
    </el-card>

    <el-card shadow="never" v-if="(stats.dailyTrend || []).length">
      <div class="text-base font-medium mb-3">日趋势</div>
      <el-table :data="stats.dailyTrend" border>
        <el-table-column prop="date" label="日期" width="140" />
        <el-table-column prop="unitsSold" label="销量(件)" width="120" />
        <el-table-column prop="revenue" label="销售额" width="160" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import http from '../../services/http'

const loading = ref(false)
const range = ref<[string, string] | null>(null)

const stats = reactive<any>({
  totalRevenue: 0,
  orderCount: 0,
  unitsSold: 0,
  avgOrderValue: 0,
  topProducts: [],
  dailyTrend: []
})

async function fetch() {
  loading.value = true
  try {
    const params: any = {}
    if (range.value?.[0] && range.value?.[1]) {
      params.startTime = new Date(`${range.value[0]}T00:00:00`).getTime()
      params.endTime = new Date(`${range.value[1]}T23:59:59`).getTime()
    }
    const res = await http.get('/ai/stats/sales', { params })
    Object.assign(stats, res.data || {})
  } finally {
    loading.value = false
  }
}

fetch()
</script>

