
<template>
  <div
    class="min-h-screen relative flex items-center justify-center px-6 bg-cover bg-center"
    :style="{ backgroundImage: 'url(/images/login-left.png)' }"
  >
    <div class="absolute inset-0 bg-black/40"></div>
    <div class="relative w-full flex justify-center">
      <el-card class="w-[480px]" shadow="hover">
        <div class="text-2xl font-semibold mb-1">欢迎使用 👋</div>
        <div class="text-gray-500 mb-4">门店库存管理系统</div>
        <el-form :model="form" label-width="90px">
          <el-form-item label="账号"><el-input v-model="form.userAccount" /></el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.userPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="滑块验证">
            <el-button @click="sliderVisible = true" type="primary" plain>{{
              sliderPassed ? "已通过" : "点击进行验证"
            }}</el-button>
          </el-form-item>
          <div class="flex items-center justify-between mb-2">
            <el-checkbox v-model="remember">记住我</el-checkbox>
            <el-link type="primary">忘记密码了？</el-link>
          </div>
          <el-button type="primary" class="w-full" :disabled="!sliderPassed" @click="submit"
            >登录</el-button
          >
          <div class="text-center mt-3">
            还没有账号？<el-link type="primary" @click="toRegister">注册新账号</el-link>
          </div>
        </el-form>
      </el-card>
    </div>
    <SliderCaptcha v-model="sliderVisible" @success="onCaptchaSuccess" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import http from "../../services/http";
import auth from "../../services/auth";
import SliderCaptcha from "../../components/SliderCaptcha.vue";

const router = useRouter();
const form = reactive({ userAccount: "", userPassword: "" });
const sliderVisible = ref(false);
const sliderPassed = ref(false);
const remember = ref(true);
const REMEMBER_KEY = "REMEMBER_ME_INFO";

onMounted(() => {
  const stored = localStorage.getItem(REMEMBER_KEY);
  if (stored) {
    try {
      const { u, p } = JSON.parse(stored);
      form.userAccount = u;
      form.userPassword = p;
      remember.value = true;
    } catch (e) {
      localStorage.removeItem(REMEMBER_KEY);
    }
  }
});

function normalizeMenuPath(path?: string) {
  if (!path) return "";
  return path.startsWith("/") ? path : `/${path}`;
}

async function submit() {
  await http.post("/user/login", { ...form });
  if (remember.value) {
    localStorage.setItem(
      REMEMBER_KEY,
      JSON.stringify({ u: form.userAccount, p: form.userPassword })
    );
  } else {
    localStorage.removeItem(REMEMBER_KEY);
  }
  await auth.refreshAccess();
  await auth.ensureDynamicRoutes(router);
  router.replace("/dashboard");
}
function toRegister() {
  router.push("/register");
}
function onCaptchaSuccess() {
  sliderPassed.value = true;
}
function quick(u: string, p: string) {
  form.userAccount = u;
  form.userPassword = p;
  sliderPassed.value = true;
}
</script>

<style scoped lang="scss">
</style>
