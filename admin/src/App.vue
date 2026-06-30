<script setup>
import { onMounted } from 'vue';
import { useThemeStore } from "@/stores/theme";

const themeStore = useThemeStore();

// 应用主题
onMounted(() => {
  themeStore.applyTheme();
});
</script>

<template>
  <router-view></router-view>
</template>

<style scoped></style>
