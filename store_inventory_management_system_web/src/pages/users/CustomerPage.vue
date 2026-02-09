<template>
  <div class="space-y-4">
    <div class="flex justify-between">
      <div class="text-lg font-medium">客户管理</div>
      <el-button type="primary" @click="openCreate" v-perm="'user:add'"
        ><el-icon><Plus /></el-icon><span>新建客户</span></el-button
      >
    </div>
    <EpTable
      :rows="rows"
      :columns="cols"
      :loading="loading"
      :pagination="page"
      edit-perm="user:edit"
      remove-perm="user:delete"
      reset-perm="user:reset-password"
      @refresh="fetch"
      @edit="openEdit"
      @remove="remove"
      @reset="openReset"
      @search="setKeyword"
      @update:current="onPageChange"
      @update:size="onSizeChange"
      @selection-change="onSelectionChange"
    >
      <template #toolbar>
        <el-button @click="batchStatus(1)" :disabled="selectedIds.length === 0" v-perm="'user:edit'"
          ><el-icon><Unlock /></el-icon>批量启用</el-button
        >
        <el-button @click="batchStatus(0)" :disabled="selectedIds.length === 0" v-perm="'user:edit'"
          ><el-icon><Lock /></el-icon>批量禁用</el-button
        >
      </template>
    </EpTable>
    <el-dialog
      v-model="visible"
      :title="form.id ? '编辑客户' : '新建客户'"
      width="520px"
    >
      <el-form :model="form" label-width="110px">
        <el-form-item label="登录账号" v-if="!form.id"
          ><el-input v-model="form.userAccount"
        /></el-form-item>
        <el-form-item label="登录密码" v-if="!form.id"
          ><el-input v-model="form.userPassword" type="password"
        /></el-form-item>
        <el-form-item label="邮箱"
          ><el-input v-model="form.email" :disabled="!!form.id"
        /></el-form-item>
        <el-form-item label="客户名称"
          ><el-input v-model="form.userName" placeholder="请输入客户名称"
        /></el-form-item>
        <el-form-item label="联系电话"
          ><el-input v-model="form.phone"
        /></el-form-item>
        <el-form-item label="状态"
          ><el-switch
            v-model="switchStatus"
        /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="resetVisible" title="重置密码" width="420px">
      <el-form :model="resetForm" label-width="100px">
        <el-form-item label="新密码"
          ><el-input v-model="resetForm.newPassword" type="password"
        /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReset">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import EpTable from "../../components/ep/EpTable.vue";
import http from "../../services/http";
import { ElIcon, ElMessageBox } from "element-plus";
import { Plus, Unlock, Lock } from "@element-plus/icons-vue";

const loading = ref(false);
const rows = ref<any[]>([]);
const cols = [
  { prop: "id", label: "ID", width: 80 },
  { prop: "userAccount", label: "账号" },
  { prop: "userName", label: "客户名称" },
  { prop: "phone", label: "联系电话" },
  { prop: "email", label: "邮箱" },
  { prop: "statusText", label: "状态", width: 100 },
];
const page = reactive({ total: 0, current: 1, size: 10 });
const visible = ref(false);
const form = reactive<any>({
  id: 0,
  userAccount: "",
  userPassword: "",
  email: "",
  userName: "",
  phone: "",
  status: 1,
});
const switchStatus = ref(true);
const keyword = ref("");
const selectedIds = ref<any[]>([]);
const selectedRows = ref<any[]>([]);

// 角色相关
const targetRoleName = "客户";
let targetRoleId = "";

async function fetch() {
  loading.value = true;
  const res = await http.get("/user/list", {
    params: {
      current: page.current,
      size: page.size,
      userName: keyword.value,
      userAccount: keyword.value,
      roleName: targetRoleName, // 过滤客户角色
    },
  });
  rows.value = (res.data.records || []).map((it: any) => ({
    ...it,
    statusText: it.status === 0 ? "禁用" : "启用",
  }));
  page.total = res.data.total;
  loading.value = false;
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
function onSelectionChange(rows: any[]) {
  selectedRows.value = rows;
  selectedIds.value = rows.map((r) => String(r.id));
}
async function batchStatus(status: number) {
  if (selectedIds.value.length === 0) return;
  await http.post("/user/batch-status", { ids: selectedIds.value, status });
  fetch();
}
const resetVisible = ref(false);
const resetForm = reactive<{ id: number; newPassword: string }>({
  id: 0,
  newPassword: "",
});
function openReset(row: any) {
  resetForm.id = row.id;
  resetForm.newPassword = "";
  resetVisible.value = true;
}
async function submitReset() {
  await http.post(`/user/${resetForm.id}/reset-password`, {
    newPassword: resetForm.newPassword,
  });
  resetVisible.value = false;
}
function openCreate() {
  Object.assign(form, {
    id: 0,
    userAccount: "",
    userPassword: "",
    email: "",
    userName: "",
    phone: "",
    status: 1,
  });
  switchStatus.value = true;
  visible.value = true;
}
function openEdit(row: any) {
  Object.assign(form, {
    id: row.id,
    userAccount: row.userAccount,
    email: row.email,
    userName: row.userName,
    phone: row.phone,
    status: row.status ?? 1,
  });
  switchStatus.value = row.status === 1;
  visible.value = true;
}
async function submit() {
  form.status = switchStatus.value ? 1 : 0;
  
  // 确保获取了 RoleID
  if (!targetRoleId) {
    await loadTargetRole();
  }
  
  const roleIds = targetRoleId ? [targetRoleId] : [];

  if (form.id) {
    await http.put(`/user/${form.id}`, {
      userName: form.userName,
      phone: form.phone,
      status: form.status,
      roleIds: roleIds,
    });
  } else {
    const res = await http.post("/user", {
      userAccount: form.userAccount,
      userPassword: form.userPassword,
      email: form.email,
      userName: form.userName,
      phone: form.phone,
      status: form.status,
    });
    const newId = res.data;
    if (newId && roleIds.length) {
      await http.put(`/user/${newId}`, {
        userName: form.userName,
        phone: form.phone,
        status: form.status,
        roleIds: roleIds,
      });
    }
  }
  visible.value = false;
  fetch();
}
async function remove(row: any) {
  await ElMessageBox.confirm("确定要删除该客户吗？", "提示", {
    type: "warning",
  });
  await http.delete(`/user/${row.id}`);
  fetch();
}

async function loadTargetRole() {
  const res = await http.get("/role/list", { params: { current: 1, size: 1000 } });
  const roles = res.data.records || [];
  const r = roles.find((it: any) => it.roleName === targetRoleName);
  if (r) {
    targetRoleId = String(r.id);
  } else {
    console.error(`Role ${targetRoleName} not found!`);
    ElMessageBox.alert(`系统中缺少 ${targetRoleName} 角色，请联系管理员`, "错误");
  }
}

loadTargetRole();
fetch();
</script>

<style scoped lang="scss"></style>
