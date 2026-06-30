<script setup>
import { onMounted } from 'vue';
import { useThemeStore } from '@/stores/theme';

const themeStore = useThemeStore();

// 应用保存的主题配置
onMounted(() => {
  themeStore.applyTheme();
});
</script>

<template>
  <div>
    <router-view></router-view>
  </div>
</template>

<style lang="scss" scoped>

</style>
