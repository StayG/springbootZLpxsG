<template>
  <div class="module-container">
    <template v-if="isGradingMode">
      <div class="top-card">
        <div class="top-left">
          <div class="title">{{ headerInfo.examName || "批改试卷" }}</div>
          <div class="meta-grid">
            <div class="meta-item"><span class="k">试卷名称：</span><span class="v">{{ headerInfo.examPaperName || "-" }}</span></div>
            <div class="meta-item"><span class="k">卷面总分：</span><span class="v">{{ paperFullScoreDisplay }} 分</span></div>
            <div class="meta-item"><span class="k">已阅卷：</span><span class="v high">{{ gradedCount }}</span><span class="v"> / {{ subjectiveList.length }} 题</span></div>
            <div class="meta-item"><span class="k">阅卷任务：</span><span class="v">主观题阅卷（共 {{ subjectiveList.length }} 题）</span></div>
            <div class="meta-item"><span class="k">题目总分：</span><span class="v">{{ totalMaxScore }} 分</span></div>
            <div class="meta-item progress-cell">
              <span class="k">进度：</span>
              <span class="v">{{ progressPercent }}%</span>
              <el-progress :percentage="progressPercent" :show-text="false" :stroke-width="8" />
            </div>
          </div>
        </div>
        <el-button class="end-grading-btn" type="primary" plain @click="endGrading">结束阅卷</el-button>
      </div>

      <div class="question-nav">
        <el-button @click="prevQuestion" :disabled="currentIndex === 0">上一题</el-button>
        <span class="nav-text">{{ displayIndex }} / {{ subjectiveList.length }}</span>
        <el-button @click="nextQuestion" :disabled="currentIndex >= subjectiveList.length - 1">下一题</el-button>
      </div>

      <div class="grading-layout" v-if="currentDetail">
        <div class="left-panel">
          <h3>
            <span class="question-no">{{ currentIndex + 1 }}.</span>
            <span class="question-title rich-text-content" v-html="formatRichText(currentDetail.examQuestionName || '未命名试题')" />
            <span class="score-tip">（{{ maxScore }}分）</span>
          </h3>
          <div class="question-stem rich-text-content" v-if="questionStem" v-html="formatRichText(questionStem)" />
          <div class="answer-block student-answer-block" :class="{ 'has-answer': hasStudentAnswer, 'no-answer': !hasStudentAnswer }">
            <div class="block-title student-title">
              <span>考生答案</span>
              <el-tag class="answer-status-tag" :type="studentAnswerStatusType" effect="light">{{ studentAnswerStatusText }}</el-tag>
            </div>
            <div class="block-content student-answer-content">{{ studentAnswerText }}</div>
          </div>
          <div class="answer-block">
            <div class="block-title">参考答案</div>
            <div class="block-content">{{ currentDetail.examQuestionAnswer || "-" }}</div>
          </div>
          <div class="answer-block" v-if="currentDetail.examQuestionAnalysis">
            <div class="block-title">答案解析</div>
            <div class="block-content rich-text-content" v-html="formatRichText(currentDetail.examQuestionAnalysis)" />
          </div>

          <div class="left-extra-grid">
            <div class="extra-card">
              <div class="extra-title">考生信息</div>
              <div class="info-grid">
                <div class="info-item"><span class="k">考生姓名：</span><span class="v">{{ headerInfo.nickname || "-" }}</span></div>
                <div class="info-item"><span class="k">考试时长：</span><span class="v">{{ headerInfo.durationText || "-" }}</span></div>
                <div class="info-item"><span class="k">交卷时间：</span><span class="v">{{ headerInfo.endTime || "-" }}</span></div>
              </div>
            </div>
            <div class="extra-card">
              <div class="extra-title">试卷附件（0）</div>
              <el-empty class="extra-empty" description="暂无附件" :image-size="62" />
            </div>
          </div>
        </div>

        <div class="right-panel">
          <div class="score-card">
            <div class="score-title">试卷得分</div>
            <div class="score-main">
              <span class="score-num">{{ currentScoreDisplay }}</span>
              <span class="score-den"> / {{ maxScore }} 分</span>
            </div>
          </div>

          <div class="label">评分</div>
          <div class="quick-scores">
            <el-button
              v-for="item in quickScores"
              :key="item"
              class="quick-score-btn"
              :class="{ selected: Number(localScore) === item }"
              :type="Number(localScore) === item ? 'primary' : 'default'"
              :disabled="!isSubjectiveQuestion || isReviewedCurrent"
              @click="setQuickScore(item)"
            >
              {{ item }}
            </el-button>
          </div>

          <div class="custom-score">
          <el-input-number v-model="localScore" :min="0" :max="maxScore" :disabled="!isSubjectiveQuestion || isReviewedCurrent" />
            <span class="unit">分</span>
          </div>

          <div class="label">评语（选填）</div>
          <el-input
            v-model="localComment"
            type="textarea"
            :rows="5"
            maxlength="200"
            show-word-limit
            :disabled="!isSubjectiveQuestion || isReviewedCurrent"
            placeholder="请输入评语，最多200字"
          />

          <div class="label">快捷评语</div>
          <div class="quick-comments">
            <el-tag
              v-for="item in quickCommentOptions"
              :key="item"
              class="quick-comment-tag"
              effect="light"
              @click="appendQuickComment(item)"
            >
              {{ item }}
            </el-tag>
          </div>

          <div class="action-col">
            <el-button class="primary-action" type="primary" :disabled="isReviewedCurrent" @click="saveAndNext">保存并进入下一题</el-button>
            <div class="secondary-actions">
              <el-button :disabled="isReviewedCurrent" @click="saveCurrent">保存</el-button>
              <el-button @click="backToList">返回列表</el-button>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-else description="暂无需要教师批改的简答题" />
    </template>

    <template v-else>
      <div class="fallback-card">
        <el-empty description="请从阅卷管理或考试记录进入批改页面" />
        <el-button type="primary" @click="goBack">返回</el-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import request from "@/utils/request";
