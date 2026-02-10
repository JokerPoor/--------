<template>
  <div class="space-y-6 p-4">
    <!-- Header -->
    <div class="flex justify-between items-center bg-white p-4 rounded-lg shadow-sm">
      <div>
        <h1 class="text-2xl font-bold text-gray-800">在线商城</h1>
        <p class="text-gray-500 mt-1">浏览门店精选商品，随时随地下单采购</p>
      </div>
    </div>

    <!-- Filter -->
    <div class="flex gap-4 bg-white p-4 rounded-lg shadow-sm sticky top-0 z-10">
      <el-input
        v-model="keyword"
        placeholder="搜索商品..."
        prefix-icon="Search"
        clearable
        @clear="fetch"
        @keyup.enter="fetch"
        class="w-full sm:w-80"
      />
      <el-button type="primary" @click="fetch">搜索</el-button>
    </div>

    <!-- Product Grid -->
    <div v-loading="loading" class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6">
      <div
        v-for="item in products"
        :key="item.id"
        class="bg-white rounded-xl shadow-sm hover:shadow-xl transition-all duration-300 overflow-hidden flex flex-col group"
      >
        <!-- Image -->
        <div class="relative h-56 bg-gray-100 overflow-hidden">
          <img
            :src="item.productUrl || 'https://via.placeholder.com/300x300?text=No+Image'"
            class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
            alt="Product"
          />
          <div class="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/60 to-transparent p-4 translate-y-full group-hover:translate-y-0 transition-transform duration-300">
             <p class="text-white text-sm truncate">{{ item.storeName || '未知门店' }}</p>
          </div>
        </div>

        <!-- Content -->
        <div class="p-4 flex-1 flex flex-col">
          <h3 class="text-lg font-bold text-gray-800 mb-1 truncate" :title="item.productName">{{ item.productName }}</h3>
          <p class="text-gray-500 text-xs mb-3 line-clamp-2 h-8">{{ item.productDescription || '暂无描述' }}</p>
          
          <div class="mt-auto">
             <div class="flex justify-between items-baseline mb-3">
               <span class="text-2xl font-bold text-red-600">¥{{ item.productPrice }}</span>
               <span class="text-xs text-gray-400">库存: {{ item.quantity }}</span>
             </div>
             <div class="flex gap-2">
                 <el-button class="flex-1" @click="openDetail(item)">详情</el-button>
                 <el-button type="primary" class="flex-1" @click="openBuy(item)" :disabled="item.quantity <= 0">
                    {{ item.quantity > 0 ? '购买' : '缺货' }}
                 </el-button>
             </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-if="!loading && products.length === 0" class="flex flex-col items-center justify-center py-20 text-gray-400">
      <el-icon :size="64" class="mb-4"><Goods /></el-icon>
      <p>暂无在售商品</p>
    </div>

    <!-- Pagination -->
    <div class="flex justify-center mt-8">
      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :total="page.total"
        :page-sizes="[10, 20, 40]"
        layout="prev, pager, next"
        background
        @current-change="onPageChange"
      />
    </div>

    <!-- Detail Dialog -->
    <el-dialog
      v-model="detailVisible"
      title="商品详情"
      width="600px"
      destroy-on-close
      class="rounded-xl"
    >
      <div v-if="detailProduct" class="flex flex-col md:flex-row gap-6">
         <div class="w-full md:w-1/2">
             <img :src="detailProduct.productUrl || 'https://via.placeholder.com/300x300'" class="w-full h-64 object-cover rounded-lg shadow-sm" />
         </div>
         <div class="w-full md:w-1/2 flex flex-col">
             <h2 class="text-xl font-bold text-gray-800 mb-2">{{ detailProduct.productName }}</h2>
             <div class="flex items-center gap-2 mb-4">
                 <span class="text-2xl font-bold text-red-600">¥{{ detailProduct.productPrice }}</span>
                 <el-tag :type="detailProduct.quantity > 0 ? 'success' : 'danger'" size="small">
                     {{ detailProduct.quantity > 0 ? '库存充足' : '暂时缺货' }}
                 </el-tag>
             </div>
             
             <div class="flex-1 overflow-y-auto max-h-48 mb-4">
                 <p class="text-gray-600 text-sm leading-relaxed whitespace-pre-wrap">{{ detailProduct.productDescription || '暂无详细描述' }}</p>
             </div>
             
             <div class="mt-auto pt-4 border-t border-gray-100">
                 <p class="text-xs text-gray-400 mb-2">店铺: {{ detailProduct.storeName || '未知门店' }}</p>
                 <el-button type="primary" class="w-full" @click="openBuyFromDetail" :disabled="detailProduct.quantity <= 0">
                    立即购买
                 </el-button>
             </div>
         </div>
      </div>
    </el-dialog>

    <!-- Buy Dialog -->
    <el-dialog
      v-model="buyVisible"
      title="确认购买"
      width="400px"
      destroy-on-close
      class="rounded-xl"
    >
      <div v-if="selectedProduct" class="text-center">
         <img :src="selectedProduct.productUrl" class="w-32 h-32 object-cover rounded-lg mx-auto mb-4" />
         <h3 class="font-bold text-lg">{{ selectedProduct.productName }}</h3>
         <p class="text-red-500 text-xl font-bold my-2">¥{{ selectedProduct.productPrice }}</p>
         
         <div class="flex items-center justify-center gap-4 mt-6">
            <span class="text-gray-600">购买数量:</span>
            <el-input-number v-model="buyQuantity" :min="1" :max="selectedProduct.quantity" />
         </div>
         <p class="text-gray-400 text-xs mt-2">剩余库存: {{ selectedProduct.quantity }}</p>
         
         <div class="bg-gray-50 p-3 rounded-lg mt-6 flex justify-between items-center">
            <span>总计金额:</span>
            <span class="text-xl font-bold text-red-600">¥{{ (selectedProduct.productPrice * buyQuantity).toFixed(2) }}</span>
         </div>
      </div>
      <template #footer>
        <el-button @click="buyVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBuy" :loading="buying">提交订单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from "vue";
