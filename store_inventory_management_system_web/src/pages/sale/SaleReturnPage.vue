<template>
  <div class="p-4 bg-white">
    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="我的退货" name="my">
        <EpTable
          ref="myTable"
          :rows="myRows"
          :columns="cols"
          :loading="loading"
          :pagination="pagination"
          @refresh="fetchMy"
          @search="onSearch"
          @update:current="pagination.current = $event; fetchMy()"
          @update:size="pagination.size = $event; fetchMy()"
        >
          <template #url="{ row }">
            <el-image :src="row.productUrl" class="w-10 h-10 object-cover rounded" :preview-src-list="[row.productUrl]" preview-teleported />
          </template>

          <template #status="{ row }">
            <el-tag v-if="row.status === 0">处理中</el-tag>
            <el-tag v-else-if="row.status === 1" type="success">已完成</el-tag>
            <el-tag v-else-if="row.status === 2" type="info">已取消</el-tag>
          </template>
        </EpTable>
      </el-tab-pane>

      <el-tab-pane label="门店退货管理" name="store" v-if="hasManagePerm">
        <EpTable
          ref="storeTable"
          :rows="storeRows"
          :columns="cols"
          :loading="loading"
          :pagination="pagination"
          @refresh="fetchStore"
          @search="onSearch"
          @update:current="pagination.current = $event; fetchStore()"
          @update:size="pagination.size = $event; fetchStore()"
        >
          <template #url="{ row }">
            <el-image :src="row.productUrl" class="w-10 h-10 object-cover rounded" :preview-src-list="[row.productUrl]" preview-teleported />
          </template>

          <template #status="{ row }">
            <el-tag v-if="row.status === 0">处理中</el-tag>
            <el-tag v-else-if="row.status === 1" type="success">已完成</el-tag>
            <el-tag v-else-if="row.status === 2" type="info">已取消</el-tag>
          </template>

          <template #row-actions="{ row }">
            <el-button v-if="row.status === 0" link type="success" @click="onConfirm(row)" v-perm="'inventory:sale-return:confirm'">确认退货</el-button>
          </template>
        </EpTable>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed } from 'vue'
import EpTable from '../../components/ep/EpTable.vue'
import http from '../../services/http'
import { ElMessage, ElMessageBox } from 'element-plus'
import auth from '../../services/auth'

const hasManagePerm = computed(() => auth.hasPermission('inventory:sale-return:confirm'))

const activeTab = ref('my')
const myRows = ref([])
const storeRows = ref([])
const loading = ref(false)
const pagination = reactive({ current: 1, size: 10, total: 0 })
const keyword = ref('')

const cols = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'productUrl', label: '图片', width: 80, slot: 'url' },
  { prop: 'productName', label: '商品名称' },
  { prop: 'productPrice', label: '单价', width: 100 },
  { prop: 'productQuantity', label: '数量', width: 100 },
  { prop: 'totalAmount', label: '退款金额', width: 120 },
  { prop: 'reason', label: '原因' },
  { prop: 'status', label: '状态', width: 100, slot: 'status' },
  { prop: 'createTime', label: '创建时间', width: 180 }
]

async function fetchMy() {
  loading.value = true
  try {
    const res = await http.get('/sale/return/my', {
      params: {
        current: pagination.current,
        size: pagination.size,
        productName: keyword.value
      }
    })
    if (res.data) {
      myRows.value = res.data.records
      pagination.total = res.data.total
    }
  } finally {
    loading.value = false
  }
}

async function fetchStore() {
  loading.value = true
  try {
    const res = await http.get('/sale/return/store', {
      params: {
        current: pagination.current,
        size: pagination.size,
        productName: keyword.value
      }
    })
    if (res.data) {
      storeRows.value = res.data.records
      pagination.total = res.data.total
    }
  } finally {
    loading.value = false
  }
}

function onTabChange() {
  pagination.current = 1
  if (activeTab.value === 'my') fetchMy()
  else fetchStore()
}

function onSearch(k: string) {
  keyword.value = k
  pagination.current = 1
  if (activeTab.value === 'my') fetchMy()
  else fetchStore()
}

async function onConfirm(row: any) {
    await ElMessageBox.confirm('确认收到退货并入库?', '提示', { type: 'warning' })
    // Use plain post body for Long
    await http.post('/inventory/sale-return/confirm', row.id, { headers: { 'Content-Type': 'application/json' } })
    ElMessage.success('确认退货成功')
    fetchStore()
}

onMounted(fetchMy)
</script>
