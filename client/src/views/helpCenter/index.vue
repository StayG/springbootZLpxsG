<template>
  <div class="help-center-page">
    <!-- 顶部横幅 -->
    <div class="help-hero">
      <div class="help-hero-inner">
        <div class="help-hero-text">
          <h1 class="help-title">帮助中心</h1>
          <p class="help-subtitle">为您提供详细的使用指南和常见问题解答</p>
          <div class="help-search-wrap">
            <el-input
              v-model="keyword"
              class="help-search-input"
              placeholder="请输入关键词搜索帮助内容"
              clearable
              size="large"
            >
              <template #prefix>
                <el-icon class="help-search-icon"><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </div>
        <div class="help-hero-art" aria-hidden="true">
          <div class="art-book">
            <span class="art-q">?</span>
          </div>
          <div class="art-glass"></div>
        </div>
      </div>
    </div>

    <!-- 左：快速帮助 + 帮助分类；右：仍未解决 + 热门问题 -->
    <div class="help-body-grid">
      <div class="help-col-left">
        <section class="section-block section-quick">
          <h2 class="section-heading">快速帮助</h2>
          <div class="quick-scroll">
            <div
              v-for="card in quickCards"
              :key="card.id"
              class="quick-card"
              @click="onQuickCard(card)"
            >
              <div class="quick-icon" :style="{ background: card.tint }">
                <el-icon :size="22"><component :is="card.icon" /></el-icon>
              </div>
              <div class="quick-title">{{ card.title }}</div>
              <div class="quick-desc">{{ card.desc }}</div>
            </div>
          </div>
        </section>

        <section class="section-block section-categories">
          <h2 class="section-heading">帮助分类</h2>
          <div class="panel panel-list">
            <div class="category-list">
              <div
                v-for="cat in filteredCategories"
                :key="cat.slug"
                class="category-row"
                @click="goArticle(cat.slug)"
              >
                <div class="category-icon" :style="{ background: cat.tint }">
                  <el-icon :size="18"><component :is="cat.icon" /></el-icon>
                </div>
                <div class="category-body">
                  <div class="category-name">{{ cat.title }}</div>
                  <div class="category-desc">{{ cat.desc }}</div>
                </div>
                <el-icon class="category-chevron"><ArrowRight /></el-icon>
              </div>
              <div v-if="filteredCategories.length === 0" class="empty-hint">未找到匹配的分类，请尝试其他关键词</div>
            </div>
          </div>
        </section>
      </div>

      <aside class="help-col-right" aria-label="客服与热门问题">
        <div class="panel panel-support">
          <h3 class="support-title">仍未解决？</h3>
          <p class="support-text">如果您还有其他问题，欢迎联系我们的客服团队</p>
          <el-button type="primary" class="support-btn" @click="onContactService">联系客服</el-button>
          <ul class="support-lines">
            <li>
              <el-icon class="line-icon"><ChatDotRound /></el-icon>
              <div>
                <div class="line-label">在线客服</div>
                <div class="line-value">工作日 9:00–18:00</div>
              </div>
            </li>
            <li>
              <el-icon class="line-icon"><Phone /></el-icon>
              <div>
                <div class="line-label">客服电话</div>
                <div class="line-value">400-123-4567</div>
              </div>
            </li>
            <li>
              <el-icon class="line-icon"><Message /></el-icon>
              <div>
                <div class="line-label">客服邮箱</div>
                <div class="line-value">service@exam.com</div>
              </div>
            </li>
          </ul>
        </div>

        <div class="panel panel-hot">
          <h3 class="panel-title hot-title">热门问题</h3>
          <ol class="hot-list">
            <li
              v-for="(item, idx) in filteredHotQuestions"
              :key="item.slug"
              @click="goArticle(item.slug)"
            >
              <span class="hot-num">{{ idx + 1 }}</span>
              <span class="hot-qtext">{{ item.text }}</span>
            </li>
          </ol>
          <div v-if="filteredHotQuestions.length === 0" class="empty-hint small">未找到匹配的问题</div>
          <div class="hot-footer">
            <a href="javascript:void(0)" class="link-all" @click.prevent="onViewAllHot">查看全部 &gt;</a>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import {
  Search,
  ArrowRight,
  Document,
  Monitor,
  WarningFilled,
  Tickets,
  Lock,
  Calendar,
  EditPen,
  Medal,
  User,
  MoreFilled,
  ChatDotRound,
  Phone,
  Message,
} from "@element-plus/icons-vue";
import { isValidHelpSlug } from "./helpArticles.js";

