<template>
  <div class="detail-page">
    <!-- 返回导航 -->
    <div class="nav-bar">
      <el-button class="back-btn" @click="goBack">
        <el-icon>
          <ArrowLeft />
        </el-icon>
        返回列表
      </el-button>
    </div>

    <!-- 详情内容 -->
    <div class="detail-container">
      <!-- 标题区域 -->
      <div class="detail-header">
        <span class="type-tag" :style="{ color: getCategoryColor(), background: getCategoryBgColor() }">
          {{ getCategoryName() }}
        </span>
        <h1 class="detail-title">{{ detailData.title }}</h1>
        <div class="detail-meta">
          <span class="meta-item">
            <el-icon>
              <User />
            </el-icon>
            发布人：{{ publisherName }}
          </span>
          <span class="meta-item">
            <el-icon>
              <Clock />
            </el-icon>
            {{ formatTime(detailData.createTime) }}
          </span>
        </div>
      </div>

      <!-- 分割线 -->
      <el-divider />

      <!-- 公告图片 -->
      <div v-if="detailData.pictures" class="notice-images">
        <el-image
          v-for="(img, index) in pictureList"
          :key="index"
          :src="img"
          :preview-src-list="pictureList"
          :initial-index="index"
          fit="cover"
          class="notice-image"
          :preview-teleported="true"
        />
      </div>

      <!-- 内容区域 -->
      <div class="detail-content" v-html="processedContent"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ArrowLeft, Clock, User } from "@element-plus/icons-vue";
import request from "@/utils/request.js";
import { useUserStore } from "@/stores/user";
import { markNoticeRead } from "@/utils/studentNoticeRead.js";
import config from "@/config/config";

const route = useRoute();
const userStore = useUserStore();
const router = useRouter();
const detailData = ref({});
const publisherName = ref("系统管理员");

// 处理图片列表
const pictureList = computed(() => {
  if (!detailData.value.pictures) return [];
  
  // pictures 可能是单个路径或逗号分隔的多个路径
  const paths = detailData.value.pictures.split(',').filter(p => p.trim());
  
  return paths.map(path => {
    const trimmedPath = path.trim();
    
    // 如果已经是完整的 URL，直接返回
    if (trimmedPath.startsWith('http://') || trimmedPath.startsWith('https://')) {
      return trimmedPath;
    }
    
    // 如果已经包含 baseUrl，直接返回
    if (trimmedPath.startsWith(config.baseUrl)) {
      return trimmedPath;
    }
    
    // 拼接 baseUrl
    const fullPath = trimmedPath.startsWith('/') ? trimmedPath : `/${trimmedPath}`;
    return `${config.baseUrl}${fullPath}`;
  });
});

// 处理内容中的图片路径
const processedContent = computed(() => {
  const content = detailData.value.content;
  if (!content) return '';
  
  console.log('========== 公告内容处理开始 ==========');
  console.log('原始内容长度:', content.length);
  console.log('原始内容:', content);
  
  // 使用正则表达式替换 img 标签中的 src 属性
  const processed = content.replace(/<img([^>]*?)src=["']([^"']+)["']([^>]*?)>/gi, (match, before, src, after) => {
    console.log('---');
    console.log('找到图片 src:', src);
    console.log('完整 img 标签:', match);
    
    // 如果已经是完整的 URL，不处理
    if (src.startsWith('http://') || src.startsWith('https://')) {
      console.log('✓ 外部图片，不处理');
      return match;
    }
    
    // 如果已经包含 baseUrl，不重复添加
    if (src.startsWith(config.baseUrl)) {
      console.log('✓ 已包含 baseUrl，不处理');
      return match;
    }
    
    // 拼接 baseUrl
    const path = src.startsWith('/') ? src : `/${src}`;
    const fullUrl = `${config.baseUrl}${path}`;
    
    console.log('✓ 处理后的完整路径:', fullUrl);
    
    return `<img${before}src="${fullUrl}"${after}>`;
  });
  
  console.log('---');
  console.log('处理后的内容:', processed);
  console.log('========== 公告内容处理完成 ==========');
  
  return processed;
});

// 获取分类名称
const getCategoryName = () => {
  if (!detailData.value.teacherId || detailData.value.teacherId === 0) {
    return "系统公告";
  }
  return "考试通知";
};

// 获取分类类型
const getCategoryType = () => {
  if (!detailData.value.teacherId || detailData.value.teacherId === 0) {
    return "system";
  }
  return "exam";
};

// 获取分类颜色
const getCategoryColor = () => {
  const type = getCategoryType();
  return type === "system" ? "#16a34a" : "#4f46e5";
};

// 获取分类背景色
const getCategoryBgColor = () => {
  const type = getCategoryType();
  return type === "system" ? "#ecfdf3" : "#eef2ff";
};

