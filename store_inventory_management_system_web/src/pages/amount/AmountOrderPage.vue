<template>
  <div class="space-y-4 p-4">
    <!-- Header -->
    <div class="flex justify-between items-center bg-white p-4 rounded-lg shadow-sm">
      <div>
        <h1 class="text-2xl font-bold text-gray-800">金额订单管理</h1>
        <p class="text-gray-500 mt-1">查看和管理所有资金往来记录</p>
      </div>
      <el-button type="primary" @click="fetch"><el-icon><Refresh /></el-icon> 刷新</el-button>
    </div>

    <!-- Filter -->
    <div class="bg-white p-4 rounded-lg shadow-sm">
      <el-form :inline="true" :model="searchForm" class="demo-form-inline">
        <el-form-item label="订单类型">
          <el-select v-model="searchForm.type" placeholder="全部" clearable style="width: 140px">
            <el-option label="采购" :value="0" />
            <el-option label="采退" :value="1" />
            <el-option label="销售" :value="2" />
            <el-option label="销退" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待支付" :value="0" />
            <el-option label="已支付" :value="1" />
            <el-option label="已取消" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- Order List -->
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
      <template #type="{ row }">
        <el-tag v-if="row.type === 0" type="primary">采购</el-tag>
        <el-tag v-else-if="row.type === 1" type="warning">采退</el-tag>
        <el-tag v-else-if="row.type === 2" type="success">销售</el-tag>
        <el-tag v-else-if="row.type === 3" type="danger">销退</el-tag>
        <el-tag v-else type="info">未知</el-tag>
      </template>

      <template #status="{ row }">
        <el-tag v-if="row.status === 0" type="danger">待支付</el-tag>
        <el-tag v-else-if="row.status === 1" type="success">已支付</el-tag>
        <el-tag v-else-if="row.status === 2" type="info">已取消</el-tag>
      </template>
      
      <template #amount="{ row }">
        <span class="font-bold">¥{{ row.amount }}</span>
      </template>

      <template #actions="{ row }">
        <el-button link type="primary" @click="openDetail(row)">详情</el-button>
        <el-button v-if="row.status === 0 && canPay(row)" link type="success" @click="onPay(row.id)">支付</el-button>
        <el-button v-if="row.status === 0 && canPay(row)" link type="warning" @click="onMockPay(row.id)">一键支付</el-button>
      </template>
    </EpTable>

    <!-- Detail Dialog -->
    <el-dialog v-model="detailVisible" title="订单详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ detail?.orderId }}</el-descriptions-item>
        <el-descriptions-item label="金额单ID">{{ detail?.amountOrderId }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag v-if="detail?.type === 0">采购</el-tag>
          <el-tag v-else-if="detail?.type === 1" type="warning">采退</el-tag>
          <el-tag v-else-if="detail?.type === 2" type="success">销售</el-tag>
          <el-tag v-else-if="detail?.type === 3" type="danger">销退</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="金额">¥{{ detail?.amount }}</el-descriptions-item>
        <el-descriptions-item label="状态">
            <el-tag v-if="detail?.amountOrderStatus === 0" type="danger">待支付</el-tag>
            <el-tag v-else-if="detail?.amountOrderStatus === 1" type="success">已支付</el-tag>
            <el-tag v-else-if="detail?.amountOrderStatus === 2" type="info">已取消</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">{{ detail?.payType === 'alipay' ? '支付宝' : detail?.payType }}</el-descriptions-item>
        <el-descriptions-item label="付款方ID">{{ detail?.payerId }}</el-descriptions-item>
        <el-descriptions-item label="收款方ID">{{ detail?.storeId }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detail?.createTime }}</el-descriptions-item>
        <el-descriptions-item label="流水号">{{ detail?.tradeNo || '-' }}</el-descriptions-item>
      </el-descriptions>
      
      <div v-if="detail?.productName" class="mt-4">
        <h3 class="font-bold mb-2">关联商品信息</h3>
        <div class="flex gap-4 items-center bg-gray-50 p-2 rounded">
             <el-image :src="detail.productUrl" class="w-16 h-16 rounded object-cover" />
             <div>
                 <div class="font-bold">{{ detail.productName }}</div>
                 <div class="text-sm text-gray-500">{{ detail.productDescription }}</div>
                 <div class="text-sm">单价: ¥{{ detail.productPrice }} x {{ detail.productQuantity }}</div>
             </div>
        </div>
      </div>
    </el-dialog>
    
    <!-- Pay Form (Hidden) -->
    <div v-html="payForm" class="hidden"></div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import EpTable from '../../components/ep/EpTable.vue'
import http from '../../services/http'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import auth from '../../services/auth'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const pagination = reactive({ current: 1, size: 10, total: 0 })
const rows = ref([])
const searchForm = reactive({
  type: undefined as number | undefined,
  status: undefined as number | undefined
})
const currentUser = auth.getUser()

