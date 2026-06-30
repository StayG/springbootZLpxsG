<template>
  <div class="personal-center-page">
    <div class="page-head">
      <div class="title">个人中心</div>
      <div class="sub">管理个人信息，查看学习情况</div>
    </div>

    <div class="profile-card">
      <div class="left">
        <div class="avatar">
          <el-avatar :size="88" :src="avatarUrl">{{ displayName.slice(0, 1) }}</el-avatar>
        </div>
        <div class="info">
          <div class="name-row">
            <div class="name">{{ displayName }}</div>
            <el-tag size="small" effect="light" class="role-tag">{{ roleText }}</el-tag>
          </div>
          <div class="meta">
            <div class="meta-item">
              <el-icon><Document /></el-icon>
              <span>账号：{{ userInfo.userName || "-" }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Phone /></el-icon>
              <span>手机号：{{ userInfo.phone || userInfo.mobile || "-" }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Male /></el-icon>
              <span>性别：{{ userInfo.gender || "-" }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Message /></el-icon>
              <span>邮箱：{{ userInfo.email || "-" }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="right">
        <el-button type="primary" plain class="edit-btn" @click="openEditProfile">
          <el-icon><Edit /></el-icon>
          编辑资料
        </el-button>
      </div>
    </div>

    <div class="overview-grid">
      <div class="overview-card">
        <div class="icon blue"><el-icon><CircleCheckFilled /></el-icon></div>
        <div class="content">
          <div class="label">已完成考试</div>
          <div class="value">{{ overview.finished }} <span>场</span></div>
        </div>
      </div>
      <div class="overview-card">
        <div class="icon green"><el-icon><TrendCharts /></el-icon></div>
        <div class="content">
          <div class="label">平均成绩</div>
          <div class="value">{{ overview.avgScore }} <span>分</span></div>
        </div>
      </div>
      <div class="overview-card">
        <div class="icon purple"><el-icon><Trophy /></el-icon></div>
        <div class="content">
          <div class="label">最高成绩</div>
          <div class="value">{{ overview.maxScore }} <span>分</span></div>
        </div>
      </div>
      <div class="overview-card">
        <div class="icon orange"><el-icon><Medal /></el-icon></div>
        <div class="content">
          <div class="label">获得证书</div>
          <div class="value">{{ overview.certCount }} <span>个</span></div>
        </div>
      </div>
    </div>

    <div class="block">
      <div class="block-head">
        <div class="h">最近考试</div>
        <div class="more" @click="goScorePage">查看全部 <el-icon><ArrowRight /></el-icon></div>
      </div>

      <div v-if="recentList.length" class="recent-list">
        <div v-for="item in recentList" :key="item.key" class="recent-row">
          <div class="row-left">
            <div class="row-icon" :class="item.iconClass">
              <el-icon><Document /></el-icon>
            </div>
            <div class="row-main">
              <div class="row-title">{{ item.title }}</div>
              <div class="row-sub">{{ item.timeText }}</div>
            </div>
          </div>
          <div class="row-tail">
            <div class="row-score-col">
              <div class="row-score-label">{{ item.rowKind === "published" ? "成绩" : "" }}</div>
              <div class="row-score-value" :class="{ 'is-pending': item.rowKind === 'pending' }">{{ item.scoreDisplay }}</div>
            </div>
            <div class="row-right">
              <el-button class="detail-btn" @click="viewExamDetail(item)">{{ item.actionLabel }}</el-button>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty">暂无考试记录</div>
    </div>

    <div class="block">
      <div class="block-head">
        <div class="h">账户设置</div>
      </div>
      <div class="settings-grid">
        <div class="setting-item" @click="openChangePassword">
          <el-icon><Lock /></el-icon>
          <span>修改密码</span>
          <el-icon class="arrow"><ArrowRight /></el-icon>
        </div>
        <div class="setting-item" @click="openEditProfile">
          <el-icon><Message /></el-icon>
          <span>绑定邮箱</span>
          <el-icon class="arrow"><ArrowRight /></el-icon>
        </div>
        <div class="setting-item disabled">
          <el-icon><Bell /></el-icon>
          <span>通知设置</span>
          <el-icon class="arrow"><ArrowRight /></el-icon>
        </div>
        <div class="setting-item disabled">
          <el-icon><Hide /></el-icon>
          <span>隐私设置</span>
          <el-icon class="arrow"><ArrowRight /></el-icon>
        </div>
        <div class="setting-item disabled">
          <el-icon><Key /></el-icon>
          <span>安全设置</span>
          <el-icon class="arrow"><ArrowRight /></el-icon>
        </div>
        <div class="setting-item danger" @click="logout">
          <el-icon><SwitchButton /></el-icon>
          <span>退出登录</span>
          <el-icon class="arrow"><ArrowRight /></el-icon>
        </div>
      </div>
    </div>

    <el-dialog v-model="editDialogVisible" title="编辑资料" width="640px" class="edit-dialog">
      <el-form :model="editForm" label-width="90px" :rules="profileRules" ref="profileFormRef">
        <el-form-item label="用户名">
          <el-input v-model="editForm.userName" disabled />
        </el-form-item>
        <el-form-item v-if="isTeacher" label="真实姓名" prop="realName">
          <el-input v-model="editForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item v-else label="姓名" prop="nickname">
          <el-input v-model="editForm.nickname" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="头像">
          <file-upload action="file/upload" :fileUrls="editForm.avatar" :limit="1" :show-file-list="false" @change="handleAvatarChange" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="editForm.gender" placeholder="请选择性别" style="width: 100%">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { ElMessage } from "element-plus";
import {
  ArrowRight,
  Bell,
  CircleCheckFilled,
  Document,
  Edit,
  Hide,
  Key,
  Lock,
  Medal,
  Message,
  Male,
  Phone,
  SwitchButton,
  TrendCharts,
  Trophy,
  User,
} from "@element-plus/icons-vue";
import { useUserStore } from "@/stores/user";
import { useModalsStore } from "@/stores/modals";
import { useRoute, useRouter } from "vue-router";
import storage from "@/utils/storage";
import config from "@/config/config";
import request from "@/utils/request.js";
import { setLoggingOut } from "@/utils/request.js";

const userStore = useUserStore();
const route = useRoute();
const router = useRouter();
const tableName = storage.get(config.CURRENT_SESSION_TABLE_KEY);
const modals = useModalsStore();

const userInfo = ref({});
const editForm = ref({});
const profileFormRef = ref();
const editDialogVisible = ref(false);

const overview = ref({
  finished: 0,
  avgScore: 0,
  maxScore: 0,
  certCount: 0,
});
const recentList = ref([]);

// 判断是否是教师角色
const isTeacher = computed(() => tableName === "teachers");
const roleText = computed(() => (isTeacher.value ? "教师" : "学生"));
const displayName = computed(() => userInfo.value?.nickname || userInfo.value?.realName || userInfo.value?.userName || "同学");

// 处理头像 URL（添加 baseUrl 前缀）
const avatarUrl = computed(() => {
  const avatar = userInfo.value?.avatar;
  if (!avatar) return '';
  
  // 如果已经是完整的 URL（http 或 https 开头），直接返回
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar;
  }
  
  // 如果是相对路径，拼接 baseUrl
  // 确保路径以 / 开头
  const path = avatar.startsWith('/') ? avatar : `/${avatar}`;
  return `${config.baseUrl}${path}`;
});

onMounted(() => {
  if (route.query.tab === "updatePassword") {
    modals.openChangePassword();
  }
  loadOverview();
});

// 表单校验规则（根据角色动态设置）
const profileRules = computed(() => {
  const rules = {
    email: [
      { required: true, message: "请输入邮箱", trigger: "blur" },
      { type: "email", message: "邮箱格式不正确", trigger: ["blur", "change"] }
    ],
    phone: [
      { required: true, message: "请输入手机号", trigger: "blur" },
      { pattern: /^1[3-9]\d{9}$/, message: "手机号格式不正确", trigger: ["blur", "change"] }
    ]
  };
  
  // 教师使用 realName，学生使用 nickname
  if (isTeacher.value) {
    rules.realName = [{ required: true, message: "请输入真实姓名", trigger: "blur" }];
  } else {
    rules.nickname = [{ required: true, message: "请输入姓名", trigger: "blur" }];
  }
  
  return rules;
});

// 头像回调
const handleAvatarChange = (fileUrls) => {
  editForm.value.avatar = fileUrls;
};

// 保存
const saveEdit = () => {
  profileFormRef.value.validate(async (valid) => {
    if (!valid) return;
    await request.post(`/${tableName}/update`, editForm.value);
    const { data } = await request.get(`/${tableName}/session`);
    userStore.setUserInfo(data);
    ElMessage.success("保存成功");
    editDialogVisible.value = false;
  });
};

const openEditProfile = () => {
  editForm.value = { ...userInfo.value };
  editDialogVisible.value = true;
};

const openChangePassword = () => {
  modals.openChangePassword();
};

const goScorePage = () => {
  router.push("/index/studentExamRecord");
};

const viewExamDetail = (item) => {
  if (!item?.uuid) return;
  if (item.rowKind === "pending") {
    router.push({ path: "/index/examRecord", query: { uuid: item.uuid, view: "answerSheet" } });
    return;
  }
  router.push({ path: "/index/examRecord", query: { uuid: item.uuid } });
};

const logout = async () => {
  // 设置退出登录标志，避免其他请求失败时显示错误提示
  setLoggingOut(true);
  
  try {
    const t = storage.get(config.CURRENT_SESSION_TABLE_KEY);
    if (t) await request.post(`/${t}/logout`);
  } catch (e) {
    // ignore backend logout error
  } finally {
    userStore.reset();
    ElMessage.success("退出登录成功");
    router.push("/login");
    // 重置标志
    setTimeout(() => setLoggingOut(false), 1000);
  }
};

const loadOverview = async () => {
  try {
    const { data } = await request.post("/examRecord/page", { page: 1, limit: 2000, onlyLatest: 1 });
    const list = data?.list || [];
    const finishedList = list.filter((r) => [1, 2, 3].includes(Number(r?.status)));
    /** 成绩已发布：与「我的成绩」页 getRowKind === published 一致 */
    const publishedList = finishedList.filter((r) => !(Number(r?.pendingReviewCount) > 0));
    const scores = publishedList.map((r) => Number(r?.totalScore)).filter((n) => Number.isFinite(n));
    const maxScore = scores.length ? Math.max(...scores) : 0;
    const avgScore = scores.length ? Math.round((scores.reduce((a, b) => a + b, 0) / scores.length) * 10) / 10 : 0;
    const certCount = publishedList.filter((r) => {
      const total = Number(r?.examPaperMyscore || r?.examPaperScore || 100);
      const s = Number(r?.totalScore);
      if (!Number.isFinite(total) || total <= 0 || !Number.isFinite(s)) return false;
      return (s / total) * 100 >= 90;
    }).length;

    overview.value = {
      finished: finishedList.length,
      avgScore,
      maxScore,
      certCount,
    };

    const getRecentRowKind = (r) => {
      if (Number(r?.pendingReviewCount) > 0) return "pending";
      return "published";
    };

    const formatRecentScoreDisplay = (r, kind) => {
      if (kind === "pending") return "待阅卷";
      const user = Number(r?.totalScore);
      const full = Number(r?.examPaperMyscore ?? r?.exam_paper_myscore);
      if (Number.isFinite(user) && Number.isFinite(full) && full > 0) return `${user}/${full}`;
      if (Number.isFinite(user)) return `${user}分`;
      return "—";
    };

    const recent = finishedList
      .slice()
      .sort((a, b) => new Date(b?.insertTime || 0).getTime() - new Date(a?.insertTime || 0).getTime())
      .slice(0, 4)
      .map((r, idx) => {
        const rowKind = getRecentRowKind(r);
        return {
          key: `${r?.examInfoId || r?.examPaperId || r?.id || idx}`,
          uuid: r?.examRecordUuidNumber,
          title: r?.examName || r?.examPaperName || "考试",
          timeText: formatTimeRange(r),
          rowKind,
          scoreDisplay: formatRecentScoreDisplay(r, rowKind),
          actionLabel: rowKind === "pending" ? "查看答卷" : "查看成绩",
          iconClass: ["blue", "green", "orange", "purple"][idx % 4],
        };
      });
    recentList.value = recent;
  } catch (e) {
    recentList.value = [];
  }
};

const formatTimeRange = (row) => {
  const t = String(row?.insertTime || "").replace("T", " ").slice(0, 16);
  return t ? `${t}` : "-";
};

// 同步用户信息
watch(
  () => userStore.userInfo,
  () => {
    userInfo.value = userStore.getUserInfo();
    editForm.value = { ...userInfo.value };
  },
  { immediate: true }
);
</script>


<style lang="scss" scoped>
.personal-center-page {
  width: 100%;
  max-width: none;
  margin: 0;
  min-height: 100%;
}

.page-head {
  margin: 0 0 14px;
  .title {
    font-size: 28px;
    font-weight: 700;
    color: #0f172a;
    line-height: 1;
    margin-bottom: 10px;
  }
  .sub {
    color: #8a8f99;
    font-size: 14px;
  }
}

.profile-card {
  background: #4f46e5;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 14px;
  padding: 18px 18px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 16px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);

  .left {
    display: flex;
    align-items: flex-start;
    gap: 18px;
    min-width: 0;
  }

  .info {
    min-width: 0;
  }

  .name-row {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-top: 2px;
    margin-bottom: 10px;
  }

  .name {
    font-size: 20px;
    font-weight: 800;
    color: #ffffff;
  }

  .role-tag {
    background: rgba(255, 255, 255, 0.18);
    border-color: rgba(255, 255, 255, 0.32);
    color: #ffffff;
    font-weight: 700;
  }

  .meta {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 10px 16px;
    color: rgba(255, 255, 255, 0.92);
    font-size: 13px;
  }

  .meta-item {
    display: flex;
    align-items: center;
    gap: 8px;
    min-width: 0;
    span {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    :deep(svg) {
      color: rgba(255, 255, 255, 0.92);
    }
  }

  .edit-btn {
    border-radius: 8px;
    font-weight: 700;
  }
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 16px;
}

.overview-card {
  display: flex;
  align-items: center;
  gap: 14px;
  background: #fff;
  border: 1px solid #e7ecf4;
  border-radius: 12px;
  padding: 18px 18px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);

  .icon {
    width: 52px;
    height: 52px;
    border-radius: 50%;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 26px;
    font-weight: 700;
  }
  .icon.blue { background: #4f46e5; }
  .icon.green { background: #28c191; }
  .icon.purple { background: #7d5fff; }
  .icon.orange { background: #ffb020; }

  .label {
    font-size: 14px;
    color: #6b7280;
    font-weight: 700;
    line-height: 1;
    margin-bottom: 8px;
  }

  .value {
    font-size: 26px;
    font-weight: 800;
    color: #202733;
    line-height: 1;
    span {
      font-size: 14px;
      color: #6b7280;
      margin-left: 4px;
      font-weight: 700;
    }
  }
}

.block {
  background: #fff;
  border: 1px solid #e7ecf4;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}

.block-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 12px;
  border-bottom: 1px solid #eef2f7;
  margin-bottom: 12px;

  .h {
    font-size: 16px;
    font-weight: 800;
    color: #202733;
  }
  .more {
    cursor: pointer;
    color: #4f46e5;
    font-weight: 700;
    font-size: 13px;
    display: inline-flex;
    align-items: center;
    gap: 6px;
  }
}

.recent-list {
  display: flex;
  flex-direction: column;
}

.recent-row {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 6px;
  border-bottom: 1px solid #f1f3f8;

  &:last-child {
    border-bottom: none;
  }

  .row-left {
    display: flex;
    align-items: center;
    gap: 12px;
    min-width: 0;
    flex: 1;
  }

  .row-tail {
    display: flex;
    align-items: center;
    align-self: stretch;
    gap: 12px;
    flex-shrink: 0;
  }

  .row-icon {
    width: 38px;
    height: 38px;
    border-radius: 12px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    flex-shrink: 0;
  }
  .row-icon.blue { background: #4f46e5; }
  .row-icon.green { background: #28c191; }
  .row-icon.orange { background: #ffb020; }
  .row-icon.purple { background: #7d5fff; }

  .row-score-col {
    flex-shrink: 0;
    min-width: 72px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: flex-end;
    align-self: stretch;
    text-align: right;
  }

  .row-score-label {
    font-size: 11px;
    color: #94a3b8;
    font-weight: 600;
    line-height: 1.2;
    margin-bottom: 2px;
    min-height: 14px;
    width: 100%;
    text-align: right;
  }

  .row-score-value {
    font-size: 15px;
    font-weight: 800;
    color: #22b573;
    line-height: 1.25;
    font-variant-numeric: tabular-nums;
    text-align: right;
    width: 100%;
  }

  .row-score-value.is-pending {
    font-size: 13px;
    font-weight: 700;
    color: #64748b;
  }

  .row-right {
    display: flex;
    align-items: center;
    align-self: stretch;
  }

  .row-main {
    min-width: 0;
  }

  .row-title {
    font-size: 14px;
    font-weight: 800;
    color: #202733;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .row-sub {
    margin-top: 4px;
    font-size: 12px;
    color: #8a8f99;
  }

  .detail-btn {
    min-width: 92px;
    border-radius: 8px;
    background: #eef2ff;
    border-color: #c7d2fe;
    color: #4338ca;
    font-weight: 800;
  }
}

.settings-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 14px;
}

.setting-item {
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid #eef2f7;
  border-radius: 10px;
  padding: 14px 14px;
  cursor: pointer;
  background: #fff;
  color: #202733;
  font-weight: 700;

  .arrow {
    margin-left: auto;
    color: #a3a8b3;
  }

  &:hover {
    background: #f7f9fd;
    border-color: #e6ebf5;
  }

  &.disabled {
    cursor: not-allowed;
    opacity: 0.6;
  }

  &.danger {
    border-color: #f2d6d8;
    &:hover {
      background: #fff7f7;
    }
  }
}

.empty {
  color: #8a8f99;
  padding: 14px 2px;
  font-size: 13px;
}

:deep(.edit-dialog .el-dialog__body),
:deep(.pwd-dialog .el-dialog__body) {
  padding-top: 8px;
}

@media (max-width: 1100px) {
  .overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .profile-card .meta {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .settings-grid {
    grid-template-columns: 1fr;
  }
  .profile-card {
    flex-direction: column;
    align-items: stretch;
  }
  .profile-card .right {
    display: flex;
    justify-content: flex-end;
  }
}
</style>
