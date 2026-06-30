<template>
  <div ref="examRootRef" class="exam-container">
    <!-- 1. 顶部悬浮信息栏 (Sticky Header) -->
    <div class="exam-header">
      <div class="header-left">
        <h2 class="paper-title">{{ examPaperName || '加载中...' }}</h2>
      </div>
      <div class="header-center">
        <span>考试课程：{{ examCourseText }}</span>
        <span>作答剩余：约 {{ examDurationMinutesText }} 分钟</span>
        <span>总分：{{ examPaperMyscore }}分</span>
      </div>
      <div class="header-right">
        <el-button class="header-submit-btn" @click="handleSubmit()" :loading="submitting">交卷</el-button>
      </div>
    </div>

    <div class="exam-body">
      <!-- 2. 左侧：题目列表 + 底部题号切换 -->
      <div class="question-column">
      <div ref="questionListRef" class="question-list">
        <!-- 单选题组 -->
        <div v-if="groupedQuestions.singleChoice.length > 0" class="question-group">
          <div class="group-header">
            <span class="group-title">{{ getGroupTitle('一', '单选题', groupedQuestions.singleChoice, singleChoiceTotalScore) }}</span>
          </div>
          <div v-for="(item, idx) in groupedQuestions.singleChoice" :key="item.originalIndex"
            :id="'question-' + item.originalIndex" class="question-card">
            <div class="q-header">
              <span class="q-index">{{ item.originalIndex + 1 }}</span>
            </div>
            <div class="q-content">
              <div class="q-title" v-html="item.examQuestionName"></div>
              <div class="options-list">
                <div v-for="opt in item.parsedOptions" :key="opt.code" class="option-item"
                  :class="{ 'selected': isSelected(item.originalIndex, opt.code) }"
                  @click="handleSelect(item.originalIndex, item, opt.code)">
                  <span class="opt-code"></span>
                  <span class="opt-text">{{ opt.code }}、{{ opt.text }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 多选题组 -->
        <div v-if="groupedQuestions.multipleChoice.length > 0" class="question-group">
          <div class="group-header">
            <span class="group-title">{{ getGroupTitle('二', '多选题', groupedQuestions.multipleChoice, multipleChoiceTotalScore) }}</span>
          </div>
          <div v-for="(item, idx) in groupedQuestions.multipleChoice" :key="item.originalIndex"
            :id="'question-' + item.originalIndex" class="question-card">
            <div class="q-header">
              <span class="q-index">{{ item.originalIndex + 1 }}</span>
            </div>
            <div class="q-content">
              <div class="q-title" v-html="item.examQuestionName"></div>
              <div class="options-list">
                <div v-for="opt in item.parsedOptions" :key="opt.code" class="option-item"
                  :class="{ 'selected': isSelected(item.originalIndex, opt.code) }"
                  @click="handleSelect(item.originalIndex, item, opt.code)">
                  <span class="opt-code"></span>
                  <span class="opt-text">{{ opt.code }}、{{ opt.text }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 判断题组 -->
        <div v-if="groupedQuestions.trueFalse.length > 0" class="question-group">
          <div class="group-header">
            <span class="group-title">{{ getGroupTitle('三', '判断题', groupedQuestions.trueFalse, trueFalseTotalScore) }}</span>
          </div>
          <div v-for="(item, idx) in groupedQuestions.trueFalse" :key="item.originalIndex"
            :id="'question-' + item.originalIndex" class="question-card">
            <div class="q-header">
              <span class="q-index">{{ item.originalIndex + 1 }}</span>
            </div>
            <div class="q-content">
              <div class="q-title" v-html="item.examQuestionName"></div>
              <div class="options-list">
                <div v-for="opt in item.parsedOptions" :key="opt.code" class="option-item"
                  :class="{ 'selected': isSelected(item.originalIndex, opt.code) }"
                  @click="handleSelect(item.originalIndex, item, opt.code)">
                  <span class="opt-code"></span>
                  <span class="opt-text">{{ opt.code }}、{{ opt.text }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 填空题组 -->
        <div v-if="groupedQuestions.fillBlank.length > 0" class="question-group">
          <div class="group-header">
            <span class="group-title">{{ getGroupTitle('四', '填空题', groupedQuestions.fillBlank, fillBlankTotalScore) }}</span>
          </div>
          <div v-for="(item, idx) in groupedQuestions.fillBlank" :key="item.originalIndex"
            :id="'question-' + item.originalIndex" class="question-card">
            <div class="q-header">
              <span class="q-index">{{ item.originalIndex + 1 }}</span>
            </div>
            <div class="q-content">
              <div class="q-title" v-html="item.examQuestionName"></div>
              <div class="text-answer-area">
                <MathAnswerInput v-model="answerList[item.originalIndex].answer" :rows="5"
                  :formula-type="item.formulaType || 'none'"
                  @focus="setCurrentQuestion(item.originalIndex)"
                  :disabled="isSubmitted" />
              </div>
            </div>
          </div>
        </div>

        <!-- 简答题组 -->
        <div v-if="groupedQuestions.shortAnswer.length > 0" class="question-group">
          <div class="group-header">
            <span class="group-title">{{ getGroupTitle('五', '简答题', groupedQuestions.shortAnswer, shortAnswerTotalScore) }}</span>
          </div>
          <div v-for="(item, idx) in groupedQuestions.shortAnswer" :key="item.originalIndex"
            :id="'question-' + item.originalIndex" class="question-card">
            <div class="q-header">
              <span class="q-index">{{ item.originalIndex + 1 }}</span>
            </div>
            <div class="q-content">
              <div class="q-title" v-html="item.examQuestionName"></div>
              <div class="text-answer-area">
                <MathAnswerInput v-model="answerList[item.originalIndex].answer" :rows="5"
                  :formula-type="item.formulaType || 'none'"
                  @focus="setCurrentQuestion(item.originalIndex)"
                  :disabled="isSubmitted" />
              </div>
            </div>
          </div>
        </div>

        <!-- 其他题型组 -->
        <div v-if="groupedQuestions.other.length > 0" class="question-group">
          <div class="group-header">
            <span class="group-title">{{ getGroupTitle('六', '其他题型', groupedQuestions.other, otherTotalScore) }}</span>
          </div>
          <div v-for="(item, idx) in groupedQuestions.other" :key="item.originalIndex"
            :id="'question-' + item.originalIndex" class="question-card">
            <div class="q-header">
              <span class="q-index">{{ item.originalIndex + 1 }}</span>
            </div>
            <div class="q-content">
              <div class="q-title" v-html="item.examQuestionName"></div>
              <div v-if="item.examQuestionTypes != 4 && item.examQuestionTypes != 5" class="options-list">
                <div v-for="opt in item.parsedOptions" :key="opt.code" class="option-item"
                  :class="{ 'selected': isSelected(item.originalIndex, opt.code) }"
                  @click="handleSelect(item.originalIndex, item, opt.code)">
                  <span class="opt-code"></span>
                  <span class="opt-text">{{ opt.code }}、{{ opt.text }}</span>
                </div>
              </div>
              <div v-else class="text-answer-area">
                <MathAnswerInput v-model="answerList[item.originalIndex].answer" :rows="5"
                  :formula-type="item.formulaType || 'none'"
                  @focus="setCurrentQuestion(item.originalIndex)"
                  :disabled="isSubmitted" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="question-nav-bar">
        <button
          type="button"
          class="nav-btn nav-btn-prev"
          :disabled="isSubmitted || !canGoPrev"
          @click="goPrevQuestion"
        >
          上一题
        </button>
        <span class="nav-progress">{{ questionNavLabel }}</span>
        <button
          type="button"
          class="nav-btn nav-btn-next"
          :disabled="isSubmitted || !canGoNext"
          @click="goNextQuestion"
        >
          下一题
        </button>
      </div>
      </div>

      <!-- 3. 右侧：答题卡 (Sticky Sidebar) -->
      <div class="sidebar-wrapper">
        <div class="time-card">
          <div class="time-card-head">
            <span>剩余时间</span>
            <button class="collapse-btn" type="button" @click="timeCardCollapsed = !timeCardCollapsed">
              {{ timeCardCollapsed ? '展开' : '收起' }}
              <span class="chev" :class="{ down: timeCardCollapsed }"></span>
            </button>
          </div>
          <div v-show="!timeCardCollapsed">
            <div class="time-value" :class="{ urgent: count < 300 }">{{ formattedTime }}</div>
            <div class="progress-row">
              <span>答题进度：{{ answeredCount }} / {{ answerList.length }}</span>
              <span>{{ progressPercent }}%</span>
            </div>
            <el-progress :percentage="progressPercent" :show-text="false" :stroke-width="8" />
          </div>
        </div>

        <div class="answer-sheet-card">
          <div class="sheet-title">答题卡</div>
          <div class="sheet-legend">
            <div class="legend-item"><span class="dot done"></span> 已答</div>
            <div class="legend-item"><span class="dot"></span> 未答</div>
            <div class="legend-item"><span class="dot current"></span> 当前</div>
          </div>
          <div class="sheet-section-wrap">
            <div class="sheet-section" v-for="section in answerSheetSections" :key="section.key">
              <div class="sheet-section-title">{{ section.title }}</div>
              <div class="sheet-grid">
                <div
                  v-for="item in section.questions"
                  :key="item.originalIndex"
                  class="sheet-item"
                :class="{
                  done: answerList[item.originalIndex]?.answer,
                  current: currentQuestionIndex === item.originalIndex
                }"
                @click="scrollToQuestion(item.originalIndex)"
                >
                  {{ item.originalIndex + 1 }}
                </div>
              </div>
            </div>
          </div>

          <div class="answer-sheet-integrity" role="note">
            <span class="integrity-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" width="22" height="22" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2L4 5v6.09c0 5.05 3.41 9.76 8 10.91 4.59-1.15 8-5.86 8-10.91V5l-8-3z" stroke="currentColor" stroke-width="1.6" stroke-linejoin="round" />
                <path d="M9 12l2 2 4-4" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
              </svg>
            </span>
            <p class="integrity-text">
              考试过程中请诚信作答，严禁切屏、<br />复制等作弊行为！
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import request from "@/utils/request.js";
import { ElMessage, ElMessageBox } from 'element-plus';
import { formatCountdownHms } from "@/utils/examTime.js";
import { getKemuOptions } from "@/utils/dictionary.js";
import {
  registerExamScreenSwitchContext,
  unregisterExamScreenSwitchContext,
  tryReportExamScreenSwitch,
} from "@/utils/examScreenSwitchSession.js";


const route = useRoute();
const router = useRouter();

/** 左侧题目滚动容器；不用 document scrollIntoView，避免整页被带着滚 */
const questionListRef = ref(null);
/** 考试根节点：自动全屏失败时，首次点击此处再试一次全屏（需用户手势） */
const examRootRef = ref(null);
let clickFullscreenRetryCleanup = null;

// 核心数据
const dataList = ref([]);
const answerList = ref([]);
const examPaperName = ref("");
const examPaperMyscore = ref(0);
const examDurationMinutesRef = ref(null);
const examCourseRef = ref("");
const examCourseText = computed(() => {
  const fromRef = examCourseRef.value;
  if (fromRef) return fromRef;
  const fromQuestion = dataList.value?.[0]?.kemuValue;
  if (fromQuestion) return fromQuestion;
  return '未设置';
});
const submitting = ref(false);
/** 交卷后是否在弹窗中展示分数：来自考试配置 showAnswerAfterSubmit；未加载到配置时为 null，沿用原「有分数则展示」逻辑 */
const showScoreInSubmitDialog = ref(null);
const isSubmitted = ref(false); // 新增：标记是否已提交，用于禁用所有输入
const currentExamRecordUuid = ref("");
const isInitializingAnswers = ref(true);
const currentQuestionIndex = ref(-1);
const timeCardCollapsed = ref(false);
const kemuOptionMap = ref({});

// 防作弊配置
const antiCheatConfig = ref({
  allowScreenSwitch: 1,
  maxScreenSwitch: 3,
  allowCopyPaste: 1,
  screenSwitchCount: 0
});
/** 与 document 绑定的防作弊监听，必须在 setup 顶层 onUnmounted 中移除（不可在 async 回调里注册 onUnmounted） */
let antiCheatClipboardHandler = null;
let antiCheatVisibilityHandler = null;
let antiCheatFullscreenHandler = null;
/** 本次考试是否曾进入过全屏；用于区分「从未全屏」与「退出全屏」 */
const examHadFullscreen = ref(false);

const cleanupClickFullscreenRetry = () => {
  if (clickFullscreenRetryCleanup) {
    clickFullscreenRetryCleanup();
    clickFullscreenRetryCleanup = null;
  }
};

const clearAntiCheatListeners = () => {
  cleanupClickFullscreenRetry();
  unregisterExamScreenSwitchContext();
  examHadFullscreen.value = false;
  if (antiCheatClipboardHandler) {
    document.removeEventListener('copy', antiCheatClipboardHandler);
    document.removeEventListener('cut', antiCheatClipboardHandler);
    document.removeEventListener('paste', antiCheatClipboardHandler);
    antiCheatClipboardHandler = null;
  }
  if (antiCheatVisibilityHandler) {
    document.removeEventListener('visibilitychange', antiCheatVisibilityHandler);
    antiCheatVisibilityHandler = null;
  }
  if (antiCheatFullscreenHandler) {
    document.removeEventListener('fullscreenchange', antiCheatFullscreenHandler);
    antiCheatFullscreenHandler = null;
  }
  if (typeof document !== 'undefined' && document.fullscreenElement) {
    document.exitFullscreen().catch(() => {});
  }
};

let autoSaveTimer = null;
let saveDebounceTimer = null;
let beforeUnloadHandler = null;

// 按题型分组题目
const groupedQuestions = computed(() => {
  const groups = {
    singleChoice: [],    // 单选题 (类型 1)
    multipleChoice: [],  // 多选题 (类型 2)
    trueFalse: [],       // 判断题 (类型 3)
    fillBlank: [],       // 填空题 (类型 4)
    shortAnswer: [],     // 简答题 (类型 5)
    other: []            // 其他题型
  };

  dataList.value.forEach((item, index) => {
    // 关键修复：使用 globalIndex 而不是 forEach 的 index
    // globalIndex 是在 initData 中根据后端返回顺序设置的
    const questionWithIndex = { 
      ...item, 
      originalIndex: item.globalIndex !== undefined ? item.globalIndex : index 
    };

    if (item.examQuestionTypes == 1) {
      groups.singleChoice.push(questionWithIndex);
    } else if (item.examQuestionTypes == 2) {
      groups.multipleChoice.push(questionWithIndex);
    } else if (item.examQuestionTypes == 3) {
      groups.trueFalse.push(questionWithIndex);
    } else if (item.examQuestionTypes == 4) {
      groups.fillBlank.push(questionWithIndex);
    } else if (item.examQuestionTypes == 5) {
      groups.shortAnswer.push(questionWithIndex);
    } else {
      groups.other.push(questionWithIndex);
    }
  });

  return groups;
});

// 各题型总分计算
const singleChoiceTotalScore = computed(() => {
  return groupedQuestions.value.singleChoice.reduce((sum, item) => sum + (item.examPaperTopicNumber || 0), 0);
});

const multipleChoiceTotalScore = computed(() => {
  return groupedQuestions.value.multipleChoice.reduce((sum, item) => sum + (item.examPaperTopicNumber || 0), 0);
});

const trueFalseTotalScore = computed(() => {
  return groupedQuestions.value.trueFalse.reduce((sum, item) => sum + (item.examPaperTopicNumber || 0), 0);
});

const fillBlankTotalScore = computed(() => {
  return groupedQuestions.value.fillBlank.reduce((sum, item) => sum + (item.examPaperTopicNumber || 0), 0);
});

const shortAnswerTotalScore = computed(() => {
  return groupedQuestions.value.shortAnswer.reduce((sum, item) => sum + (item.examPaperTopicNumber || 0), 0);
});

const otherTotalScore = computed(() => {
  return groupedQuestions.value.other.reduce((sum, item) => sum + (item.examPaperTopicNumber || 0), 0);
});

const getGroupTitle = (order, name, questions, totalScore) => {
  const count = questions?.length || 0;
  if (count === 0) {
    return `${order}、${name}`;
  }
  const scores = questions.map((q) => Number(q.examPaperTopicNumber || 0));
  const minS = Math.min(...scores);
  const maxS = Math.max(...scores);
  const scorePhrase =
    minS === maxS
      ? `每小题${minS}分`
      : '各小题分值不同';
  return `${order}、${name}（本大题共${count}小题，${scorePhrase}，共${totalScore}分）`;
};

const loadKemuOptionMap = async () => {
  if (Object.keys(kemuOptionMap.value).length > 0) return;
  try {
    const list = await getKemuOptions();
    const map = {};
    (list || []).forEach((item) => {
      map[String(item.value)] = item.label;
    });
    kemuOptionMap.value = map;
  } catch (error) {
    console.warn('加载考试课程字典失败', error);
  }
};

const resolveCourseText = (kemuTypes) => {
  if (kemuTypes === null || kemuTypes === undefined || kemuTypes === '') return '';
  const key = String(kemuTypes);
  return kemuOptionMap.value[key] || key;
};

const loadExamInfoMeta = async (examInfoId) => {
  if (!examInfoId) return;
  await loadKemuOptionMap();
  try {
    const infoRes = await request.get(`examInfo/info/${examInfoId}`);
    const examInfo = infoRes?.data && typeof infoRes.data === 'object' ? infoRes.data : infoRes;
    if (!examInfo || typeof examInfo !== 'object') return;

    const examName = examInfo.examName ?? examInfo.exam_name;
    if (examName) {
      examPaperName.value = examName;
    }

    const kemuTypes = examInfo.kemuTypes ?? examInfo.kemu_types;
    const courseText = resolveCourseText(kemuTypes);
    if (courseText) examCourseRef.value = courseText;

    const sa = examInfo.showAnswerAfterSubmit ?? examInfo.show_answer_after_submit;
    showScoreInSubmitDialog.value = sa === null || sa === undefined ? null : Number(sa) === 1;

    // 顶栏「考试时长」在 examInfo/enter 成功后再与倒计时对齐写入，此处不写 duration，避免配置分钟与 endTimestamp 剩余不一致（断点续考、距窗口结束裁剪）
  } catch (error) {
    console.warn('加载考试信息详情失败', error);
  }
};

const answeredCount = computed(() => answerList.value.filter((item) => !!item.answer).length);
const progressPercent = computed(() => {
  if (!answerList.value.length) return 0;
  return Math.floor((answeredCount.value / answerList.value.length) * 100);
});

const answerSheetSections = computed(() => {
  const sections = [
    { key: "singleChoice", title: `一、单选题（共${groupedQuestions.value.singleChoice.length}题）`, questions: groupedQuestions.value.singleChoice },
    { key: "multipleChoice", title: `二、多选题（共${groupedQuestions.value.multipleChoice.length}题）`, questions: groupedQuestions.value.multipleChoice },
    { key: "trueFalse", title: `三、判断题（共${groupedQuestions.value.trueFalse.length}题）`, questions: groupedQuestions.value.trueFalse },
    { key: "fillBlank", title: `四、填空题（共${groupedQuestions.value.fillBlank.length}题）`, questions: groupedQuestions.value.fillBlank },
    { key: "shortAnswer", title: `五、简答题（共${groupedQuestions.value.shortAnswer.length}题）`, questions: groupedQuestions.value.shortAnswer },
    { key: "other", title: `六、其他题（共${groupedQuestions.value.other.length}题）`, questions: groupedQuestions.value.other }
  ];
  return sections.filter(section => section.questions.length > 0);
});

// 倒计时相关
const count = ref(0);
let timerInterval = null;
const examEndTimestamp = ref(0);
const serverTimeOffsetMs = ref(0);

/** 顶栏分钟数与右侧倒计时同源（count 秒），避免配置 duration 与 endTimestamp 在断点续考时不一致 */
const examDurationMinutesText = computed(() => {
  if (examEndTimestamp.value > 0 && !isSubmitted.value) {
    const m = Math.ceil(Math.max(0, count.value) / 60);
    return String(Math.max(m, 0));
  }
  const fromEnter = examDurationMinutesRef.value;
  if (fromEnter !== null && fromEnter !== undefined && fromEnter !== '') return String(fromEnter);
  const fromQuestionsDuration = dataList.value?.[0]?.duration;
  if (fromQuestionsDuration !== null && fromQuestionsDuration !== undefined && fromQuestionsDuration !== '') return String(fromQuestionsDuration);
  const fromQuestions = dataList.value?.[0]?.examPaperDate;
  if (fromQuestions !== null && fromQuestions !== undefined && fromQuestions !== '') return String(fromQuestions);
  return '-';
});

const updateRemainingSeconds = () => {
  if (!examEndTimestamp.value) return count.value;
  const syncedNow = Date.now() + serverTimeOffsetMs.value;
  const remain = Math.max(Math.floor((examEndTimestamp.value - syncedNow) / 1000), 0);
  count.value = remain;
  return remain;
};

// 计算属性：格式化时间
const formattedTime = computed(() => {
  return formatCountdownHms(count.value);
});

const getLocalProgressKey = () => currentExamRecordUuid.value ? `exam_progress_${currentExamRecordUuid.value}` : '';

const normalizeAnswer = (answer) => {
  if (!answer || answer === '未作答') {
    return '';
  }
  return answer;
};

const normalizeRouteParam = (value) => {
  if (value === undefined || value === null || value === '' || value === 'null' || value === 'undefined') {
    return null;
  }
  return value;
};

const serializeAnswerList = () => answerList.value.map(item => ({
  examQuestionId: item.examQuestionId,
  answer: item.answer || ''
}));

const persistLocalProgress = () => {
  const localKey = getLocalProgressKey();
  if (localKey && answerList.value.length > 0) {
    localStorage.setItem(localKey, JSON.stringify(answerList.value));
  }
};

/** 按题目 ID 合并本地缓存，避免试卷调序后答案错位 */
const mergeLocalExamProgressByQuestionId = (savedAnswers) => {
  if (!Array.isArray(savedAnswers) || answerList.value.length === 0) return 0;
  const byId = new Map();
  savedAnswers.forEach((s) => {
    if (!s || s.examQuestionId === undefined || s.examQuestionId === null || s.examQuestionId === '') return;
    const id = String(s.examQuestionId);
    if (s.answer) byId.set(id, s.answer);
  });
  let merged = 0;
  answerList.value.forEach((row) => {
    const id = String(row.examQuestionId);
    if (!byId.has(id)) return;
    const val = byId.get(id);
    if (!val) return;
    if (row.answer !== val) {
      row.answer = val;
      merged++;
    }
  });
  return merged;
};

const saveAnswersToServer = async (silent = true) => {
  if (!currentExamRecordUuid.value || isSubmitted.value || answerList.value.length === 0) {
    return;
  }

  // 验证必需的参数
  const examPaperId = normalizeRouteParam(route.query.examPaperId);
  if (!examPaperId) {
    console.error('保存答案失败：试卷ID为空', {
      routeQuery: route.query,
      examPaperId: route.query.examPaperId
    });
    if (!silent) {
      ElMessage.error('试卷ID丢失，请重新进入考试');
    }
    return;
  }

  try {
    const res = await request.post('examPaperTopic/saveAnswer', {
      examPaperId: examPaperId,
      examInfoId: normalizeRouteParam(route.query.examInfoId),
      examRecordUuid: currentExamRecordUuid.value,
      answerList: serializeAnswerList()
    });

    const recordUuid = res?.data?.examRecordUuid || res?.examRecordUuid;
    if (recordUuid) {
      currentExamRecordUuid.value = recordUuid;
    }
  } catch (error) {
    console.error('保存答案失败', error);
    if (!silent) {
      ElMessage.warning('答案保存失败，已保留本地进度');
    }
  }
};

const scheduleSave = () => {
  if (isInitializingAnswers.value || isSubmitted.value) {
    return;
  }
  if (saveDebounceTimer) {
    clearTimeout(saveDebounceTimer);
  }
  saveDebounceTimer = setTimeout(() => {
    saveAnswersToServer();
  }, 800);
};

const startAutoSave = () => {
  if (autoSaveTimer) {
    clearInterval(autoSaveTimer);
  }
  autoSaveTimer = setInterval(() => {
    saveAnswersToServer();
  }, 30000);
};

const stopAutoSave = () => {
  if (autoSaveTimer) {
    clearInterval(autoSaveTimer);
    autoSaveTimer = null;
  }
  if (saveDebounceTimer) {
    clearTimeout(saveDebounceTimer);
    saveDebounceTimer = null;
  }
};

// 初始化数据
const initData = async () => {
  //获取试卷题目
  const paperId = route.query.examPaperId;
  const examRecordUuid = route.query.examRecordUuid;

  console.log('=== 答题页面初始化 ===');
  console.log('examPaperId:', paperId);
  console.log('examRecordUuid:', examRecordUuid);

  if (!paperId) {
    ElMessage.error("参数错误：未找到试卷ID");
    return;
  }

  try {
    if (timerInterval) {
      clearInterval(timerInterval);
      timerInterval = null;
    }
    examEndTimestamp.value = 0;
    serverTimeOffsetMs.value = 0;

    // 1. 获取考试时间信息 (统一入口，无论首次进入还是断点续考)
    let totalTimeSeconds = 0;
    let hasTimeInfo = false; // 标记是否成功获取了后端时间信息
    const examInfoId = route.query.examInfoId;
    await loadExamInfoMeta(examInfoId);

    console.log('🔍 准备获取时间信息 - examInfoId:', examInfoId, 'examRecordUuid:', examRecordUuid);

    if (examInfoId) {
      try {
        // 从 sessionStorage 获取密码（由考试中心在进入时保存）
        const examPassword = sessionStorage.getItem(`exam_pwd_${examInfoId}`);
        const params = examPassword ? { password: examPassword } : {};

        console.log('📡 正在调用接口: examInfo/enter/' + examInfoId, params);
        const response = await request.get(`examInfo/enter/${examInfoId}`, { params });
        console.log('✅ 接口返回完整数据:', response);

        // 兼容处理：判断返回的是 Map 对象还是旧版字符串 UUID
        let timeData = response;

        // 如果返回的是字符串，说明后端未重启，提示用户
        if (typeof response === 'string') {
          console.error('❌ 后端返回了旧版字符串格式，请重启后端服务！');
          ElMessage.warning('后端服务未更新，正在尝试使用默认时长...');
          timeData = null; // 触发兜底逻辑
        } else if (response.data && typeof response.data === 'object' && response.data.examRecordUuid) {
          // 标准返回格式：response.data 包含业务数据
          console.log('📦 解析标准响应格式，业务数据在 response.data 中');
          timeData = response.data;
        } else if (response.examRecordUuid) {
          // 拦截器已解包的情况：response 本身就是业务数据
          console.log('📦 解析已解包响应格式，业务数据在 response 中');
          timeData = response;
        }

        if (timeData && typeof timeData === 'object' && timeData.examPaperDate && timeData.insertTime) {
          hasTimeInfo = true;
          currentExamRecordUuid.value = timeData.examRecordUuid || examRecordUuid || "";
          const totalMinutes = parseInt(timeData.examPaperDate, 10);
          if (!examCourseRef.value) {
            examCourseRef.value = timeData.kemuValue || timeData.subjectName || "";
          }
          const startTime = new Date(timeData.insertTime).getTime();
          const now = new Date().getTime();
          const serverTimestamp = Number(timeData.serverTimestamp || 0);
          const endTimestamp = Number(timeData.endTimestamp || 0);

          if (serverTimestamp > 0 && endTimestamp > 0) {
            // 用服务端时间校准本机时间，消除客户端时钟快慢导致的秒级偏差
            serverTimeOffsetMs.value = serverTimestamp - now;
            examEndTimestamp.value = endTimestamp;
            totalTimeSeconds = Math.max(Math.floor((endTimestamp - (now + serverTimeOffsetMs.value)) / 1000), 0);
            console.log(`⏱️ 时间计算详情(服务端时间戳): serverNow=${serverTimestamp}, end=${endTimestamp}, offset=${serverTimeOffsetMs.value}ms, 剩余=${totalTimeSeconds}秒`);
          } else {
            const endTime = startTime + totalMinutes * 60 * 1000;
            const elapsedSeconds = Math.floor((now - startTime) / 1000);
            serverTimeOffsetMs.value = 0;
            examEndTimestamp.value = endTime;
            totalTimeSeconds = Math.max(Math.floor((endTime - now) / 1000), 0);
            console.log(`⏱️ 时间计算详情(兼容模式): 总时长=${totalMinutes}分钟, 开始时间=${timeData.insertTime}, 已过=${elapsedSeconds}秒, 剩余=${totalTimeSeconds}秒`);
          }

          // 更新防作弊配置
          antiCheatConfig.value.screenSwitchCount = timeData.screenSwitchCount || 0;
          antiCheatConfig.value.allowScreenSwitch = timeData.allowScreenSwitch !== undefined ? timeData.allowScreenSwitch : 1;
          antiCheatConfig.value.maxScreenSwitch = timeData.maxScreenSwitch || 3;
          antiCheatConfig.value.allowCopyPaste = timeData.allowCopyPaste !== undefined ? timeData.allowCopyPaste : 1;

          if (totalTimeSeconds <= 0) {
            ElMessage.error("考试时间已到，无法开始或继续作答");
          }
        } else if (timeData && typeof timeData === 'object') {
          console.warn('⚠️ 接口返回数据缺少必要字段:', {
            hasExamPaperDate: !!timeData?.examPaperDate,
            hasInsertTime: !!timeData?.insertTime,
            dataStructure: timeData
          });
        }
      } catch (e) {
        console.error('❌ 获取考试时间信息失败:', e);
      }
    } else {
      console.warn('⚠️ 路由参数中未找到 examInfoId，无法获取精确时间');
    }

    // 2. 获取试卷题目
    const { data } = await request.get(`examPaperTopic/getExamQuestion`, {
      params: {
        examPaperId: paperId,
        examInfoId: route.query.examInfoId
      }
    });

    if (!data || data.length === 0) {
      ElMessage.warning("该试卷暂无题目");
      return;
    }

    // 初始化基础信息
    if (!examPaperName.value) {
      examPaperName.value = data[0].examPaperName;
    }
    examPaperMyscore.value = data[0].examPaperMyscore;

    // 处理题目数据
    dataList.value = data.map((item, index) => {
      // 解析选项 JSON 字符串
      let parsedOptions = [];
      if (item.examQuestionTypes != 4 && item.examQuestionOptions) {
        try {
          const rawOptions = JSON.parse(item.examQuestionOptions);

          // 检查是否是包含 code 和 text 字段的对象数组
          if (Array.isArray(rawOptions) && rawOptions.every(opt =>
            typeof opt === 'object' && opt !== null && 'code' in opt && 'text' in opt
          )) {
            // 直接使用包含 code 和 text 的对象数组
            parsedOptions = rawOptions;
          } else if (Array.isArray(rawOptions)) {
            // 处理旧格式的字符串数组
            parsedOptions = rawOptions.map((opt, index) => {
              // 提取选项代码（A/B/C/D等）
              const match = opt.match(/^([A-Za-z])./);
              if (match) {
                return {
                  code: match[1],
                  text: opt.substring(2).trim()
                };
              } else {
                // 如果没有匹配到前缀（如判断题的"正确"/"错误"），自动分配选项代码
                const code = String.fromCharCode(65 + index); // 65是'A'的ASCII码
                return {
                  code: code,
                  text: opt.trim()
                };
              }
            });
          }
        } catch (e) {
          console.error("解析选项失败", e);
        }
      }
      
      // 关键修复：显式设置 globalIndex，确保序号从1开始连续
      return { ...item, parsedOptions, globalIndex: index };
    });
    
    // 调试日志：验证题目顺序
    console.log('📋 题目列表加载完成，共', dataList.value.length, '题');
    console.log('🔢 前3道题的全局序号:', dataList.value.slice(0, 3).map((item, idx) => ({
      position: idx + 1,
      globalIndex: item.globalIndex,
      questionName: item.examQuestionName?.substring(0, 20)
    })));

    // 初始化答案数组
    answerList.value = data.map(item => ({
      examQuestionId: item.examQuestionId,
      examQuestionTypes: item.examQuestionTypes,
      answer: ""
    }));
    if (data.length > 0 && currentQuestionIndex.value < 0) {
      currentQuestionIndex.value = 0;
    }

    // 3. 如果有 examRecordUuid，尝试恢复答题进度
    const activeExamRecordUuid = currentExamRecordUuid.value || examRecordUuid;
    if (activeExamRecordUuid) {
      currentExamRecordUuid.value = activeExamRecordUuid;
      console.log('检测到考试记录UUID，尝试恢复答题进度...');

      // 优先尝试从本地缓存恢复
      const localKey = `exam_progress_${activeExamRecordUuid}`;
      const localData = localStorage.getItem(localKey);

      let localMergedCount = 0;
      if (localData) {
        try {
          const savedAnswers = JSON.parse(localData);
          console.log('从本地缓存恢复进度（按题目 ID），共', savedAnswers.length, '条');
          localMergedCount = mergeLocalExamProgressByQuestionId(savedAnswers);
        } catch (e) {
          console.error('解析本地缓存失败', e);
        }
      }
      const serverRestoredCount = await restoreExamProgress(activeExamRecordUuid);
      if (localMergedCount > 0 || serverRestoredCount > 0) {
        ElMessage.success('已恢复之前的答题进度');
      }
    }
    isInitializingAnswers.value = false;
    persistLocalProgress();
    startAutoSave();

    // 4. 启动倒计时
    console.log('🎯 倒计时启动判断 - totalTimeSeconds:', totalTimeSeconds, 'hasTimeInfo:', hasTimeInfo);

    if (totalTimeSeconds > 0) {
      if (!examEndTimestamp.value) {
        examEndTimestamp.value = Date.now() + totalTimeSeconds * 1000;
        serverTimeOffsetMs.value = 0;
      }
      count.value = totalTimeSeconds;
      startTimer();
      initAntiCheat(currentExamRecordUuid.value); // 初始化防作弊功能，传入 UUID
      console.log('✅ 倒计时已启动，剩余时间:', formattedTime.value);
    } else if (!hasTimeInfo && data[0].examPaperDate) {
      // 兜底方案：仅在未获取到后端精确时间时使用试卷默认时长
      count.value = parseInt(data[0].examPaperDate) * 60;
      examEndTimestamp.value = Date.now() + count.value * 1000;
      serverTimeOffsetMs.value = 0;
      startTimer();
      initAntiCheat(currentExamRecordUuid.value || examRecordUuid || '');
      console.warn('⚠️ 未能从后端获取精确时间，使用试卷默认时长:', count.value);
    } else if (hasTimeInfo && totalTimeSeconds <= 0) {
      // 如果获取了时间但已超时，不做处理（已在上面提示）
      console.log('⏰ 考试已超时，不启动倒计时');
    } else {
      ElMessage.warning("未能获取考试时长信息，请刷新重试");
    }

  } catch (e) {
    console.error(e);
    ElMessage.error("加载试题失败");
  }
};

// 倒计时逻辑
const startTimer = () => {
  if (timerInterval) {
    clearInterval(timerInterval);
    timerInterval = null;
  }

  updateRemainingSeconds();
  timerInterval = setInterval(() => {
    const remain = updateRemainingSeconds();
    if (remain <= 0) {
      clearInterval(timerInterval);
      timerInterval = null;
      ElMessage.warning("考试时间到，正在自动交卷...");
      handleSubmit(true); // 强制提交
    }
  }, 1000);
};

// 判断选项是否被选中
const isSelected = (index, code) => {
  const currentAnswer = answerList.value[index].answer;
  if (!currentAnswer) return false;

  // 多选逻辑 (逗号分隔)
  if (dataList.value[index].examQuestionTypes == 2) {
    return currentAnswer.split(',').includes(code);
  }
  // 单选逻辑
  return currentAnswer === code;
};

// 选题逻辑 (核心)
const handleSelect = (index, item, code) => {
  // 关键修复：如果已提交，禁止任何操作
  if (isSubmitted.value) {
    ElMessage.warning('试卷已提交，无法修改答案');
    return;
  }

  const type = item.examQuestionTypes;
  let currentAns = answerList.value[index].answer;

  if (type == 2) {
    // 多选题
    let arr = currentAns ? currentAns.split(',') : [];
    const idx = arr.indexOf(code);
    if (idx !== -1) {
      arr.splice(idx, 1); // 取消选中
    } else {
      arr.push(code); // 选中
    }
    answerList.value[index].answer = arr.join(',');
  } else {
    // 单选/判断题：点击选中，再次点击取消
    if (currentAns === code) {
      // 如果点击的是已选中的选项，则取消选中
      answerList.value[index].answer = "";
    } else {
      // 否则选中新选项
      answerList.value[index].answer = code;
    }
  }
  currentQuestionIndex.value = index;
  persistLocalProgress();
  scheduleSave();
};

// 仅在左侧 .question-list 内滚动到指定题，避免 scrollIntoView 滚动外层导致整页上移
const scrollToQuestion = (index) => {
  setCurrentQuestion(index);
  saveAnswersToServer();
  nextTick(() => {
    const el = document.getElementById(`question-${index}`);
    const container = questionListRef.value;
    if (!el) return;
    if (!container) {
      el.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      return;
    }
    const cRect = container.getBoundingClientRect();
    const eRect = el.getBoundingClientRect();
    const elTopInContent = eRect.top - cRect.top + container.scrollTop;
    const targetTop =
      elTopInContent - container.clientHeight / 2 + eRect.height / 2;
    const maxTop = Math.max(0, container.scrollHeight - container.clientHeight);
    const top = Math.max(0, Math.min(targetTop, maxTop));
    container.scrollTo({ top, behavior: 'smooth' });
  });
};

const setCurrentQuestion = (index) => {
  currentQuestionIndex.value = index;
};

const questionNavLabel = computed(() => {
  const total = answerList.value.length;
  if (!total) return "0 / 0";
  let cur = currentQuestionIndex.value;
  if (cur < 0) cur = 0;
  if (cur >= total) cur = total - 1;
  return `${cur + 1} / ${total}`;
});

const canGoPrev = computed(() => {
  const total = answerList.value.length;
  if (total <= 1) return false;
  const cur = currentQuestionIndex.value < 0 ? 0 : currentQuestionIndex.value;
  return cur > 0;
});

const canGoNext = computed(() => {
  const total = answerList.value.length;
  if (total <= 1) return false;
  const cur = currentQuestionIndex.value < 0 ? 0 : currentQuestionIndex.value;
  return cur < total - 1;
});

const goPrevQuestion = () => {
  if (isSubmitted.value) return;
  const total = answerList.value.length;
  if (total === 0) return;
  let cur = currentQuestionIndex.value;
  if (cur < 0) cur = 0;
  scrollToQuestion(Math.max(0, cur - 1));
};

const goNextQuestion = () => {
  if (isSubmitted.value) return;
  const total = answerList.value.length;
  if (total === 0) return;
  let cur = currentQuestionIndex.value;
  if (cur < 0) cur = 0;
  scrollToQuestion(Math.min(total - 1, cur + 1));
};

// 恢复考试进度（断点续考），返回成功回填的题目数（由调用方统一提示）
const restoreExamProgress = async (uuid) => {
  try {
    console.log('正在从后端获取答题记录:', uuid);
    const { data } = await request.get(`examRecord/examPaperInfo/${uuid}`);

    if (!data || !data.examDetailsRespList || data.examDetailsRespList.length === 0) {
      console.log('未找到历史答题记录，将以新考试开始');
      return 0;
    }

    console.log('找到历史答题记录，共', data.examDetailsRespList.length, '题');

    // 调试：打印第一条记录的字段信息
    if (data.examDetailsRespList.length > 0) {
      console.log('历史答案记录样例:', {
        examQuestionId: data.examDetailsRespList[0].examQuestionId,
        examQuestionIdType: typeof data.examDetailsRespList[0].examQuestionId,
        examDetailsMyanswer: data.examDetailsRespList[0].examDetailsMyanswer
      });
    }

    // 调试：打印当前 answerList 的第一项
    if (answerList.value.length > 0) {
      console.log('当前 answerList 样例:', {
        examQuestionId: answerList.value[0].examQuestionId,
        examQuestionIdType: typeof answerList.value[0].examQuestionId
      });
    }

    // 构建答案映射表：examQuestionId -> answer
    const answerMap = {};
    data.examDetailsRespList.forEach((detail, idx) => {
      if (detail.examQuestionId && detail.examDetailsMyanswer) {
        const key = String(detail.examQuestionId);
        answerMap[key] = normalizeAnswer(detail.examDetailsMyanswer);
        console.log(`[历史记录 ${idx}] examQuestionId: ${key}, 答案: ${detail.examDetailsMyanswer}`);
      }
    });

    console.log('--- 开始匹配当前试卷题目 ---');
    // 回填答案到 answerList
    let restoredCount = 0;
    answerList.value.forEach((item, index) => {
      const currentId = String(item.examQuestionId);
      const matchedAnswer = answerMap[currentId];

      if (matchedAnswer !== undefined) {
        item.answer = matchedAnswer;
        restoredCount++;
        console.log(`✅ 题目 ${index + 1} 匹配成功 (ID: ${currentId})`);
      } else {
        console.warn(`❌ 题目 ${index + 1} 未匹配 (ID: ${currentId}), 历史 IDs:`, Object.keys(answerMap));
      }
    });

    console.log(`成功恢复 ${restoredCount} 道题的答题进度`);

    return restoredCount;
  } catch (error) {
    console.error('恢复答题进度失败:', error);
    ElMessage.warning('无法恢复之前的答题进度，将以新考试开始');
    return 0;
  }
};

// 提交试卷
const handleSubmit = async (force = false) => {

  if (submitting.value) {
    ElMessage.warning('试卷正在提交中，请勿重复提交');
    return;
  }

  if (isSubmitted.value) {
    ElMessage.warning('试卷已提交，无需重复操作');
    return;
  }

  // 1. 检查未做题目
  const notDone = [];
  answerList.value.forEach((item, idx) => {
    if (!item.answer) notDone.push(idx + 1);
  });

  if (!force) {
    let msg = notDone.length > 0
      ? `您有 ${notDone.length} 道题未做（第 ${notDone.join(', ')} 题），确定要提交吗？`
      : '确定要提交试卷吗？';
    try {
      await ElMessageBox.confirm(msg, '交卷提示', {
        confirmButtonText: '确定交卷',
        cancelButtonText: '再检查一下',
        type: 'warning'
      });
    } catch {
      return; // 用户取消
    }
  }

  submitting.value = true;
  stopAutoSave();
  await saveAnswersToServer();

  // 验证必需的参数
  const examPaperId = route.query.examPaperId;
  if (!examPaperId) {
    ElMessage.error('试卷ID丢失，无法提交试卷');
    submitting.value = false;
    return;
  }

  // 构造参数
  const params = {
    // usersId: userInfo.value.id, // 如果后端session里取不到这里要传

    examPaperId: examPaperId,
    examInfoId: normalizeRouteParam(route.query.examInfoId), // 添加考试ID，用于精确关联考试记录
    examRecordUuid: currentExamRecordUuid.value,
    forceSubmit: !!force,
    answerList: JSON.stringify(answerList.value)
  };

  try {
    const result = await request.post(`examPaperTopic/submit`, params);

    // 关键修复：提交成功后立即标记为已提交状态
    isSubmitted.value = true;

    // 清除定时器
    if (timerInterval) {
      clearInterval(timerInterval);
      timerInterval = null;
    }

    // 检查后端是否返回成绩数据
    let score = null;
    let hasScore = false;

    // 尝试从各种可能的字段获取成绩
    if (result.data?.score !== undefined) {
      score = result.data.score;
      hasScore = true;
    } else if (result.score !== undefined) {
      score = result.score;
      hasScore = true;
    } else if (result.data?.data?.score !== undefined) {
      score = result.data.data.score;
      hasScore = true;
    }

    const totalScore = examPaperMyscore.value || 100;

    const allowScorePopup =
      showScoreInSubmitDialog.value === null ? true : showScoreInSubmitDialog.value === true;

    if (!allowScorePopup) {
      await ElMessageBox.alert(
        '成绩将在考试结束并阅卷完成后发布，请前往「我的成绩」查看',
        '考试结果',
        {
          confirmButtonText: '确定',
          type: 'success',
          center: true,
          showClose: false
        }
      );
    } else if (hasScore && score !== null) {
      await ElMessageBox.alert(
        `您的考试成绩：${score} 分 / ${totalScore} 分`,
        '考试结果',
        {
          confirmButtonText: '确定',
          type: 'success',
          center: true,
          showClose: false
        }
      );
    } else {
      await ElMessageBox.alert(
        '试卷提交成功！\n\n成绩信息请在考试记录中查看。',
        '交卷成功',
        {
          confirmButtonText: '确定',
          type: 'success',
          center: true,
          showClose: false
        }
      );
    }

    // 跳转回列表页
    // 跳转回列表页前清理缓存
    const localKey = getLocalProgressKey();
    if (localKey) {
      localStorage.removeItem(localKey);
    }

    // 全页跳转：带上 Vite base 与 hash，与 createWebHashHistory 一致，避免生产环境路径错误
    setTimeout(() => {
      const base = import.meta.env.BASE || '/';
      const withSlash = base.endsWith('/') ? base : `${base}/`;
      window.location.assign(`${withSlash}#/index/examCenter`);
    }, 500);
  } catch (e) {
    ElMessage.error("交卷失败，请重试");
    submitting.value = false;
    startAutoSave();
  }
};

const applyScreenSwitchReport = async ({ screenSwitchCount, exceeded }) => {
  antiCheatConfig.value.screenSwitchCount = screenSwitchCount;
  ElMessage.warning(
    `警告：检测到切屏行为 (${antiCheatConfig.value.screenSwitchCount}/${antiCheatConfig.value.maxScreenSwitch})`
  );
  if (exceeded) {
    try {
      await ElMessageBox.alert(
        `您已超过最大切屏次数（${antiCheatConfig.value.screenSwitchCount}/${antiCheatConfig.value.maxScreenSwitch}），系统将强制交卷。`,
        '强制交卷',
        { type: 'warning', confirmButtonText: '确定', showClose: false }
      );
    } catch (e) {
      // ignore
    }
    handleSubmit(true);
  }
};

/** 自动全屏失败后，用户第一次点击考试区域时同步调用 requestFullscreen（保留用户激活） */
const attachOneClickFullscreenRetry = () => {
  cleanupClickFullscreenRetry();
  const el = examRootRef.value;
  if (!el) return;
  const handler = () => {
    if (antiCheatConfig.value.allowScreenSwitch !== 0 || isSubmitted.value) {
      cleanupClickFullscreenRetry();
      return;
    }
    const root = document.documentElement;
    if (document.fullscreenElement === root) {
      cleanupClickFullscreenRetry();
      return;
    }
    root.requestFullscreen().catch(() => {
      ElMessage.warning('仍无法进入全屏，请检查浏览器权限或使用支持全屏的环境。');
    });
    cleanupClickFullscreenRetry();
  };
  el.addEventListener('click', handler, { once: true, capture: false });
  clickFullscreenRetryCleanup = () => {
    el.removeEventListener('click', handler, { capture: false });
  };
};

/** 进入考试页后尝试全屏（禁止切屏时）；失败不阻断考试；失败则绑定「首次点击页面再试一次」 */
const attemptEnterExamFullscreen = async () => {
  if (antiCheatConfig.value.allowScreenSwitch !== 0 || isSubmitted.value) return true;
  const root = document.documentElement;
  if (document.fullscreenElement === root) return true;
  try {
    await root.requestFullscreen();
    return true;
  } catch (e) {
    console.warn('自动全屏失败', e);
    ElMessage.warning(
      '请尽量使用全屏作答；点击考试页面任意处可再次尝试进入全屏。离开考试页或退出全屏均会计切屏次数。'
    );
    nextTick(() => attachOneClickFullscreenRetry());
    return false;
  }
};

// 防作弊功能初始化（监听移除统一在 clearAntiCheatListeners / 组件 onUnmounted）
const initAntiCheat = (examRecordUuid) => {
  clearAntiCheatListeners();

  if (antiCheatConfig.value.allowScreenSwitch === 0) {
    registerExamScreenSwitchContext({
      getExamRecordUuid: () => currentExamRecordUuid.value || examRecordUuid || '',
      getAllowScreenSwitch: () => antiCheatConfig.value.allowScreenSwitch,
      getScreenSwitchCount: () => antiCheatConfig.value.screenSwitchCount,
      isSubmitted: () => isSubmitted.value,
      saveAnswersToServer: () => saveAnswersToServer(),
      onReported: applyScreenSwitchReport,
    });
  }

  if (antiCheatConfig.value.allowCopyPaste === 0) {
    antiCheatClipboardHandler = (e) => {
      e.preventDefault();
      ElMessage.warning('考试期间禁止复制、剪切或粘贴');
    };
    document.addEventListener('copy', antiCheatClipboardHandler);
    document.addEventListener('cut', antiCheatClipboardHandler);
    document.addEventListener('paste', antiCheatClipboardHandler);
  }

  if (antiCheatConfig.value.allowScreenSwitch === 0) {
    antiCheatVisibilityHandler = () => {
      if (document.hidden && !isSubmitted.value) {
        void tryReportExamScreenSwitch('visibility');
      }
    };
    document.addEventListener('visibilitychange', antiCheatVisibilityHandler);

    antiCheatFullscreenHandler = () => {
      if (isSubmitted.value || antiCheatConfig.value.allowScreenSwitch !== 0) return;
      const fs = document.fullscreenElement;
      if (fs) {
        examHadFullscreen.value = true;
      } else if (examHadFullscreen.value) {
        examHadFullscreen.value = false;
        void tryReportExamScreenSwitch('fullscreen');
      }
    };
    document.addEventListener('fullscreenchange', antiCheatFullscreenHandler);

    nextTick(() => {
      void attemptEnterExamFullscreen();
    });
  }
};

// 在组件加载时开启监听
onMounted(() => {
  initData();

  beforeUnloadHandler = () => {
    persistLocalProgress();
    saveAnswersToServer();
  };
  window.addEventListener('beforeunload', beforeUnloadHandler);

  watch(answerList, (newVal) => {
    if (currentExamRecordUuid.value && newVal.length > 0) {
      persistLocalProgress();
      scheduleSave();
    }
  }, { deep: true });
});

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval);
  stopAutoSave();
  clearAntiCheatListeners();
  persistLocalProgress();
  saveAnswersToServer();
  if (beforeUnloadHandler) {
    window.removeEventListener('beforeunload', beforeUnloadHandler);
  }
});
</script>

