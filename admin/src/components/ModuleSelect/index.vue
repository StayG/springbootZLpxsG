<template>
  <el-select :modelValue="modelValue" @update:modelValue="emit('update:modelValue', $event)" :placeholder="placeholder" :disabled="disabled" clearable>
    <el-option v-for="item in dataList" :label="getSelectText(item)" :value="getSelectKey(item)" :key="getSelectKey(item)"></el-option>
  </el-select>
</template>

<script setup>
import { onMounted, ref } from "vue";
import request from "@/utils/request";
const dataList = ref([]);
const emit = defineEmits(["update:modelValue"]);
const props = defineProps({
  modelValue: {
    type: [Number, String],
  },
  titleName: {
    type: String,
    default: "title",
  },
  url: {
    type: String,
    required: true,
  },
  optionKey: {
    type: String,
    default: "id",
  },
  placeholder: {
    type: String,
    default: "请选择", 
  },
  disabled: {
    type: Boolean,
    default: false,
  }
});
const getModuleList = () => {
  // 解析 URL 中的查询参数
  const urlParts = props.url.split('?');
  const urlPath = urlParts[0];
  const params = {};
  
  if (urlParts[1]) {
    urlParts[1].split('&').forEach(pair => {
      const [key, value] = pair.split('=');
      params[key] = value;
    });
  }

  // 使用 POST 请求发送 JSON 参数
  request.post(urlPath, params).then(({ data }) => {
    // 兼容 PageUtils 返回结构
    if (data && data.list) {
      dataList.value = data.list;
    } else if (Array.isArray(data)) {
      dataList.value = data;
    } else {
      dataList.value = [];
    }
  });
};
const getSelectText = (item) => {
  return props.titleName ? item[props.titleName] : item.title;
};
const getSelectKey = (item) => {
  return props.optionKey ? item[props.optionKey] : item.id;
};
onMounted(() => {
  getModuleList();
});
</script>

<style scoped></style>