const router = useRouter();
const keyword = ref("");

/** 快速入口 -> 帮助说明 slug */
const quickCardSlugMap = {
  flow: "take",
  device: "prep",
  faq: "other",
  rules: "rule",
  privacy: "account",
};

const quickCards = [
  { id: "flow", title: "考试流程", desc: "了解考试全流程", icon: Document, tint: "#e8f1ff" },
  { id: "device", title: "设备要求", desc: "考试环境与设备要求", icon: Monitor, tint: "#e8f8ef" },
  { id: "faq", title: "常见问题", desc: "常见问题与解答", icon: WarningFilled, tint: "#f3e8ff" },
  { id: "rules", title: "考试规则", desc: "考试规则与注意事项", icon: Tickets, tint: "#fff4e6" },
  { id: "privacy", title: "隐私与安全", desc: "隐私政策与安全说明", icon: Lock, tint: "#e6fffa" },
];

const categories = [
  {
    slug: "prep",
    title: "考前准备",
    desc: "登录、设备调试等相关说明",
    searchText: "登录 密码 浏览器 chrome edge 摄像头 麦克风 设备 调试 网络",
    icon: Calendar,
    tint: "#eef2ff",
  },
  {
    slug: "take",
    title: "参加考试",
    desc: "进入考试、答题、交卷等",
    searchText: "进入考试 答题 保存 交卷 倒计时 我的考试",
    icon: EditPen,
    tint: "#e8f1ff",
  },
  {
    slug: "rule",
    title: "考试规则",
    desc: "考试纪律、违规处理等",
    searchText: "纪律 违规 作弊 切屏 诚信 录屏",
    icon: WarningFilled,
    tint: "#fff4e6",
  },
  {
    slug: "score",
    title: "成绩与证书",
    desc: "成绩查询、证书下载等",
    searchText: "成绩 分数 证书 下载 查询 我的成绩",
    icon: Medal,
    tint: "#f3e8ff",
  },
  {
    slug: "account",
    title: "账号与安全",
    desc: "账号管理、密码找回等",
    searchText: "账号 密码 修改密码 找回 安全 盗号",
    icon: User,
    tint: "#e8f8ef",
  },
  {
    slug: "other",
    title: "其他问题",
    desc: "其他零散问题汇总",
    searchText: "其他 客服 联系 问题",
    icon: MoreFilled,
    tint: "#f1f5f9",
  },
];

const hotQuestions = [
  { slug: "hot-load", text: "考试页面打不开或一直加载怎么办？" },
  { slug: "hot-ime", text: "考试中如何切换输入法？" },
  { slug: "hot-submit", text: "误操作交卷了能否恢复？" },
  { slug: "hot-media", text: "摄像头或麦克风检测失败如何处理？" },
  { slug: "hot-score", text: "成绩多久可以查询？" },
];

const norm = (s) => String(s || "").toLowerCase();

const matchKeyword = (k, ...fields) => {
  if (!k) return true;
  const blob = norm(fields.filter(Boolean).join(" "));
  return blob.includes(k);
};

const filteredCategories = computed(() => {
  const k = norm(keyword.value).trim();
  if (!k) return categories;
  return categories.filter((c) =>
    matchKeyword(k, c.title, c.desc, c.searchText)
  );
});

