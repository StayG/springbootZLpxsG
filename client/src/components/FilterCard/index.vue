<template>
  <div class="filter-section">
    <div class="section-header">
      <h3 class="section-title">
        <span class="title-icon">{{ titleIcon }}</span>
        {{ title }}
      </h3>
    </div>
    <div class="filter-grid">
      <div
        v-for="item in displayList"
        :key="item[keyField] || item.id"
        class="filter-card"
        :class="{ active: isActive(item) }"
        @click="handleSelect(item)"
      >
        <div class="item-icon">{{ item.icon || '🏷️' }}</div>
        <div class="item-content">
          <div class="item-name">{{ item[nameField] }}</div>
          <div class="item-desc">{{ item[descField] || '暂无简介' }}</div>
        </div>
        <div class="item-check" v-if="isActive(item)">
          ✓
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  // 数据列表
  list: {
    type: Array,
    default: () => []
  },
  // 当前选中的值（支持字符串和数字类型）
  modelValue: {
    type: [String, Number],
    default: ''
  },
  // key
  keyField: {
    type: String,
    default: 'id'
  },
  // 名称字段
  nameField: {
    type: String,
    default: 'name'
  },
  // 描述字段
  descField: {
    type: String,
    default: 'description'
  },
  // 是否显示"全部"选项
  showAll: {
    type: Boolean,
    default: true
  },
  // "全部"选项的文本
  allText: {
    type: String,
    default: '全部类型'
  },
  // "全部"选项的描述
  allDesc: {
    type: String,
    default: '查看所有'
  },
  // 标题
  title: {
    type: String,
    default: '分类'
  },
  // 标题图标
  titleIcon: {
    type: String,
    default: '🏥'
  }
});

const emit = defineEmits(['update:modelValue', 'change']);

// 显示列表（包含"全部"选项）
const displayList = computed(() => {
  if (props.showAll) {
    return [
      {
        id: 0,
        [props.keyField]: '', // 使用空字符串作为"全部"的值
        [props.nameField]: props.allText,
        [props.descField]: props.allDesc,
        icon: '📋',
        __isAllOption: true // 标记这是"全部"选项
      },
      ...props.list
    ];
  }
  return props.list;
});

// 判断是否激活
const isActive = (item) => {
  const itemValue = item[props.keyField];
  const isEmptyModel = props.modelValue === '' || props.modelValue === null || props.modelValue === undefined;
  
  // 如果是"全部"选项（通过特殊标记识别）
  if (item.__isAllOption) {
    return isEmptyModel;
  }
  
  // 普通选项：使用宽松比较（==）来处理数字和字符串的比较
  // 确保 itemValue 不为空才进行比较
  if (itemValue === '' || itemValue === null || itemValue === undefined) {
    return false; // 如果数据本身没有这个字段值，不应该被激活
  }
  
  return props.modelValue == itemValue;
};

// 处理选择
const handleSelect = (item) => {
  const value = item[props.keyField];
  // 如果选中的值为空，则传递空字符串
  const emitValue = (value === null || value === undefined) ? '' : value;
  emit('update:modelValue', emitValue);
  emit('change', emitValue, item);
};
</script>

<style scoped lang="scss">
/* ========== 筛选卡片区域 ========== */
.filter-section {
  background: white;
  border-radius: 16px;
  padding: 28px;
  margin-bottom: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  animation: slideUp 0.7s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.section-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #f1f3f4;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 1.4rem;
  font-weight: 700;
  color: #202124;
  margin: 0;
  
  .title-icon {
    font-size: 1.6rem;
  }
}

/* 筛选卡片网格 */
.filter-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.filter-card {
  display: flex;
  align-items: center;
  gap: 12px;
  background: white;
  border: 2px solid #e8eaed;
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 4px;
    background: linear-gradient(180deg, #1a73e8 0%, #34a853 100%);
    transform: scaleY(0);
    transition: transform 0.3s ease;
  }
  
  &:hover {
    transform: translateX(4px);
    box-shadow: 0 4px 16px rgba(26, 115, 232, 0.15);
    border-color: #1a73e8;
    
    &::before {
      transform: scaleY(1);
    }
  }
  
  &.active {
    background: linear-gradient(135deg, #e8f4fd 0%, #f0f8ff 100%);
    border-color: #1a73e8;
    box-shadow: 0 4px 16px rgba(26, 115, 232, 0.2);
    
    &::before {
      transform: scaleY(1);
    }
    
    .item-name {
      color: #1a73e8;
    }
  }
}

.item-icon {
  font-size: 2rem;
  flex-shrink: 0;
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-size: 1rem;
  font-weight: 600;
  color: #202124;
  margin-bottom: 4px;
  transition: color 0.3s;
}

.item-desc {
  font-size: 0.85rem;
  color: #5f6368;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-check {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #1a73e8 0%, #34a853 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9rem;
  font-weight: bold;
  animation: checkPop 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes checkPop {
  0% {
    transform: scale(0);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

/* ========== 响应式设计 ========== */
@media (max-width: 768px) {
  .filter-section {
    padding: 20px;
  }
  
  .filter-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}
</style>

