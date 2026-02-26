<template>
  <div class="space-y-6 p-4">
    <!-- Header -->
    <div
      class="flex justify-between items-center bg-white p-4 rounded-lg shadow-sm"
    >
      <div>
        <h1 class="text-2xl font-bold text-gray-800">我的商品库</h1>
        <p class="text-gray-500 mt-1">
          管理您提供给门店的商品，上架新商品或下架旧商品
        </p>
      </div>
      <el-button type="primary" @click="openCreate" v-perm="'product:add'">
        <el-icon class="mr-2"><Plus /></el-icon>发布新商品
      </el-button>
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
      <el-select
        v-model="filterStatus"
        placeholder="商品状态"
        clearable
        @change="fetch"
        class="w-40"
      >
        <el-option label="上架中" :value="1" />
        <el-option label="已下架" :value="0" />
      </el-select>
      <el-button @click="fetch">查询</el-button>
    </div>

    <!-- Product Grid -->
    <div
      v-loading="loading"
      class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6"
    >
      <div
        v-for="item in products"
        :key="item.id"
        class="bg-white rounded-xl shadow-sm hover:shadow-md transition-shadow duration-300 overflow-hidden border border-gray-100 flex flex-col"
      >
        <!-- Image Area -->
        <div class="relative h-48 bg-gray-50 group">
          <img
            :src="
              item.url || 'https://via.placeholder.com/300x200?text=No+Image'
            "
            class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
            alt="Product"
          />
          <div class="absolute top-2 right-2">
            <el-tag
              :type="item.status === 1 ? 'success' : 'info'"
              effect="dark"
            >
              {{ item.status === 1 ? "上架中" : "已下架" }}
            </el-tag>
          </div>
          <div
            class="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-20 transition-all duration-300 flex items-center justify-center opacity-0 group-hover:opacity-100"
          >
            <div class="flex gap-2">
              <el-button
                circle
                type="primary"
                icon="Edit"
                @click="openEdit(item)"
                v-perm="'product:update'"
              />
              <el-button
                circle
                type="danger"
                icon="Delete"
                @click="remove(item)"
                v-perm="'product:delete'"
              />
            </div>
          </div>
        </div>

        <!-- Content Area -->
        <div class="p-4 flex-1 flex flex-col">
          <h3
            class="text-lg font-semibold text-gray-800 mb-2 truncate"
            :title="item.name"
          >
            {{ item.name }}
          </h3>
          <p class="text-gray-500 text-sm mb-4 line-clamp-2 h-10">
            {{ item.description || "暂无描述" }}
          </p>

          <div
            class="mt-auto flex justify-between items-center pt-4 border-t border-gray-100"
          >
            <span class="text-2xl font-bold text-red-500"
              >¥{{ item.price }}</span
            >
            <span class="text-xs text-gray-400">ID: {{ item.id }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div
      v-if="!loading && products.length === 0"
      class="flex flex-col items-center justify-center py-20 text-gray-400"
    >
      <el-icon :size="64" class="mb-4"><Box /></el-icon>
      <p>暂无商品数据</p>
      <el-button type="primary" link class="mt-2" @click="openCreate"
        >去发布第一个商品</el-button
      >
    </div>

    <!-- Pagination -->
    <div class="flex justify-end mt-4 bg-white p-4 rounded-lg shadow-sm">
      <el-pagination
        v-model:current-page="page.current"
        v-model:page-size="page.size"
        :total="page.total"
        :page-sizes="[8, 12, 24, 48]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="onSizeChange"
        @current-change="onPageChange"
      />
    </div>

    <!-- Edit/Create Dialog -->
    <el-dialog
      v-model="visible"
      :title="form.id ? '编辑商品' : '发布新商品'"
      width="500px"
      destroy-on-close
      class="rounded-xl"
    >
      <el-form :model="form" label-position="top" class="mt-2">
        <el-form-item label="商品名称" required>
          <el-input v-model="form.name" placeholder="请输入商品名称" />
        </el-form-item>

        <el-form-item label="商品售价" required>
          <el-input-number
            v-model="form.price"
            :precision="2"
            :step="0.1"
            :min="0.01"
            class="w-full"
            controls-position="right"
            :disabled="!!form.id"
            :title="form.id ? '商品发布后不可修改价格' : ''"
          />
          <div v-if="form.id" class="text-xs text-red-400 mt-1">
            商品发布后，不可修改价格
          </div>
        </el-form-item>

        <el-form-item label="商品图片URL">
          <el-input v-model="form.url" placeholder="https://...">
            <template #append>
              <el-button icon="Picture" />
            </template>
          </el-input>
          <div
            v-if="form.url"
            class="mt-2 h-32 w-full rounded-lg bg-gray-50 overflow-hidden border border-gray-200"
          >
            <img :src="form.url" class="h-full w-full object-contain" />
          </div>
        </el-form-item>

        <el-form-item label="商品描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入商品详细描述..."
          />
        </el-form-item>

        <el-form-item label="上架状态">
          <el-switch
            v-model="switchStatus"
            active-text="立即上架"
            inactive-text="暂不上架"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="visible = false">取消</el-button>
          <el-button type="primary" @click="submit" :loading="submitting"
            >确认发布</el-button
          >
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from "vue";
import http from "../../services/http";
import { ElMessageBox, ElMessage } from "element-plus";
import {
  Plus,
  Edit,
  Delete,
  Search,
  Box,
  Picture,
} from "@element-plus/icons-vue";

// Data
const loading = ref(false);
const submitting = ref(false);
const products = ref<any[]>([]);
const page = reactive({ total: 0, current: 1, size: 12 });
const keyword = ref("");
const filterStatus = ref<number | undefined>(undefined);

// Form
const visible = ref(false);
const switchStatus = ref(true);
const form = reactive({
  id: 0,
  name: "",
  description: "",
  url: "",
  price: 0,
  status: 1,
});

// Methods
async function fetch() {
  loading.value = true;
  try {
    const res = await http.get("/product/list/own", {
      params: {
        current: page.current,
        size: page.size,
        name: keyword.value,
        status: filterStatus.value,
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

function onSizeChange(s: number) {
  page.size = s;
  page.current = 1;
  fetch();
}

function openCreate() {
  Object.assign(form, {
    id: 0,
    name: "",
    description: "",
    url: "",
    price: 0,
    status: 1,
  });
  switchStatus.value = true;
  visible.value = true;
}

function openEdit(item: any) {
  Object.assign(form, item);
  switchStatus.value = item.status === 1;
  visible.value = true;
}

async function submit() {
  if (!form.name || form.price <= 0) {
    ElMessage.warning("请完善商品名称和价格信息");
    return;
  }

  submitting.value = true;
  try {
    form.status = switchStatus.value ? 1 : 0;
    if (form.id) {
      await http.put("/product", form);
      ElMessage.success("商品更新成功");
    } else {
      await http.post("/product", form);
      ElMessage.success("商品发布成功");
    }
    visible.value = false;
    fetch();
  } finally {
    submitting.value = false;
  }
}

async function remove(item: any) {
  try {
    await ElMessageBox.confirm(`确定要删除商品 "${item.name}" 吗？`, "警告", {
      type: "warning",
      confirmButtonText: "确定删除",
      cancelButtonText: "取消",
    });
    await http.delete(`/product/${item.id}`);
    ElMessage.success("删除成功");
    fetch();
  } catch (e) {
    // Cancelled
  }
}

onMounted(() => {
  fetch();
});
</script>