<style lang="scss" scoped>
.exam-container {
  height: 100%;
  min-height: 0;
  flex: 1 1 auto;
  background: #f8fafc;
  padding-bottom: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.exam-header {
  flex-shrink: 0;
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 20px;
  background: #f8fafc;
  border-bottom: none;
  padding: 10px 16px 12px;
  margin: 0;
  border-radius: 0;

  .paper-title {
    margin: 0;
    font-size: 30px;
    font-weight: 700;
    color: #1f2533;
  }

  .header-submit-btn {
    min-width: 72px;
    height: 34px;
    border-radius: 8px;
    border: 1px solid #c7d2fe;
    color: #4338ca;
    background: #eef2ff;
    font-weight: 600;
  }
}

.header-center {
  justify-self: center;
  display: flex;
  align-items: center;
  gap: 100px;
  color: #374151;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
}

.header-right {
  justify-self: end;
  display: flex;
  align-items: center;
  gap: 16px;
}
.exam-body {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 16px;
  padding: 14px 16px 12px;
  max-width: none;
  margin: 0;
  flex: 1 1 auto;
  min-height: 0;
  overflow: hidden;
  align-items: stretch;
}

.exam-body > * {
  min-height: 0;
}

.question-column {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  height: 100%;
  background: #f8fafc;
  border-radius: 10px;
  border: 1px solid #e9edf5;
  overflow: hidden;
}

.question-list {
  min-width: 0;
  min-height: 0;
  flex: 1 1 auto;
  overflow-y: auto;
  padding: 12px 12px 20px;
  scrollbar-width: thin;
}

.question-list::-webkit-scrollbar {
  width: 8px;
}

.question-list::-webkit-scrollbar-thumb {
  background: #c7d2e0;
  border-radius: 4px;
}

.question-list::-webkit-scrollbar-track {
  background: transparent;
}

.question-group {
  margin-bottom: 12px;
  border: 1px solid #e9edf5;
  border-radius: 10px;
  background: #fff;
  overflow: hidden;
}

.group-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 14px;
  border-bottom: 1px solid #e7eaf3;
  border-radius: 0;
  background: #fff;
}

