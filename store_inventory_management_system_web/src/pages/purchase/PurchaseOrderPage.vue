<template>
  <div>
    <div class="mb-4 bg-white p-4 rounded shadow-sm">
      <el-form :inline="true" :model="searchForm" class="demo-form-inline">
        <el-form-item label="商品名称">
          <el-input v-model="searchForm.productName" placeholder="输入商品名称" clearable />
        </el-form-item>
        <el-form-item label="物流状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="待发货" :value="0" />
            <el-option label="已发货" :value="1" />
            <el-option label="已入库" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="订单类型">
          <el-select v-model="searchForm.type" placeholder="全部" clearable style="width: 120px">
            <el-option label="手动发起" :value="0" />
            <el-option label="阈值触发" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

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
      <template #toolbar>
        <el-button type="primary" @click="openCreate" v-perm="'purchase:order:add'">新建采购单</el-button>
      </template>
      
      <template #url="{ row }">
        <el-image :src="row.productUrl" class="w-10 h-10 object-cover rounded" :preview-src-list="[row.productUrl]" preview-teleported />
      </template>

      <template #type="{ row }">
        <el-tag v-if="row.purchaseOrderType === 0">手动</el-tag>
        <el-tag v-else-if="row.purchaseOrderType === 1" type="warning">自动</el-tag>
        <el-tag v-else type="info">未知</el-tag>
      </template>

      <template #status="{ row }">
        <el-tag v-if="row.purchaseOrderStatus === 0">待发货</el-tag>
        <el-tag v-else-if="row.purchaseOrderStatus === 1" type="warning">已发货</el-tag>
        <el-tag v-else-if="row.purchaseOrderStatus === 2" type="success">已入库</el-tag>
      </template>

      <template #payStatus="{ row }">
        <el-tag v-if="row.amountOrderStatus === 0" type="danger">待支付</el-tag>
        <el-tag v-else-if="row.amountOrderStatus === 1" type="success">已支付</el-tag>
        <el-tag v-else-if="row.amountOrderStatus === 2" type="info">已取消</el-tag>
      </template>

      <template #actions="{ row }">
        <el-button v-if="row.amountOrderStatus === 0" link type="primary" @click="onPay(row.amountOrderId)" v-perm="'POST:/amount/order/payorder/:id'">付款</el-button>
        <el-button v-if="row.amountOrderStatus === 0" link type="warning" @click="onMockPay(row.amountOrderId)">一键支付</el-button>
        <el-button v-if="row.purchaseOrderStatus === 0" link type="primary" @click="onShip(row)" v-perm="'purchase:order:ship'">发货</el-button>
        <el-button v-if="row.purchaseOrderStatus === 1" link type="success" @click="openStockIn(row)" v-perm="'purchase:order:stock-in'">入库</el-button>
        <el-button v-if="row.purchaseOrderStatus === 2" link type="danger" @click="openReturn(row)" v-perm="'purchase:return:add'">退货</el-button>
      </template>
    </EpTable>

    <!-- Create Dialog -->
    <el-dialog v-model="createVisible" title="新建采购订单" width="500px">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="选择商品" required>
          <el-select
            v-model="createForm.productId"
            filterable
            remote
            :remote-method="searchProducts"
            placeholder="搜索商品名称"
            :loading="productLoading"
            @change="onProductSelect"
          >
            <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id">
              <span class="float-left">{{ p.name }}</span>
              <span class="float-right text-gray-400 text-xs">¥{{ p.price }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="采购数量" required>
          <el-input-number v-model="createForm.quantity" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="预估总价">
          <span class="text-red-500 font-bold">¥{{ estimatedTotal }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate" :loading="submitting">创建</el-button>
      </template>
    </el-dialog>

    <!-- Stock In Dialog -->
    <el-dialog v-model="stockInVisible" title="入库分配" width="600px">
      <div class="mb-4">
        <div class="font-bold text-lg mb-2">订单商品: {{ currentOrder?.productName }}</div>
        <div class="flex gap-4 text-sm text-gray-600">
          <span>待入库总数: {{ currentOrder?.productQuantity }}</span>
          <span :class="{'text-green-500': remainingQty === 0, 'text-red-500': remainingQty !== 0}">
            剩余分配: {{ remainingQty }}
          </span>
        </div>
      </div>
      
      <div v-for="(item, idx) in stockInItems" :key="idx" class="flex gap-2 mb-2 items-center">
        <el-select v-model="item.warehouseId" placeholder="选择仓库" style="width: 200px">
          <el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" />
        </el-select>
        <el-input-number v-model="item.quantity" :min="1" placeholder="数量" />
        <el-button type="danger" circle @click="stockInItems.splice(idx, 1)" :disabled="stockInItems.length === 1">
          <el-icon><Delete /></el-icon>
        </el-button>
      </div>
      <el-button type="primary" link @click="addStockInItem">+ 添加仓库分配</el-button>

      <template #footer>
        <el-button @click="stockInVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStockIn" :loading="submitting" :disabled="remainingQty !== 0">确认入库</el-button>
      </template>
    </el-dialog>

    <!-- Return Dialog -->
    <el-dialog v-model="returnVisible" title="采购退货" width="500px">
      <el-form :model="returnForm" label-width="100px">
        <el-form-item label="退货仓库" required>
          <el-select v-model="returnForm.warehouseId" placeholder="请选择退货仓库" style="width: 100%">
            <el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="退货数量" required>
          <el-input-number v-model="returnForm.returnQuantity" :min="1" :max="currentOrder?.productQuantity" style="width: 100%" />
        </el-form-item>
        <el-form-item label="退货原因">
          <el-input v-model="returnForm.remark" type="textarea" placeholder="请输入退货原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="returnVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReturn" :loading="submitting">确认退货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, onBeforeUnmount } from 'vue'
import EpTable from '../../components/ep/EpTable.vue'
import http from '../../services/http'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'

console.log('[PurchaseOrderPage] Component script loaded')

const rows = ref([])
const loading = ref(false)
const pagination = reactive({ current: 1, size: 10, total: 0 })
const searchForm = reactive({
  productName: '',
  status: undefined as number | undefined,
  type: undefined as number | undefined
})

const cols = [
  { prop: 'purchaseOrderId', label: 'ID', width: 80 },
  { prop: 'productUrl', label: '图片', width: 80, slot: 'url' },
  { prop: 'productName', label: '商品名称' },
  { prop: 'purchaseOrderType', label: '类型', width: 100, slot: 'type' },
  { prop: 'productPrice', label: '单价', width: 100 },
  { prop: 'productQuantity', label: '数量', width: 100 },
  { prop: 'amount', label: '总金额', width: 120 },
  { prop: 'purchaseOrderStatus', label: '物流状态', width: 100, slot: 'status' },
  { prop: 'amountOrderStatus', label: '支付状态', width: 100, slot: 'payStatus' },
  { prop: 'createTime', label: '创建时间', width: 180 }
]

async function fetch() {
  console.log('[PurchaseOrderPage] Fetching data...')
  loading.value = true
  try {
    const res = await http.get('/purchase/order/list', {
      params: {
        current: pagination.current,
        size: pagination.size,
        productName: searchForm.productName,
        status: searchForm.status,
        type: searchForm.type
      }
    })
    if (res.data) {
      rows.value = res.data.records
      pagination.total = res.data.total
      console.log('[PurchaseOrderPage] Data loaded:', rows.value.length, 'records')
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
  searchForm.status = undefined
  searchForm.type = undefined
  onSearch()
}

// Create Logic
const createVisible = ref(false)
const submitting = ref(false)
const createForm = reactive({
  productId: undefined as number | undefined,
  quantity: 1,
  price: 0
})
const products = ref<any[]>([])
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

function openCreate() {
  createForm.productId = undefined
  createForm.quantity = 1
  createForm.price = 0
  createVisible.value = true
  searchProducts(' ') // Load initial
}

function onProductSelect(id: number) {
  const p = products.value.find(x => x.id === id)
  if (p) createForm.price = p.price
}

const estimatedTotal = computed(() => {
  return (createForm.price * createForm.quantity).toFixed(2)
})

async function submitCreate() {
  if (!createForm.productId) return ElMessage.warning('请选择商品')
  submitting.value = true
  try {
    await http.post('/purchase/order', {
      productId: createForm.productId,
      quantity: createForm.quantity
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
    console.log(res,'res')
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
      // 清理 DOM
      setTimeout(() => {
        document.body.removeChild(div)
      }, 1000)
    }
  } catch (e) {
    console.error(e)
  }
}

// Mock Pay Logic
async function onMockPay(id: number) {
  if (!id) return ElMessage.error('订单异常，缺少金额单ID')
  try {
    await ElMessageBox.confirm('确定使用模拟支付直接完成订单？', '一键支付', { type: 'warning' })
    await http.post(`/amount/order/mock-pay/${id}`)
    ElMessage.success('支付成功')
    fetch()
  } catch (e) {
    console.error(e)
  }
}

// Ship Logic
async function onShip(row: any) {
    await ElMessageBox.confirm('确认已发货?', '提示', { type: 'info' })
    await http.post(`/purchase/order/ship/${row.purchaseOrderId}`)
    ElMessage.success('发货成功')
    fetch()
}

// Stock In Logic
const stockInVisible = ref(false)
const currentOrder = ref<any>(null)
const stockInItems = ref<any[]>([])
const warehouses = ref<any[]>([])

async function openStockIn(row: any) {
  currentOrder.value = row
  stockInItems.value = [{ warehouseId: undefined, quantity: row.productQuantity }]
  
  // Load warehouses if not loaded
  if (warehouses.value.length === 0) {
    const res = await http.get('/warehouse/list', { params: { size: 100 } })
    warehouses.value = res.data?.records || []
  }
  
  stockInVisible.value = true
}

function addStockInItem() {
  stockInItems.value.push({ warehouseId: undefined, quantity: 0 })
}

const remainingQty = computed(() => {
  if (!currentOrder.value) return 0
  const total = currentOrder.value.productQuantity
  const allocated = stockInItems.value.reduce((sum, item) => sum + (item.quantity || 0), 0)
  return total - allocated
})

async function submitStockIn() {
  if (remainingQty.value !== 0) return ElMessage.warning('分配数量必须等于订单总数')
  if (stockInItems.value.some(x => !x.warehouseId)) return ElMessage.warning('请选择仓库')
  
  submitting.value = true
  try {
    await http.post('/inventory/stock-in', {
      purchaseOrderId: currentOrder.value.purchaseOrderId,
      items: stockInItems.value
    })
    ElMessage.success('入库成功')
    stockInVisible.value = false
    fetch()
  } finally {
    submitting.value = false
  }
}

// Return Logic
const returnVisible = ref(false)
const returnForm = reactive({
  productId: undefined as number | undefined,
  warehouseId: undefined as number | undefined,
  returnQuantity: 1,
  remark: ''
})

async function openReturn(row: any) {
  currentOrder.value = row
  returnForm.productId = row.productId
  returnForm.returnQuantity = row.productQuantity
  returnForm.warehouseId = undefined
  returnForm.remark = ''
  
  // Load warehouses if not loaded
  if (warehouses.value.length === 0) {
    const res = await http.get('/warehouse/list', { params: { size: 100 } })
    warehouses.value = res.data?.records || []
  }
  returnVisible.value = true
}

async function submitReturn() {
  if (!returnForm.warehouseId) return ElMessage.warning('请选择退货仓库')
  submitting.value = true
  try {
    await http.post('/purchase/return/create', {
      productId: returnForm.productId,
      warehouseId: returnForm.warehouseId,
      returnQuantity: returnForm.returnQuantity
    })
    ElMessage.success('退货申请已提交')
    returnVisible.value = false
    fetch()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  console.log('[PurchaseOrderPage] Component mounted')
  fetch()
})

onBeforeUnmount(() => {
  console.log('[PurchaseOrderPage] Component unmounting')
})
</script>