const cols = [
  { prop: 'id', label: 'ID', width: 180 },
  { prop: 'orderId', label: '业务单号', width: 180 },
  { prop: 'type', label: '类型', width: 100, slot: 'type' },
  { prop: 'amount', label: '金额', width: 120, slot: 'amount' },
  { prop: 'payerId', label: '付款方ID', width: 180 },
  { prop: 'payeeId', label: '收款方ID', width: 180 },
  { prop: 'status', label: '状态', width: 100, slot: 'status' },
  { prop: 'createTime', label: '创建时间', width: 180 },
  { prop: 'actions', label: '操作', width: 150, slot: 'actions', fixed: 'right' }
]

async function fetch() {
  loading.value = true
  try {
    const res = await http.get('/amount/order/list', {
      params: {
        current: pagination.current,
        size: pagination.size,
        type: searchForm.type,
        status: searchForm.status
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
  searchForm.type = undefined
  searchForm.status = undefined
  onSearch()
}

// Permission check helper
function canPay(row: any) {
    // Basic check: if status is unpaid (0)
    if (row.status !== 0) return false
    
    // 1. If I am the payer, I can pay
    if (currentUser && String(row.payerId) === String(currentUser.id)) return true
    
    // 2. If I am Store Manager, I can pay orders where payer is the System Manager
    // We check if current user has "门店管理员" role
    // Note: currentUser structure depends on auth.ts. Assuming it has roles or we check implicit permission.
    // If we can't check roles easily here, we might need to rely on the button being visible and backend handling the check,
    // OR we allow the button if type is Sale Return (3) or Purchase Order (0) AND we suspect we are manager.
    
    // Let's check roles if available.
    const roles = (currentUser as any)?.roles || [];
    const isManager = roles.some((r: any) => r.roleName === '门店管理员' || r.roleName === '超级管理员');
    
    if (isManager) {
        // Manager can pay Purchase Orders (0) and Sale Return Orders (3)
        // Usually these have payerId = System Manager ID, which differs from current user ID
        if (row.type === 0 || row.type === 3) {
            return true;
        }
    }

    return false
}

// Detail Logic
const detailVisible = ref(false)
const detail = ref<any>(null)

async function openDetail(row: any) {
    try {
        const res = await http.get(`/amount/order/${row.id}`)
        if (res.data) {
            detail.value = res.data
            // Map type manually if needed for display, but switch logic in template handles it
            detail.value.type = row.type // Ensure type is passed or use backend value
            detailVisible.value = true
        }
    } catch (e) {
        // Error handled by interceptor
    }
}

// Payment Logic
const payForm = ref('')
async function onPay(id: number) {
    try {
        // Direct post to get HTML form
        // Note: The backend returns HTML string directly to response stream, 
        // but our http client expects JSON usually. 
        // We might need to handle this differently or open in new tab.
        // Actually the backend code writes to response: response.getWriter().write(form);
        // This is not friendly for SPA AJAX.
        // Better way: Open a new window with the URL
        
        // Construct URL
        const token = localStorage.getItem('token')
        // We need to pass token if auth is required? Backend gets user from request.
        // If we open in new tab, we might miss the header.
        // If backend relies on 'Authorization' header, window.open won't work easily unless we use cookies or query param.
        
        // Alternative: Fetch the HTML blob/text and render it?
        // Let's try to request it as text.
        
        // Wait, the backend method:
        // void payOrder(Long id, HttpServletRequest request, HttpServletResponse response)
        // It writes to response.
        
        // Let's try submitting a form or using a temporary standard submit?
        // Or just use our http client to get the HTML string.
        const res = await http.post(`/amount/order/payorder/${id}`, {}, {
            responseType: 'text' 
        })
        
        // The backend returns the Alipay form. We can append it to document and submit it.
        const div = document.createElement('div')
        // Note: http interceptor returns response.data directly. 
        // If responseType is text, res is the HTML string.
        div.innerHTML = (res as any) 
        document.body.appendChild(div)
        const form = div.querySelector('form')
        if (form) {
            form.submit()
        }
    } catch (e) {
        console.error(e)
        ElMessage.error('支付发起失败')
    }
}

// Mock Pay Logic
async function onMockPay(id: number) {
    if (!id) return ElMessage.error('订单异常，缺少金额单ID')
    try {
        await http.post(`/amount/order/mock-pay/${id}`)
        ElMessage.success('支付成功')
        fetch()
    } catch (e) {
        console.error(e)
        // Error already handled by interceptor usually
    }
}

// Sync Status Logic (Triggered on return from Alipay)
async function checkAlipayReturn() {
    const { out_trade_no } = route.query
    if (out_trade_no) {
        try {
            loading.value = true
            const res = await http.post(`/amount/order/sync/${out_trade_no}`)
            if (res.data) {
                ElMessage.success('支付确认成功')
            } else {
                // ElMessage.warning('支付状态未更新，请稍后刷新')
            }
            // Clear query params to clean up URL
            router.replace({ query: {} })
        } catch (e) {
            console.error(e)
        } finally {
            // Always fetch latest data
            fetch()
        }
    } else {
        fetch()
    }
}

onMounted(() => {
  checkAlipayReturn()
})
</script>

<style scoped>
</style>