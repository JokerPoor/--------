<template>
  <div class="ep-card" v-loading="loading" element-loading-text="加载中..." element-loading-background="rgba(255,255,255,0.6)">
    <div class="flex justify-between mb-3">
      <div class="flex gap-2">
        <el-input v-model="keyword" placeholder="搜索" clearable @input="onSearch" style="width:220px" />
        <slot name="toolbar"></slot>
      </div>
      <el-button type="primary" @click="$emit('refresh')"><el-icon><Refresh /></el-icon><span>刷新</span></el-button>
    </div>
    <el-table ref="tableRef" :data="rows" border stripe @selection-change="onSelectionChange">
      <el-table-column type="selection" width="48" />
      <el-table-column v-for="c in columns" :key="c.prop" :prop="c.prop" :label="c.label" :width="c.width">
        <template #default="{ row }">
          <slot v-if="c.slot" :name="c.slot" :row="row"></slot>
          <span v-else>{{ row[c.prop] }}</span>
        </template>
      </el-table-column>
      <el-table-column v-if="hasDefaultActions" label="操作" width="200" fixed="right">
        <template #default="scope">
          <el-button v-if="editPerm" link type="primary" @click="$emit('edit', scope.row)" v-perm="editPerm"><el-icon><Edit /></el-icon>编辑</el-button>
          <el-button v-if="removePerm" link type="danger" @click="$emit('remove', scope.row)" v-perm="removePerm"><el-icon><Delete /></el-icon>删除</el-button>
          <el-button v-if="resetPerm" link type="warning" @click="$emit('reset', scope.row)" v-perm="resetPerm"><el-icon><Key /></el-icon>重置密码</el-button>
        </template>
      </el-table-column>
      <el-table-column v-if="$slots.actions" label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <slot name="actions" :row="row"></slot>
        </template>
      </el-table-column>
    </el-table>
    <div class="mt-3 flex justify-end">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="onSizeChange"
        @current-change="onCurrentChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { debounce } from 'lodash-es'
import { ElTable, ElIcon } from 'element-plus'
import { Refresh, Edit, Delete, Key } from '@element-plus/icons-vue'

const props = defineProps<{
  rows: any[],
  columns: { prop: string, label: string, width?: number, slot?: string }[],
  loading: boolean,
  pagination: { total: number, current: number, size: number },
  editPerm?: string,
  removePerm?: string,
  resetPerm?: string
}>()
const emit = defineEmits(['refresh','edit','remove','reset','search','update:current','update:size','selection-change'])
const keyword = ref('')
const onSearch = debounce(() => { emit('search', keyword.value) }, 300)
const tableRef = ref<InstanceType<typeof ElTable>>()

// 只有当至少有一个权限被定义时才显示默认操作列
const hasDefaultActions = computed(() => {
  return !!(props.editPerm || props.removePerm || props.resetPerm)
})

function onSelectionChange(selection: any[]) {
  emit('selection-change', selection)
}

function onSizeChange(size: number) {
  emit('update:size', size)
}

function onCurrentChange(current: number) {
  emit('update:current', current)
}
</script>

<style scoped lang="scss">
.ep-card {
  background-color: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: var(--el-border-radius-base);
  box-shadow: var(--el-box-shadow-light);
  padding: var(--app-card-padding);
}
</style>