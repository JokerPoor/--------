<template>
  <div class="space-y-4 p-4">
    <!-- Header -->
    <div class="flex justify-between items-center bg-white p-4 rounded-lg shadow-sm">
      <div>
        <h1 class="text-2xl font-bold text-gray-800">库存调拨记录</h1>
        <p class="text-gray-500 mt-1">查看所有仓库间的商品调拨历史</p>
      </div>
      <el-button type="primary" @click="fetch"><el-icon><Refresh /></el-icon> 刷新</el-button>
    </div>

    <!-- Filter -->
    <div class="bg-white p-4 rounded-lg shadow-sm">
      <el-form :inline="true" :model="searchForm" class="demo-form-inline">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.productName" placeholder="输入商品名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- List -->
    <EpTable
      :rows="rows"
      :columns="cols"
      :loading="loading"
      :pagination="pagination"
      :show-search="false"
      @refresh="fetch"
      @update:current="pagination.current = $event; fetch()"
      @update:size="pagination.size = $event; fetch()"
    >
      <template #product="{ row }">
        <div class="flex items-center gap-2">
            <el-image v-if="row.productUrl" :src="row.productUrl" class="w-10 h-10 rounded object-cover" />
            <span>{{ row.productName }}</span>
        </div>
      </template>
      <template #source="{ row }">
          <el-tag type="info">{{ row.sourceWarehouseName || row.sourceWarehouseId }}</el-tag>
      </template>
      <template #target="{ row }">
          <el-tag type="success">{{ row.targetWarehouseName || row.targetWarehouseId }}</el-tag>
      </template>
    </EpTable>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import EpTable from '../../components/ep/EpTable.vue'
import http from '../../services/http'
import { Refresh } from '@element-plus/icons-vue'

const loading = ref(false)
const pagination = reactive({ current: 1, size: 10, total: 0 })
const rows = ref([])
const searchForm = reactive({
  productName: ''
})

const cols = [
  { prop: 'id', label: 'ID', width: 180 },
  { prop: 'transferOrderId', label: '调拨单号', width: 180 },
  { prop: 'productName', label: '商品', minWidth: 200, slot: 'product' },
  { prop: 'transferQuantity', label: '调拨数量', width: 120 },
  { prop: 'sourceWarehouseName', label: '源仓库', width: 150, slot: 'source' },
  { prop: 'targetWarehouseName', label: '目标仓库', width: 150, slot: 'target' },
  { prop: 'remark', label: '备注', width: 200 },
  { prop: 'createTime', label: '调拨时间', width: 180 }
]

async function fetch() {
  loading.value = true
  try {
    const res = await http.get('/transfer/log/list', {
      params: {
        current: pagination.current,
        size: pagination.size,
        // 目前后端接口未实现按商品名称模糊搜索，这里可能需要后端配合修改，
        // 或者前端传productId。暂时先传分页。
        // 如果需要按商品名搜索，后端DTO需要支持或者前端先查商品ID。
        // 这里暂时忽略searchForm.productName的传递，后续完善。
      }
    })
    if (res.data) {
      rows.value = res.data.records
      pagination.total = res.data.total
    }
  } finally {
    loading.value = false
  }
}

function onSearch() {
  pagination.current = 1
  fetch()
}

function onReset() {
  searchForm.productName = ''
  onSearch()
}

onMounted(() => {
  fetch()
})
</script>

<style scoped>
</style>
