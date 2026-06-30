<template>
  <div class="module-container grading-view">
    <template v-if="isReady">
      <div class="page-head">
        <div class="page-title">阅卷记录详情</div>
        <el-button class="back-btn" plain @click="backToList">
          <span class="back-icon">←</span>
          <span>返回</span>
        </el-button>
      </div>

      <div class="top-card">
        <div class="top-left">
          <div class="title">
            {{ headerInfo.examName || "阅卷详情" }}
            <el-tag v-if="overallStatusTag" :type="overallStatusTag.type" effect="light" class="status-tag">
              {{ overallStatusTag.text }}
            </el-tag>
          </div>
          <div class="meta-row">
            <div class="meta-item"><span class="k">考生姓名：</span><span class="v">{{ headerInfo.nickname || "-" }}</span></div>
            <div class="meta-item"><span class="k">考生账号：</span><span class="v">{{ headerInfo.userName || "-" }}</span></div>
            <div class="meta-item"><span class="k">考试时长：</span><span class="v">{{ headerInfo.durationText || "-" }}</span></div>
            <div class="meta-item"><span class="k">交卷时间：</span><span class="v">{{ headerInfo.endTime || "-" }}</span></div>
            <div class="meta-item score-item"><span class="k">得分：</span><span class="v score">{{ recordScoreDisplay }}</span><span class="v"> / {{ paperFullScoreDisplay }}分</span></div>
          </div>
        </div>
      </div>

      <div class="layout">
        <aside class="sidebar">
          <div class="side-card">
            <div class="side-title">题目列表</div>
            <div class="side-tabs">
              <div class="tab" :class="{ active: activeFilter === 'all' }" @click="activeFilter = 'all'">全部</div>
              <div class="tab" :class="{ active: activeFilter === 'reviewed' }" @click="activeFilter = 'reviewed'">已批阅</div>
              <div class="tab" :class="{ active: activeFilter === 'pending' }" @click="activeFilter = 'pending'">未批阅</div>
            </div>

            <div class="side-groups">
              <div v-for="group in groupedQuestions" :key="group.key" class="group">
                <div class="group-title">{{ group.title }}（{{ group.items.length }}题，共{{ group.totalScore }}分）</div>
                <div class="num-grid">
                  <button
                    v-for="item in group.items"
                    :key="item.__index"
                    class="num"
                    :class="numClass(item)"
                    @click="goToIndex(item.__index)"
                  >
                    {{ item.__displayNo }}
                  </button>
                </div>
              </div>
            </div>

            <div class="legend">
              <div class="dot done" />已批阅
              <div class="dot todo" />未批阅
              <div class="dot cur" />当前题
            </div>
          </div>
        </aside>

        <main class="content">
          <div class="content-card" v-if="current">
            <div class="q-head">
              <div class="q-title">
                {{ currentGroupHeading }}
              </div>
              <div class="q-score">
                <span class="k">题目分值：</span>
                <span class="v">{{ current.examPaperTopicNumber ?? "-" }}分</span>
                <span class="divider">|</span>
                <span class="k">得分：</span>
                <span class="v red">{{ currentScoreText }}分</span>
              </div>
            </div>

            <div class="q-stem" v-if="questionStem">
              <span class="stem-no">{{ current.__displayNo }}.</span>
              <div class="stem-content rich-text-content" v-html="formatRichText(questionStem)" />
            </div>

            <div v-if="isChoiceQuestion(current)" class="options">
              <div
                v-for="opt in parsedOptions"
                :key="opt.key"
                class="opt"
                :class="{
                  correct: isCorrectOption(opt.key),
                  selected: isUserSelectedOption(opt.key),
                }"
              >
                <span class="opt-key">{{ opt.key }}.</span>
                <span class="opt-text">{{ opt.text }}</span>
              </div>
            </div>

            <div class="answer-row">
              <div class="answer-line"><span class="k">考生答案：</span><span class="v student-answer">{{ current.examDetailsMyanswer || "未作答" }}</span></div>
              <div class="answer-line"><span class="k">正确答案：</span><span class="v correct-answer">{{ current.examQuestionAnswer || "-" }}</span></div>
            </div>

            <div class="analysis" v-if="current.examQuestionAnalysis">
              <div class="k">答案解析</div>
              <div class="v rich-text-content" v-html="formatRichText(current.examQuestionAnalysis)" />
            </div>

            <div class="grading-block" v-if="isSubjective(current)">
              <div class="grading-title">阅卷评语（仅简答题、论述题显示）</div>
              <el-input
                v-model="editor.comment"
                type="textarea"
                :rows="4"
                maxlength="200"
                show-word-limit
                :disabled="isSubjectiveReviewed(current)"
                placeholder="请输入评语（选填）"
              />
            </div>
            <div class="grading-block" v-else>
              <div class="grading-title">阅卷评语（仅简答题、论述题显示）</div>
              <el-input type="textarea" :rows="4" disabled placeholder="客观题不支持评语" />
            </div>

            <div class="bottom-bar">
              <div class="bar-left">
                <el-button @click="prev" :disabled="!canPrev">← 上一题</el-button>
                <div class="nav-mid">{{ currentIndex + 1 }} / {{ filteredFlatList.length }}</div>
                <el-button @click="next" :disabled="!canNext">下一题 →</el-button>
              </div>

              <div class="bar-right">
                <template v-if="isSubjective(current)">
                  <el-button class="btn-edit-score" :disabled="isSubjectiveReviewed(current)" @click="toggleEditingScore">
                    {{ editingScore ? "收起改分" : "修改得分" }}
                  </el-button>
                  <el-input-number
                    v-if="editingScore"
                    v-model="editor.score"
                    class="score-input"
                    :min="0"
                    :max="Number(current.examPaperTopicNumber || 10)"
                    :disabled="isSubjectiveReviewed(current)"
                  />
                  <el-button class="btn-save-score" type="primary" :loading="saving" :disabled="isSubjectiveReviewed(current) || (!editingScore && !hasDirty)" @click="save(true)">保存</el-button>
                </template>
                <template v-else>
                  <el-button class="btn-edit-score" disabled>修改得分</el-button>
                  <el-button class="btn-save-score" type="primary" disabled>保存</el-button>
                </template>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无题目" />
        </main>
      </div>
    </template>

    <template v-else>
      <div class="fallback">
        <el-empty description="请从阅卷管理进入阅卷详情页" />
        <el-button type="primary" @click="backToList">返回</el-button>
      </div>
    </template>

    <el-backtop v-if="isReady" target=".content-main .el-scrollbar__wrap" :right="28" :bottom="96" />
  </div>
