<template>
  <div class="space-y-4">
    <div class="flex justify-between">
      <div class="text-lg font-medium">页面管理</div>
      <el-button type="primary" @click="openCreate"
        ><el-icon><Plus /></el-icon>新建页面</el-button
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
      <template #row-visible="{ row }">
        {{ visibleText(row.visible) }}
      </template>
      <template #actions="{ row }">
        <el-button plain round type="primary" @click="openEdit(row)"
          ><el-icon><Edit /></el-icon>编辑</el-button
        >
        <el-button plain round type="primary" @click="openPerm(row)"
          ><el-icon><Key /></el-icon>权限设置</el-button
        >
        <el-button plain round type="danger" @click="remove(row)"
          ><el-icon><Delete /></el-icon>删除</el-button
        >
      </template>
    </EpTable>

    <el-dialog
      v-model="visible"
      :title="form.id ? '编辑页面' : '新建页面'"
      width="640px"
    >
      <el-form :model="form" label-width="110px">
        <el-form-item label="父页面ID"
          ><el-input v-model.number="form.parentId"
        /></el-form-item>
        <el-form-item label="名称"
          ><el-input v-model="form.name"
        /></el-form-item>
        <el-form-item label="路径"
          ><el-input v-model="form.path"
        /></el-form-item>
        <el-form-item label="组件"
          ><el-input v-model="form.component"
        /></el-form-item>
        <el-form-item label="图标"
          ><el-input v-model="form.icon"
        /></el-form-item>
        <el-form-item label="排序"
          ><el-input-number v-model.number="form.orderNum" :min="0"
        /></el-form-item>
        <el-form-item label="可见">
          <el-switch v-model="visibleSwitch" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="permVisible" title="页面权限设置" width="520px">
      <div class="space-y-3">
        <el-checkbox-group v-model="assignIds">
          <el-checkbox v-for="p in permList" :key="p.id" :label="p.id">{{
            p.name
          }}</el-checkbox>
        </el-checkbox-group>
        <div class="flex items-center space-x-2">
          <el-select
            v-model="newPermId"
            placeholder="选择单个权限"
            style="width: 280px"
          >
            <el-option
              v-for="p in permList"
              :key="p.id"
              :value="p.id"
              :label="p.name"
            />
          </el-select>
          <el-button @click="addOne" :disabled="!currentPageId || !newPermId"
            >新增</el-button
          >
        </div>
      </div>
      <template #footer>
        <el-button @click="permVisible = false">取消</el-button>
        <el-button type="primary" @click="savePerms">保存</el-button>
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
  { prop: "parentId", label: "父ID", width: 100 },
  { prop: "name", label: "名称" },
  { prop: "path", label: "路径" },
  { prop: "component", label: "组件" },
  { prop: "orderNum", label: "排序", width: 100 },
  { prop: "visible", label: "可见", width: 100, slot: "row-visible" },
];

const visible = ref(false);
const visibleSwitch = ref(true);
const form = reactive<any>({
  id: 0,
  parentId: 0 as number,
  name: "",
  path: "",
  component: "",
  icon: "",
  orderNum: 0,
  visible: 1,
  meta: "",
});

const permVisible = ref(false);
const currentPageId = ref<number | null>(null);
const permList = ref<any[]>([]);
const assignIds = ref<number[]>([]);
const newPermId = ref<number | null>(null);

function visibleText(value: number) {
  return value === 1 ? "是" : "否";
}

onMounted(() => {
  fetch();
});

async function fetch() {
  loading.value = true;
  const res = await http.get("/page/list", {
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
  Object.assign(form, {
    id: 0,
    parentId: 0,
    name: "",
    path: "",
    component: "",
    icon: "",
    orderNum: 0,
    visible: 1,
    meta: "",
  });
  visibleSwitch.value = true;
  visible.value = true;
}
function openEdit(row: any) {
  Object.assign(form, row);
  visibleSwitch.value = row.visible === 1;
  visible.value = true;
}
async function submit() {
  form.visible = visibleSwitch.value ? 1 : 0;
  if (form.id) await http.put(`/page/${form.id}`, form);
  else await http.post("/page", { ...form, meta: form.meta || "" });
  visible.value = false;
  fetch();
}
async function remove(row: any) {
  await ElMessageBox.confirm("确定要删除该页面吗？", "提示", {
    type: "warning",
  });
  await http.delete(`/page/${row.id}`);
  fetch();
}
async function openPerm(row: any) {
  currentPageId.value = row.id;
  const resAll = await http.get("/permission/list", {
    params: { current: 1, size: 200 },
  });
  permList.value = resAll.data.records;
  const res = await http.get(`/page/${row.id}`);
  assignIds.value = (res.data.permissions || []).map((x: any) => x.id);
  permVisible.value = true;
}
async function savePerms() {
  if (!currentPageId.value) return;
  await http.put(`/page/${currentPageId.value}/permission`, {
    permissionIds: assignIds.value,
  });
  permVisible.value = false;
  fetch();
}
async function addOne() {
  if (!currentPageId.value || !newPermId.value) return;
  await http.post(`/page/${currentPageId.value}/permission/${newPermId.value}`);
  const res = await http.get(`/page/${currentPageId.value}`);
  assignIds.value = (res.data.permissions || []).map((x: any) => x.id);
}
fetch();
</script>

<style scoped lang="scss"></style>
