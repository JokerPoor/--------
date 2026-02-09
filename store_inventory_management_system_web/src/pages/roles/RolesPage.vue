<template>
  <div class="space-y-4">
    <div class="flex justify-between">
      <div class="text-lg font-medium">角色管理</div>
      <el-button type="primary" @click="openCreate"
        ><el-icon><Plus /></el-icon>新建角色</el-button
      >
    </div>
    <vxe-table :data="rows" :loading="loading" border stripe>
      <vxe-column field="id" title="ID" width="80" />
      <vxe-column field="roleName" title="角色名称" />
      <vxe-column field="description" title="描述" />
      <vxe-column field="updateTime" title="更新时间" />
      <vxe-column title="操作" width="240">
        <template #default="scope">
          <el-button link type="primary" @click="openEdit(scope.row)"
            ><el-icon><Edit /></el-icon>编辑</el-button
          >
          <el-button link type="primary" @click="openAssignPages(scope.row)"
            ><el-icon><Edit /></el-icon>页面分配</el-button
          >
          <el-button link type="primary" @click="openAssignPerms(scope.row)"
            ><el-icon><Key /></el-icon>权限分配</el-button
          >
          <el-button
            v-perm="'角色管理'"
            link
            type="danger"
            :disabled="scope.row.roleName === 'admin' || scope.row.id === 1"
            @click="remove(scope.row)"
            ><el-icon><Delete /></el-icon>删除</el-button
          >
        </template>
      </vxe-column>
    </vxe-table>
    <vxe-pager
      class="mt-3 flex justify-end"
      :total="page.total"
      :current-page="page.current"
      :page-size="page.size"
      @page-change="onPager"
    />
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
        <el-checkbox v-for="p in permList" :key="p.id" :label="p.id">{{
          p.name
        }}</el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="assignPermVisible = false">取消</el-button>
        <el-button type="primary" @click="assignPerms">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from "vue";
import http from "../../services/http";
import { ElIcon, ElMessageBox } from "element-plus";
import { Plus, Edit, Delete, Key } from "@element-plus/icons-vue";

const loading = ref(false);
const rows = ref<any[]>([]);
const page = reactive({ total: 0, current: 1, size: 10 });

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
  const res = await http.get("/role/list", {
    params: { current: page.current, size: page.size },
  });
  rows.value = res.data.records;
  page.total = res.data.total;
  loading.value = false;
}
function onPager(e: any) {
  page.current = e.currentPage;
  page.size = e.pageSize;
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
  assignPageIds.value = [];
  assignPageVisible.value = true;
}
async function assignPages() {
  if (!currentRoleId.value) return;
  await http.post(`/role/${currentRoleId.value}/assign-pages`, {
    pageIds: assignPageIds.value,
  });
  assignPageVisible.value = false;
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
}
fetch();
</script>

<style scoped lang="scss"></style>

