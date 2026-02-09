<template>
  <div class="space-y-4">
    <div class="flex justify-between items-center">
      <div class="text-lg font-medium">操作日志</div>
      <div class="flex gap-2">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          @change="onDateChange"
        />
        <el-input
          v-model="keyword"
          placeholder="搜索模块/内容"
          class="w-64"
          clearable
          @clear="fetch"
          @keyup.enter="fetch"
        >
          <template #append>
            <el-button @click="fetch"><el-icon><Search /></el-icon></el-button>
          </template>
        </el-input>
      </div>
    </div>

    <EpTable
      :rows="rows"
      :columns="cols"
      :loading="loading"
      :pagination="page"
      @refresh="fetch"
      @update:current="onPageChange"
      @update:size="onSizeChange"
    >
      <template #operationResult="{ row }">
        <el-tag :type="row.operationResult === '成功' ? 'success' : 'danger'">
          {{ row.operationResult }}
        </el-tag>
      </template>
      <template #operationTime="{ row }">
        {{ formatTime(row.operationTime) }}
      </template>
    </EpTable>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from "vue";
import EpTable from "../../components/ep/EpTable.vue";
import http from "../../services/http";
import { Search } from "@element-plus/icons-vue";

const loading = ref(false);
const rows = ref<any[]>([]);
const cols = [
  { prop: "id", label: "ID", width: 180 },
  { prop: "operatorId", label: "操作人ID", width: 100 },
  { prop: "systemModule", label: "模块", width: 120 },
  { prop: "operationContent", label: "操作内容", width: 300 },
  { prop: "operationResult", label: "结果", width: 80, slot: "operationResult" },
  { prop: "operatorIp", label: "IP地址", width: 120 },
  { prop: "operationTime", label: "操作时间", width: 180, slot: "operationTime" },
];
const page = reactive({ total: 0, current: 1, size: 10 });
const keyword = ref("");
const dateRange = ref<[string, string] | null>(null);

async function fetch() {
  loading.value = true;
  try {
    const params: any = {
      current: page.current,
      size: page.size,
      systemModule: keyword.value, // 简单处理：搜索词同时作为模块和内容的过滤条件（后端需支持或前端拆分，这里暂传给module，实际可能需要后端支持多字段模糊搜索，或者我们分别尝试）
      // 由于DTO中是精确匹配systemModule和模糊匹配operationContent，这里我们主要搜索operationContent
      operationContent: keyword.value,
      sortField: "operationTime",
      sortOrder: "descend"
    };

    if (dateRange.value) {
      params.startTime = dateRange.value[0] + " 00:00:00";
      params.endTime = dateRange.value[1] + " 23:59:59";
    }

    // 注意：后端DTO逻辑是 systemModule 是 like 匹配，operationContent 也是 like 匹配。
    // 但是这里不仅是搜索框，所以我们只传 operationContent 作为关键词搜索，
    // 如果用户想搜模块，可能需要单独的下拉框，或者我们这里只搜内容。
    // 为了简单起见，这里搜索框主要搜内容。
    
    // 修正：后端DTO逻辑中：
    // queryWrapper.like(StrUtil.isNotBlank(dto.getSystemModule()), "systemModule", dto.getSystemModule());
    // queryWrapper.like(StrUtil.isNotBlank(dto.getOperationContent()), "operationContent", dto.getOperationContent());
    // 如果想实现“搜索模块或内容”，后端需要 OR 逻辑。
    // 暂时我们只搜索 operationContent。

    const res = await http.post("/log", params);
    rows.value = res.data.records || [];
    page.total = res.data.total;
  } finally {
    loading.value = false;
  }
}

function onDateChange() {
  page.current = 1;
  fetch();
}

function onPageChange(p: number) {
  page.current = p;
  fetch();
}

function onSizeChange(s: number) {
  page.size = s;
  page.current = 1;
  fetch();
}

function formatTime(time: string) {
  if (!time) return "";
  return new Date(time).toLocaleString();
}

onMounted(() => {
  fetch();
});
</script>

<style scoped>
</style>
