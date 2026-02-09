<template>
  <div class="space-y-4">
    <div class="flex justify-between">
      <div class="text-lg font-medium">商品管理</div>
      <el-button type="primary" @click="openCreate" v-perm="'product:add'">
        <el-icon><Plus /></el-icon><span>新建商品</span>
      </el-button>
    </div>
    <EpTable
      :rows="rows"
      :columns="cols"
      :loading="loading"
      :pagination="page"
      edit-perm="product:edit"
      remove-perm="product:delete"
      @refresh="fetch"
      @edit="openEdit"
      @remove="remove"
      @search="setKeyword"
      @update:current="onPageChange"
      @update:size="onSizeChange"
    >
      <template #url="{ row }">
        <el-image
          v-if="row.url"
          style="width: 50px; height: 50px"
          :src="row.url"
          :preview-src-list="[row.url]"
          fit="cover"
          preview-teleported
        />
        <span v-else class="text-gray-400">无图片</span>
      </template>
    </EpTable>
    <el-dialog
      v-model="visible"
      :title="form.id ? '编辑商品' : '新建商品'"
      width="520px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="商品名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="商品售价" required>
          <el-input-number v-model="form.price" :precision="2" :step="0.1" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="图片URL">
          <el-input v-model="form.url" placeholder="请输入图片链接" />
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="switchStatus"
            active-text="上架"
            inactive-text="下架"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed } from "vue";
import EpTable from "../../components/ep/EpTable.vue";
import http from "../../services/http";
import { Plus } from "@element-plus/icons-vue";
import { ElMessageBox, ElMessage } from "element-plus";

const loading = ref(false);
const rows = ref<any[]>([]);
const cols = [
  { prop: "id", label: "ID", width: 80 },
  { prop: "url", label: "图片", width: 80, slot: "url" },
  { prop: "name", label: "商品名称" },
  { prop: "price", label: "售价", width: 100 },
  { prop: "statusText", label: "状态", width: 80 },
  { prop: "description", label: "描述" },
];
const page = reactive({ total: 0, current: 1, size: 10 });
const visible = ref(false);
const form = reactive<any>({
  id: 0,
  name: "",
  price: 0,
  url: "",
  description: "",
  status: 1,
});

// 计算属性处理switch和form.status的同步
const switchStatus = computed({
  get: () => form.status === 1,
  set: (val) => {
    form.status = val ? 1 : 0;
  },
});

const keyword = ref("");

async function fetch() {
  loading.value = true;
  try {
    const res = await http.get("/product/list", {
      params: {
        current: page.current,
        size: page.size,
        name: keyword.value,
      },
    });
    rows.value = (res.data.records || []).map((it: any) => ({
      ...it,
      statusText: it.status === 1 ? "上架" : "下架",
    }));
    page.total = res.data.total;
  } finally {
    loading.value = false;
  }
}

function setKeyword(k: string) {
  keyword.value = k;
  page.current = 1;
  fetch();
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
    price: 0,
    url: "",
    description: "",
    status: 1,
  });
  visible.value = true;
}

function openEdit(row: any) {
  Object.assign(form, { ...row, status: row.status === 1 ? 1 : 0 }); // 确保status是数字
  visible.value = true;
}

async function submit() {
  if (!form.name) {
    ElMessage.warning("请输入商品名称");
    return;
  }
  if (form.price === undefined || form.price === null) {
    ElMessage.warning("请输入商品售价");
    return;
  }

  try {
    if (form.id) {
      await http.put("/product", form);
      ElMessage.success("更新成功");
    } else {
      await http.post("/product", form);
      ElMessage.success("创建成功");
    }
    visible.value = false;
    fetch();
  } catch (e) {
    // 错误已由拦截器处理
  }
}

async function remove(row: any) {
  try {
    await ElMessageBox.confirm("确定要删除该商品吗？此操作不可恢复。", "警告", {
      type: "warning",
      confirmButtonText: "确定删除",
      cancelButtonText: "取消",
    });
    await http.delete(`/product/${row.id}`);
    ElMessage.success("删除成功");
    fetch();
  } catch (e) {
    if (e !== "cancel") {
      console.error(e);
    }
  }
}
</script>

<style scoped>
</style>