.group-title {
  font-size: 16px;
  font-weight: 700;
  color: #1f2533;
}

.group-info {
  color: #6b778c;
  font-size: 13px;
}

.question-card {
  background: #fff;
  border: none;
  border-top: 1px solid #e7eaf3;
  border-radius: 0;
  padding: 16px 16px 14px;
  box-shadow: none;
  position: relative;

  &:last-child {
    border-radius: 0;
  }

  .q-header {
    position: absolute;
    left: 16px;
    top: 16px;
    margin-bottom: 0;
  }

  .q-index {
    width: 26px;
    height: 26px;
    border-radius: 8px;
    background: #4f46e5;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 13px;
  }

  .q-content {
    padding-left: 34px;
  }

  .q-title {
    font-size: 15px;
    line-height: 1.75;
    color: #1f2533;
    margin-bottom: 14px;

    :deep(img) {
      max-width: 100%;
      height: auto;
      display: block;
      margin: 10px 0;
      border-radius: 6px;
    }

    :deep(table) {
      width: 100%;
      border-collapse: collapse;
      margin: 10px 0;
      table-layout: fixed;
      word-break: break-word;
    }

    :deep(th),
    :deep(td) {
      border: 1px solid #dfe6f2;
      padding: 8px 10px;
      text-align: left;
      vertical-align: top;
      font-size: 13px;
      color: #2b3648;
    }

    :deep(th) {
      background: #f6f8fc;
      font-weight: 600;
    }
  }
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  padding: 12px 14px;
  cursor: pointer;
  background: #fff;

  &:hover {
    border-color: #c7d2fe;
  }

  &.selected {
    background: #eef2ff;
    border-color: #c7d2fe;
  }

  .opt-code {
    width: 20px;
    height: 20px;
    border-radius: 50%;
    border: 2px solid #cbd5e1;
    color: transparent;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-size: 0;
    font-weight: 700;
    background: #fff;
    flex-shrink: 0;
  }

  &.selected .opt-code {
    border-color: #4f46e5;
    box-shadow: inset 0 0 0 5px #4f46e5;
  }

  .opt-text {
    color: #1f2533;
    font-size: 14px;
  }
}