import { ElMessage, ElMessageBox } from "element-plus";

const route = useRoute();
const router = useRouter();

const examRecordId = route.query.examRecordId;
const examDetailsUuidNumber = route.query.examDetailsUuidNumber;
const isGradingMode = computed(() => !!examRecordId);

const detailsList = ref([]);
const subjectiveList = computed(() => detailsList.value.filter((item) => Number(item.examQuestionTypes) === 5));
const currentIndex = ref(0);
const localScore = ref(0);
const localComment = ref("");
const saving = ref(false);

const currentDetail = computed(() => subjectiveList.value[currentIndex.value] || null);
const isSubjectiveQuestion = computed(() => Number(currentDetail.value?.examQuestionTypes) === 5);
const isReviewedCurrent = computed(() => Number(currentDetail.value?.reviewStatus ?? 0) === 1);
const maxScore = computed(() => Number(currentDetail.value?.examPaperTopicNumber || 10));
const currentScoreDisplay = computed(() => Number(localScore.value || 0).toFixed(1));
const displayIndex = computed(() => (subjectiveList.value.length ? currentIndex.value + 1 : 0));
const totalScore = computed(() =>
  subjectiveList.value.reduce((sum, item) => sum + Number(item.examDetailsMyscore || item.teacherScore || 0), 0).toFixed(1)
);
const totalMaxScore = computed(() =>
  subjectiveList.value.reduce((sum, item) => sum + Number(item.examPaperTopicNumber || 10), 0)
);
const gradedCount = computed(() => subjectiveList.value.filter((item) => Number(item.reviewStatus) === 1).length);
const progressPercent = computed(() => {
  const total = subjectiveList.value.length;
  if (!total) return 0;
  return Number(((gradedCount.value / total) * 100).toFixed(0));
});

const paperFullScore = ref(null);
const paperFullScoreDisplay = computed(() => {
  const v = Number(paperFullScore.value);
  if (paperFullScore.value === null || paperFullScore.value === undefined) return "-";
  return Number.isNaN(v) ? "-" : v;
});

