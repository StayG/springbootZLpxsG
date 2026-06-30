<template>
  <div class="recommend" v-if="items.length > 0">
    <!-- 标题区域 -->
    <div class="section-header" v-if="showTitle">
      <div class="header-line"></div>
      <h2 class="section-title">
        <span class="title-icon">📋</span>
        {{ title }}
      </h2>
      <div class="header-line"></div>
    </div>

    <!-- 卡片网格 -->
    <div class="card-grid">
      <div 
        v-for="(item, index) in items" 
        :key="item[pkField] || index" 
        class="card-item"
        :style="{ animationDelay: `${index * 0.1}s` }"
        @click="handleModuleClick(item)"
      >
        <!-- 图片容器 -->
        <div class="card-image" v-if="item[imgField]">
          <ImageView 
            :src="item[imgField]" 
            :alt="item[titleField]"
            fit="cover"
          />
          <div class="image-overlay"></div>
        </div>
        
        <!-- 无图片占位 -->
        <div class="card-image-placeholder" v-else>
          <span class="placeholder-icon">📄</span>
        </div>

        <!-- 卡片内容 -->
        <div class="card-content">
          <h3 class="card-title">{{ item[titleField] }}</h3>
          <p class="card-desc" v-if="item[descField]">{{ formatDesc(item[descField]) }}</p>
          
          <!-- 扩展字段插槽 -->
          <slot name="extra" :item="item"></slot>
          
          <div class="card-footer">
            <button class="detail-btn" @click.stop="handleModuleClick(item)">
              查看详情
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 查看更多按钮 -->
    <div class="more-container" v-if="moreUrl">
      <button class="more-btn" @click="goToMore">
        <span>查看更多</span>
        <span class="more-icon">›</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";
import { watchEffect, computed } from "vue";
import ImageView from "@/components/ImageView/index.vue";

const router = useRouter();

const props = defineProps({
  items: {
    type: Array,
    default: () => [],
  },
  title: {
    type: String,
    default: "信息推荐",
  },
  showTitle: {
    type: Boolean,
    default: true,
  },
  moreUrl: {
    required: false,
    type: String,
    default: "",
  },
  tableName: {
    type: String,
    required: true, // 新增表名 prop
  },
  pkField: {
    type: String,
    default: "id", // 主键字段名
  },
  titleField: {
    type: String,
    default: "title", // 标题字段名
  },
  imgField: {
    type: String,
    default: "pictures", // 图片字段名
  },
  descField: {
    type: String,
    default: "desc", // 描述字段名
  },
  detailRouteName: {
    type: String,
    default: "", // 详情页路由名，默认自动拼接
  },
  detailRouteParam: {
    type: String,
    default: "id", // 详情页参数名
  },
  columns: {
    type: Number,
    default: 3,
    validator: (v) => v > 0 && v < 10,
  },
});

// 详情页路由名自动拼接逻辑
const routeName = computed(() => {
  return props.detailRouteName || (props.tableName ? props.tableName + "Detail" : "");
});

const handleModuleClick = (item) => {
  if (routeName.value && props.detailRouteParam) {
    router.push({
      name: routeName.value,
      query: { [props.detailRouteParam]: item[props.pkField] },
    });
  }
};

const goToMore = () => {
  router.push(props.moreUrl);
};

// 格式化描述文本（截取前50个字符）
const formatDesc = (desc) => {
  if (!desc) return '';
  return desc.length > 50 ? desc.substring(0, 50) + '...' : desc;
};

// 动态设置 grid 列数
watchEffect(() => {
  document.documentElement.style.setProperty("--card-columns", props.columns);
});
</script>

<style lang="scss" scoped>
.recommend {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0;
}

/* ========== 标题区域 ========== */
.section-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 32px;
  padding: 0 10px;
}

.header-line {
  flex: 1;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--content-theme-color, #2c3e50), transparent);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 1.8rem;
  font-weight: 700;
  color: #202124;
  margin: 0;
  white-space: nowrap;
  
  .title-icon {
    font-size: 2rem;
  }
}

/* ========== 卡片网格 ========== */
.card-grid {
  display: grid;
  grid-template-columns: repeat(var(--card-columns, 3), 1fr);
  gap: 24px;
  margin-top: 24px;
}

