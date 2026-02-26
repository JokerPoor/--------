<template>
  <div class="flex items-center gap-3 actions">
    <el-button type="primary" plain @click="$emit('open-customizer')"
      >主题设置</el-button
    >
  </div>
</template>

<script setup lang="ts"></script>

<style scoped lang="scss">
.actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
