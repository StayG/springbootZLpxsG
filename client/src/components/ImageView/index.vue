<template>
  <img :class="['base-image', customClass]" :style="imageStyle" :src="pictureUrl" :alt="alt" />
</template>

<script setup>
import { ref, watch, computed } from "vue";
import config from "@/config/config.js";

const pictureUrl = ref("");

const props = defineProps({
  // 图片 URL
  src: {
    type: String,
    default: null,
  },
  width: {
    type: [String, Number],
    default: "100%",
  },
  height: {
    type: [String, Number],
    default: "100%",
  },
  fit: {
    type: String,
    default: "cover",
    validator: (value) => ["contain", "cover", "fill", "none", "scale-down"].includes(value),
  },
  customClass: {
    type: String,
    default: "",
  },
  alt: {
    type: String,
    default: "",
  },
});

const imageStyle = computed(() => ({
  width: typeof props.width === "number" ? `${props.width}px` : props.width,
  height: typeof props.height === "number" ? `${props.height}px` : props.height,
  "object-fit": props.fit,
  // display: "block", // 确保图片是块级元素，避免基线对齐问题
}));

const init = () => {
  if (props.src) {
    let url = props.src;
    if (url.startsWith("http") || url.startsWith("data:") || url.startsWith("blob:")) {
      pictureUrl.value = url;
    } else {
      pictureUrl.value = config.baseUrl + url;
    }
  } else {
    pictureUrl.value = "";
  }
};

watch(
    () => props.src,
    () => init(),
    { immediate: true }
);
</script>

<style scoped lang="scss">
.base-image {
  max-width: 100%; // 确保图片不会超出容器
  height: auto; // 保持宽高比
  vertical-align: middle; // 解决图片底部空白问题
  font-style: italic; // 替代文本的备用样式
  color: transparent; // 隐藏破损图片的alt文本
  background-repeat: no-repeat;
  background-size: contain;
  background-position: center;

  &:after {
    content: attr(alt);
    display: block;
    position: absolute;
    top: 50%;
    left: 0;
    width: 100%;
    text-align: center;
    transform: translateY(-50%);
    color: #888;
    font-size: 12px;
    font-style: normal;
  }
}
</style>
