<template>
  <div class="module-container">
    <div class="page-head">
      <div class="page-title">错题详情</div>
      <el-button class="back-btn" plain @click="emit('close')">
        <span class="back-icon">←</span>
        <span>返回</span>
      </el-button>
    </div>

    <section class="card-section">
      <div class="section-head">题目</div>
      <div v-if="question" class="question-wrap">
        <div class="q-meta">
          <el-tag size="small" type="info">{{ subjectLabel }}</el-tag>
          <el-tag size="small" type="info">{{ typeLabel }}</el-tag>
          <el-tag size="small" type="info">{{ question.knowledgePoint || "未设置知识点" }}</el-tag>
          <el-tag size="small" :type="difficultyTagType">{{ difficultyLabel }}</el-tag>
        </div>

        <div class="q-stem rich-text-content" v-html="formatRichText(question.examQuestionName || '-')"></div>

        <div v-if="shouldShowOptions && parsedOptions.length" class="options">
          <div v-for="opt in parsedOptions" :key="opt.key" class="opt-row">
            <span class="opt-key">{{ opt.key }}.</span>
            <span class="opt-text">{{ opt.text }}</span>
          </div>
        </div>

        <div class="answer-card">
          <div class="row">
            <span class="k">{{ isSubjective ? "参考答案" : "正确答案" }}：</span>
            <span class="v">{{ normalizedAnswer }}</span>
          </div>
          <div class="row" v-if="question.examQuestionAnalysis">
            <span class="k">解析：</span>
            <span class="v rich-text-content" v-html="formatRichText(question.examQuestionAnalysis)"></span>
          </div>
        </div>
      </div>
      <el-empty v-else description="未找到题目信息" />
    </section>

    <section class="card-section">
      <div class="section-head">统计数据</div>
      <div class="stats-grid">
        <div class="stat-card">
          <div class="label">总错误次数</div>
          <div class="value">{{ totalWrongCount }}</div>
        </div>
        <div class="stat-card">
          <div class="label">错误人数</div>
          <div class="value">{{ wrongUserCount }}</div>
        </div>
        <div class="stat-card">
          <div class="label">总错误率</div>
          <div class="value red">{{ formatPercent(totalWrongRate) }}</div>
          <div class="sub">按全局参与考生数计算：{{ globalUserCount }}</div>
        </div>
        <div class="stat-card">
          <div class="label">出现考试数</div>
          <div class="value">{{ examRows.length }}</div>
        </div>
      </div>
    </section>

    <section class="card-section">
      <div class="section-head">出现过的考试</div>
      <el-table :data="examRows" border stripe>
        <el-table-column label="考试名称" min-width="220" align="center" header-align="center">
          <template #default="{ row }">
            {{ row.examName || "-" }}
          </template>
        </el-table-column>
        <el-table-column label="错误次数" width="110" align="center" header-align="center" prop="wrongCount" />
        <el-table-column label="错误人数" width="110" align="center" header-align="center" prop="wrongUserCount" />
        <el-table-column label="最近错误时间" min-width="170" align="center" header-align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.lastWrongTime) }}
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!examRows.length" description="暂无明细数据" />
    </section>
  </div>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
  question: { type: Object, default: null },
  records: { type: Array, default: () => [] },
  globalUserCount: { type: Number, default: 1 },
  examNameByInfoId: { type: Object, default: null }, // Map: exam_info_id -> exam_name
  examRecordToInfoMap: { type: Object, default: null }, // Map: exam_record_id -> exam_info_id
  kemuOptions: { type: Array, default: () => [] },
});

const emit = defineEmits(["close"]);

const formatRichText = (html) => {
  const text = String(html || "");
  if (!text) return "";
  return text
    .replace(/<script[\s\S]*?>[\s\S]*?<\/script>/gi, "")
    .replace(/<iframe[\s\S]*?>[\s\S]*?<\/iframe>/gi, "")
    .replace(/<img([^>]*?)style=""/gi, "<img$1");
};

const formatDateTime = (value) => {
  if (value === null || value === undefined || value === "") return "-";
  let v = value;
  // 时间戳（毫秒）兼容：number 或 数字字符串
  if (typeof v === "string" && /^[0-9]{10,16}$/.test(v.trim())) v = Number(v.trim());
  const date = new Date(v);
  if (Number.isNaN(date.getTime())) return String(value);
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  const hh = String(date.getHours()).padStart(2, "0");
  const mm = String(date.getMinutes()).padStart(2, "0");
  const ss = String(date.getSeconds()).padStart(2, "0");
  return `${y}-${m}-${d} ${hh}:${mm}:${ss}`;
};

