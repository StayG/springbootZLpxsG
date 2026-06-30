<template>
  <div class="redo-page" :style="{ '--redo-font-size': `${settings.fontPx}px` }">
    <header class="redo-header">
      <button type="button" class="icon-btn" aria-label="返回" @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
      </button>
      <h1 class="redo-title">重做错题</h1>
    </header>

    <div v-if="loading" class="redo-loading">
      <el-icon class="spin"><Loading /></el-icon>
      <span>加载题目中…</span>
    </div>

    <template v-else-if="questions.length">
      <div class="redo-status">
        <span class="status-text">
          <template v-if="showMultiQuestionNav">共 {{ questions.length }} 题，当前第 {{ currentIndex + 1 }} 题</template>
          <template v-else>单题重做：提交后可查看判分与解析</template>
        </span>
        <div class="status-actions">
          <button type="button" class="link-btn gear" @click="settingsVisible = true">
            <el-icon><Setting /></el-icon>
            答题设置
          </button>
          <button type="button" class="link-btn plain exit-redo-btn" @click="handleExit">退出重做</button>
        </div>
      </div>

      <div class="redo-scroll" :class="{ 'redo-scroll--single': !showMultiQuestionNav }">
        <section class="question-card">
          <div class="card-head">
            <span class="type-tag">{{ typeName }}</span>
          </div>
          <div class="q-index-title">
            <span class="num">{{ currentIndex + 1 }}.</span>
            <div class="q-html" v-html="current.examQuestionName || '—'" />
          </div>

          <!-- 客观选项 -->
          <div v-if="isChoiceType" class="options">
            <div
              v-for="opt in parsedOptions"
              :key="opt.code"
              class="opt-row"
              :class="{ selected: isOptionSelected(opt.code), multi: current.examQuestionTypes === 2 }"
              role="button"
              tabindex="0"
              @click="onOptClick(opt.code)"
              @keyup.enter="onOptClick(opt.code)"
            >
              <span class="opt-disc" />
              <span class="opt-label">{{ opt.code }}. {{ opt.text }}</span>
            </div>
          </div>

          <!-- 填空 / 简答 -->
          <div v-else class="text-block">
            <el-input
              v-model="textDraft"
              type="textarea"
              :rows="6"
              placeholder="请输入作答内容"
              resize="vertical"
              class="redo-textarea"
              @blur="commitTextAnswer"
            />
          </div>
        </section>

        <div v-if="!isCurrentSubmitted" class="submit-bar">
          <button
            type="button"
            class="submit-answer-btn"
            :disabled="!canSubmitCurrent || submitting"
            @click="submitCurrent"
          >
            {{ submitting ? "提交中…" : "提交作答" }}
          </button>
          <p v-if="!canSubmitCurrent && !submitting" class="submit-hint">请先完成本题作答后再提交</p>
        </div>

        <template v-if="isCurrentSubmitted && currentFeedback">
          <div
            v-if="currentFeedback.objective"
            class="feedback-row verdict-row"
            :class="currentFeedback.correct ? 'is-right' : 'is-wrong'"
          >
            <span class="pill">{{ currentFeedback.correct ? "回答正确" : "回答错误" }}</span>
            <span v-if="currentFeedback.correct" class="result ok">
              <el-icon><CircleCheck /></el-icon>
            </span>
            <span v-else class="result bad">
              <el-icon><CircleClose /></el-icon>
            </span>
          </div>
          <div v-else class="feedback-row verdict-row is-neutral">
            <span class="pill">已提交</span>
            <span class="neutral-hint">本题不自动判分，请对照参考答案与解析自评</span>
          </div>

          <div class="feedback-row detail-row">
            <span class="pill">你的答案</span>
            <span class="val">{{ formatAnswerShown(currentFeedback.yourAnswer) }}</span>
          </div>
          <div v-if="currentFeedback.objective" class="feedback-row detail-row">
            <span class="pill">正确答案</span>
            <span class="val ok">{{ formatAnswerShown(currentFeedback.correctAnswer) }}</span>
          </div>
          <div v-else class="feedback-row detail-row">
            <span class="pill">参考答案</span>
            <span class="val ok">{{ formatAnswerShown(currentFeedback.referenceAnswer) }}</span>
          </div>

          <section class="analysis-card">
            <span class="pill">解析</span>
            <div class="analysis-body" v-html="formatAnalysisHtml(currentFeedback.analysis)" />
          </section>

          <section class="history-card">
            <div class="history-header" @click="toggleHistory">
              <span class="pill">查看历史作答</span>
              <el-icon class="toggle-icon" :class="{ expanded: historyExpanded[currentIndex] }">
                <DArrowRight />
              </el-icon>
            </div>
            <div v-if="historyExpanded[currentIndex]" class="history-body">
              <div v-if="historyLoading[currentIndex]" class="history-loading">加载中…</div>
              <template v-else-if="historyRecords[currentIndex] && historyRecords[currentIndex].length">
                <div v-for="(record, idx) in historyRecords[currentIndex]" :key="record.id" class="history-item">
                  <div class="history-item-header">
                    <span class="history-index">第 {{ idx + 1 }} 次错误</span>
                    <span class="history-time">{{ formatHistoryTime(record.insertTime) }}</span>
                  </div>
                  <div class="history-item-body">
                    <div class="history-row">
                      <span class="history-label">来源：</span>
                      <span class="history-val">{{ record.examName || record.examPaperName || "-" }}</span>
                    </div>
                    <div class="history-row">
                      <span class="history-label">你的答案：</span>
                      <span class="history-val wrong">{{ formatAnswerShown(record.examDetailsMyanswer) }}</span>
                    </div>
                    <div v-if="record.examDetailsMyscore != null" class="history-row">
                      <span class="history-label">得分：</span>
                      <span class="history-val">{{ record.examDetailsMyscore }} 分</span>
                    </div>
                  </div>
                </div>
              </template>
              <div v-else class="history-empty">暂无历史记录</div>
            </div>
          </section>
        </template>
      </div>

      <footer v-if="showMultiQuestionNav" class="redo-footer">
        <button type="button" class="footer-prev" :disabled="currentIndex <= 0" @click="prev">
          <el-icon><DArrowLeft /></el-icon>
          上一题
        </button>
        <span class="footer-progress">{{ currentIndex + 1 }} / {{ questions.length }}</span>
        <div class="footer-right">
          <button type="button" class="link-btn plain" @click="jumpVisible = true">跳题</button>
          <button type="button" class="footer-next" @click="next">
            下一题
            <el-icon><DArrowRight /></el-icon>
          </button>
        </div>
      </footer>
    </template>

    <div v-else class="redo-empty">暂无题目数据</div>

    <el-dialog v-model="settingsVisible" title="答题设置" width="400px" append-to-body>
      <div class="setting-row">
        <span>字体大小</span>
        <el-radio-group v-model="settings.fontPx">
          <el-radio-button :label="14">小</el-radio-button>
          <el-radio-button :label="16">中</el-radio-button>
          <el-radio-button :label="18">大</el-radio-button>
        </el-radio-group>
      </div>
      <template #footer>
        <el-button type="primary" @click="settingsVisible = false">完成</el-button>
      </template>
    </el-dialog>

    <el-dialog v-if="showMultiQuestionNav" v-model="jumpVisible" title="跳转到题目" width="420px" append-to-body>
      <div class="jump-grid">
        <button
          v-for="(_, i) in questions"
          :key="i"
          type="button"
          class="jump-cell"
          :class="{ active: i === currentIndex }"
          @click="jumpTo(i)"
        >
          {{ i + 1 }}
        </button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  ArrowLeft,
  CircleCheck,
  CircleClose,
  DArrowLeft,
  DArrowRight,
  Loading,
  Setting,
} from "@element-plus/icons-vue";
import request from "@/utils/request";

