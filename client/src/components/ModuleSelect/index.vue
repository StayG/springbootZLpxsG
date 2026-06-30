<template>
  <el-select :modelValue="modelValue" @update:modelValue="emit('update:modelValue', $event)" :placeholder="placeholder" :disabled="disabled" clearable>
    <el-option v-for="item in dataList" :label="getSelectText(item)" :value="getSelectKey(item)" :key="getSelectKey(item)"></el-option>
  </el-select>
</template>

<script setup lang="ts">
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
  request.get(props.url).then(({ data }) => {
    dataList.value = data;
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