const filteredHotQuestions = computed(() => {
  const k = norm(keyword.value).trim();
  if (!k) return hotQuestions;
  return hotQuestions.filter((item) => matchKeyword(k, item.text));
});

const goArticle = (slug) => {
  const s = String(slug || "").trim();
  if (!isValidHelpSlug(s)) {
    ElMessage.warning("该帮助条目不存在");
    return;
  }
  router.push({ name: "helpArticle", params: { slug: s } });
};

const onQuickCard = (card) => {
  const slug = quickCardSlugMap[card.id];
  if (slug) goArticle(slug);
};

const onViewAllHot = () => {
  keyword.value = "";
};

const onContactService = () => {
  ElMessage.success("您可通过下方电话、邮箱或在线时段联系客服。");
};
</script>

<style lang="scss" scoped>
.help-center-page {
  flex: 1;
  min-height: 0;
  width: 100%;
  max-width: none;
  margin: 0;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  overflow-x: hidden;
  box-sizing: border-box;
  padding-bottom: 0;
}

.help-hero {
  flex-shrink: 0;
  position: relative;
  overflow: hidden;
  border-radius: 16px;
  background: #4f46e5;
  border: 1px solid rgba(255, 255, 255, 0.18);
  margin-bottom: 12px;
  color: #ffffff;
}

.help-hero::before,
.help-hero::after {
  content: "";
  position: absolute;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  pointer-events: none;
}

.help-hero::before {
  width: 260px;
  height: 260px;
  right: 180px;
  top: -130px;
}

.help-hero::after {
  width: 380px;
  height: 380px;
  right: -120px;
  top: -130px;
}

.help-hero-inner {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 22px 28px;
}

.help-hero-text {
  flex: 1;
  min-width: 0;
}

.help-title {
  margin: 0 0 8px;
  font-size: 28px;
  font-weight: 800;
  color: #ffffff;
  letter-spacing: 0.02em;
}

.help-subtitle {
  margin: 0 0 16px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.92);
  line-height: 1.5;
}

.help-search-wrap {
  max-width: 520px;
}

.help-search-input {
  :deep(.el-input__wrapper) {
    border-radius: 12px;
    background: rgba(255, 255, 255, 0.96);
    box-shadow: 0 4px 14px rgba(15, 23, 42, 0.12);
    padding-left: 12px;
  }
}

.help-search-icon {
  color: #64748b;
}

.help-hero-art {
  position: relative;
  z-index: 1;
  width: 160px;
  height: 120px;
  flex-shrink: 0;
}

.art-book {
  position: absolute;
  right: 28px;
  top: 12px;
  width: 72px;
  height: 88px;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.35);
  border-radius: 8px 10px 10px 8px;
  box-shadow: 8px 12px 24px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
}

.art-q {
  font-size: 42px;
  font-weight: 800;
  color: #ffffff;
  line-height: 1;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}

.art-glass {
  position: absolute;
  right: 0;
  bottom: 18px;
  width: 56px;
  height: 56px;
  border: 4px solid rgba(255, 255, 255, 0.45);
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.25), 4px 8px 16px rgba(0, 0, 0, 0.12);
}

.art-glass::after {
  content: "";
  position: absolute;
  width: 10px;
  height: 22px;
  background: rgba(255, 255, 255, 0.55);
  border-radius: 4px;
  right: -6px;
  bottom: 8px;
  transform: rotate(35deg);
}

.section-block {
  margin-bottom: 0;
}

.section-quick {
  margin-bottom: 18px;
}

.section-heading {
  margin: 14px 0 16px;
  font-size: 17px;
  font-weight: 700;
  color: #1f2937;
}

.quick-scroll {
  display: flex;
  gap: 14px;
  overflow-x: auto;
  padding-bottom: 6px;
  scrollbar-width: thin;
}

.quick-card {
  flex: 0 0 168px;
  background: #fff;
  border-radius: 14px;
  padding: 18px 16px;
  box-shadow: 0 4px 16px rgba(15, 23, 42, 0.06);
  border: 1px solid #eef2f7;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.quick-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.1);
}