</template>

<script setup>
import { computed, onBeforeMount, onMounted, reactive, ref, watch } from "vue";
import { onBeforeRouteLeave, useRoute, useRouter } from "vue-router";
import request from "@/utils/request";
import { scrollAdminLayoutToTop } from "@/utils/adminScrollLayout.js";
import { ElMessage, ElMessageBox } from "element-plus";

const route = useRoute();
const router = useRouter();

const examRecordId = route.query.examRecordId;
const examDetailsUuidNumber = route.query.examDetailsUuidNumber;
const isReady = computed(() => !!examRecordId || !!examDetailsUuidNumber);

const detailsList = ref([]);
const paperFullScore = ref(null);
const examDurationMinutes = ref(null);
const examEndTimeText = ref("");
const recordTotalScore = ref(null);
const recordStatus = ref(null);

const activeFilter = ref("all"); // all | reviewed | pending
const currentIndex = ref(0);
const saving = ref(false);
const editingScore = ref(false);

const editor = reactive({
  score: 0,
  comment: "",
  initialScore: 0,
  initialComment: "",
});

const paperFullScoreDisplay = computed(() => {
  const v = Number(paperFullScore.value);
  if (paperFullScore.value === null || paperFullScore.value === undefined) return "-";
  return Number.isNaN(v) ? "-" : v;
});

const headerInfo = computed(() => {
  const first = detailsList.value?.[0] || {};
  const durationValue = examDurationMinutes.value ?? route.query.duration ?? first.duration;
  const durationNum = Number(durationValue);
  const durationText = Number.isFinite(durationNum) && durationNum > 0 ? `${durationNum} 分钟` : "";
  return {
    examName: route.query.examName || first.examName || first.examPaperName,
    examPaperName: route.query.examPaperName || first.examPaperName,
    nickname: route.query.nickname || first.nickname,
    userName: route.query.userName || first.userName || first.user_name,
    endTime: examEndTimeText.value || route.query.endTime || route.query.insertTime || first.endTime || first.insertTime,
    durationText,
  };
});

