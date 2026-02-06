<template>
  <el-dialog
    v-model="visible"
    title="滑块验证"
    width="520px"
    :close-on-click-modal="false"
  >
    <div class="space-y-3">
      <div
        ref="box"
        class="relative w-full h-[240px] overflow-hidden rounded border"
        @mouseleave="onMouseLeave"
      >
        <img
          src="/images/captcha-bg.png"
          class="w-full h-full object-cover select-none pointer-events-none"
        />
        <div class="absolute" :style="{ top: posY + 'px', left: targetX + 'px' }">
          <div class="w-[44px] h-[44px] bg-blue-400/20" :style="{ clipPath: shapeClip }"></div>
        </div>
        <div class="absolute" :style="{ top: posY + 'px', left: pieceX + 'px' }">
          <div class="w-[44px] h-[44px] bg-white/70 backdrop-blur-sm shadow" :style="{ clipPath: shapeClip }"></div>
        </div>
      </div>
      <el-slider
        v-model="slider"
        :show-tooltip="false"
        :max="trackMax"
        @input="onSlide"
        @change="onRelease"
        @mousedown="onStart"
        @mouseup="onEnd"
      />
      <div class="text-center">
        <el-tag :type="passed ? 'success' : 'info'">{{
          passed ? "验证通过" : "拖动滑块，使拼图对准"
        }}</el-tag>
      </div>
    </div>
    <template #footer>
      <el-button @click="reset">刷新</el-button>
      <el-button type="primary" :disabled="!passed" @click="confirm"
        >确定</el-button
      >
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onBeforeUnmount } from "vue";
import { ElMessage } from "element-plus";

const props = defineProps<{ modelValue: boolean }>();
const emit = defineEmits<{
  (e: "update:modelValue", v: boolean): void;
  (e: "success"): void;
  (e: "fail"): void;
}>();

const visible = ref(false);
watch(
  () => props.modelValue,
  (v) => {
    visible.value = v;
    if (v) init();
  }
);
watch(visible, (v) => emit("update:modelValue", v));

const trackMax = 420;
const slider = ref(0);
const targetX = ref(0);
const pieceX = ref(0);
const passed = ref(false);
const dragging = ref(false);
const box = ref<HTMLElement | null>(null);
const posY = ref(100);
const shapeClip = ref('circle(22px at 50% 50%)');

function init() {
  passed.value = false;
  slider.value = 0;
  targetX.value = Math.floor(80 + Math.random() * 280);
  pieceX.value = 0;
  posY.value = Math.floor(60 + Math.random() * 100);
  shapeClip.value = pickShape();
}
function reset() {
  init();
}
function onSlide() {
  pieceX.value = slider.value;
}
function onRelease() {
  pieceX.value = slider.value;
  evaluate();
}
function confirm() {
  if (passed.value) {
    visible.value = false;
    emit("success");
  } else {
    emit("fail");
  }
}
function onStart() {
  dragging.value = true;
}
function onEnd() {
  dragging.value = false;
  evaluate();
  if (!passed.value) fail();
}
function onMouseLeave() {
  if (dragging.value) {
    evaluate();
    if (!passed.value) fail();
    dragging.value = false;
  }
}
function onWindowUp() {
  if (dragging.value) {
    dragging.value = false;
    evaluate();
    if (!passed.value) fail();
  }
}
function fail() {
  ElMessage.error("验证失败，请重试");
  visible.value = false;
  emit("fail");
}
function evaluate() { passed.value = Math.abs(pieceX.value - targetX.value) <= 8 }
function pickShape() {
  const arr = [
    'circle(22px at 50% 50%)',
    'polygon(50% 0, 100% 50%, 50% 100%, 0 50%)',
    'polygon(12% 0, 100% 0, 100% 80%, 80% 100%, 0 100%, 0 20%)'
  ];
  return arr[Math.floor(Math.random() * arr.length)];
}

onMounted(() => {
  if (props.modelValue) init();
  window.addEventListener("mouseup", onWindowUp);
});
onBeforeUnmount(() => {
  window.removeEventListener("mouseup", onWindowUp);
});
</script>

<style scoped>
</style>