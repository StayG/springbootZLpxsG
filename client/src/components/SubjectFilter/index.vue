<template>
  <div class="subject-filter">
    <div class="filter-label">科目筛选：</div>
    <div class="filter-tags">
      <button
        class="filter-tag"
        :class="{ active: selectedSubject === null }"
        @click="handleSelect(null)"
      >
        全部
      </button>
      <button
        v-for="item in subjects"
        :key="item.value"
        class="filter-tag"
        :class="{ active: selectedSubject === item.value }"
        @click="handleSelect(item.value)"
      >
        {{ item.label }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';

const props = defineProps({
  subjects: {
    type: Array,
    default: () => []
  },
  modelValue: {
    type: [Number, String, null],
    default: null
  }
});

const emit = defineEmits(['update:modelValue', 'change']);

const selectedSubject = ref(props.modelValue);

watch(() => props.modelValue, (newVal) => {
  selectedSubject.value = newVal;
});

const handleSelect = (value) => {
  selectedSubject.value = value;
  emit('update:modelValue', value);
  emit('change', value);
};
</script>

<style lang="scss" scoped>
.subject-filter {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-top: 16px;
  flex-wrap: wrap;
}

.filter-label {
  font-size: 0.95rem;
  font-weight: 600;
  color: #333;
  white-space: nowrap;
}

.filter-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  flex: 1;
}

.filter-tag {
  padding: 8px 20px;
  border: 1px solid #d0d0d0;
  background: #ffffff;
  color: #666;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
  
  &:hover {
    border-color: var(--content-theme-color, #2c3e50);
    color: var(--content-theme-color, #2c3e50);
    background: color-mix(in srgb, var(--content-theme-color, #2c3e50) 5%, transparent);
  }
  
  &.active {
    background: var(--content-theme-color, #2c3e50);
    color: #ffffff;
    border-color: var(--content-theme-color, #2c3e50);
    box-shadow: 0 2px 8px color-mix(in srgb, var(--content-theme-color, #2c3e50) 25%, transparent);
  }
  
  &:active {
    transform: scale(0.96);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .subject-filter {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .filter-tags {
    width: 100%;
  }
  
  .filter-tag {
    padding: 7px 16px;
    font-size: 0.85rem;
  }
}
</style>