.quick-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
  color: #334155;
}

.quick-card:nth-child(1) .quick-icon {
  color: #2563eb;
}
.quick-card:nth-child(2) .quick-icon {
  color: #16a34a;
}
.quick-card:nth-child(3) .quick-icon {
  color: #9333ea;
}
.quick-card:nth-child(4) .quick-icon {
  color: #ea580c;
}
.quick-card:nth-child(5) .quick-icon {
  color: #0d9488;
}

.quick-title {
  font-size: 15px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 6px;
}

.quick-desc {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.45;
}

.help-body-grid {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 16px;
  align-items: stretch;
}

.help-col-left {
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

@media (max-width: 900px) {
  .help-body-grid {
    grid-template-columns: 1fr;
  }
  .help-hero-inner {
    flex-direction: column;
    align-items: flex-start;
  }
  .help-hero-art {
    align-self: flex-end;
  }
}

.panel {
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e7ecf4;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.04);
  padding: 18px 20px;
}

.panel-title {
  margin: 0 0 16px;
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
}

.category-row {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 13px 4px;
  border-bottom: 1px solid #f1f5f9;
  cursor: pointer;
  transition: background 0.15s ease;
}

.category-row:last-child {
  border-bottom: none;
}

.category-row:hover {
  background: #f8fafc;
  margin-left: -8px;
  margin-right: -8px;
  padding-left: 12px;
  padding-right: 12px;
  border-radius: 10px;
}

.category-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #475569;
  flex-shrink: 0;
}

.category-body {
  flex: 1;
  min-width: 0;
}

.category-name {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 4px;
}

.category-desc {
  font-size: 13px;
  color: #6b7280;
  line-height: 1.4;
}

.category-chevron {
  color: #cbd5e1;
  flex-shrink: 0;
}

.category-row:hover .category-chevron {
  color: #94a3b8;
}

.empty-hint {
  padding: 28px 12px;
  text-align: center;
  color: #9ca3af;
  font-size: 14px;
}
.empty-hint.small {
  padding: 12px;
}

.help-col-right {
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.panel-support .support-title {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 700;
  color: #111827;
}

.support-text {
  margin: 0 0 16px;
  font-size: 13px;
  color: #6b7280;
  line-height: 1.55;
}

.support-btn {
  width: 100%;
  height: 40px;
  border-radius: 10px;
  font-weight: 600;
  margin-bottom: 18px;
}

.support-lines {
  list-style: none;
  margin: 0;
  padding: 0;
}

.support-lines li {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 10px 0;
  border-top: 1px solid #f1f5f9;
}

.support-lines li:first-of-type {
  border-top: none;
  padding-top: 0;
}

.line-icon {
  color: #64748b;
  font-size: 18px;
  margin-top: 2px;
}

.line-label {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
}

.line-value {
  font-size: 12px;
  color: #6b7280;
  margin-top: 2px;
}

.hot-title {
  margin-bottom: 12px;
}

.hot-list {
  margin: 0;
  padding: 0;
  list-style: none;
  counter-reset: hot;
}

.hot-list li {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  padding: 10px 0;
  font-size: 13px;
  color: #374151;
  line-height: 1.5;
  border-bottom: 1px solid #f1f5f9;
  cursor: pointer;
  transition: color 0.15s ease;
}

.hot-list li:last-child {
  border-bottom: none;
}

.hot-list li:hover {
  color: #4f46e5;
}

.hot-num {
  flex-shrink: 0;
  width: 22px;
  height: 22px;
  border-radius: 6px;
  background: #eef2ff;
  color: #4f46e5;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hot-footer {
  text-align: right;
  margin-top: 12px;
  padding-top: 4px;
}

.link-all {
  font-size: 13px;
  font-weight: 600;
  color: #4f46e5;
  text-decoration: none;
}

.link-all:hover {
  color: #4338ca;
  text-decoration: underline;
}
</style>
