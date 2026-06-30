<template>
  <div class="help-detail-page">
    <div class="help-detail-bar">
      <el-button text type="primary" class="back-btn" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回帮助中心
      </el-button>
    </div>

    <div v-if="article" class="help-detail-card">
      <h1 class="help-detail-title">{{ article.title }}</h1>
      <div class="help-detail-body">
        <p v-for="(p, i) in article.blocks" :key="i" class="help-detail-p">{{ p }}</p>
      </div>
    </div>

    <div v-else class="help-detail-missing">
      <el-empty description="未找到该帮助说明">
        <el-button type="primary" @click="goHelpHome">返回帮助中心</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ArrowLeft } from "@element-plus/icons-vue";
import { getHelpArticle } from "./helpArticles.js";

const route = useRoute();
const router = useRouter();

const slug = computed(() => String(route.params.slug || "").trim());

const article = computed(() => getHelpArticle(slug.value));

const goBack = () => {
  router.push({ name: "helpCenter" });
};

const goHelpHome = () => {
  router.push({ name: "helpCenter" });
};
</script>

<style lang="scss" scoped>
.help-detail-page {
  flex: 1;
  min-height: 0;
  width: 100%;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  overflow-x: hidden;
  box-sizing: border-box;
}

.help-detail-bar {
  flex-shrink: 0;
  margin-bottom: 12px;
}

.back-btn {
  padding-left: 4px;
  font-weight: 600;
  color: #4f46e5;
}

.back-btn :deep(.el-icon) {
  margin-right: 4px;
}

.help-detail-card {
  flex-shrink: 0;
  background: #fff;
  border: 1px solid #e7ecf4;
  border-radius: 14px;
  padding: 22px 24px 28px;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.04);
}

.help-detail-title {
  margin: 0 0 18px;
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
  line-height: 1.35;
}

.help-detail-body {
  max-width: 880px;
}

.help-detail-p {
  margin: 0 0 14px;
  font-size: 15px;
  line-height: 1.75;
  color: #334155;
}

.help-detail-p:last-child {
  margin-bottom: 0;
}

.help-detail-missing {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 240px;
}
</style>
