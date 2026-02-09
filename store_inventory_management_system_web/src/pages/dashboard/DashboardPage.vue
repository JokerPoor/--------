<template>
  <div class="space-y-6">
    <div class="welcome-banner mb-6">
      <h1 class="text-3xl font-bold text-gray-800">
        欢迎回来，{{ user?.userName || user?.userAccount }} 👋
      </h1>
      <p class="text-gray-500 mt-2">今天是 {{ today }}</p>
    </div>

    <!-- Weather & Role Info -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
      <!-- Weather Card -->
      <el-card shadow="hover" class="stat-card" v-if="weather && weather.length > 0">
        <template #header>
          <div class="card-header flex justify-between items-center">
            <div class="flex items-center gap-2">
              <span>{{ currentCityName }}</span>
              <el-dropdown @command="handleCityChange" trigger="click">
                <span class="el-dropdown-link cursor-pointer text-xs text-blue-500">
                  [切换]
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-for="city in cities" :key="city.id" :command="city">
                      {{ city.name }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
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

      <!-- Role Info Card -->
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
    </div>

    <el-card shadow="hover">
      <template #header>
        <div class="font-bold">快捷入口</div>
      </template>
      <div class="flex gap-4">
        <el-button type="primary" plain @click="router.push('/users')" v-if="isAdmin">用户管理</el-button>
        <el-button type="success" plain @click="showInfo">个人信息</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { User, UserFilled, Goods, Shop, ArrowUp, ArrowDown } from "@element-plus/icons-vue";
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
const currentCityName = ref('北京朝阳');

const cities = [
  { name: '北京朝阳', id: '110105' },
  { name: '北京海淀', id: '110108' },
  { name: '上海浦东', id: '310115' },
  { name: '广州天河', id: '440106' },
  { name: '深圳南山', id: '440305' },
  { name: '杭州西湖', id: '330106' },
  { name: '成都武侯', id: '510107' },
  { name: '武汉武昌', id: '420106' },
  { name: '南京鼓楼', id: '320106' },
  { name: '西安雁塔', id: '610113' },
];

async function fetchWeather(districtId: string) {
  try {
    const res = await http.get('/weather', { params: { districtId } });
    if (res.data) {
      weather.value = res.data;
    }
  } catch (e) {
    console.error('Failed to fetch weather:', e);
  }
}

function handleCityChange(city: { name: string; id: string }) {
  currentCityName.value = city.name;
  localStorage.setItem('weather_city', JSON.stringify(city));
  fetchWeather(city.id);
}

onMounted(() => {
  const savedCity = localStorage.getItem('weather_city');
  if (savedCity) {
    try {
      const city = JSON.parse(savedCity);
      currentCityName.value = city.name;
      fetchWeather(city.id);
    } catch {
      fetchWeather('110105'); // Default
    }
  } else {
    fetchWeather('110105'); // Default
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
  if (!user.value) return '未知';
  if (user.value.userAccount === 'admin' || user.value.id === 1) return '超级管理员';
  return user.value.roles?.map(r => r.roleName).join(' / ') || '普通用户';
});

function showInfo() {
  ElMessageBox.alert(`
    <p><strong>账号：</strong>${user.value?.userAccount}</p>
    <p><strong>昵称：</strong>${user.value?.userName}</p>
    <p><strong>手机：</strong>${user.value?.phone || '未设置'}</p>
    <p><strong>邮箱：</strong>${user.value?.email || '未设置'}</p>
  `, '个人信息', { dangerouslyUseHTMLString: true });
}
</script>

<style scoped lang="scss">
.stat-card {
  transition: all 0.3s;
  &:hover {
    transform: translateY(-5px);
  }
}
</style>