const WRONG_REDO_STORAGE_KEY = "studentWrongRedoIds";

const router = useRouter();

const loading = ref(true);
const questions = ref([]);
const currentIndex = ref(0);
const sessionAnswers = ref({});
const textDraft = ref("");
const settingsVisible = ref(false);
const jumpVisible = ref(false);
const settings = ref({ fontPx: 16 });
/** 本题是否已在重做流程中提交（提交后才显示正确答案与解析） */
const submittedForQuestion = ref({});
const submitting = ref(false);
/** 各题提交后接口返回的判分与展示数据 */
const redoFeedback = ref({});
/** 历史作答记录 */
const historyRecords = ref({});
const historyLoading = ref({});
const historyExpanded = ref({});

const current = computed(() => questions.value[currentIndex.value] || {});

/** 错题集「重做」仅传入一题；多题时保留上一题/下一题/跳题（如日后批量入口） */
const showMultiQuestionNav = computed(() => questions.value.length > 1);

const isCurrentSubmitted = computed(() => !!submittedForQuestion.value[currentIndex.value]);

const currentFeedback = computed(() => redoFeedback.value[currentIndex.value] || null);

const canSubmitCurrent = computed(() => {
  if (isCurrentSubmitted.value) return false;
  if (isChoiceType.value) {
    const raw = sessionAnswerRaw();
    return raw != null && String(raw).trim() !== "";
  }
  const raw = sessionAnswers.value[currentIndex.value];
  return raw != null && String(raw).trim() !== "";
});

