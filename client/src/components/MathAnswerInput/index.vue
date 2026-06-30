<template>
  <div class="math-answer-input">
    <div v-if="toolbarButtons.length && !disabled" class="formula-toolbar-wrap">
      <p class="formula-toolbar-hint">常用符号（鼠标悬停可看插入说明）</p>
      <div class="formula-toolbar" role="toolbar" aria-label="公式快捷插入">
      <button
        v-for="(btn, i) in toolbarButtons"
        :key="i"
        type="button"
        class="formula-btn"
        :title="buttonTooltip(btn)"
        @mousedown.prevent
        @click="insertSnippet(btn)"
      >
        {{ btn.label }}
      </button>
      </div>
    </div>
    <el-input
      ref="inputRef"
      :model-value="modelValue"
      type="textarea"
      :rows="rows"
      :placeholder="placeholder"
      :disabled="disabled"
      resize="none"
      @update:model-value="$emit('update:modelValue', $event)"
      @focus="$emit('focus')"
    />
    <div class="math-preview-wrap">
      <span class="math-preview-label">公式预览</span>
      <div ref="previewRef" class="math-preview" />
      <p v-if="previewHint" class="math-preview-hint">{{ previewHint }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick, computed } from "vue";
import renderMathInElement from "katex/contrib/auto-render";
import "katex/dist/katex.min.css";
import { getFormulaToolbarButtons } from "@/utils/examFormulaToolbar.js";

const props = defineProps({
  modelValue: { type: String, default: "" },
  disabled: { type: Boolean, default: false },
  rows: { type: Number, default: 5 },
  /** 与试题表 formula_type 一致：none | math | physics | chemistry | biology | geography */
  formulaType: { type: String, default: "none" },
  placeholder: {
    type: String,
    default:
      "请输入答案；数学公式可用 $行内$ 或 $$独立公式$$，亦支持 \\(\\)、\\[\\]",
  },
});

const emit = defineEmits(["update:modelValue", "focus"]);

const inputRef = ref(null);
const previewRef = ref(null);
const previewHint = ref("");

const toolbarButtons = computed(() => getFormulaToolbarButtons(props.formulaType));

const buttonTooltip = (btn) => {
  if (btn.tip) return btn.tip;
  if (btn.title) return btn.title;
  const code = (btn.insert || "").replace(/\n/g, "⏎");
  return code ? `点击插入：${code}` : "";
};

const KATEX_OPTS = {
  delimiters: [
    { left: "$$", right: "$$", display: true },
    { left: "$", right: "$", display: false },
    { left: "\\(", right: "\\)", display: false },
    { left: "\\[", right: "\\]", display: true },
  ],
  throwOnError: false,
  strict: "ignore",
  trust: true,
};

const runPreview = async () => {
  await nextTick();
  const el = previewRef.value;
  if (!el) return;
  const raw = props.modelValue ?? "";
  el.textContent = raw;
  previewHint.value = "";
  if (!raw.trim()) {
    return;
  }
  try {
    renderMathInElement(el, KATEX_OPTS);
  } catch (e) {
    previewHint.value = "部分公式无法渲染，请检查 LaTeX 语法";
    console.warn("KaTeX preview", e);
  }
};

watch(() => props.modelValue, runPreview, { immediate: true });

const insertSnippet = (btn) => {
  const ta = inputRef.value?.textarea;
  if (!ta || props.disabled) return;

  const v = props.modelValue ?? "";
  const start = ta.selectionStart ?? v.length;
  const end = ta.selectionEnd ?? start;
  const ins = btn.insert ?? "";
  const before = v.slice(0, start);
  const after = v.slice(end);
  const next = before + ins + after;
  emit("update:modelValue", next);

  const caret =
    btn.caretInInsert != null && typeof btn.caretInInsert === "number"
      ? start + btn.caretInInsert
      : start + ins.length;

  nextTick(() => {
    ta.focus();
    const pos = Math.max(0, Math.min(caret, next.length));
    ta.setSelectionRange(pos, pos);
  });
};
</script>

<style lang="scss" scoped>
.math-answer-input {
  width: 100%;
}

.formula-toolbar-wrap {
  margin-bottom: 8px;
}

.formula-toolbar-hint {
  margin: 0 0 6px;
  font-size: 12px;
  color: #64748b;
}

.formula-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 8px 10px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  max-height: 88px;
  overflow-y: auto;
}

.formula-btn {
  padding: 5px 10px;
  font-size: 13px;
  font-family: system-ui, "Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  background: #fff;
  color: #1e293b;
  cursor: pointer;
  line-height: 1.35;
  min-height: 30px;
}

.formula-btn:hover {
  border-color: #6366f1;
  color: #4338ca;
  background: #eef2ff;
}

.math-preview-wrap {
  margin-top: 10px;
}

.math-preview-label {
  display: block;
  font-size: 12px;
  color: #64748b;
  margin-bottom: 6px;
}

.math-preview {
  min-height: 36px;
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px dashed #c7d2fe;
  background: #f8fafc;
  font-size: 15px;
  line-height: 1.65;
  color: #1f2533;
  word-break: break-word;

  :deep(.katex) {
    font-size: 1.05em;
  }

  :deep(.katex-display) {
    margin: 0.6em 0;
  }
}

.math-preview-hint {
  margin: 6px 0 0;
  font-size: 12px;
  color: #c2410c;
}
</style>

<style lang="scss">
.math-answer-input {
  :deep(.el-textarea__inner) {
    border-radius: 8px;
    border: 1px solid #dfe5f2;
    padding: 12px;
    min-height: 120px;
    font-size: 15px;
    line-height: 1.7;
  }
}
</style>
