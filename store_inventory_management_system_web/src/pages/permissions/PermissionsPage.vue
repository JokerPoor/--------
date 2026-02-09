<template>
  <div class="space-y-4">
    <div class="flex justify-between">
      <div class="text-lg font-medium">权限管理</div>
      <el-button type="primary" @click="openCreate"
        ><el-icon><Plus /></el-icon>新建权限</el-button
      >
    </div>
    <vxe-table :data="rows" :loading="loading" border stripe>
      <vxe-column field="id" title="ID" width="80" />
      <vxe-column field="name" title="权限名称" />
      <vxe-column field="description" title="描述" />
      <vxe-column title="操作" width="200">
        <template #default="scope">
          <el-button
            v-perm="'权限分配'"
            link
            type="primary"
            @click="openEdit(scope.row)"
            ><el-icon><Edit /></el-icon>编辑</el-button
          >
          <el-button
            v-perm="'权限分配'"
            link
            type="danger"
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
import { reactive, ref } from "vue";
import http from "../../services/http";
import { ElIcon, ElMessageBox } from "element-plus";
import { Plus, Edit, Delete } from "@element-plus/icons-vue";

const loading = ref(false);
const rows = ref<any[]>([]);
const page = reactive({ total: 0, current: 1, size: 10 });
const visible = ref(false);
const form = reactive<any>({ id: 0, name: "", description: "" });

async function fetch() {
  loading.value = true;
  const res = await http.get("/permission/list", {
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