// 格式化时间
const formatTime = (time) => {
  if (!time) return "";
  const date = new Date(time);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hours = String(date.getHours()).padStart(2, "0");
  const minutes = String(date.getMinutes()).padStart(2, "0");
  return `${year}-${month}-${day} ${hours}:${minutes}`;
};

const loadDataDetail = async () => {
  const { data } = await request.get(`/notices/info/${route.query.id}`);
  detailData.value = data;
  
  // 获取发布人信息
  if (data.teacherId && data.teacherId !== 0) {
    try {
      const teacherRes = await request.get(`/teachers/info/${data.teacherId}`);
      publisherName.value = teacherRes.data?.realName || teacherRes.data?.nickname || "教师";
    } catch (e) {
      console.error("获取教师信息失败:", e);
      publisherName.value = "教师";
    }
  } else {
    publisherName.value = "系统管理员";
  }
  
  const nid = route.query.id;
  if (nid != null && nid !== "") {
    markNoticeRead(userStore.userInfo?.id, nid);
  }
};

const goBack = () => {
  router.go(-1);
};

onMounted(() => {
  loadDataDetail();
});
</script>

<style lang="scss" scoped>
.detail-page {
  padding: 0;
  background-color: transparent;
  min-height: calc(100vh - 60px);
}

/* 导航栏 */
.nav-bar {
  margin-bottom: 20px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: #fff;
  border: 1px solid #dcdfe6;
  color: #606266;
  border-radius: 8px;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    color: #4f46e5;
    border-color: #4f46e5;
    background: #eef2ff;
  }
}

/* 详情容器 */
.detail-container {
  background: #fff;
  border-radius: 14px;
  padding: 32px;
  border: 1px solid #e7ecf4;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04), 0 10px 24px rgba(15, 23, 42, 0.05);
}

/* 标题区域 */
.detail-header {
  margin-bottom: 24px;
}

.type-tag {
  display: inline-block;
  padding: 4px 12px;
  font-size: 13px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.detail-title {
  font-size: 22px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 16px 0;
  line-height: 1.5;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 20px;
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #909399;
}

/* 分割线 */
:deep(.el-divider) {
  margin: 0 0 24px 0;
}

/* 公告图片 */
.notice-images {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 24px;
}

.notice-image {
  width: 200px;
  height: 200px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid #e7ecf4;

  &:hover {
    transform: scale(1.05);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
}

/* 内容区域 */
.detail-content {
  font-size: 15px;
  line-height: 1.8;
  color: #303133;

  :deep(p) {
    margin: 0 0 16px 0;
    text-align: justify;
  }

  :deep(h1),
  :deep(h2),
  :deep(h3),
  :deep(h4) {
    color: #1a1a1a;
    font-weight: 600;
    margin: 24px 0 16px 0;
  }

  :deep(h1) {
    font-size: 20px;
  }

  :deep(h2) {
    font-size: 18px;
  }

  :deep(h3) {
    font-size: 16px;
  }

  :deep(h4) {
    font-size: 15px;
  }

  :deep(ul),
  :deep(ol) {
    margin: 16px 0;
    padding-left: 24px;

    li {
      margin-bottom: 8px;
      line-height: 1.8;
    }
  }

  :deep(img) {
    max-width: 100%;
    height: auto;
    border-radius: 6px;
    margin: 16px 0;
  }

  :deep(a) {
    color: #4f46e5;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }

  :deep(table) {
    width: 100%;
    border-collapse: collapse;
    margin: 16px 0;
    border: 1px solid #ebeef5;

    th,
    td {
      padding: 10px 12px;
      text-align: left;
      border: 1px solid #ebeef5;
    }

    th {
      background: #f5f7fa;
      font-weight: 600;
    }
  }

  :deep(code) {
    background: #f5f7fa;
    color: #e6a23c;
    padding: 2px 6px;
    border-radius: 3px;
    font-size: 13px;
  }

  :deep(pre) {
    background: #f5f7fa;
    border-radius: 6px;
    padding: 16px;
    overflow-x: auto;
    margin: 16px 0;

    code {
      background: none;
      padding: 0;
    }
  }

  :deep(blockquote) {
    margin: 16px 0;
    padding: 12px 16px;
    background: #f5f7fa;
    border-left: 4px solid #4f46e5;
    border-radius: 4px;
    color: #606266;
  }

  :deep(strong),
  :deep(b) {
    font-weight: 600;
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .detail-page {
    padding: 0;
  }

  .detail-container {
    padding: 20px;
  }

  .detail-title {
    font-size: 18px;
  }

  .detail-content {
    font-size: 14px;
  }
}
</style>
