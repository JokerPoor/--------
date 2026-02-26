<template>
  <div class="space-y-4">
    <div class="flex justify-between">
      <div class="text-lg font-medium">角色管理</div>
      <el-button type="primary" @click="openCreate" v-perm="'role:add'"
        ><el-icon><Plus /></el-icon>新建角色</el-button
      >
    </div>
    <EpTable
      :rows="rows"
      :columns="cols"
      :loading="loading"
      :pagination="page"
      @refresh="fetch"
      @update:current="onPageChange"
      @update:size="onSizeChange"
    >
      <template #actions="{ row }">
        <el-button link type="primary" @click="openEdit(row)" v-perm="'role:edit'"
          ><el-icon><Edit /></el-icon>编辑</el-button
        >
        <el-button link type="primary" @click="openAssignPages(row)" v-perm="'role:edit'"
          ><el-icon><Edit /></el-icon>页面分配</el-button
        >
        <el-button link type="primary" @click="openAssignPerms(row)" v-perm="'权限分配'"
          ><el-icon><Key /></el-icon>权限分配</el-button
        >
        <el-button
          v-perm="'role:delete'"
          link
          type="danger"
          :disabled="['超级管理员', '门店管理员', '供应商', '客户', 'admin'].includes(row.roleName) || row.id === 1"
          @click="remove(row)"
          ><el-icon><Delete /></el-icon>删除</el-button
        >
      </template>
    </EpTable>

    <el-dialog
      v-model="visible"
      :title="form.id ? '编辑角色' : '新建角色'"
      width="520px"
    >
      <el-form :model="form" label-width="110px">
        <el-form-item label="角色名称"
          ><el-input v-model="form.roleName"
        /></el-form-item>
        <el-form-item label="角色描述"
          ><el-input v-model="form.description"
        /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="assignPageVisible" title="给角色分配页面" width="520px">
      <el-checkbox-group v-model="assignPageIds">
        <el-checkbox v-for="p in pageList" :key="p.id" :label="p.id">{{
          p.name
        }}</el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="assignPageVisible = false">取消</el-button>
        <el-button type="primary" @click="assignPages">确定</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="assignPermVisible" title="给角色分配权限" width="520px">
      <el-checkbox-group v-model="assignPermIds">
        <el-checkbox v-for="p in permList" :key="p.id" :label="p.id" style="width: 100%; margin-right: 0; margin-bottom: 8px;">
          <span :title="p.name">{{ p.description || p.name }}</span>
          <span class="text-gray-400 text-xs ml-2">({{ p.name }})</span>
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="assignPermVisible = false">取消</el-button>
        <el-button type="primary" @click="assignPerms">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from "vue";
import EpTable from "../../components/ep/EpTable.vue";
import http from "../../services/http";
import { ElIcon, ElMessageBox } from "element-plus";
import { Plus, Edit, Delete, Key } from "@element-plus/icons-vue";

const loading = ref(false);
const rows = ref<any[]>([]);
const page = reactive({ total: 0, current: 1, size: 10 });

const cols = [
  { prop: "id", label: "ID", width: 80 },
  { prop: "roleName", label: "角色名称" },
  { prop: "description", label: "描述" },
  { prop: "updateTime", label: "更新时间" },
];

const visible = ref(false);
const form = reactive<any>({ id: 0, roleName: "", description: "" });
const assignPageVisible = ref(false);
const assignPageIds = ref<number[]>([]);
const currentRoleId = ref<number | null>(null);
const pageList = ref<any[]>([]);
const permList = ref<any[]>([]);
const assignPermVisible = ref(false);
const assignPermIds = ref<number[]>([]);

async function fetch() {
  loading.value = true;
  try {
    const res = await http.get("/role/list", {
      params: { current: page.current, size: page.size },
    });
    rows.value = res.data.records;
    page.total = res.data.total;
  } finally {
    loading.value = false;
  }
}

function onPageChange(current: number) {
  page.current = current;
  fetch();
}

function onSizeChange(size: number) {
  page.size = size;
  page.current = 1;
  fetch();
}

function openCreate() {
  Object.assign(form, { id: 0, roleName: "", description: "" });
  visible.value = true;
}
function openEdit(row: any) {
  Object.assign(form, row);
  visible.value = true;
}
async function submit() {
  if (form.id) await http.put(`/role/${form.id}`, form);
  else await http.post("/role", form);
  visible.value = false;
  fetch();
}
async function remove(row: any) {
  await ElMessageBox.confirm("确定要删除该角色吗？", "提示", {
    type: "warning",
  });
  await http.delete(`/role/${row.id}`);
  fetch();
}
async function openAssignPages(row: any) {
  currentRoleId.value = row.id;
  const resPages = await http.get("/page/list", {
    params: { current: 1, size: 200 },
  });
  pageList.value = resPages.data.records;
  // 获取角色详情以回显已分配页面
  const resRole = await http.get(`/role/${row.id}`);
  assignPageIds.value = (resRole.data.pages || []).map((p: any) => p.id);
  assignPageVisible.value = true;
}
async function assignPages() {
  if (!currentRoleId.value) return;
  await http.post(`/role/${currentRoleId.value}/assign-pages`, {
    pageIds: assignPageIds.value,
  });
  assignPageVisible.value = false;
  fetch();
}
async function openAssignPerms(row: any) {
  currentRoleId.value = row.id;
  const resPerms = await http.get("/permission/list", {
    params: { current: 1, size: 200 },
  });
  permList.value = resPerms.data.records;
  assignPermIds.value = (row.permissions || []).map((p: any) => p.id);
  assignPermVisible.value = true;
}
async function assignPerms() {
  if (!currentRoleId.value) return;
  await http.put(`/role/${currentRoleId.value}/permission`, {
    permissionIds: assignPermIds.value,
  });
  assignPermVisible.value = false;
  fetch();
}

onMounted(() => {
  fetch();
});
</script>

<style scoped lang="scss"></style>