const overallStatusTag = computed(() => {
  // exam_record.status: 0考试中 1已提交 2强制交卷 3已完成
  const s = Number(recordStatus.value);
  if (s === 3) return { text: "已完成", type: "success" };
  if (s === 2) return { text: "强制交卷", type: "warning" };
  if (s === 1) return { text: "已提交", type: "warning" };
  if (s === 0) return { text: "考试中", type: "primary" };
  return null;
});

const recordScoreDisplay = computed(() => {
  const v = recordTotalScore.value ?? route.query.totalScore;
  if (v === null || v === undefined || v === "") return "-";
  return Number(v);
});

const normalizeList = (list) => {
  const arr = Array.isArray(list) ? list.slice() : [];
  // 按试卷题序正序展示，保证题号从小到大
  arr.sort((a, b) => Number(a.examPaperTopicSequence ?? 0) - Number(b.examPaperTopicSequence ?? 0));
  return arr.map((item, idx) => ({
    ...item,
    __index: idx,
    __displayNo: idx + 1,
  }));
};

const flatList = computed(() => normalizeList(detailsList.value));

const filteredFlatList = computed(() => {
  const list = flatList.value;
  if (activeFilter.value === "all") return list;
  // 已批阅：客观题视为已批阅；主观题需 review_status=1
  if (activeFilter.value === "reviewed") {
    return list.filter((q) => !isSubjective(q) || Number(q.reviewStatus) === 1);
  }
  // 未批阅：仅主观题 review_status=0/null
  if (activeFilter.value === "pending") return list.filter((q) => isSubjective(q) && Number(q.reviewStatus ?? 0) === 0);
  return list;
});

watch(activeFilter, () => {
  currentIndex.value = 0;
});

const current = computed(() => filteredFlatList.value[currentIndex.value] || null);
const canPrev = computed(() => currentIndex.value > 0);
const canNext = computed(() => currentIndex.value < filteredFlatList.value.length - 1);

const isSubjective = (q) => Number(q?.examQuestionTypes) === 5;
const isSubjectiveReviewed = (q) => isSubjective(q) && Number(q?.reviewStatus ?? 0) === 1;
const isChoiceQuestion = (q) => [1, 2, 3].includes(Number(q?.examQuestionTypes)); // 单选/多选/判断

const questionTypeLabel = (t) => {
  const n = Number(t);
  if (n === 1) return "单选题";
  if (n === 2) return "多选题";
  if (n === 3) return "判断题";
  if (n === 4) return "填空题";
  if (n === 5) return "简答题";
  return "题目";
};

const questionTypeGroupTitle = (t) => {
  const n = Number(t);
  if (n === 1) return "一、单选题";
  if (n === 2) return "二、多选题";
  if (n === 3) return "三、判断题";
  if (n === 4) return "四、填空题";
  if (n === 5) return "五、简答题";
  return questionTypeLabel(n);
};

const questionStem = computed(() => {
  const row = current.value || {};
  return (
    row.examQuestionContent ||
    row.examQuestionTitle ||
    row.examQuestionName ||
    row.examQuestionText ||
    row.examQuestionProblem ||
    row.questionContent ||
    ""
  );
});

const formatRichText = (html) => {
  const text = String(html || "");
  if (!text) return "";
  return text
    .replace(/<script[\s\S]*?>[\s\S]*?<\/script>/gi, "")
    .replace(/<iframe[\s\S]*?>[\s\S]*?<\/iframe>/gi, "")
    .replace(/<img([^>]*?)style=""/gi, "<img$1");
};

const parsedOptions = computed(() => {
  const raw = current.value?.examQuestionOptions;
  if (!raw) return [];
  try {
    const obj = typeof raw === "string" ? JSON.parse(raw) : raw;
    // 常见格式1：[{label:'A',value:'xxx'}]
    if (Array.isArray(obj)) {
      return obj
        .map((it, idx) => {
          const key = (it?.label || it?.key || it?.option || it?.name || String.fromCharCode(65 + idx)).toString().trim();
          const text = (it?.value || it?.text || it?.content || it?.labelText || it?.nameText || "").toString().trim();
          return { key, text: text || String(it?.value ?? it?.text ?? it ?? "") };
        })
        .filter((x) => x.key);
    }
    // 常见格式2：{A:'xxx',B:'yyy'}
    if (obj && typeof obj === "object") {
      return Object.entries(obj).map(([k, v]) => ({ key: String(k).trim(), text: String(v).trim() }));
    }
  } catch (e) {
    // 常见格式3：A. xxx\nB. yyy
    const lines = String(raw).split(/\r?\n/).map((s) => s.trim()).filter(Boolean);
    const parsed = lines
      .map((line) => {
        const m = line.match(/^([A-Z])[\.\、\s]+(.+)$/);
        if (!m) return null;
        return { key: m[1], text: m[2] };
      })
      .filter(Boolean);
    if (parsed.length) return parsed;
  }
  return [];
});

