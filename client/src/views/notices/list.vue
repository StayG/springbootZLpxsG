<template>
  <div class="notices-page">
    <!-- 页面标题 -->
    <div class="page-title-section">
      <h2 class="page-title">通知公告</h2>
    </div>

    <!-- 提示横幅 -->
    <div class="notice-banner">
      <div class="banner-icon">
        <el-icon :size="32">
          <Bell />
        </el-icon>
      </div>
      <div class="banner-content">
        <div class="banner-title">查看最新考试通知、系统公告和重要信息</div>
        <div class="banner-desc">请及时关注，避免错过重要考试安排</div>
      </div>
    </div>

    <!-- 分类标签和搜索 -->
    <div class="filter-section">
      <div class="category-tabs">
        <span v-for="tab in categoryTabs" :key="tab.value" :class="['tab-item', { active: activeCategory === tab.value }]"
          @click="handleCategoryChange(tab.value)">
          {{ tab.label }}
        </span>
      </div>
      <div class="search-box">
        <el-input v-model="searchParams.title" placeholder="搜索公告标题" clearable @clear="handleSearch"
          @keyup.enter="handleSearch">
          <template #prefix>
            <el-icon>
              <Search />
            </el-icon>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 列表区：页内滚动 + 底部分页（与考试中心一致） -->
    <div class="notices-board">
      <div class="notices-board-body">
        <div ref="listScrollEl" class="notices-board-list">
          <div class="notices-list">
            <div v-for="item in filteredDataList" :key="item.id" class="notice-card" @click="handleViewDetail(item.id)">
              <div class="card-icon" :style="{ background: getCategoryColor(item) }">
                <el-icon :size="28">
                  <component :is="getCategoryIcon(item)" />
                </el-icon>
              </div>
              <div class="card-content">
                <div class="card-header">
                  <span class="type-tag" :style="{ color: getCategoryColor(item), background: getCategoryBgColor(item) }">
                    {{ getCategoryName(item) }}
                  </span>
                  <span class="card-title">{{ item.title }}</span>
                  <span v-if="!item.isRead" class="unread-dot"></span>
                </div>
                <div class="card-desc">{{ stripHtml(item.content) }}</div>
              </div>
              <div class="card-date">{{ formatTime(item.createTime) }}</div>
            </div>

            <div v-if="dataList.length === 0" class="empty-state">
              <el-empty description="暂无公告信息" />
            </div>
          </div>
        </div>

        <div class="pagination-section" v-if="dataList.length > 0">
          <span class="total-text">共 {{ total }} 条</span>
          <el-pagination
            v-model:current-page="searchParams.page"
            v-model:page-size="searchParams.limit"
            :total="total"
            layout="prev, pager, next, jumper"
            background
            @current-change="handleCurrentChange"
          />
        </div>

        <div class="tips-section">
          <div class="tips-icon">
            <el-icon :size="18">
              <InfoFilled />
            </el-icon>
          </div>
          <div class="tips-content">
            <div class="tips-title">温馨提示</div>
            <div class="tips-desc">建议开启消息通知，以便及时接收重要考试信息。您可以在个人中心设置通知方式。</div>
          </div>
          <el-button class="tips-btn" size="small">去设置</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import {
  Bell,
  Search,
  InfoFilled,
  Document,
  Reading
} from "@element-plus/icons-vue";
import request from "@/utils/request.js";
import { useUserStore } from "@/stores/user";
import { isNoticeUnread } from "@/utils/studentNoticeRead.js";

const router = useRouter();
const userStore = useUserStore();
const listScrollEl = ref(null);

const dataList = ref([]);
const total = ref(0);
const activeCategory = ref("");

// 过滤后的数据列表
const filteredDataList = computed(() => {
  if (!activeCategory.value) return dataList.value;
  return dataList.value.filter(item => {
    const type = getCategoryType(item);
    return type === activeCategory.value;
  });
});

// 分类标签
const categoryTabs = [
  { label: "全部", value: "" },
  { label: "系统公告", value: "system" },
  { label: "考试通知", value: "exam" }
];

// 搜索参数
const searchParams = reactive({
  page: 1,
  limit: 10,
  title: ""
});

const scrollListToTop = async () => {
  await nextTick();
  const el = listScrollEl.value;
  if (el) el.scrollTop = 0;
};

// 获取分类名称
const getCategoryName = (item) => {
  if (!item.teacherId || item.teacherId === 0) {
    return "系统公告";
  }
  return "考试通知";
};

// 获取分类类型
const getCategoryType = (item) => {
  if (!item.teacherId || item.teacherId === 0) {
    return "system";
  }
  return "exam";
};

// 获取分类颜色
const getCategoryColor = (item) => {
  const type = getCategoryType(item);
  return type === "system" ? "#16a34a" : "#4f46e5";
};

// 获取分类背景色
const getCategoryBgColor = (item) => {
  const type = getCategoryType(item);
  return type === "system" ? "#ecfdf3" : "#eef2ff";
};

// 获取分类图标
const getCategoryIcon = (item) => {
  const type = getCategoryType(item);
  return type === "system" ? Document : Bell;
};

// 格式化时间
const formatTime = (time) => {
  if (!time) return "";
  const date = new Date(time);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
};

// 去除HTML标签
const stripHtml = (html) => {
  if (!html) return "";
  const div = document.createElement("div");
  div.innerHTML = html;
  const text = div.textContent || div.innerText || "";
  return text.length > 80 ? text.substring(0, 80) + "..." : text;
};

