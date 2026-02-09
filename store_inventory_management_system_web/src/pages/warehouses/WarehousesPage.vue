<template>
  <div class="space-y-4">
    <div class="flex justify-between">
      <div class="text-lg font-medium">仓库管理</div>
      <el-button type="primary" @click="openCreate" v-perm="'warehouse:add'">
        <el-icon><Plus /></el-icon><span>新建仓库</span>
      </el-button>
    </div>
    <EpTable
      :rows="rows"
      :columns="cols"
      :loading="loading"
      :pagination="page"
      edit-perm="warehouse:edit"
      remove-perm="warehouse:delete"
      @refresh="fetch"
      @edit="openEdit"
      @remove="remove"
      @search="setKeyword"
      @update:current="onPageChange"
      @update:size="onSizeChange"
    >
    </EpTable>
    <el-dialog
      v-model="visible"
      :title="form.id ? '编辑仓库' : '新建仓库'"
      width="520px"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="仓库名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="仓库地址" required>
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="仓库描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
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
import { reactive, ref, onMounted } from "vue";
import EpTable from "../../components/ep/EpTable.vue";
import http from "../../services/http";
import { Plus } from "@element-plus/icons-vue";
import { ElMessageBox, ElMessage } from "element-plus";

const loading = ref(false);
const rows = ref<any[]>([]);
const cols = [
  { prop: "id", label: "ID", width: 80 },
  { prop: "name", label: "仓库名称" },
  { prop: "address", label: "地址" },
  { prop: "description", label: "描述" },
];
const page = reactive({ total: 0, current: 1, size: 10 });
const visible = ref(false);
const form = reactive<any>({
  id: 0,
  name: "",
  address: "",
  description: "",
});

const keyword = ref("");

async function fetch() {
  loading.value = true;
  try {
    const res = await http.get("/warehouse/page", {
      params: {
        current: page.current,
        size: page.size,
        name: keyword.value,
      },
    });
    rows.value = res.data.records || [];
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
    address: "",
    description: "",
  });
  visible.value = true;
}

function openEdit(row: any) {
  Object.assign(form, { ...row });
  visible.value = true;
}

async function submit() {
  if (!form.name) {
    ElMessage.warning("请输入仓库名称");
    return;
  }
  if (!form.address) {
    ElMessage.warning("请输入仓库地址");
    return;
  }

  try {
    if (form.id) {
      await http.put("/warehouse/update", form);
      ElMessage.success("更新成功");
    } else {
      await http.post("/warehouse/add", form);
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
    await ElMessageBox.confirm("确定要删除该仓库吗？此操作不可恢复。", "警告", {
      type: "warning",
      confirmButtonText: "确定删除",
      cancelButtonText: "取消",
    });
    await http.delete(`/warehouse/delete/${row.id}`);
    ElMessage.success("删除成功");
    fetch();
  } catch (e) {
    if (e !== "cancel") {
      console.error(e);
    }
  }
}

onMounted(() => {
  fetch();
});
</script>

<style scoped>
</style>
