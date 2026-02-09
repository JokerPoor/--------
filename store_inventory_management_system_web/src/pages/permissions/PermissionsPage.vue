<template>
  <div class="space-y-4">
    <div class="flex justify-between">
      <div class="text-lg font-medium">权限管理</div>
      <el-button type="primary" @click="openCreate"
        ><el-icon><Plus /></el-icon>新建权限</el-button
      >
    </div>
    <EpTable
      :rows="rows"
      :columns="cols"
      :loading="loading"
      :pagination="page"
      @refresh="fetch"
      @update:current="onPager"
      @update:size="onSizeChange"
    >
      <template #row-actions="{ row }">
          <el-button
            v-perm="'权限分配'"
            link
            type="primary"
            @click="openEdit(row)"
            ><el-icon><Edit /></el-icon>编辑</el-button
          >
          <el-button
            v-perm="'权限分配'"
            link
            type="danger"
            @click="remove(row)"
            ><el-icon><Delete /></el-icon>删除</el-button
          >
      </template>
    </EpTable>
    <el-dialog
      v-model="visible"
      :title="form.id ? '编辑权限' : '新建权限'"
      width="520px"
    >
      <el-form :model="form" label-width="110px">
        <el-form-item label="权限标识"
          ><el-input v-model="form.name"
        /></el-form-item>
        <el-form-item label="权限描述"
          ><el-input v-model="form.description"
        /></el-form-item>
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
import { ElIcon, ElMessageBox } from "element-plus";
import { Plus, Edit, Delete } from "@element-plus/icons-vue";

const loading = ref(false);
const rows = ref<any[]>([]);
const page = reactive({ total: 0, current: 1, size: 10 });
const visible = ref(false);
const form = reactive<any>({ id: 0, name: "", description: "" });

const cols = [
  { prop: "id", label: "ID", width: 80 },
  { prop: "name", label: "权限名称" },
  { prop: "description", label: "描述" },
  { prop: "actions", label: "操作", width: 200, slot: "row-actions" },
];

onMounted(() => {
  fetch();
});

async function fetch() {
  loading.value = true;
  const res = await http.get("/permission/list", {
    params: { current: page.current, size: page.size },
  });
  rows.value = res.data.records;
  page.total = res.data.total;
  loading.value = false;
}
function onPager(current: number) {
  page.current = current;
  fetch();
}
function onSizeChange(size: number) {
  page.size = size;
  fetch();
}
function openCreate() {
  Object.assign(form, { id: 0, name: "", description: "" });
  visible.value = true;
}
function openEdit(row: any) {
  Object.assign(form, row);
  visible.value = true;
}
async function submit() {
  if (form.id) await http.put(`/permission/${form.id}`, form);
  else await http.post("/permission", form);
  visible.value = false;
  fetch();
}
async function remove(row: any) {
  await ElMessageBox.confirm("确定要删除该权限吗？", "提示", {
    type: "warning",
  });
  await http.delete(`/permission/${row.id}`);
  fetch();
}
fetch();
</script>

<style scoped lang="scss"></style>

