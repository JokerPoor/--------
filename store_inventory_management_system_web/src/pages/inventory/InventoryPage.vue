<template>
  <div>
    <EpTable
      :rows="rows"
      :columns="cols"
      :loading="loading"
      :pagination="pagination"
      edit-perm="inventory:update"
      @refresh="fetch"
      @search="onSearch"
      @update:current="
        pagination.current = $event;
        fetch();
      "
      @update:size="
        pagination.size = $event;
        fetch();
      "
      @edit="onEdit"
    >
      <template #url="{ row }">
        <el-image
          :src="row.productUrl"
          class="w-10 h-10 object-cover rounded"
          :preview-src-list="[row.productUrl]"
          preview-teleported
        />
      </template>
    </EpTable>

    <el-dialog v-model="editVisible" title="编辑库存商品信息" width="500px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="商品名称">
          <el-input v-model="editForm.productName" />
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="editForm.productDescription" type="textarea" />
        </el-form-item>
        <el-form-item label="图片URL">
          <el-input v-model="editForm.productUrl" />
        </el-form-item>
        <el-form-item label="出售单价">
          <el-input-number
            v-model="editForm.productPrice"
            :precision="2"
            :step="0.1"
            :min="0"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="预警阈值">
          <el-input-number
            v-model="editForm.warningThreshold"
            :min="0"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="submitting"
          >确定</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from "vue";
import EpTable from "../../components/ep/EpTable.vue";
import http from "../../services/http";
import { ElMessage } from "element-plus";

const rows = ref([]);
const loading = ref(false);
const pagination = reactive({ current: 1, size: 10, total: 0 });
const keyword = ref("");

const cols = [
  { prop: "id", label: "ID", width: 80 },
  { prop: "productUrl", label: "图片", width: 80, slot: "url" },
  { prop: "productName", label: "商品名称" },
  { prop: "productDescription", label: "描述" },
  { prop: "productPrice", label: "售价", width: 100 },
  { prop: "quantity", label: "库存数量", width: 100 },
  { prop: "warningThreshold", label: "预警阈值", width: 100 },
  { prop: "createTime", label: "创建时间", width: 180 },
];

async function fetch() {
  loading.value = true;
  try {
    const res = await http.get("/inventory/list", {
      params: {
        current: pagination.current,
        size: pagination.size,
        productName: keyword.value,
      },
    });
    console.log(res, "res");
    if (res.data) {
      rows.value = res.data.records;
      pagination.total = res.data.total;
    }
  } finally {
    loading.value = false;
  }
}

function onSearch(k: string) {
  keyword.value = k;
  pagination.current = 1;
  fetch();
}

// Edit Logic
const editVisible = ref(false);
const submitting = ref(false);
const editForm = reactive({
  id: 0,
  productName: "",
  productDescription: "",
  productUrl: "",
  productPrice: 0,
  warningThreshold: 0,
});

function onEdit(row: any) {
  Object.assign(editForm, row);
  editVisible.value = true;
}

async function submitEdit() {
  submitting.value = true;
  try {
    await http.post("/inventory/update", editForm);
    ElMessage.success("更新成功");
    editVisible.value = false;
    fetch();
  } finally {
    submitting.value = false;
  }
}

onMounted(fetch);
</script>