const examDurationMinutes = ref(null);
const examEndTimeText = ref("");

const headerInfo = computed(() => {
  const first = subjectiveList.value[0] || detailsList.value[0] || {};
  const durationValue = examDurationMinutes.value ?? route.query.duration ?? first.duration;
  const durationNum = Number(durationValue);
  const durationText = Number.isFinite(durationNum) && durationNum > 0 ? `${durationNum} 分钟` : "";
  return {
    examName: route.query.examName || first.examName || first.examPaperName,
    examPaperName: route.query.examPaperName || first.examPaperName,
    nickname: route.query.nickname || first.nickname,
    endTime: examEndTimeText.value || route.query.endTime || route.query.insertTime || first.endTime || first.insertTime,
    durationText,
  };
});

const quickScores = computed(() => {
  const full = maxScore.value || 10;
  const values = [1, 0.8, 0.6, 0.4, 0.2, 0].map((r) => Number((full * r).toFixed(1)));
  return [...new Set(values)];
});

const questionStem = computed(() => {
  const row = currentDetail.value || {};
  return (
    row.examQuestionContent ||
    row.examQuestionTitle ||
    row.examQuestionText ||
    row.examQuestionProblem ||
    row.questionContent ||
    ""
  );
});

const studentAnswerRawText = computed(() => String(currentDetail.value?.examDetailsMyanswer ?? ""));
const hasStudentAnswer = computed(() => studentAnswerRawText.value.trim().length > 0);
const studentAnswerText = computed(() => (hasStudentAnswer.value ? studentAnswerRawText.value : "未作答"));
const studentAnswerStatusText = computed(() => (hasStudentAnswer.value ? "已作答" : "未作答"));
const studentAnswerStatusType = computed(() => (hasStudentAnswer.value ? "success" : "info"));

const formatRichText = (html) => {
  const text = String(html || "");
  if (!text) return "";
  return text
    .replace(/<script[\s\S]*?>[\s\S]*?<\/script>/gi, "")
    .replace(/<iframe[\s\S]*?>[\s\S]*?<\/iframe>/gi, "")
    .replace(/<img([^>]*?)style=""/gi, "<img$1");
};

const quickCommentOptions = [
  "答案完整，逻辑清晰",
  "要点未写全，注意补充",
  "思路不清楚，需加强理解",
  "答案有误，请核对关键点",
  "其他",
];
const appendQuickComment = (text) => {
  if (!isSubjectiveQuestion.value || isReviewedCurrent.value) return;
  const current = String(localComment.value || "").trim();
  const next = current ? `${current}\n${text}` : text;
  localComment.value = next.slice(0, 200);
};

const syncEditorFromCurrent = () => {
  const row = currentDetail.value;
  if (!row) return;
  localScore.value = Number(row.teacherScore ?? row.examDetailsMyscore ?? 0);
  localComment.value = row.teacherComment || "";
};

watch(currentDetail, () => {
  syncEditorFromCurrent();
});

const normalizedLocalScore = computed(() => clampScore(localScore.value));
const isDirty = computed(() => {
  const row = currentDetail.value;
  if (!row) return false;
  const savedScore = Number(row.teacherScore ?? row.examDetailsMyscore ?? 0);
  const savedComment = String(row.teacherComment || "");
  return Number(normalizedLocalScore.value) !== Number(savedScore) || String(localComment.value || "") !== savedComment;
});