const typeLabel = computed(() => {
  const t = Number(props.question?.examQuestionTypes);
  if (t === 1) return "单选题";
  if (t === 2) return "多选题";
  if (t === 3) return "判断题";
  if (t === 4) return "填空题";
  if (t === 5) return "简答题";
  return "-";
});

const subjectLabel = computed(() => {
  const kemuValue = Number(props.question?.kemuTypes);
  if (!kemuValue) return "-";
  const hit = (props.kemuOptions || []).find((item) => Number(item.value) === kemuValue);
  return hit?.label || props.question?.kemuTypesName || "-";
});

const difficultyLabel = computed(() => {
  const d = Number(props.question?.difficultyLevel);
  if (d === 1) return "简单";
  if (d === 2) return "中等";
  if (d === 3) return "困难";
  return "-";
});

const difficultyTagType = computed(() => {
  const d = Number(props.question?.difficultyLevel);
  if (d === 1) return "success";
  if (d === 2) return "warning";
  if (d === 3) return "danger";
  return "info";
});

const isSubjective = computed(() => Number(props.question?.examQuestionTypes) === 5);
const shouldShowOptions = computed(() => [1, 2, 3].includes(Number(props.question?.examQuestionTypes)));

const parseQuestionOptions = (raw) => {
  if (!raw) return [];
  try {
    const obj = typeof raw === "string" ? JSON.parse(raw) : raw;
    if (Array.isArray(obj)) {
      return obj
        .map((it, idx) => {
          const key = (it?.label || it?.key || it?.option || it?.name || it?.code || String.fromCharCode(65 + idx)).toString().trim();
          const text = (it?.value || it?.text || it?.content || it?.labelText || it?.nameText || "").toString().trim();
          return { key: String(key).replace(/[.、:：\s]+$/g, ""), text: text || "" };
        })
        .filter((x) => x.key && x.text);
    }
    if (obj && typeof obj === "object") {
      return Object.entries(obj)
        .map(([k, v]) => ({ key: String(k).replace(/[.、:：\s]+$/g, ""), text: String(v || "").trim() }))
        .filter((x) => x.key && x.text);
    }
  } catch (e) {
    // ignore
  }
  const lines = String(raw)
    .split(/\r?\n/)
    .map((s) => s.trim())
    .filter(Boolean);
  return lines
    .map((line) => {
      const m = line.match(/^([A-Z])[\.\、\s]+(.+)$/i);
      if (!m) return null;
      return { key: m[1].toUpperCase(), text: (m[2] || "").trim() };
    })
    .filter(Boolean);
};

const parsedOptions = computed(() => parseQuestionOptions(props.question?.examQuestionOptions));

const normalizedAnswer = computed(() => {
  const t = Number(props.question?.examQuestionTypes);
  const a = props.question?.examQuestionAnswer;
  if (t === 2 && Array.isArray(a)) return a.join(",");
  return String(a ?? "-");
});

const formatPercent = (value) => `${(Number(value || 0) * 100).toFixed(2)}%`;

const totalWrongCount = computed(() => (props.records || []).length);
const wrongUserCount = computed(() => new Set((props.records || []).map((r) => Number(r?.usersId ?? r?.users_id ?? 0)).filter(Boolean)).size);
const totalWrongRate = computed(() => wrongUserCount.value / (Number(props.globalUserCount) || 1));

const toTime = (value) => {
  if (!value) return null;
  let v = value;
  if (typeof v === "string" && /^[0-9]{10,16}$/.test(v.trim())) v = Number(v.trim());
  const t = new Date(v).getTime();
  return Number.isNaN(t) ? null : t;
};

