<template>
  <div class="dashboard-container">
    <div class="welcome-banner mb-6">
      <h1 class="text-3xl font-bold text-gray-800">
        欢迎回来，{{ user?.userName || user?.userAccount }} 👋
      </h1>
      <p class="text-gray-500 mt-2">今天是 {{ today }}</p>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
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
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import { User, UserFilled, Goods } from "@element-plus/icons-vue";
import auth from "../../services/auth";
import { ElMessageBox } from "element-plus";

const router = useRouter();
const user = computed(() => auth.state.user);
const today = new Date().toLocaleDateString("zh-CN", {
  year: "numeric",
  month: "long",
  day: "numeric",
  weekday: "long",
});

const isAdmin = computed(() => {
  return (
    user.value?.userAccount === "admin" ||
    auth.state.user?.id === 1 ||
    // 假设有角色判断逻辑，这里简单判断
    false
  );
});

const roleName = computed(() => {
  // 暂时无法直接获取角色名，需要从用户信息接口扩展，或者简单显示
  return isAdmin.value ? "管理员" : "普通用户";
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