.text-answer-area {
  max-width: 100%;
}

.question-nav-bar {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 16px 14px;
  background: #fff;
  border-top: 1px solid #e7eaf3;
}

.nav-btn {
  min-width: 88px;
  height: 38px;
  padding: 0 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s, color 0.15s, opacity 0.15s;
}

.nav-btn:disabled {
  cursor: not-allowed;
  opacity: 0.45;
}

.nav-btn-prev {
  border: 1px solid #d1d5db;
  background: #fff;
  color: #374151;
}

.nav-btn-prev:not(:disabled):hover {
  border-color: #9ca3af;
  background: #f9fafb;
}

.nav-btn-next {
  border: 1px solid #4f46e5;
  background: #4f46e5;
  color: #fff;
}

.nav-btn-next:not(:disabled):hover {
  background: #4338ca;
  border-color: #4338ca;
}

.nav-progress {
  font-size: 15px;
  font-weight: 600;
  color: #1f2533;
  flex: 1;
  text-align: center;
}

.sidebar-wrapper {
  width: 360px;
  min-height: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
  padding-bottom: 16px;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE/旧 Edge */
}

.sidebar-wrapper::-webkit-scrollbar {
  width: 0;
  height: 0;
}

.time-card,
.answer-sheet-card {
  background: #fff;
  border: 1px solid #e9edf5;
  border-radius: 10px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(17, 24, 39, 0.06);
}