const examRows = computed(() => {
  const map = new Map();
  (props.records || []).forEach((r) => {
    // 先获取 exam_record_id
    const recordId = Number(r?.examRecordId ?? r?.exam_record_id ?? 0) || null;
    
    // 通过 exam_record_id 获取 exam_info_id
    const examInfoId = recordId && props.examRecordToInfoMap?.get 
      ? props.examRecordToInfoMap.get(recordId) 
      : null;
    
    // 使用 exam_info_id 作为key，确保同一试卷的不同考试能够分开统计
    const key = examInfoId || `record_${recordId}`;
    
    // 获取考试名称：从 examNameByInfoId Map 中获取
    let examName = "-";
    if (examInfoId && props.examNameByInfoId?.get) {
      examName = props.examNameByInfoId.get(examInfoId) || "-";
    }
    
    if (!map.has(key)) {
      map.set(key, {
        examInfoId,
        examRecordId: recordId,
        examName,
        wrongCount: 0,
        wrongUserSet: new Set(),
        lastWrongTime: "",
      });
    }
    const it = map.get(key);
    it.wrongCount += 1;
    const uid = Number(r?.usersId ?? r?.users_id ?? 0) || null;
    if (uid) it.wrongUserSet.add(uid);
    const cur = toTime(r?.insertTime);
    const prev = toTime(it.lastWrongTime);
    if (cur && (!prev || cur > prev)) it.lastWrongTime = r?.insertTime || "";
  });
  return Array.from(map.values())
    .map((it) => ({
      examInfoId: it.examInfoId,
      examRecordId: it.examRecordId,
      examName: it.examName,
      wrongCount: it.wrongCount,
      wrongUserCount: it.wrongUserSet.size,
      lastWrongTime: it.lastWrongTime,
    }))
    .sort((a, b) => (toTime(b.lastWrongTime) || 0) - (toTime(a.lastWrongTime) || 0));
});
</script>

<style lang="scss" scoped>
.page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.page-title {
  font-size: 30px;
  font-weight: 700;
  color: #303133;
}

.back-btn {
  min-width: 104px;
  height: 36px;
  border-radius: 6px;
  border-color: #dbe6f3;
  color: #2f8cff;
  font-size: 16px;
  font-weight: 500;
  padding: 0 18px;
}

.back-icon {
  margin-right: 6px;
  font-size: 18px;
  line-height: 1;
}

.card-section {
  margin-bottom: 14px;
  padding: 16px;
  background: var(--card-bg);
  border-radius: var(--card-border-radius);
  box-shadow: var(--card-shadow);
  border-top: var(--card-border-top);
  border-right: var(--card-border-right);
  border-bottom: var(--card-border-bottom);
  border-left: var(--card-border-left);
}

.section-head {
  border-left: 3px solid #409eff;
  padding-left: 10px;
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 14px;
}

.q-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
}

.q-stem {
  line-height: 1.9;
  color: #303133;
}

.options {
  margin-top: 10px;
  display: grid;
  gap: 8px;
  padding-left: 10px;
}

.opt-row {
  display: grid;
  grid-template-columns: 30px 1fr;
  gap: 8px;
  align-items: start;
  color: #606266;
}

.opt-key {
  color: #409eff;
  font-weight: 700;
}

.answer-card {
  margin-top: 14px;
  border: 1px solid #e8edf4;
  border-radius: 10px;
  padding: 12px 14px;
  background: #f9fbff;
  display: grid;
  gap: 10px;
}

.row {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 10px;
  align-items: start;
}

.k {
  font-weight: 700;
  color: #606266;
  white-space: nowrap;
}

.v {
  color: #303133;
  white-space: pre-wrap;
  line-height: 1.8;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(180px, 1fr));
  gap: 12px;
}

.stat-card {
  padding: 14px 16px;
  border-radius: 10px;
  background: #fff;
  border: 1px solid #edf0f5;
}

.stat-card .label {
  color: #5f6b7a;
  font-size: 14px;
  font-weight: 600;
}

.stat-card .value {
  margin-top: 6px;
  color: #1f2d3d;
  font-size: 30px;
  font-weight: 800;
  line-height: 1.1;
}

.stat-card .value.red {
  color: #f56c6c;
}

.stat-card .sub {
  margin-top: 6px;
  color: #909399;
  font-size: 12px;
}

.rich-text-content {
  line-height: 1.8;
  color: #303133;
}

.rich-text-content :deep(p) {
  margin: 0 0 8px;
}

.rich-text-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 6px;
  margin: 6px 0;
}

.rich-text-content :deep(table) {
  width: 100% !important;
  border-collapse: collapse;
  margin: 10px 0;
  border: 1px solid #dcdfe6;
  background: #fff;
}

.rich-text-content :deep(th),
.rich-text-content :deep(td) {
  border: 1px solid #dcdfe6;
  padding: 8px 10px;
  text-align: left;
  font-size: 14px;
  color: #303133;
}

.rich-text-content :deep(thead tr) {
  background: #f5f7fa;
}

@media (max-width: 1100px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(180px, 1fr));
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>

