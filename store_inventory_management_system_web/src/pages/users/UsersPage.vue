<template>
  <div class="space-y-4">
    <div class="flex justify-between">
      <div class="text-lg font-medium">用户管理</div>
      <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon><span>新建用户</span></el-button>
    </div>
    <EpTable
      :rows="rows"
      :columns="cols"
      :loading="loading"
      :pagination="page"
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
        <el-button @click="batchStatus(1)" :disabled="selectedIds.length===0"><el-icon><Unlock /></el-icon>批量启用</el-button>
        <el-button @click="batchStatus(0)" :disabled="selectedIds.length===0"><el-icon><Lock /></el-icon>批量禁用</el-button>
      </template>
    </EpTable>
    <el-dialog v-model="visible" :title="form.id ? '编辑用户' : '新建用户'" width="520px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="登录账号" v-if="!form.id"><el-input v-model="form.userAccount" /></el-form-item>
        <el-form-item label="登录密码" v-if="!form.id"><el-input v-model="form.userPassword" type="password" /></el-form-item>
        <el-form-item label="邮箱" v-if="!form.id"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="真实姓名"><el-input v-model="form.userName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="switchStatus" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleIds" multiple style="width:100%">
            <el-option v-for="r in roleOptions" :key="r.id" :label="r.roleName" :value="String(r.id)" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible=false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="resetVisible" title="重置密码" width="420px">
      <el-form :model="resetForm" label-width="100px">
        <el-form-item label="新密码"><el-input v-model="resetForm.newPassword" type="password" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetVisible=false">取消</el-button>
        <el-button type="primary" @click="submitReset">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import EpTable from '../../components/ep/EpTable.vue'
import http from '../../services/http'
import { ElIcon } from 'element-plus'
import { Plus, Unlock, Lock } from '@element-plus/icons-vue'

const loading = ref(false)
const rows = ref<any[]>([])
const cols = [
  { prop: 'id', label: 'ID', width: 80 },
  { prop: 'userAccount', label: '账号' },
  { prop: 'userName', label: '姓名' },
  { prop: 'phone', label: '手机号' },
  { prop: 'email', label: '邮箱' },
  { prop: 'statusText', label: '状态', width: 100 },
  { prop: 'roleNames', label: '角色' },
]
const page = reactive({ total: 0, current: 1, size: 10 })
const visible = ref(false)
const form = reactive<any>({ id: 0, userAccount: '', userPassword: '', email: '', userName: '', phone: '', status: 1, roleIds: [] })
const switchStatus = ref(true)
const roleOptions = ref<any[]>([])
const keyword = ref('')
const selectedIds = ref<any[]>([])

async function fetch() {
  loading.value = true
  const res = await http.get('/user/list', { params: { current: page.current, size: page.size, userName: keyword.value, userAccount: keyword.value } })
  rows.value = (res.data.records || []).map((it: any) => ({
    ...it,
    statusText: it.status === 0 ? '禁用' : '启用',
    roleNames: (it.roles || []).map((r: any) => r.roleName).join('、')
  }))
  page.total = res.data.total
  loading.value = false
}
function setKeyword(k: string) { keyword.value = k; page.current = 1; fetch() }
function onPageChange(p: number) { page.current = p; fetch() }
function onSizeChange(s: number) { page.size = s; page.current = 1; fetch() }
function onSelectionChange(rows: any[]) { selectedIds.value = rows.map(r => String(r.id)) }
async function batchStatus(status: number) {
  if (selectedIds.value.length === 0) return
  await http.post('/user/batch-status', { ids: selectedIds.value, status })
  fetch()
}
const resetVisible = ref(false)
const resetForm = reactive<{ id: number, newPassword: string }>({ id: 0, newPassword: '' })
function openReset(row: any) { resetForm.id = row.id; resetForm.newPassword = ''; resetVisible.value = true }
async function submitReset() {
  await http.post(`/user/${resetForm.id}/reset-password`, { newPassword: resetForm.newPassword })
  resetVisible.value = false
}
function openCreate() {
  Object.assign(form, { id: 0, userAccount: '', userPassword: '', email: '', userName: '', phone: '', status: 1, roleIds: [] })
  switchStatus.value = true
  visible.value = true
}
function openEdit(row: any) {
  Object.assign(form, { id: row.id, userName: row.userName, phone: row.phone, status: row.status ?? 1, roleIds: (row.roles || []).map((r: any) => String(r.id)) })
  switchStatus.value = row.status === 1
  visible.value = true
}
async function submit() {
  form.status = switchStatus.value ? 1 : 0
  if (form.id) {
    await http.put(`/user/${form.id}`, { userName: form.userName, phone: form.phone, status: form.status, roleIds: (form.roleIds || []).map((id: any) => String(id)) })
  } else {
    const res = await http.post('/user', { userAccount: form.userAccount, userPassword: form.userPassword, email: form.email, userName: form.userName, phone: form.phone, status: form.status })
    const newId = res.data
    if (newId && form.roleIds && form.roleIds.length) {
      await http.put(`/user/${newId}`, { userName: form.userName, phone: form.phone, status: form.status, roleIds: (form.roleIds || []).map((id: any) => String(id)) })
    }
  }
  visible.value = false
  fetch()
}
async function remove(row: any) {
  await http.delete(`/user/${row.id}`)
  fetch()
}
async function loadRoles() {
  const res = await http.get('/role/list', { params: { current: 1, size: 200 } })
  roleOptions.value = (res.data.records || []).map((r: any) => ({ ...r, id: String(r.id) }))
}
loadRoles()
fetch()
</script>

<style scoped lang="scss">
</style>