const normalizeAnswer = (val) => String(val || "").replaceAll(" ", "").toUpperCase();
const correctSet = computed(() => new Set(normalizeAnswer(current.value?.examQuestionAnswer).split("").filter(Boolean)));
const userSet = computed(() => new Set(normalizeAnswer(current.value?.examDetailsMyanswer).split("").filter(Boolean)));

const isCorrectOption = (key) => correctSet.value.has(String(key).toUpperCase());
const isUserSelectedOption = (key) => userSet.value.has(String(key).toUpperCase());

const currentScoreText = computed(() => {
  const q = current.value;
  if (!q) return "-";
  if (isSubjective(q)) return Number(editor.score ?? 0);
  return q.examDetailsMyscore ?? 0;
});

const clampScore = (score, maxScore) => {
  const value = Number(score || 0);
  const max = Number(maxScore || 10);
  if (Number.isNaN(value)) return 0;
  const clamped = Math.max(0, Math.min(value, max));
  return Math.round(clamped);
};

const syncEditorFromCurrent = () => {
  const q = current.value;
  if (!q || !isSubjective(q)) return;
  const s = Number(q.teacherScore ?? q.examDetailsMyscore ?? 0);
  const c = String(q.teacherComment || "");
  editor.score = s;
  editor.comment = c;
  editor.initialScore = s;
  editor.initialComment = c;
};

watch(current, () => {
  syncEditorFromCurrent();
  editingScore.value = false;
});

const hasDirty = computed(() => {
  const q = current.value;
  if (!q || !isSubjective(q)) return false;
  return Number(editor.score) !== Number(editor.initialScore) || String(editor.comment || "") !== String(editor.initialComment || "");
});

const toggleEditingScore = () => {
  if (isSubjectiveReviewed(current.value)) {
    ElMessage.warning("该简答题已批阅，不能再修改分数和评语");
    return;
  }
  editingScore.value = !editingScore.value;
};

const goToIndex = (absoluteIndex) => {
  const idx = filteredFlatList.value.findIndex((q) => q.__index === absoluteIndex);
  if (idx >= 0) currentIndex.value = idx;
};

const numClass = (q) => {
  const isCur = current.value?.__index === q.__index;
  const reviewed = isSubjective(q) ? Number(q.reviewStatus) === 1 : true;
  return {
    cur: isCur,
    done: reviewed && !isCur,
    todo: !reviewed && !isCur,
  };
};

const prev = () => {
  if (canPrev.value) currentIndex.value -= 1;
};
const next = () => {
  if (canNext.value) currentIndex.value += 1;
};

const save = async (markReviewed) => {
  const q = current.value;
  if (!q || !isSubjective(q) || saving.value) return;
  if (isSubjectiveReviewed(q)) {
    ElMessage.warning("该简答题已批阅，不能再修改分数和评语");
    return;
  }
  if (!editingScore.value && !hasDirty.value) {
    ElMessage.warning("请先点击修改得分后再保存");
    return;
  }
  saving.value = true;
  try {
    const score = clampScore(editor.score, q.examPaperTopicNumber);
    await request.post("/examDetails/gradeSubjective", {
      examDetailsId: q.id,
      teacherScore: score,
      teacherComment: editor.comment || "",
      reviewStatus: markReviewed ? 1 : 0,
      recalculate: !!markReviewed,
    });

    q.teacherScore = score;
    q.teacherComment = editor.comment || "";
    q.examDetailsMyscore = score;
    q.reviewStatus = markReviewed ? 1 : 0;

    // 同步顶部总分展示（局部回显），避免保存后信息区滞后
    const objectiveTotal = flatList.value
      .filter((item) => !isSubjective(item))
      .reduce((sum, item) => sum + Number(item.examDetailsMyscore || 0), 0);
    const subjectiveTotal = flatList.value
      .filter((item) => isSubjective(item))
      .reduce((sum, item) => sum + Number(item.examDetailsMyscore || item.teacherScore || 0), 0);
    recordTotalScore.value = objectiveTotal + subjectiveTotal;

    editor.score = score;
    editor.initialScore = score;
    editor.initialComment = String(editor.comment || "");

    editingScore.value = false;
    ElMessage.success("保存成功");
  } catch (e) {
    console.error(e);
    ElMessage.error("保存失败，请稍后重试");
  } finally {
    saving.value = false;
  }
};