const getDataList = async () => {
  try {
    // 优先走考试记录详情接口：能同时拿到试卷满分(examPaperMyscore)与试题明细
    if (examDetailsUuidNumber) {
      const { data } = await request.get(`examRecord/examPaperInfo/${examDetailsUuidNumber}`);
      paperFullScore.value = data?.examPaperMyscore ?? null;
      examEndTimeText.value = data?.endTime || "";
      if (data?.examInfoId !== undefined && data?.examInfoId !== null) {
        try {
          const resp = await request.get(`examInfo/info/${data.examInfoId}`);
          examDurationMinutes.value = resp?.data?.duration ?? null;
        } catch (e) {
          // ignore duration fetch failure
        }
      }
      if (Array.isArray(data?.examDetailsRespList) && data.examDetailsRespList.length) {
        detailsList.value = data.examDetailsRespList;
      } else {
        detailsList.value = [];
      }
    } else {
      const { data } = await request.post("examDetails/page", {
        page: 1,
        limit: 2000,
        examRecordId,
      });
      detailsList.value = data?.list || [];
      // 兜底：如果明细里带了试卷满分字段，顺便取一次
      const first = detailsList.value?.[0];
      paperFullScore.value = first?.examPaperMyscore ?? first?.examPaper?.examPaperMyscore ?? paperFullScore.value;
      examEndTimeText.value = first?.endTime || "";
    }
    currentIndex.value = 0;
    syncEditorFromCurrent();
  } catch (error) {
    console.error("加载试题失败:", error);
    ElMessage.error("加载试题失败");
  }
};

const setQuickScore = (value) => {
  localScore.value = value;
};

const clampScore = (score) => {
  const value = Number(score || 0);
  if (Number.isNaN(value)) return 0;
  const clamped = Math.max(0, Math.min(value, maxScore.value));
  return Math.round(clamped);
};

// 仅保存：更新 teacher_score / teacher_comment，但保持 review_status=0，不跳转、不改变题目批阅状态
const saveCurrent = async () => {
  if (!currentDetail.value || saving.value) return false;
  if (isReviewedCurrent.value) {
    ElMessage.warning("该简答题已批阅，不能再修改分数和评语");
    return false;
  }
  if (!isSubjectiveQuestion.value) {
    ElMessage.info("客观题无需手动批改");
    return true;
  }
  saving.value = true;
  try {
    const score = clampScore(localScore.value);
    await request.post("/examDetails/gradeSubjective", {
      examDetailsId: currentDetail.value.id,
      teacherScore: score,
      teacherComment: localComment.value || "",
      reviewStatus: 0,
      recalculate: false,
    });
    currentDetail.value.teacherScore = score;
    currentDetail.value.teacherComment = localComment.value || "";
    currentDetail.value.examDetailsMyscore = score;
    ElMessage.success("保存成功");
    return true;
  } catch (error) {
    console.error("保存批改失败:", error);
    ElMessage.error("保存失败，请稍后重试");
    return false;
  } finally {
    saving.value = false;
  }
};

// 保存并标记已批阅：更新分数/评语、review_status=1，并触发后端总分重算
const saveAndMarkReviewed = async () => {
  if (!currentDetail.value || saving.value) return false;
  if (isReviewedCurrent.value) {
    ElMessage.warning("该简答题已批阅，不能再修改分数和评语");
    return false;
  }
  if (!isSubjectiveQuestion.value) {
    ElMessage.info("客观题无需手动批改");
    return true;
  }
  saving.value = true;
  try {
    const score = clampScore(localScore.value);
    await request.post("/examDetails/gradeSubjective", {
      examDetailsId: currentDetail.value.id,
      teacherScore: score,
      teacherComment: localComment.value || "",
      reviewStatus: 1,
      recalculate: true,
    });
    currentDetail.value.teacherScore = score;
    currentDetail.value.teacherComment = localComment.value || "";
    currentDetail.value.examDetailsMyscore = score;
    currentDetail.value.reviewStatus = 1;
    ElMessage.success("保存成功");
    return true;
  } catch (error) {
    console.error("保存批改失败:", error);
    ElMessage.error("保存失败，请稍后重试");
    return false;
  } finally {
    saving.value = false;
  }
};

