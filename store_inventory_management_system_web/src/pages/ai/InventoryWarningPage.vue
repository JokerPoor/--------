<template>
  <div class="space-y-4">
    <div class="flex justify-between items-center">
      <div class="text-lg font-medium">库存预警</div>
      <el-button type="primary" :loading="loading" @click="fetch">刷新</el-button>
    </div>

    <el-card shadow="never">
      <div class="flex gap-2 items-center">
        <el-input v-model="productName" placeholder="商品名称(模糊)" style="max-width: 280px" />
        <el-button @click="onSearch">查询</el-button>
      </div>
    </el-card>

    <el-card shadow="never">
      <el-table :data="rows" border :loading="loading">
        <el-table-column prop="productId" label="商品ID" width="120" />
        <el-table-column prop="productName" label="商品名称" min-width="180" />
        <el-table-column prop="warehouseId" label="仓库ID" width="120" />
        <el-table-column prop="quantity" label="当前库存" width="120" />
        <el-table-column prop="warningThreshold" label="预警阈值" width="120" />
        <el-table-column label="缺口" width="120">
          <template #default="{ row }">
            {{ Math.max(0, Number(row.warningThreshold || 0) - Number(row.quantity || 0)) }}
          </template>
        </el-table-column>
      </el-table>
      <div class="pt-3 flex justify-end">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :total="page.total"
          :page-size="page.size"
          :current-page="page.current"
          @current-change="onPageChange"
          @size-change="onSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import http from '../../services/http'

const loading = ref(false)
const rows = ref<any[]>([])
const productName = ref('')

const page = reactive({ total: 0, current: 1, size: 10 })

async function fetch() {
  loading.value = true
  try {
    const res = await http.get('/ai/inventory/warnings', {
      params: {
        current: page.current,
        size: page.size,
        productName: productName.value || undefined
      }
    })
    rows.value = res.data.records || []
    page.total = res.data.total || 0
  } finally {
    loading.value = false
  }
}

function onSearch() {
  page.current = 1
  fetch()
}

function onPageChange(v: number) {
  page.current = v
  fetch()
}

function onSizeChange(v: number) {
  page.size = v
  page.current = 1
  fetch()
}

fetch()
</script>