const groupedQuestions = computed(() => {
  const list = filteredFlatList.value;
  const byType = new Map();
  for (const q of list) {
    const t = Number(q.examQuestionTypes);
    if (!byType.has(t)) byType.set(t, []);
    byType.get(t).push(q);
  }
  const order = [1, 2, 3, 4, 5, 0];
  return order
    .filter((t) => byType.has(t) && byType.get(t).length)
    .map((t) => {
      const items = byType.get(t);
      const totalScore = items.reduce((sum, it) => sum + Number(it.examPaperTopicNumber || 0), 0);
      return {
        key: String(t),
        title: questionTypeGroupTitle(t),
        items,
        totalScore,
      };
    });
});

const currentGroupHeading = computed(() => {
  const q = current.value;
  if (!q) return "";
  const t = Number(q.examQuestionTypes);
  const sameType = filteredFlatList.value.filter((x) => Number(x.examQuestionTypes) === t);
  const count = sameType.length;
  const total = sameType.reduce((sum, it) => sum + Number(it.examPaperTopicNumber || 0), 0);
  const each = Number(q.examPaperTopicNumber || 0);
  return `${questionTypeGroupTitle(t)}（每题${each}分，共${total}分）`;
});

const backToList = async () => {
  if (hasDirty.value) {
    try {
      await ElMessageBox.confirm("当前修改未保存，确定要离开吗？", "提示", {
        confirmButtonText: "确定离开",
        cancelButtonText: "取消",
        type: "warning",
      });
    } catch {
      return;
    }
  }
  let tab = "pending";
  const statusText = String(route.query.reviewStatus || "");
  if (statusText.includes("已完成")) tab = "completed";
  else if (statusText.includes("阅卷中")) tab = "reviewing";
  else if (statusText.includes("待阅卷")) tab = "pending";
  router.push({ path: "/examGrading", query: { tab } });
};

onBeforeRouteLeave(async () => {
  if (!hasDirty.value) return true;
  try {
    await ElMessageBox.confirm("当前修改未保存，确定要离开吗？", "提示", {
      confirmButtonText: "确定离开",
      cancelButtonText: "取消",
      type: "warning",
    });
    return true;
  } catch {
    return false;
  }
});

const loadData = async () => {
  try {
    if (!examDetailsUuidNumber) return;
    const { data } = await request.get(`examRecord/examPaperInfo/${examDetailsUuidNumber}`);
    paperFullScore.value = data?.examPaperMyscore ?? null;
    examEndTimeText.value = data?.endTime || "";
    recordTotalScore.value = data?.totalScore ?? null;
    recordStatus.value = data?.status ?? null;
    if (data?.examInfoId !== undefined && data?.examInfoId !== null) {
      try {
        const resp = await request.get(`examInfo/info/${data.examInfoId}`);
        examDurationMinutes.value = resp?.data?.duration ?? null;
      } catch {
        // ignore
      }
    }
    detailsList.value = Array.isArray(data?.examDetailsRespList) ? data.examDetailsRespList : [];
    currentIndex.value = 0;
    scrollAdminLayoutToTop();
  } catch (e) {
    console.error(e);
    ElMessage.error("加载阅卷详情失败");
  }
};

onBeforeMount(() => {
  if (isReady.value) loadData();
});

onMounted(() => {
  scrollAdminLayoutToTop();
});

watch(
  () => route.fullPath,
  () => {
    scrollAdminLayoutToTop();
  }
);
</script>

<style lang="scss" scoped>
.grading-view {
  min-height: calc(100vh - 120px);
  max-width: 100%;
  box-sizing: border-box;
  overflow-x: hidden;
}

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