const findNextPendingIndex = () => {
  const list = subjectiveList.value || [];
  if (!list.length) return -1;
  for (let i = currentIndex.value + 1; i < list.length; i += 1) {
    if (Number(list[i]?.reviewStatus ?? 0) === 0) return i;
  }
  for (let i = 0; i < currentIndex.value; i += 1) {
    if (Number(list[i]?.reviewStatus ?? 0) === 0) return i;
  }
  return -1;
};

// 保存并进入下一题：保存+置为已批阅，并自动定位到下一道 review_status=0 的主观题
const saveAndNext = async () => {
  const ok = await saveAndMarkReviewed();
  if (!ok) return;
  const nextIdx = findNextPendingIndex();
  if (nextIdx >= 0) {
    currentIndex.value = nextIdx;
    return;
  }
  ElMessage.success("已是最后一题");
};

const prevQuestion = () => {
  if (currentIndex.value > 0) currentIndex.value -= 1;
};

const nextQuestion = () => {
  if (currentIndex.value < subjectiveList.value.length - 1) currentIndex.value += 1;
};

// 结束阅卷：保存当前题并标记已批阅，然后返回阅卷管理列表页
const endGrading = async () => {
  const ok = await saveAndMarkReviewed();
  if (!ok) return;
  router.push({ path: "/examGrading" });
};

// 返回列表：不保存，若有未保存修改则二次确认
const backToList = async () => {
  if (isDirty.value) {
    try {
      await ElMessageBox.confirm("当前修改未保存，确定要离开吗？", "提示", {
        confirmButtonText: "确定离开",
        cancelButtonText: "取消",
        type: "warning",
      });
    } catch (e) {
      return;
    }
  }
  const tab = route.query.reviewTab;
  router.push({ path: "/examGrading", query: tab ? { tab } : {} });
};

onMounted(() => {
  if (isGradingMode.value) {
    getDataList();
  }
});
</script>

