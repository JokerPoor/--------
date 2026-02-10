<template>
  <div>
    <EpTable
      :rows="rows"
      :columns="cols"
      :loading="loading"
      :pagination="pagination"
      @refresh="fetch"
      @search="onSearch"
      @update:current="pagination.current = $event; fetch()"
      @update:size="pagination.size = $event; fetch()"
    >
      <template #toolbar>
        <el-button type="primary" @click="openCreate" v-perm="'purchase:return:add'">新建采退单</el-button>
      </template>
      
      <template #url="{ row }">
        <el-image :src="row.productUrl" class="w-10 h-10 object-cover rounded" :preview-src-list="[row.productUrl]" preview-teleported />
      </template>

      <template #status="{ row }">
        <el-tag v-if="row.status === 0">待确认</el-tag>
        <el-tag v-else-if="row.status === 1" type="success">已完成</el-tag>
        <el-tag v-else-if="row.status === 2" type="info">已取消</el-tag>
      </template>

      <template #payStatus="{ row }">
        <el-tag v-if="row.amountOrderStatus === 0" type="danger">待退款</el-tag>
        <el-tag v-else-if="row.amountOrderStatus === 1" type="success">已退款</el-tag>
        <el-tag v-else-if="row.amountOrderStatus === 2" type="info">已取消</el-tag>
      </template>

      <template #actions="{ row }">
        <el-button v-if="row.amountOrderStatus === 0" link type="primary" @click="onPay(row.amountOrderId)" v-perm="'POST:/amount/order/payorder/:id'">退款</el-button>
        <el-button v-if="row.amountOrderStatus === 0" link type="warning" @click="onMockPay(row.amountOrderId)">一键退款</el-button>
        <el-button v-if="row.status === 0" link type="primary" @click="onConfirm(row)" v-perm="'purchase:return:confirm'">确认退货</el-button>
      </template>
    </EpTable>

    <!-- Create Dialog -->
    <el-dialog v-model="createVisible" title="新建采退订单" width="500px">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="选择商品" required>
          <el-select
            v-model="createForm.productId"
            filterable
            remote
            :remote-method="searchProducts"
            placeholder="搜索商品名称"
            :loading="productLoading"
          >
            <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择仓库" required>
          <el-select v-model="createForm.warehouseId" placeholder="选择仓库">
            <el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="退货数量" required>
          <el-input-number v-model="createForm.returnQuantity" :min="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate" :loading="submitting">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, onBeforeUnmount } from 'vue'
import EpTable from '../../components/ep/EpTable.vue'
import http from '../../services/http'
import { ElMessage, ElMessageBox } from 'element-plus'

const rows = ref<any[]>([])
const loading = ref(false)
const pagination = reactive({ current: 1, size: 10, total: 0 })
const keyword = ref('')

const cols = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'productUrl', label: '图片', width: 80, slot: 'url' },
  { prop: 'productName', label: '商品名称' },
  { prop: 'productPrice', label: '单价', width: 100 },
  { prop: 'productQuantity', label: '退货数量', width: 100 },
  { prop: 'totalAmount', label: '退款金额', width: 120 },
  { prop: 'status', label: '状态', width: 100, slot: 'status' },
  { prop: 'amountOrderStatus', label: '退款状态', width: 100, slot: 'payStatus' },
  { prop: 'createTime', label: '创建时间', width: 180 }
]

async function fetch() {
  console.log('[PurchaseReturnPage] Fetching data...')
  loading.value = true
  try {
    const res = await http.get('/purchase/return/list', {
      params: {
        current: pagination.current,
        size: pagination.size,
        productName: keyword.value
      }
    })
    if (res.data) {
      rows.value = res.data.records
      pagination.total = res.data.total
      console.log('[PurchaseReturnPage] Data loaded:', rows.value.length, 'records')
    }
  } finally {
    loading.value = false
  }
}

function onSearch(k: string) {
  keyword.value = k
  pagination.current = 1
  fetch()
}

// Create Logic
const createVisible = ref(false)
const submitting = ref(false)
const createForm = reactive({
  productId: undefined,
  returnQuantity: 1,
  warehouseId: undefined
})
const products = ref<any[]>([])
const warehouses = ref<any[]>([])
const productLoading = ref(false)

async function searchProducts(query: string) {
  if (!query) return
  productLoading.value = true
  try {
    const res = await http.get('/product/list', { params: { name: query, current: 1, size: 20 } })
    products.value = res.data?.records || []
  } finally {
    productLoading.value = false
  }
}

async function openCreate() {
  createForm.productId = undefined
  createForm.returnQuantity = 1
  createForm.warehouseId = undefined
  createVisible.value = true
  searchProducts(' ')
  
  if (warehouses.value.length === 0) {
    const res = await http.get('/warehouse/list', { params: { size: 100 } })
    warehouses.value = res.data?.records || []
  }
}

async function submitCreate() {
  if (!createForm.productId || !createForm.warehouseId) return ElMessage.warning('请补全信息')
  submitting.value = true
  try {
    await http.post('/purchase/return', {
      productId: createForm.productId,
      returnQuantity: createForm.returnQuantity,
      warehouseId: createForm.warehouseId
    })
    ElMessage.success('创建成功')
    createVisible.value = false
    fetch()
  } finally {
    submitting.value = false
  }
}

// Pay Logic
async function onPay(id: number) {
  if (!id) return ElMessage.error('订单异常，缺少金额单ID')
  try {
    const res = await http.post(`/amount/order/payorder/${id}`)
    // 后端返回的是HTML表单
    if (typeof res === 'string') {
      const div = document.createElement('div')
      div.innerHTML = res
      document.body.appendChild(div)
      const form = div.getElementsByTagName('form')[0]
      if (form) {
        form.submit()
      } else {
        ElMessage.error('支付跳转失败')
      }
    } else {
       ElMessage.success('支付请求已发送')
       fetch()
    }
  } catch (e) {
    // error handled by interceptor
  }
}

async function onMockPay(id: number) {
  if (!id) return
  try {
    await ElMessageBox.confirm('是否确认模拟退款（仅用于测试）？', '提示')
    await http.post(`/amount/order/mock-pay/${id}`)
    ElMessage.success('模拟退款成功')
    fetch()
  } catch (e) {
    // cancelled
  }
}

async function onConfirm(row: any) {
    await ElMessageBox.confirm('确认退货? 将扣减库存并生成退款单', '提示', { type: 'warning' })
    await http.post(`/purchase/return/confirm/${row.id}`)
    ElMessage.success('已确认')
    fetch()
}

onMounted(() => {
  console.log('[PurchaseReturnPage] Component mounted')
  fetch()
})

onBeforeUnmount(() => {
  console.log('[PurchaseReturnPage] Component unmounting')
})
</script>
