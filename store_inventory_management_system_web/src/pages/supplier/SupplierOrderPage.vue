<template>
  <div class="space-y-4 p-4">
    <!-- Header -->
    <div class="flex justify-between items-center bg-white p-4 rounded-lg shadow-sm">
      <div>
        <h1 class="text-2xl font-bold text-gray-800">采购订单管理</h1>
        <p class="text-gray-500 mt-1">查看门店发来的采购订单，并进行发货操作</p>
      </div>
    </div>

    <!-- Filter -->
    <div class="flex gap-4 bg-white p-4 rounded-lg shadow-sm">
      <el-input
        v-model="keyword"
        placeholder="搜索商品名称..."
        prefix-icon="Search"
        clearable
        @clear="fetch"
        @keyup.enter="fetch"
        class="w-64"
      />
      <el-select v-model="filterStatus" placeholder="订单状态" clearable @change="fetch" class="w-40">
        <el-option label="待发货" :value="0" />
        <el-option label="已发货" :value="1" />
        <el-option label="已入库" :value="2" />
      </el-select>
      <el-button type="primary" @click="fetch">查询</el-button>
    </div>

    <!-- Order List -->
    <EpTable
      :rows="rows"
      :columns="cols"
      :loading="loading"
      :pagination="page"
      @refresh="fetch"
      @update:current="onPageChange"
      @update:size="onSizeChange"
    >
      <template #productName="{ row }">
        <div class="flex items-center gap-2">
          <el-image 
            :src="row.productUrl" 
            class="w-10 h-10 object-cover rounded" 
            :preview-src-list="[row.productUrl]" 
            preview-teleported 
          />
          <span class="font-medium">{{ row.productName }}</span>
        </div>
      </template>

      <template #status="{ row }">
        <el-tag v-if="row.purchaseOrderStatus === 0" type="danger">待发货</el-tag>
        <el-tag v-else-if="row.purchaseOrderStatus === 1" type="warning">已发货</el-tag>
        <el-tag v-else-if="row.purchaseOrderStatus === 2" type="success">已入库</el-tag>
      </template>

      <template #amountOrderStatus="{ row }">
        <el-tag v-if="row.amountOrderStatus === 0">待支付</el-tag>
        <el-tag v-else-if="row.amountOrderStatus === 1" type="success">已支付</el-tag>
        <el-tag v-else-if="row.amountOrderStatus === 2" type="info">已取消</el-tag>
      </template>

      <template #actions="{ row }">
        <el-button 
          v-if="row.purchaseOrderStatus === 0 && row.amountOrderStatus === 1" 
          type="primary" 
          link 
          v-perm="'purchase:order:ship'"
          @click="openShip(row)"
        >
          发货
        </el-button>
        <span v-else-if="row.purchaseOrderStatus === 0 && row.amountOrderStatus !== 1" class="text-gray-400 text-xs">
          待付款
        </span>
      </template>
    </EpTable>

    <!-- Ship Dialog -->
    <el-dialog
      v-model="shipVisible"
      title="确认发货"
      width="400px"
    >
      <div class="text-center py-4">
        <p class="text-gray-600">确定要对订单 <strong>{{ currentOrder?.productName }}</strong> 进行发货操作吗？</p>
        <p class="text-xs text-gray-400 mt-2">发货后门店将收到通知并进行入库操作</p>
      </div>
      <template #footer>
        <el-button @click="shipVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmShip" :loading="shipping">确认发货</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from "vue";
import EpTable from "../../components/ep/EpTable.vue";
import http from "../../services/http";
import { ElMessage } from "element-plus";
import { Search } from "@element-plus/icons-vue";

const loading = ref(false);
const rows = ref<any[]>([]);
const page = reactive({ total: 0, current: 1, size: 10 });
const keyword = ref("");
const filterStatus = ref<number | undefined>(undefined);

const cols = [
  { prop: "purchaseOrderId", label: "订单ID", width: 80 },
  { prop: "productName", label: "商品信息", minWidth: 200, slot: "productName" },
  { prop: "productPrice", label: "单价", width: 100 },
  { prop: "productQuantity", label: "数量", width: 80 },
  { prop: "totalAmount", label: "总金额", width: 120 },
  { prop: "amountOrderStatus", label: "支付状态", width: 100, slot: "amountOrderStatus" },
  { prop: "status", label: "发货状态", width: 100, slot: "status" },
  { prop: "createTime", label: "下单时间", width: 180 },
];

// Ship
const shipVisible = ref(false);
const currentOrder = ref<any>(null);
const shipping = ref(false);

async function fetch() {
  loading.value = true;
  try {
    const res = await http.get("/purchase/order/supplier/list", {
      params: {
        current: page.current,
        size: page.size,
        productName: keyword.value,
        status: filterStatus.value,
      },
    });
    rows.value = res.data.records || [];
    page.total = res.data.total;
  } finally {
    loading.value = false;
  }
}

function onPageChange(p: number) {
  page.current = p;
  fetch();
}

function onSizeChange(s: number) {
  page.size = s;
  page.current = 1;
  fetch();
}

function openShip(row: any) {
  currentOrder.value = row;
  shipVisible.value = true;
}

async function confirmShip() {
  if (!currentOrder.value) return;
  shipping.value = true;
  try {
    await http.post(`/purchase/order/ship/${currentOrder.value.purchaseOrderId}`);
    ElMessage.success("发货成功");
    shipVisible.value = false;
    fetch();
  } finally {
    shipping.value = false;
  }
}

onMounted(() => {
  fetch();
});
</script>