.top-card {
  margin-bottom: 16px;
  padding: var(--card-padding);
  background: var(--card-bg);
  border-radius: var(--card-border-radius);
  box-shadow: var(--card-shadow);
  border-top: var(--card-border-top);
  border-right: var(--card-border-right);
  border-bottom: var(--card-border-bottom);
  border-left: var(--card-border-left);
}

.title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 26px;
  font-weight: 700;
  margin: 4px 0 12px;
}

.status-tag {
  transform: translateY(1px);
}

.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
  color: #606266;
}

.meta-item {
  display: flex;
  align-items: center;
  padding: 0 14px;
  position: relative;
  line-height: 22px;
}

.meta-item:first-child {
  padding-left: 0;
}

.meta-item:not(:last-child)::after {
  content: "";
  position: absolute;
  right: 0;
  top: 3px;
  bottom: 3px;
  width: 1px;
  background: #e4e7ed;
}

.meta-item .k {
  font-weight: 700;
  margin-right: 6px;
}

.meta-item .v {
  color: #303133;
}

.meta-item .v.score {
  color: #e84b67;
  font-weight: 900;
}

.score-item {
  padding-right: 0;
}

.layout {
  display: grid;
  grid-template-columns: minmax(260px, 380px) minmax(0, 1fr);
  gap: 16px;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

.sidebar,
.content {
  min-width: 0;
  max-width: 100%;
}

.side-card {
  padding: var(--card-padding);
  background: var(--card-bg);
  border-radius: var(--card-border-radius);
  box-shadow: var(--card-shadow);
  border-top: var(--card-border-top);
  border-right: var(--card-border-right);
  border-bottom: var(--card-border-bottom);
  border-left: var(--card-border-left);
  min-height: 620px;
}

.side-title {
  font-weight: 700;
  margin-bottom: 10px;
}

.side-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.side-tabs .tab {
  flex: 1;
  text-align: center;
  padding: 8px 0;
  border-radius: 8px;
  background: #f5f7fa;
  color: #606266;
  cursor: pointer;
  user-select: none;
  font-weight: 600;
}

.side-tabs .tab.active {
  background: #ecf5ff;
  color: #409eff;
  border: 1px solid #c6e2ff;
}

.group {
  margin-top: 12px;
}

.group-title {
  color: #606266;
  font-weight: 700;
  margin-bottom: 8px;
}

.num-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
}

.num {
  height: 34px;
  border-radius: 8px;
  border: 1px solid #dcdfe6;
  background: #fff;
  cursor: pointer;
  font-weight: 700;
  color: #606266;
}

.num.done {
  border-color: #c2e7d4;
  background: #f0f9eb;
  color: #2e7d32;
}

.num.todo {
  border-color: #f3d19e;
  background: #fdf6ec;
  color: #b26a00;
}

.num.cur {
  border-color: #409eff;
  background: #409eff;
  color: #fff;
}

.legend {
  margin-top: 14px;
  display: flex;
  gap: 14px;
  align-items: center;
  color: #606266;
  font-weight: 600;
}

.legend .dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 6px;
  display: inline-block;
}

.legend .done {
  background: #67c23a;
}

.legend .todo {
  background: #e6a23c;
}

.legend .cur {
  background: #409eff;
}

.content-card {
  padding: var(--card-padding);
  background: var(--card-bg);
  border-radius: var(--card-border-radius);
  box-shadow: var(--card-shadow);
  border-top: var(--card-border-top);
  border-right: var(--card-border-right);
  border-bottom: var(--card-border-bottom);
  border-left: var(--card-border-left);
  min-height: 520px;
}

.q-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  padding-bottom: 10px;
  border-bottom: 1px solid #ebeef5;
}

.q-title {
  font-weight: 800;
  color: #303133;
}

.q-sub {
  font-weight: 600;
  color: #606266;
  margin-left: 8px;
}

.q-score .k {
  color: #606266;
  font-weight: 700;
}

.q-score .v {
  color: #606266;
}

.q-score .divider {
  color: #c0c4cc;
  margin: 0 8px;
}

.q-score .v.red {
  color: #e84b67;
  font-weight: 800;
}

.q-stem {
  margin: 12px 0;
  color: #303133;
  line-height: 1.8;
  white-space: normal;
  display: flex;
  gap: 6px;
  align-items: flex-start;
}

.stem-no {
  font-weight: 700;
  color: #409eff;
}

.stem-content {
  flex: 1;
}

