<template>
  <div
    class="min-h-screen relative flex items-center justify-center px-6 bg-cover bg-center"
    :style="{ backgroundImage: 'url(/images/register-left.png)' }"
  >
    <div class="absolute inset-0 bg-black/40"></div>
    <div class="relative w-full flex justify-center">
      <el-card class="w-[520px]" shadow="hover">
        <div class="text-2xl font-semibold mb-1">欢迎注册 👋</div>
        <div class="text-gray-500 mb-4">创建您的门店库存账户</div>
        <el-form :model="form" label-width="90px">
          <el-form-item label="账号"><el-input v-model="form.userAccount" /></el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.userPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
          <el-form-item label="姓名"><el-input v-model="form.userName" /></el-form-item>
          <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
          <el-form-item label="角色">
            <el-select v-model="form.roleId" style="width: 100%">
              <el-option v-for="r in roleOptions" :key="r.id" :value="r.id" :label="r.roleName" />
            </el-select>
          </el-form-item>
          <el-form-item label="滑块验证">
            <el-button @click="sliderVisible = true" type="primary" plain>{{
              sliderPassed ? "已通过" : "点击进行验证"
            }}</el-button>
          </el-form-item>
          <el-button type="primary" class="w-full" :disabled="!sliderPassed || !form.roleId" @click="submit"
            >注册</el-button
          >
          <div class="text-center mt-3">
            已有账号？<el-link type="primary" @click="toLogin">去登录</el-link>
          </div>
        </el-form>
      </el-card>
    </div>
    <SliderCaptcha v-model="sliderVisible" @success="onCaptchaSuccess" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '../../services/http'
import SliderCaptcha from '../../components/SliderCaptcha.vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const roleOptions = ref<any[]>([])
const form = reactive({ userAccount: '', userPassword: '', userName: '', email: '', phone: '', roleId: 0 })
const sliderVisible = ref(false)
const sliderPassed = ref(false)

async function submit() {
  if (!form.roleId) {
    ElMessage.warning('请先选择角色')
    return
  }
  try {
    await http.post('/user/register', { ...form })
    router.replace('/login')
  } catch (e: any) {
    ElMessage.error(e?.message || '注册失败')
  }
}
function toLogin() { router.push('/login') }
function onCaptchaSuccess() { sliderPassed.value = true }

async function loadRoles() {
  try {
    const res = await http.get('/role/public/list', { params: { current: 1, size: 20, excludeAdmin: true } })
    roleOptions.value = res.data.records || []
    if (!form.roleId && roleOptions.value.length) form.roleId = roleOptions.value[0].id
  } catch (e: any) {
    roleOptions.value = []
    form.roleId = 0
    ElMessage.error(e?.message || '获取角色列表失败')
  }
}
loadRoles()
</script>

<style scoped lang="scss">
</style>