<style lang="scss" scoped>
.top-card,
.question-nav,
.grading-layout,
.fallback-card {
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

.top-card {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.title {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 12px;
}

.top-left {
  flex: 1;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(240px, 1fr));
  gap: 4px 18px;
  color: #606266;
  margin-top: 0;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  line-height: 20px;
}

.meta-item .k {
  color: #606266;
}

.meta-item .v {
  color: #303133;
}

.meta-item .v.high {
  color: #409eff;
  font-weight: 600;
}

.progress-cell {
  gap: 8px;
}

.progress-cell :deep(.el-progress) {
  width: 160px;
}

.question-nav {
  display: flex;
  align-items: center;
  gap: 10px;
}

.nav-text {
  min-width: 80px;
  text-align: center;
  color: #606266;
}

.grading-layout {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
}

.section-title {
  color: #606266;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 10px;
}

.left-panel h3 {
  margin-bottom: 14px;
  font-size: 20px;
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.question-no {
  color: #303133;
}

.question-title {
  flex: 1;
}

.question-stem {
  margin: -6px 0 12px;
  line-height: 1.75;
  color: #303133;
  white-space: normal;
}

.score-tip {
  color: #606266;
  font-size: 16px;
}

.answer-block {
  margin-bottom: 14px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
}

.block-title {
  padding: 10px 12px;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  font-weight: 600;
}

.student-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #1890ff;
  font-weight: 700;
}

.answer-status-tag {
  flex-shrink: 0;
}

.block-content {
  padding: 14px 12px;
  line-height: 1.75;
  white-space: normal;
}

.student-answer-block {
  border-color: #c8defc;
  box-shadow: 0 6px 14px rgba(22, 119, 255, 0.08);
}

.student-answer-block.has-answer {
  background: #f2f8ff;
}

.student-answer-block.no-answer {
  background: #fff6ec;
  border-color: #f4d5b0;
  box-shadow: 0 6px 14px rgba(230, 162, 60, 0.1);
}

.student-answer-content {
  font-weight: 500;
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
  font-size: 14px;
  color: #303133;
  text-align: left;
}

.rich-text-content :deep(thead tr) {
  background: #f5f7fa;
}

.right-panel .label {
  font-weight: 600;
  margin: 12px 0 8px;
}

.score-card {
  padding-bottom: 14px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 12px;
}

.score-title {
  color: #303133;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 10px;
}

.score-main {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 6px;
}

.score-num {
  color: #e84b67;
  font-size: 30px;
  font-weight: 600;
  line-height: 1;
}

.score-den {
  color: #606266;
  font-size: 14px;
}

.quick-scores {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.quick-score-btn {
  min-width: 42px;
  height: 34px;
  padding: 0 10px;
  border-radius: 6px;
}

.quick-score-btn:not(.selected) {
  --el-button-bg-color: #ffffff;
  --el-button-text-color: #606266;
  --el-button-border-color: #dcdfe6;
  --el-button-hover-bg-color: #ffffff;
  --el-button-hover-text-color: #409eff;
  --el-button-hover-border-color: #c6e2ff;
}

.quick-score-btn.selected {
  --el-button-bg-color: #409eff;
  --el-button-text-color: #ffffff;
  --el-button-border-color: #409eff;
  --el-button-hover-bg-color: #409eff;
  --el-button-hover-text-color: #ffffff;
  --el-button-hover-border-color: #409eff;
}

.custom-score {
  margin-top: 10px;
  display: flex;
  gap: 8px;
  align-items: center;
}

.unit {
  color: #606266;
}

.quick-comments {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.quick-comment-tag {
  cursor: pointer;
  user-select: none;
  border-radius: 999px;
  padding: 6px 10px;
  border: 1px solid #d9ecff;
  background: #ecf5ff;
  color: #409eff;
}

.action-col {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.primary-action {
  width: 100%;
  height: 40px;
  font-weight: 600;
  --el-button-bg-color: #409eff;
  --el-button-border-color: #409eff;
  --el-button-hover-bg-color: #66b1ff;
  --el-button-hover-border-color: #66b1ff;
}

.secondary-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  width: 100%;
}

.secondary-actions :deep(.el-button) {
  width: 100%;
  height: 40px;
  border-radius: 6px;
}

.secondary-actions :deep(.el-button + .el-button) {
  margin-left: 0;
}

.fallback-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.end-grading-btn {
  --el-button-bg-color: #ffffff;
  --el-button-text-color: #409eff;
  --el-button-border-color: #409eff;
  --el-button-hover-bg-color: #ecf5ff;
  --el-button-hover-text-color: #409eff;
  --el-button-hover-border-color: #409eff;
  --el-button-active-bg-color: #ecf5ff;
  --el-button-active-border-color: #409eff;

  height: 36px;
  padding: 0 18px;
  border-radius: 4px;
  font-weight: 500;
}

.left-extra-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-top: 16px;
}

.extra-card {
  padding: var(--card-padding);
  background: var(--card-bg);
  border-radius: var(--card-border-radius);
  box-shadow: var(--card-shadow);
  border-top: var(--card-border-top);
  border-right: var(--card-border-right);
  border-bottom: var(--card-border-bottom);
  border-left: var(--card-border-left);
}

.left-extra-grid .extra-card {
  padding: 16px 18px;
}

.extra-title {
  font-weight: 600;
  margin-bottom: 12px;
}

.left-extra-grid .extra-title {
  margin-bottom: 10px;
  font-size: 15px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 8px;
  color: #606266;
}

.left-extra-grid .info-grid {
  gap: 8px;
}

.left-extra-grid .info-item {
  line-height: 1.55;
  font-size: 14px;
}

.left-extra-grid :deep(.extra-empty.el-empty) {
  padding: 12px 0 8px;
}

.left-extra-grid :deep(.extra-empty .el-empty__description) {
  margin-top: 10px;
}

.info-item {
  display: flex;
  gap: 4px;
}

.info-item .v {
  color: #303133;
}

@media (max-width: 1100px) {
  .meta-grid {
    grid-template-columns: 1fr;
  }

  .grading-layout {
    grid-template-columns: 1fr;
  }

  .left-extra-grid {
    grid-template-columns: 1fr;
  }
}
</style>