.answer-sheet-card {
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.time-card {
  flex-shrink: 0;
  margin-bottom: 0;
}

.time-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 16px;
  font-weight: 700;
  color: #1f2533;
  margin-bottom: 6px;
}

.collapse-btn {
  height: auto;
  min-width: 0;
  border: none;
  border-radius: 0;
  background: transparent;
  color: #4f46e5;
  font-size: 15px;
  font-weight: 600;
  line-height: 1;
  padding: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  cursor: pointer;
}

.chev {
  width: 7px;
  height: 7px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-right: 2px solid #4f46e5;
  border-bottom: 2px solid #4f46e5;
  transform: rotate(-135deg);
  margin-top: 1px;
}

.chev.down {
  transform: rotate(45deg);
}

.time-value {
  margin-top: 10px;
  margin-bottom: 12px;
  font-size: 36px;
  line-height: 1;
  font-weight: 700;
  color: #4f46e5;
  text-align: center;
  letter-spacing: 0.5px;
}

.time-value.urgent {
  color: #e24a45;
}

.progress-row {
  display: flex;
  justify-content: space-between;
  color: #5f6d84;
  font-size: 13px;
  margin-bottom: 10px;
}

.sheet-title {
  flex-shrink: 0;
  font-size: 18px;
  font-weight: 700;
  color: #1f2533;
  margin-bottom: 0;
}

