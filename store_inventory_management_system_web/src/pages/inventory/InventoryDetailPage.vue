<template>
  <div class="p-4 space-y-4">
    <!-- Search Filter -->
    <div class="bg-white p-4 rounded-lg shadow-sm">
      <el-form :inline="true" :model="query" class="flex flex-wrap gap-2">
        <el-form-item label="商品名称">
          <el-input v-model="query.productName" placeholder="输入商品名称" clearable @keyup.enter="fetchData" />
        </el-form-item>
        <el-form-item label="仓库">
          <el-select v-model="query.warehouseId" placeholder="选择仓库" clearable class="w-40">
            <el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="变动类型">
          <el-select v-model="query.type" placeholder="全部" clearable class="w-32">
            <el-option label="入库" :value="1" />
            <el-option label="出库" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务类型">
          <el-select v-model="query.orderType" placeholder="全部" clearable class="w-32">
            <el-option label="采购" :value="0" />
            <el-option label="采退" :value="1" />
            <el-option label="销售" :value="2" />
            <el-option label="销退" :value="3" />
            <el-option label="调拨转出" :value="4" />
            <el-option label="调拨转入" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- Data Table -->
    <div class="bg-white p-4 rounded-lg shadow-sm">
      <el-table v-loading="loading" :data="rows" style="width: 100%" stripe>
        <el-table-column prop="productId" label="商品信息" min-width="200">
          <template #default="{ row }">
            <div class="flex items-center gap-3">
              <el-image 
                :src="row.productUrl" 
                class="w-12 h-12 rounded bg-gray-100 object-cover flex-shrink-0"
                :preview-src-list="[row.productUrl]" 
                preview-teleported
              >
                <template #error>
                  <div class="w-full h-full flex items-center justify-center text-gray-400">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="min-w-0">
                <div class="font-medium truncate" :title="row.productName">{{ row.productName || '未知商品' }}</div>
                <div class="text-xs text-gray-500 truncate">{{ row.productDescription }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="warehouseId" label="所属仓库" min-width="120">
          <template #default="{ row }">
            {{ getWarehouseName(row.warehouseId) }}
          </template>
        </el-table-column>

        <el-table-column prop="type" label="变动方向" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'success' : 'warning'" effect="dark">
              {{ row.type === 1 ? '入库' : '出库' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="orderType" label="业务类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getOrderTypeTag(row.orderType)">
              {{ getOrderTypeText(row.orderType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="productQuantity" label="变动数量" width="120" align="right">
          <template #default="{ row }">
            <span :class="row.type === 1 ? 'text-green-600 font-bold' : 'text-orange-600 font-bold'">
              {{ row.type === 1 ? '+' : '-' }}{{ row.productQuantity }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="变动时间" width="180" />
      </el-table>

      <!-- Pagination -->
      <div class="flex justify-end mt-4">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Picture } from '@element-plus/icons-vue'
import http from '../../services/http'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const rows = ref([])
const warehouses = ref<any[]>([])
const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

const query = reactive({
  productName: '',
  warehouseId: undefined,
  type: undefined,
  orderType: undefined
})

const orderTypeMap: Record<number, string> = {
  0: '采购',
  1: '采退',
  2: '销售',
  3: '销退',
  4: '调拨转出',
  5: '调拨转入'
}

function getOrderTypeText(type: number) {
  return orderTypeMap[type] || '未知'
}

function getOrderTypeTag(type: number) {
  switch (type) {
    case 0: return 'primary' // 采购
    case 1: return 'warning' // 采退
    case 2: return 'success' // 销售
    case 3: return 'danger'  // 销退
    case 4: return 'info'    // 调拨
    case 5: return 'info'    // 调拨
    default: return ''
  }
}

async function fetchWarehouses() {
  try {
    const res = await http.get('/warehouse/list', { params: { current: 1, size: 100 } })
    if (res.data?.records) {
      warehouses.value = res.data.records
    }
  } catch (e) {
    console.error('Failed to fetch warehouses', e)
  }
}

function getWarehouseName(id: number) {
  const w = warehouses.value.find(item => item.id === id)
  return w ? w.name : `仓库#${id}`
}

async function fetchData() {
  loading.value = true
  try {
    const res = await http.get('/api/inventory/detail/list', {
      params: {
        current: pagination.current,
        size: pagination.size,
        ...query
      }
    })
    if (res.data) {
      rows.value = res.data.records
      pagination.total = res.data.total
    }
  } catch (e) {
    ElMessage.error('加载库存明细失败')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.productName = ''
  query.warehouseId = undefined
  query.type = undefined
  query.orderType = undefined
  fetchData()
}

onMounted(() => {
  fetchWarehouses()
  fetchData()
})
</script>
