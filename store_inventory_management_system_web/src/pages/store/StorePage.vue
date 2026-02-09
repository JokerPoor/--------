<template>
  <div class="max-w-3xl mx-auto">
    <el-card shadow="hover">
      <template #header>
        <div class="flex justify-between items-center">
          <span class="text-lg font-medium">门店信息管理</span>
          <el-button
            type="primary"
            @click="submit"
            v-perm="'store:update'"
            :loading="loading"
          >
            保存修改
          </el-button>
        </div>
      </template>

      <el-form :model="form" label-width="120px" class="py-4">
        <el-form-item label="门店名称" required>
          <el-input v-model="form.storeName" />
        </el-form-item>
        <el-form-item label="详细地址" required>
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="店长ID" required>
          <el-input-number v-model="form.managerId" :min="1" style="width: 100%" />
          <div class="text-gray-400 text-xs mt-1">请输入有效的用户ID作为店长</div>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="经度" required>
              <el-input-number v-model="form.longitude" :precision="6" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="纬度" required>
              <el-input-number v-model="form.latitude" :precision="6" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-divider>联系人信息 (自动关联店长)</el-divider>
        <el-form-item label="联系人姓名">
          <el-input v-model="form.contactName" disabled />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.contactPhone" disabled />
        </el-form-item>
        <el-form-item label="联系邮箱">
          <el-input v-model="form.contactEmail" disabled />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from "vue";
import http from "../../services/http";
import { ElMessage } from "element-plus";

const loading = ref(false);
const form = reactive<any>({
  id: 0,
  storeName: "",
  address: "",
  managerId: 0,
  longitude: 0,
  latitude: 0,
  contactName: "",
  contactPhone: "",
  contactEmail: ""
});

async function fetch() {
  const res = await http.get("/store");
  if (res.data) {
    Object.assign(form, res.data);
  }
}

async function submit() {
  if (!form.storeName || !form.address || !form.managerId) {
    ElMessage.warning("请完善必填信息");
    return;
  }
  
  loading.value = true;
  try {
    await http.put("/store", {
      id: form.id,
      storeName: form.storeName,
      address: form.address,
      managerId: form.managerId,
      longitude: form.longitude,
      latitude: form.latitude
    });
    ElMessage.success("门店信息更新成功");
    fetch(); // 刷新获取最新联系人信息
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  fetch();
});
</script>

<style scoped>
</style>