const typeName = computed(() => current.value.examQuestionTypesName || typeLabel(current.value.examQuestionTypes));

const isChoiceType = computed(() => {
  const t = Number(current.value.examQuestionTypes);
  return t === 1 || t === 2 || t === 3;
});

const parsedOptions = computed(() => parseOptions(current.value));

function typeLabel(t) {
  const n = Number(t);
  if (n === 1) return "单选题";
  if (n === 2) return "多选题";
  if (n === 3) return "判断题";
  if (n === 4) return "填空题";
  if (n === 5) return "简答题";
  return "题目";
}

function parseOptions(item) {
  if (!item?.examQuestionOptions) return [];
  try {
    const rawOptions = JSON.parse(item.examQuestionOptions);
    if (Array.isArray(rawOptions) && rawOptions.every((opt) => typeof opt === "object" && opt && "code" in opt && "text" in opt)) {
      return rawOptions;
    }
    if (Array.isArray(rawOptions)) {
      return rawOptions.map((opt, index) => {
        const s = String(opt);
        const match = s.match(/^([A-Za-z])./);
        if (match) {
          return { code: match[1], text: s.substring(2).trim() };
        }
        return { code: String.fromCharCode(65 + index), text: s.trim() };
      });
    }
  } catch {
    /* ignore */
  }
  return [];
}

function sessionAnswerRaw() {
  return sessionAnswers.value[currentIndex.value];
}

function isOptionSelected(code) {
  const t = Number(current.value.examQuestionTypes);
  const raw = sessionAnswerRaw();
  if (t === 2) {
    const set = new Set(String(raw || "").split(/[,，]/).map((x) => x.trim()).filter(Boolean));
    return set.has(String(code));
  }
  return String(raw || "") === String(code);
}

function onOptClick(code) {
  const t = Number(current.value.examQuestionTypes);
  const c = String(code);
  if (t === 2) {
    const set = new Set(String(sessionAnswerRaw() || "").split(/[,，]/).map((x) => x.trim()).filter(Boolean));
    if (set.has(c)) set.delete(c);
    else set.add(c);
    sessionAnswers.value = {
      ...sessionAnswers.value,
      [currentIndex.value]: [...set].sort().join(","),
    };
    return;
  }
  sessionAnswers.value = { ...sessionAnswers.value, [currentIndex.value]: c };
}

function commitTextAnswer() {
  sessionAnswers.value = { ...sessionAnswers.value, [currentIndex.value]: textDraft.value.trim() };
}