import http from "../../services/http";
import { ElMessage, ElMessageBox } from "element-plus";
import { Search, ShoppingCart, Goods } from "@element-plus/icons-vue";
import { useRouter } from "vue-router";

const router = useRouter();
const loading = ref(false);
const products = ref<any[]>([]);
const page = reactive({ total: 0, current: 1, size: 20 });
const keyword = ref("");
const cartCount = ref(0); // Mock count for now

// Buy
const buyVisible = ref(false);
const selectedProduct = ref<any>(null);
const buyQuantity = ref(1);
const buying = ref(false);

// Detail Logic
const detailVisible = ref(false);
const detailProduct = ref<any>(null);

async function fetch() {
  loading.value = true;
  try {
    // 使用 inventory/list 接口查询所有门店的库存商品
    // 注意：需要后端支持查询所有上架库存
    const res = await http.get("/inventory/list", {
      params: {
        current: page.current,
        size: page.size,
        productName: keyword.value,
        // 这里假设只展示 quantity > 0 的? 或者全部展示
      },
    });
    products.value = res.data.records || [];
    page.total = res.data.total;
  } finally {
    loading.value = false;
  }
}

function onPageChange(p: number) {
  page.current = p;
  fetch();
}

function openDetail(item: any) {
  detailProduct.value = item;
  detailVisible.value = true;
}

function openBuyFromDetail() {
  if (detailProduct.value) {
    openBuy(detailProduct.value);
    detailVisible.value = false;
  }
}

function openBuy(item: any) {
  selectedProduct.value = item;
  buyQuantity.value = 1;
  buyVisible.value = true;
}

async function confirmBuy() {
  if (!selectedProduct.value) return;
  
  buying.value = true;
  try {
    // Call Create Sale Order API
    // POST /sale/order/create
    const payload = {
      inventoryId: selectedProduct.value.id, // 库存ID
      productId: selectedProduct.value.productId, // 商品ID
      buyQuantity: buyQuantity.value
    };
    
    await http.post("/sale/order/create", payload);
    
    ElMessage.success("订单提交成功！");
    buyVisible.value = false;
    fetch(); // Refresh stock
    // router.push("/sale/order/my"); // Optional: redirect to orders
  } catch (e) {
    // Error handled by interceptor usually
  } finally {
    buying.value = false;
  }
}

onMounted(() => {
  fetch();
});
</script>
