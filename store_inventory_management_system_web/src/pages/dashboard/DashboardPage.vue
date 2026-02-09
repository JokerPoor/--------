<template>
  <div class="dashboard-container">
    <div class="welcome-banner mb-6">
      <h1 class="text-3xl font-bold text-gray-800">
        欢迎回来，{{ user?.userName || user?.userAccount }} 👋
      </h1>
      <p class="text-gray-500 mt-2">今天是 {{ today }}</p>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <!-- Weather Card -->
      <el-card shadow="hover" class="stat-card" v-if="weather && weather.length > 0">
        <template #header>
          <div class="card-header flex justify-between items-center">
            <span>今日天气</span>
            <el-tag type="success">{{ weather[0].text_day }}</el-tag>
          </div>
        </template>
        <div class="text-center py-2">
          <div class="text-3xl font-bold mb-2">{{ weather[0].low }}°C - {{ weather[0].high }}°C</div>
          <div class="text-sm text-gray-500">
            {{ weather[0].wd_day }} {{ weather[0].wc_day }}
          </div>
          <div class="text-xs text-gray-400 mt-1">
            {{ weather[0].date }} {{ weather[0].week }}
          </div>
        </div>
      </el-card>

      <el-card shadow="hover" class="stat-card">
        <template #header>
          <div class="card-header flex justify-between items-center">
            <span>我的角色</span>
            <el-tag>{{ roleName }}</el-tag>
          </div>
        </template>
        <div class="text-center py-4">
          <el-icon :size="40" class="text-blue-500 mb-2"><User /></el-icon>
          <div class="text-sm text-gray-500">当前登录身份</div>
        </div>
      </el-card>

      <!-- 管理员可见 -->
      <template v-if="isAdmin">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">系统用户</div>
          </template>
          <div class="text-center py-4">
            <el-icon :size="40" class="text-green-500 mb-2"
              ><UserFilled
            /></el-icon>
            <div class="text-2xl font-bold">管理中心</div>
            <div class="text-sm text-gray-500">点击侧边栏管理用户</div>
          </div>
        </el-card>
      </template>

      <!-- 门店管理员可见 -->
      <template v-else-if="isStoreAdmin">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">门店运营</div>
          </template>
          <div class="text-center py-4">
            <el-icon :size="40" class="text-purple-500 mb-2"><Shop /></el-icon>
            <div class="text-2xl font-bold">运营中心</div>
            <div class="text-sm text-gray-500">管理商品、采购与销售</div>
          </div>
        </el-card>
      </template>

      <!-- 供应商/客户可见 -->
      <template v-else>
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">业务中心</div>
          </template>
          <div class="text-center py-4">
            <el-icon :size="40" class="text-orange-500 mb-2"><Goods /></el-icon>
            <div class="text-sm text-gray-500">查看您的订单与库存</div>
          </div>
        </el-card>
      </template>
    </div>

    <el-card shadow="hover">
      <template #header>
        <div class="font-bold">快捷入口</div>
      </template>
      <div class="flex gap-4">
        <el-button type="primary" plain @click="router.push('/users')" v-if="isAdmin">用户管理</el-button>
        <el-button type="success" plain @click="showInfo">个人信息</el-button>
        <!-- 更多快捷入口待开发 -->
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { User, UserFilled, Goods, Shop } from "@element-plus/icons-vue";
import auth from "../../services/auth";
import http from "../../services/http";
import { ElMessageBox } from "element-plus";

const router = useRouter();
const user = computed(() => auth.state.user);
const today = new Date().toLocaleDateString("zh-CN", {
  year: "numeric",
  month: "long",
  day: "numeric",
  weekday: "long",
});

const weather = ref<any[]>([]);

onMounted(async () => {
  try {
    const res = await http.get('/weather');
    if (res.data) {
      weather.value = res.data;
    }
  } catch (e) {
    console.error('Failed to fetch weather:', e);
  }
});

const isAdmin = computed(() => {
  if (!user.value) return false;
  if (user.value.userAccount === 'admin' || user.value.id === 1) return true;
  return user.value.roles?.some(r => r.roleName === '超级管理员');
});

const isStoreAdmin = computed(() => {
  if (!user.value) return false;
  return user.value.roles?.some(r => r.roleName === '门店管理员');
});

const roleName = computed(() => {
  if (isAdmin.value) return "超级管理员";
  if (isStoreAdmin.value) return "门店管理员";
  return user.value?.roles?.[0]?.roleName || "普通用户";
});

function showInfo() {
  ElMessageBox.alert(
    `账号: ${user.value?.userAccount}\n姓名: ${user.value?.userName}`,
    "个人信息"
  );
}
</script>

<style scoped>
.stat-card {
  transition: all 0.3s;
}
.stat-card:hover {
  transform: translateY(-5px);
}
</style>