async function submitCurrent() {
  if (!isChoiceType.value) commitTextAnswer();
  if (!canSubmitCurrent.value || submitting.value) return;
  const wid = current.value.id;
  if (wid == null) {
    ElMessage.error("缺少错题记录编号，无法提交");
    return;
  }
  const redoAnswer = isChoiceType.value
    ? String(sessionAnswerRaw() ?? "").trim()
    : String(sessionAnswers.value[currentIndex.value] ?? "").trim();
  submitting.value = true;
  try {
    const { data } = await request.post("/examWrongQuestion/redoSubmit", {
      id: wid,
      redoAnswer,
    });
    redoFeedback.value = { ...redoFeedback.value, [currentIndex.value]: data || {} };
    submittedForQuestion.value = { ...submittedForQuestion.value, [currentIndex.value]: true };
  } catch (e) {
    console.error(e);
  } finally {
    submitting.value = false;
  }
}

function formatAnalysisHtml(html) {
  const raw = html != null ? String(html) : "";
  const sanitized = raw
    .replace(/<script[\s\S]*?>[\s\S]*?<\/script>/gi, "")
    .replace(/<iframe[\s\S]*?>[\s\S]*?<\/iframe>/gi, "");
  const plain = sanitized.replace(/<[^>]+>/g, "").trim();
  if (!plain) return "暂无解析";
  return sanitized;
}

function formatAnswerShown(val) {
  const s = val != null ? String(val).trim() : "";
  return s || "—";
}

async function loadHistory() {
  const wid = current.value.id;
  if (wid == null || historyRecords.value[currentIndex.value]) return;
  
  historyLoading.value = { ...historyLoading.value, [currentIndex.value]: true };
  try {
    const { data } = await request.get(`/examWrongQuestion/history/${wid}`);
    historyRecords.value = { ...historyRecords.value, [currentIndex.value]: data || [] };
  } catch (e) {
    console.error(e);
    ElMessage.error("加载历史记录失败");
  } finally {
    historyLoading.value = { ...historyLoading.value, [currentIndex.value]: false };
  }
}

function toggleHistory() {
  const expanded = historyExpanded.value[currentIndex.value];
  if (!expanded) {
    loadHistory();
  }
  historyExpanded.value = { ...historyExpanded.value, [currentIndex.value]: !expanded };
}

function formatHistoryTime(time) {
  if (!time) return "-";
  const d = new Date(time);
  if (isNaN(d.getTime())) return String(time);
  return new Intl.DateTimeFormat("zh-CN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: false,
  }).format(d);
}

function readIdsFromEntry() {
  try {
    const raw = sessionStorage.getItem(WRONG_REDO_STORAGE_KEY);
    if (raw) {
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed) && parsed.length) return parsed.map((x) => Number(x)).filter((n) => Number.isFinite(n));
    }
  } catch {
    /* ignore */
  }
  return [];
}

async function loadQuestions() {
  loading.value = true;
  const ids = readIdsFromEntry();
  if (!ids.length) {
    ElMessage.warning("请从错题集点击「重做」进入");
    router.replace("/index/wrongQuestion");
    loading.value = false;
    return;
  }
  try {
    const results = await Promise.all(ids.map((id) => request.get(`/examWrongQuestion/practice/${id}`)));
    const list = results.map((r) => r?.data).filter(Boolean);
    questions.value = list;
    submittedForQuestion.value = {};
    redoFeedback.value = {};
    currentIndex.value = 0;
    if (!list.length) {
      ElMessage.error("未能加载错题详情");
      router.replace("/index/wrongQuestion");
    }
  } catch (e) {
    console.error(e);
    ElMessage.error("加载失败，请稍后重试");
    router.replace("/index/wrongQuestion");
  } finally {
    loading.value = false;
  }
}

function prev() {
  if (currentIndex.value <= 0) return;
  currentIndex.value -= 1;
}

function next() {
  if (currentIndex.value >= questions.value.length - 1) {
    ElMessage.success("已是最后一题");
    return;
  }
  currentIndex.value += 1;
}

function jumpTo(i) {
  if (i >= 0 && i < questions.value.length) currentIndex.value = i;
  jumpVisible.value = false;
}