// 加载数据列表
const loadDataList = async () => {
  try {
    const { data } = await request.post("/notices/list", searchParams);
    const uid = userStore.userInfo?.id;
    dataList.value = (data.list || []).map((item) => ({
      ...item,
      isRead: !isNoticeUnread(uid, item.id),
    }));
    total.value = data.total || 0;
    scrollListToTop();
  } catch (error) {
    ElMessage.error("加载公告列表失败");
  }
};

// 分类切换
const handleCategoryChange = async (value) => {
  activeCategory.value = value;
  searchParams.page = 1;
  await scrollListToTop();
};

// 搜索
const handleSearch = () => {
  searchParams.page = 1;
  loadDataList();
};

// 当前页码改变
const handleCurrentChange = (page) => {
  searchParams.page = page;
  loadDataList();
};

// 查看详情
const handleViewDetail = (id) => {
  router.push({ name: "noticesDetail", query: { id } });
};

onMounted(() => {
  loadDataList();
});
</script>

<style lang="scss" scoped>
.notices-page {
  width: 100%;
  height: 100%;
  min-height: 0;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 页面标题 */
.page-title-section {
  flex-shrink: 0;
  margin-bottom: 14px;
}

.page-title {
  margin: 0 0 14px;
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
  color: #0f172a;
}

/* 提示横幅 */
.notice-banner {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  background: #e2e8ff;
  border: 1px solid #cbd5f5;
  border-radius: 14px;
  margin-bottom: 20px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}

.banner-icon {
  color: #4f46e5;
  flex-shrink: 0;
}

.banner-content {
  flex: 1;
}

.banner-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 6px;
}

.banner-desc {
  font-size: 13px;
  color: #64748b;
}

/* 分类标签和搜索 */
.filter-section {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  gap: 20px;
}

.notices-board {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #fff;
  border: 1px solid #e7ecf4;
  border-radius: 14px;
  padding: 12px 16px 14px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04), 0 10px 24px rgba(15, 23, 42, 0.05);
}

.notices-board-body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.notices-board-list {
  flex: 1;
  min-height: 0;
  overflow-x: hidden;
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;

  &::-webkit-scrollbar {
    width: 0;
    height: 0;
    display: none;
  }
}

.category-tabs {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.tab-item {
  padding: 8px 20px;
  font-size: 14px;
  color: #606266;
  background: #fff;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;

  &:hover {
    color: #4f46e5;
    background: #eef2ff;
  }

  &.active {
    color: #fff;
    background: #4f46e5;
    font-weight: 500;
  }
}

.search-box {
  width: 280px;
  flex-shrink: 0;

  :deep(.el-input__wrapper) {
    border-radius: 6px;
    box-shadow: 0 0 0 1px #dcdfe6 inset;

    &:hover {
      box-shadow: 0 0 0 1px #c0c4cc inset;
    }

    &.is-focus {
      box-shadow: 0 0 0 1px #4f46e5 inset;
    }
  }
}

/* 公告列表 */
.notices-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-bottom: 8px;
}

.notice-card {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e7ecf4;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    box-shadow: 0 10px 22px rgba(79, 70, 229, 0.12);
    border-color: #c7d2fe;
    transform: translateY(-2px);
  }
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.type-tag {
  padding: 2px 10px;
  font-size: 12px;
  border-radius: 4px;
  white-space: nowrap;
  flex-shrink: 0;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a1a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.unread-dot {
  width: 6px;
  height: 6px;
  background: #f56c6c;
  border-radius: 50%;
  flex-shrink: 0;
}

.card-desc {
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
}

.card-date {
  font-size: 13px;
  color: #909399;
  white-space: nowrap;
  flex-shrink: 0;
}

/* 空状态 */
.empty-state {
  padding: 48px 0 24px;
}

/* 分页 */
.pagination-section {
  flex-shrink: 0;
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 4px 4px;
  border-top: 1px solid #eff3f8;
}

.total-text {
  color: #8b94a3;
  font-size: 13px;
}

:deep(.el-pagination.is-background .btn-prev),
:deep(.el-pagination.is-background .btn-next),
:deep(.el-pagination.is-background .el-pager li) {
  width: 26px;
  height: 26px;
  line-height: 26px;
  min-width: 26px;
  border-radius: 4px;
  margin: 0 3px;
}

:deep(.el-pagination.is-background .el-pager li.is-active) {
  background: #4f46e5;
  color: #fff;
}

/* 温馨提示：在分页下方，不参与列表滚动 */
.tips-section {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  margin-top: 12px;
  background: #eef2ff;
  border-radius: 12px;
}

.tips-icon {
  color: #4f46e5;
  flex-shrink: 0;
}

.tips-content {
  flex: 1;
}

.tips-title {
  font-size: 14px;
  font-weight: 600;
  color: #1a3a6b;
  margin-bottom: 4px;
}

.tips-desc {
  font-size: 12px;
  color: #5a7ba8;
}

.tips-btn {
  flex-shrink: 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .notices-page {
    padding: 0;
  }

  .page-title {
    font-size: 24px;
  }

  .filter-section {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .category-tabs {
    overflow-x: auto;
    padding-bottom: 4px;
  }

  .search-box {
    width: 100%;
  }

  .notice-card {
    flex-direction: column;
    gap: 12px;
  }

  .card-date {
    align-self: flex-end;
  }

  .tips-section {
    flex-direction: column;
    align-items: flex-start;
  }

  .tips-btn {
    align-self: flex-end;
  }
}</style>