.sheet-legend {
  flex-shrink: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 10px 12px;
  margin-bottom: 0;
  color: #67748d;
  font-size: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 2px;
  background: #fff;
  border: 1px solid #d7dfef;
}

.dot.done {
  background: #4338ca;
  border-color: #4338ca;
}

.dot.current {
  background: #52c596;
  border-color: #52c596;
}

.answer-sheet-integrity {
  flex-shrink: 0;
  padding: 12px;
  background: #f3f4f6;
  border-radius: 8px;
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.integrity-icon {
  flex-shrink: 0;
  color: #6b7280;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 1px;
}

.integrity-text {
  margin: 0;
  font-size: 12px;
  line-height: 1.55;
  color: #6b7280;
}

.answer-sheet-card .sheet-section-wrap {
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
  padding-right: 4px;
  scrollbar-width: thin;
}

.answer-sheet-card .sheet-section-wrap::-webkit-scrollbar {
  width: 6px;
}

.answer-sheet-card .sheet-section-wrap::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.sheet-section + .sheet-section {
  margin-top: 12px;
}

.sheet-section-title {
  font-size: 12px;
  color: #5d6a81;
  margin-bottom: 8px;
}

.sheet-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 10px;
}

.sheet-item {
  height: 40px;
  border-radius: 6px;
  border: 1px solid #d7dfef;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #5d6a81;
  font-weight: 600;
  cursor: pointer;
  background: #fff;
  font-size: 14px;

  &.done {
    background: #4338ca;
    border-color: #4338ca;
    color: #ffffff;
  }

  &.current {
    background: #52c596;
    border-color: #52c596;
    color: #ffffff;
  }
}

@media (max-width: 1200px) {
  .exam-header .paper-title {
    font-size: 24px;
  }

  .exam-header .header-center {
    font-size: 17px;
    gap: 18px;
  }

  .group-title,
  .sheet-title {
    font-size: 22px;
  }
}

@media (max-width: 992px) {
  .exam-header {
    grid-template-columns: 1fr;
    justify-items: start;
    margin: 0 12px;
  }

  .exam-body {
    grid-template-columns: 1fr;
    padding: 0 12px 0;
  }

  .sidebar-wrapper {
    width: 100%;
  }
}
</style>