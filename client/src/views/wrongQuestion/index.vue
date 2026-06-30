<template>
  <div class="wrong-question-page">
    <h2 class="page-title">错题集</h2>
    <div class="wrong-question-page-body">
      <wrong-question />
    </div>
  </div>
</template>

<script setup>
import WrongQuestion from "@/views/personal-center/components/wrongQuestion.vue";
</script>

<style lang="scss" scoped>
.wrong-question-page {
  width: 100%;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.page-title {
  flex-shrink: 0;
  margin: 0 0 14px;
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
  color: #0f172a;
}

.wrong-question-page-body {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}
</style>