/* ========== 卡片样式 ========== */
.card-item {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  display: flex;
  flex-direction: column;
  border: 1px solid #e8eaed;
  animation: cardFadeIn 0.6s ease-out both;
  
  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 12px 24px color-mix(in srgb, var(--content-theme-color, #2c3e50) 15%, transparent);
    border-color: var(--content-theme-color, #2c3e50);
    
    .card-image {
      .image-overlay {
        opacity: 1;
      }
    }
  }
}

@keyframes cardFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ========== 图片容器 ========== */
.card-image {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: #f8f9fa;
  
  :deep(.image-view-wrapper) {
    width: 100%;
    height: 100%;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.4s ease;
    }
  }
  
  &:hover :deep(.image-view-wrapper img) {
    transform: scale(1.1);
  }
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: color-mix(in srgb, var(--content-theme-color, #2c3e50) 10%, transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
}

/* ========== 无图片占位 ========== */
.card-image-placeholder {
  width: 100%;
  height: 200px;
  background: color-mix(in srgb, var(--content-theme-color, #2c3e50) 8%, #f8f9fa);
  display: flex;
  align-items: center;
  justify-content: center;
  
  .placeholder-icon {
    font-size: 4rem;
    opacity: 0.5;
  }
}

/* ========== 卡片内容 ========== */
.card-content {
  padding: 16px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.card-title {
  font-size: 1.05rem;
  font-weight: 600;
  color: #202124;
  margin: 0 0 8px 0;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-desc {
  font-size: 0.85rem;
  color: #5f6368;
  line-height: 1.5;
  margin: 0 0 12px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* ========== 卡片底部 ========== */
.card-footer {
  margin-top: auto;
  padding-top: 10px;
  border-top: 1px solid #f1f3f4;
}

.detail-btn {
  width: 100%;
  background: var(--content-theme-color, #2c3e50);
  color: white;
  border: none;
  border-radius: 8px;
  padding: 9px 16px;
  font-size: 0.9rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px color-mix(in srgb, var(--content-theme-color, #2c3e50) 20%, transparent);
  
  &:hover {
    background: color-mix(in srgb, var(--content-theme-color, #2c3e50) 85%, black);
    box-shadow: 0 4px 12px color-mix(in srgb, var(--content-theme-color, #2c3e50) 30%, transparent);
  }
  
  &:active {
    transform: translateY(1px);
  }
}

/* ========== 查看更多按钮 ========== */
.more-container {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  padding: 20px 0;
}

.more-btn {
  background: white;
  color: var(--content-theme-color, #2c3e50);
  border: 2px solid var(--content-theme-color, #2c3e50);
  border-radius: 25px;
  padding: 12px 40px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px color-mix(in srgb, var(--content-theme-color, #2c3e50) 15%, transparent);
  
  &:hover {
    background: var(--content-theme-color, #2c3e50);
    color: white;
    transform: translateY(-2px);
    box-shadow: 0 6px 20px color-mix(in srgb, var(--content-theme-color, #2c3e50) 30%, transparent);
    
    .more-icon {
      transform: translateX(4px);
    }
  }
  
  &:active {
    transform: translateY(0);
  }
}

.more-icon {
  font-size: 1.5rem;
  transition: transform 0.3s ease;
}

/* ========== 响应式设计 ========== */
@media (max-width: 900px) {
  .recommend {
    padding: 0 16px;
  }
  
  .section-title {
    font-size: 1.5rem;
    
    .title-icon {
      font-size: 1.6rem;
    }
  }
  
  .card-grid {
    grid-template-columns: repeat(2, 1fr) !important;
    gap: 16px;
  }
  
  .card-image,
  .card-image-placeholder {
    height: 160px;
  }
  
  .card-content {
    padding: 14px;
  }
  
  .card-title {
    font-size: 1rem;
  }
  
  .card-desc {
    font-size: 0.8rem;
  }
}

@media (max-width: 600px) {
  .recommend {
    padding: 0 12px;
  }
  
  .section-header {
    margin-bottom: 24px;
    gap: 12px;
  }
  
  .section-title {
    font-size: 1.3rem;
    
    .title-icon {
      font-size: 1.4rem;
    }
  }
  
  .header-line {
    height: 1px;
  }
  
  .card-grid {
    grid-template-columns: 1fr !important;
    gap: 12px;
    margin-top: 16px;
  }
  
  .card-item {
    border-radius: 12px;
  }
  
  .card-image,
  .card-image-placeholder {
    height: 180px;
  }
  
  .card-content {
    padding: 12px;
  }
  
  .card-title {
    font-size: 0.95rem;
    margin-bottom: 6px;
  }
  
  .card-desc {
    font-size: 0.8rem;
    margin-bottom: 10px;
  }
  
  .detail-btn {
    padding: 8px 14px;
    font-size: 0.85rem;
  }
  
  .more-container {
    margin-top: 24px;
  }
  
  .more-btn {
    padding: 10px 32px;
    font-size: 0.9rem;
  }
}
</style>


