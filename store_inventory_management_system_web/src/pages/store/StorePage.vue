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
        <el-form-item label="位置坐标" required>
          <div class="flex gap-2 w-full">
            <el-input-number
              v-model="form.longitude"
              :precision="6"
              placeholder="经度"
              style="width: 160px"
            />
            <el-input-number
              v-model="form.latitude"
              :precision="6"
              placeholder="纬度"
              style="width: 160px"
            />
            <el-button type="primary" plain @click="openMap">
              <el-icon class="mr-1"><Location /></el-icon> 地图选择
            </el-button>
          </div>
        </el-form-item>
        
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
    <el-dialog
      v-model="mapVisible"
      title="选择位置"
      width="800px"
      top="5vh"
      @opened="initMap"
      append-to-body
    >
      <div id="map" style="width: 100%; height: 500px"></div>
      <template #footer>
        <div class="flex justify-between items-center">
          <span class="text-gray-500 text-sm"
            >当前选中: {{ tempLoc.lng.toFixed(6) }},
            {{ tempLoc.lat.toFixed(6) }}</span
          >
          <div>
            <el-button @click="mapVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmLoc">确定</el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from "vue";
import http from "../../services/http";
import { ElMessage } from "element-plus";
import { Location } from "@element-plus/icons-vue";
import L from "leaflet";
import "leaflet/dist/leaflet.css";

// Fix Leaflet icon paths
import markerIcon from "leaflet/dist/images/marker-icon.png";
import markerIcon2x from "leaflet/dist/images/marker-icon-2x.png";
import markerShadow from "leaflet/dist/images/marker-shadow.png";

// @ts-ignore
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow,
});

const mapVisible = ref(false);
const tempLoc = reactive({ lat: 0, lng: 0 });
let map: L.Map | null = null;
let marker: L.Marker | null = null;

function openMap() {
  // 默认北京坐标
  tempLoc.lat = form.latitude || 39.9042;
  tempLoc.lng = form.longitude || 116.4074;
  mapVisible.value = true;
}

function initMap() {
  if (map) {
    map.remove();
    map = null;
  }
  // 确保DOM已渲染
  setTimeout(() => {
    map = L.map("map").setView([tempLoc.lat, tempLoc.lng], 13);
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    }).addTo(map);

    marker = L.marker([tempLoc.lat, tempLoc.lng]).addTo(map);

    map.on("click", (e: L.LeafletMouseEvent) => {
      const { lat, lng } = e.latlng;
      tempLoc.lat = lat;
      tempLoc.lng = lng;
      if (marker) {
        marker.setLatLng([lat, lng]);
      } else {
        marker = L.marker([lat, lng]).addTo(map!);
      }
    });
  }, 100);
}

function confirmLoc() {
  form.latitude = Number(tempLoc.lat.toFixed(6));
  form.longitude = Number(tempLoc.lng.toFixed(6));
  mapVisible.value = false;
}

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