function clearRedoSessionEntry() {
  try {
    sessionStorage.removeItem(WRONG_REDO_STORAGE_KEY);
  } catch {
    /* ignore */
  }
}

function isChoiceQuestionType(t) {
  const n = Number(t);
  return n === 1 || n === 2 || n === 3;
}

/**
 * 是否存在「已作答但尚未提交」的内容（含多题中非当前题；当前主观题合并草稿与已同步答案）。
 */
function hasUncommittedAnswerSomewhere() {
  const list = questions.value;
  for (let i = 0; i < list.length; i++) {
    if (submittedForQuestion.value[i]) continue;
    const q = list[i];
    const isChoice = isChoiceQuestionType(q?.examQuestionTypes);
    if (i === currentIndex.value && !isChoice) {
      const draft = String(textDraft.value || "").trim();
      const stored = String(sessionAnswers.value[i] ?? "").trim();
      if (draft !== "" || stored !== "") return true;
      continue;
    }
    const raw = sessionAnswers.value[i];
    if (raw != null && String(raw).trim() !== "") return true;
  }
  return false;
}

/**
 * 离开重做页：有未提交作答时二次确认；否则直接离开并清理入口缓存。
 */
function tryLeaveRedo({ title, confirmButtonText, navigate }) {
  if (!hasUncommittedAnswerSomewhere()) {
    clearRedoSessionEntry();
    navigate();
    return;
  }
  return ElMessageBox.confirm(
    "未提交的作答仅保存在当前页面，离开后将清空。若本题已提交判分，客观题的掌握状态已写入错题记录，可在错题集中查看。再次进入请从错题集点击「重做」。",
    title,
    {
      confirmButtonText,
      cancelButtonText: "继续做题",
      type: "warning",
      distinguishCancelAndClose: true,
    }
  )
    .then(() => {
      clearRedoSessionEntry();
      navigate();
    })
    .catch(() => {
      /* 取消或关闭 */
    });
}

function handleBack() {
  tryLeaveRedo({
    title: "返回",
    confirmButtonText: "离开",
    navigate: () => router.back(),
  });
}

function handleExit() {
  tryLeaveRedo({
    title: "退出重做",
    confirmButtonText: "退出",
    navigate: () => router.push("/index/wrongQuestion"),
  });
}

watch(currentIndex, () => {
  const raw = sessionAnswers.value[currentIndex.value];
  textDraft.value = raw != null ? String(raw) : "";
});

watch(
  () => current.value?.examQuestionId,
  () => {
    const raw = sessionAnswers.value[currentIndex.value];
    textDraft.value = raw != null ? String(raw) : "";
  }
);

onMounted(() => {
  loadQuestions();
});
</script>

<style scoped lang="scss">
.redo-page {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  background: transparent;
  font-size: var(--redo-font-size, 16px);
  color: #2a3140;
}

.redo-header {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0 0 14px;
  padding: 0;
  background: transparent;
  border: none;
  box-shadow: none;
}

.icon-btn {
  border: none;
  background: transparent;
  padding: 6px;
  border-radius: 10px;
  cursor: pointer;
  color: #334155;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 22px;
}
.icon-btn:hover {
  background: #f3f6ff;
  color: #4f46e5;
}

.redo-title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
  color: #2a3140;
}

.redo-loading,
.redo-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #64748b;
}
.spin {
  font-size: 28px;
  color: #4f46e5;
  animation: spin 0.9s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.redo-status {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  margin-bottom: 14px;
  background: #eef2ff;
  border: 1px solid #e0e7ff;
  border-radius: 14px;
  box-shadow: 0 1px 2px rgba(79, 70, 229, 0.06);
}

.status-text {
  font-size: 14px;
  color: #334155;
  font-weight: 500;
}

.status-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.link-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 14px;
  color: #4f46e5;
  padding: 4px 0;
}
.link-btn:hover {
  color: #4338ca;
}
.link-btn.plain {
  color: #64748b;
}
.link-btn.plain:hover {
  color: #4f46e5;
}