.options {
  display: grid;
  gap: 10px;
  margin-bottom: 14px;
}

.opt {
  display: flex;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px solid transparent;
  background: #fff;
  align-items: center;
}

.opt-key {
  font-weight: 800;
  color: #606266;
  min-width: 18px;
}

.opt.correct {
  color: #2ca45c;
}

.opt.correct .opt-key,
.opt.correct .opt-text {
  color: #2ca45c;
}

.opt::before {
  content: "";
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 1px solid #dcdfe6;
  flex: 0 0 16px;
}

.opt.selected:not(.correct) {
  border-color: #d9ecff;
  background: #f5faff;
}

.opt.correct::before {
  border-color: #2ca45c;
  background: #2ca45c;
  box-shadow: inset 0 0 0 3px #fff;
}

.answer-row {
  display: grid;
  gap: 8px;
  color: #606266;
  padding: 12px 0;
  border-top: 1px dashed #ebeef5;
  border-bottom: 1px dashed #ebeef5;
}

.answer-line .k {
  font-weight: 800;
}

.answer-line .v {
  color: #303133;
  white-space: pre-wrap;
}

.answer-line .v.student-answer {
  color: #f56c6c;
  font-weight: 700;
}

.answer-line .v.correct-answer {
  color: #67c23a;
  font-weight: 700;
}

.analysis {
  margin-top: 12px;
  padding: 12px;
  border-radius: 12px;
  background: #f5f7fa;
  border: 1px solid #ebeef5;
}

.analysis .k {
  font-weight: 900;
  margin-bottom: 8px;
  color: #303133;
}

.analysis .v {
  color: #606266;
  white-space: normal;
  line-height: 1.8;
}

.rich-text-content :deep(p) {
  margin: 0 0 8px;
}

.rich-text-content :deep(blockquote) {
  margin: 8px 0;
  padding: 8px 12px;
  border-left: 3px solid #dcdfe6;
  background: #f8f9fb;
  color: #606266;
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

.grading-block {
  margin-top: 14px;
}

.grading-title {
  font-weight: 900;
  margin-bottom: 10px;
}

.bottom-bar {
  margin-top: 18px;
  padding-top: 14px;
  border-top: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.bar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-mid {
  min-width: 90px;
  text-align: center;
  color: #606266;
  font-weight: 800;
}

.bar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.bar-right :deep(.btn-edit-score) {
  min-width: 108px;
  height: 36px;
  border-radius: 8px;
  border: 1px solid #b7d7ff;
  background: #eaf3ff;
  color: #2f7eff;
  font-weight: 700;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.35);
}

.bar-right :deep(.btn-edit-score:hover) {
  border-color: #8ec1ff;
  background: #dcecff;
  color: #2a73e8;
}

.bar-right :deep(.btn-edit-score.is-disabled) {
  border-color: #d7e8ff;
  background: #f3f8ff;
  color: #9bb7e5;
}

.bar-right :deep(.btn-save-score) {
  min-width: 108px;
  height: 36px;
  border-radius: 8px;
  border: 1px solid #2f7eff;
  background: linear-gradient(180deg, #4a95ff 0%, #2f7eff 100%);
  color: #fff;
  font-weight: 700;
  box-shadow: 0 4px 10px rgba(47, 126, 255, 0.28);
}

.bar-right :deep(.btn-save-score:hover) {
  border-color: #3f8cff;
  background: linear-gradient(180deg, #62a4ff 0%, #3f8cff 100%);
  color: #fff;
}

.bar-right :deep(.btn-save-score:active) {
  border-color: #2b75eb;
  background: linear-gradient(180deg, #3f8cff 0%, #2b75eb 100%);
}

.bar-right :deep(.btn-save-score.is-disabled) {
  border-color: #a8caff;
  background: linear-gradient(180deg, #b8d4ff 0%, #a8caff 100%);
  color: #eff6ff;
  box-shadow: none;
}

.score-input {
  width: 140px;
}

.fallback {
  padding: var(--card-padding);
  background: var(--card-bg);
  border-radius: var(--card-border-radius);
  box-shadow: var(--card-shadow);
  border-top: var(--card-border-top);
  border-right: var(--card-border-right);
  border-bottom: var(--card-border-bottom);
  border-left: var(--card-border-left);
}

@media (max-width: 1200px) {
  .layout {
    grid-template-columns: 1fr;
  }
}

</style>