/* 在浅紫状态条上避免白底融进背景：用填充 + 深色字保证可辨 */
.link-btn.exit-redo-btn {
  padding: 7px 16px;
  border: 1px solid #818cf8;
  border-radius: 8px;
  background: #e0e7ff;
  color: #3730a3;
  font-weight: 600;
  box-shadow: 0 1px 2px rgba(49, 46, 129, 0.08);
}
.link-btn.exit-redo-btn:hover {
  border-color: #6366f1;
  color: #312e81;
  background: #c7d2fe;
}
.link-btn.exit-redo-btn:active {
  background: #a5b4fc;
  border-color: #4f46e5;
}

.link-btn.gear :deep(svg) {
  font-size: 16px;
}

.redo-scroll {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 0 0 100px;
  box-sizing: border-box;

  &.redo-scroll--single {
    padding-bottom: 24px;
  }
}

.question-card {
  background: #fff;
  border-radius: 14px;
  padding: 16px 18px 20px;
  border: 1px solid #e7ecf4;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
}

.card-head {
  margin-bottom: 14px;
}

.type-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  background: #eef2ff;
  color: #4f46e5;
}

.q-index-title {
  display: flex;
  gap: 6px;
  align-items: flex-start;
  margin-bottom: 16px;
  line-height: 1.65;
}
.q-index-title .num {
  font-weight: 700;
  color: #4f46e5;
  flex-shrink: 0;
}
.q-html {
  flex: 1;
  font-size: var(--redo-font-size, 16px);
}
.q-html :deep(img) {
  max-width: 100%;
  height: auto;
}

.options {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.opt-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin: 0;
  padding: 12px 14px;
  border-radius: 10px;
  border: 1px solid #e7ecf4;
  cursor: pointer;
  transition: background 0.15s ease, border-color 0.15s ease;
}
.opt-row:hover {
  border-color: #c7d2fe;
  background: #fafbff;
}
.opt-row.selected {
  background: #eef2ff;
  border-color: #818cf8;
}

.opt-disc {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid #cbd5e1;
  flex-shrink: 0;
  margin-top: 2px;
  box-sizing: border-box;
}
.opt-row.selected .opt-disc {
  border-color: #4f46e5;
  background: radial-gradient(circle, #4f46e5 0%, #4f46e5 45%, #fff 50%);
}
.opt-row.multi .opt-disc {
  border-radius: 4px;
}
.opt-row.multi.selected .opt-disc {
  background: #4f46e5;
  position: relative;
}
.opt-row.multi.selected .opt-disc::after {
  content: "";
  position: absolute;
  left: 4px;
  top: 1px;
  width: 5px;
  height: 9px;
  border: solid #fff;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.opt-label {
  flex: 1;
  font-size: var(--redo-font-size, 16px);
  color: #334155;
}

.text-block {
  margin-top: 4px;
}
.redo-textarea :deep(.el-textarea__inner) {
  font-size: var(--redo-font-size, 16px);
}
.redo-textarea :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px #4f46e5 inset;
}

.submit-bar {
  margin-top: 14px;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 8px;
}
.submit-answer-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px 18px;
  border-radius: 10px;
  border: none;
  background: #4f46e5;
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 10px 18px rgba(79, 70, 229, 0.22);
}
.submit-answer-btn:hover:not(:disabled) {
  background: #4338ca;
}
.submit-answer-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}
.submit-hint {
  margin: 0;
  font-size: 13px;
  color: #94a3b8;
  text-align: center;
}

.feedback-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px 14px;
  padding: 12px 16px;
  margin-top: 12px;
  border-radius: 10px;
  background: #fff;
  border: 1px solid #e7ecf4;
}

.pill {
  display: inline-flex;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  background: #eef2ff;
  color: #4f46e5;
}

.val {
  font-weight: 700;
  font-size: 16px;
}
.val.wrong {
  color: #dc2626;
}
.val.ok {
  color: #16a34a;
}

.result.bad {
  margin-left: auto;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #dc2626;
  font-size: 14px;
  font-weight: 600;
}

.result.ok {
  margin-left: auto;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #16a34a;
  font-size: 18px;
}

.verdict-row.is-right {
  border-color: #bbf7d0;
  background: #f0fdf4;
}
.verdict-row.is-wrong {
  border-color: #fecaca;
  background: #fef2f2;
}
.verdict-row.is-neutral {
  border-color: #e2e8f0;
  background: #f8fafc;
}
.neutral-hint {
  margin-left: 8px;
  font-size: 13px;
  color: #64748b;
}
.detail-row .pill {
  background: #f1f5f9;
  color: #475569;
}

.analysis-card {
  margin-top: 12px;
  background: #fff;
  border-radius: 14px;
  padding: 14px 16px 18px;
  border: 1px solid #e7ecf4;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
}
.analysis-body {
  margin-top: 12px;
  font-size: var(--redo-font-size, 16px);
  line-height: 1.7;
  color: #475569;
}
.analysis-body :deep(img) {
  max-width: 100%;
  height: auto;
}

.redo-footer {
  flex-shrink: 0;
  position: sticky;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
  padding: 12px 16px;
  padding-bottom: max(12px, env(safe-area-inset-bottom));
  background: #fff;
  border: 1px solid #e7ecf4;
  border-radius: 14px;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
}

.footer-prev {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  border-radius: 10px;
  border: 1px solid #e7ecf4;
  background: #fff;
  color: #475569;
  font-size: 14px;
  cursor: pointer;
}
.footer-prev:hover:not(:disabled) {
  border-color: #c7d2fe;
  color: #4f46e5;
  background: #f3f6ff;
}
.footer-prev:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.footer-progress {
  font-size: 15px;
  font-weight: 700;
  color: #2a3140;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.footer-next {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  border-radius: 10px;
  border: none;
  background: #4f46e5;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 10px 18px rgba(79, 70, 229, 0.22);
}
.footer-next:hover {
  background: #4338ca;
}

.setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.jump-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 8px;
  max-height: 320px;
  overflow-y: auto;
}
.jump-cell {
  border: 1px solid #e2e8f0;
  background: #fff;
  border-radius: 8px;
  padding: 10px 0;
  cursor: pointer;
  font-weight: 600;
  color: #334155;
}
.jump-cell:hover {
  border-color: #c7d2fe;
  color: #4f46e5;
  background: #f3f6ff;
}
.jump-cell.active {
  background: #4f46e5;
  color: #fff;
  border-color: #4f46e5;
  box-shadow: 0 10px 18px rgba(79, 70, 229, 0.22);
}

.history-card {
  margin-top: 12px;
  background: #fff;
  border-radius: 14px;
  padding: 14px 16px;
  border: 1px solid #e7ecf4;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
}

.history-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  user-select: none;
  padding: 4px 0;
}

.history-header:hover {
  opacity: 0.8;
}

.toggle-icon {
  font-size: 16px;
  color: #64748b;
  transition: transform 0.2s ease;
}

.toggle-icon.expanded {
  transform: rotate(90deg);
}

.history-body {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f1f5f9;
}

.history-loading,
.history-empty {
  padding: 12px 0;
  text-align: center;
  color: #94a3b8;
  font-size: 14px;
}

.history-item {
  padding: 12px;
  margin-bottom: 10px;
  background: #f8fafc;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
}

.history-item:last-child {
  margin-bottom: 0;
}

.history-item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e2e8f0;
}

.history-index {
  font-size: 13px;
  font-weight: 600;
  color: #475569;
}

.history-time {
  font-size: 12px;
  color: #94a3b8;
}

.history-item-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.history-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 14px;
}

.history-label {
  flex-shrink: 0;
  color: #64748b;
  font-weight: 500;
}

.history-val {
  flex: 1;
  color: #334155;
  font-weight: 500;
}

.history-val.wrong {
  color: #dc2626;
  font-weight: 600;
}
</style>
