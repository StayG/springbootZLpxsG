<template>
  <div class="module-container">
    <div v-if="showIndexFlag">
      <!-- 查询条件 -->
      <div class="filter-container">
        <div class="filter-item-group">
          <el-input v-model="params.examQuestionName" placeholder="试题名称" class="filter-item" clearable />

        </div>
        <div class="filter-btn-group">
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </div>

      <!-- 题量/分值设置条：插入到表格上方、筛选区附近 -->
      <el-row class="ad" style="margin: 10px 0; align-items: center;"
        :style="{ justifyContent: contents.btnAdAllBoxPosition === '1' ? 'flex-start' : contents.btnAdAllBoxPosition === '2' ? 'center' : 'flex-end' }">
        <!-- 左侧：题量/分值输入（仅在 zujuanTypes==1 时显示） -->
        <label v-if="zujuanTypes == 1" style="flex: 1; display: block; margin-right: 12px;">
          <div style="display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 7px;">
            <el-input style="width: 400px" type="number" min="0" :max="danXisting" placeholder="数量"
              v-model.number="danNum">
              <template #prepend>单选题数量: </template>
              <template #append>现:<span class="highlight-num">{{ danXisting }}</span>题；单题分数:</template>
            </el-input>
            <el-input style="width: 90px" placeholder="分数" type="number" min="0" max="100" v-model.number="danFen" />

            <el-input style="width: 400px" type="number" min="0" :max="duoXisting" placeholder="数量"
              v-model.number="duoNum">
              <template #prepend>多选题数量:</template>
              <template #append>现:<span class="highlight-num">{{ duoXisting }}</span>题；单题分数:</template>
            </el-input>
            <el-input style="width: 90px" placeholder="分数" type="number" min="0" max="100" v-model.number="duoFen" />
          </div>
          <div style="display: flex; gap: 8px; flex-wrap: wrap;">
            <el-input style="width: 400px" type="number" min="0" :max="panXisting" placeholder="数量"
              v-model.number="panNum">
              <template #prepend>判断题数量:</template>
              <template #append>现:<span class="highlight-num">{{ panXisting }}</span>题；单题分数:</template>
            </el-input>
            <el-input style="width: 90px" placeholder="分数" type="number" min="0" max="100" v-model.number="panFen" />

            <el-input style="width: 400px" type="number" min="0" :max="tianXisting" placeholder="数量"
              v-model.number="tianNum">
              <template #prepend>填空题数量:</template>
              <template #append>现:<span class="highlight-num">{{ tianXisting }}</span>题；单题分数:</template>
            </el-input>
            <el-input style="width: 90px" placeholder="分数" type="number" min="0" max="100" v-model.number="tianFen" />
          </div>
          <div style="display: flex; gap: 8px; flex-wrap: wrap; margin-top: 7px;">
            <el-input style="width: 400px" type="number" min="0" :max="jianXisting" placeholder="数量"
              v-model.number="jianNum">
              <template #prepend>简答题数量:</template>
              <template #append>现:<span class="highlight-num">{{ jianXisting }}</span>题；单题分数:</template>
            </el-input>
            <el-input style="width: 90px" placeholder="分数" type="number" min="0" max="100" v-model.number="jianFen" />
          </div>
          
          <!-- 难度配比输入 -->
          <div style="display: flex; gap: 8px; flex-wrap: wrap; margin-top: 15px; padding-top: 15px; border-top: 1px dashed #dcdfe6;">
            <span style="font-weight: 600; color: #303133; min-width: 100px; line-height: 32px;">🎯 难度配比：</span>
            <el-input style="width: 180px" type="number" min="0" placeholder="简单题" v-model.number="difficultyRatio.easy">
              <template #prepend>简单</template>
            </el-input>
            <span style="line-height: 32px; font-weight: bold;">:</span>
            <el-input style="width: 180px" type="number" min="0" placeholder="中等题" v-model.number="difficultyRatio.medium">
              <template #prepend>中等</template>
            </el-input>
            <span style="line-height: 32px; font-weight: bold;">:</span>
            <el-input style="width: 180px" type="number" min="0" placeholder="困难题" v-model.number="difficultyRatio.hard">
              <template #prepend>困难</template>
            </el-input>
            <el-tooltip content="例如 4:4:2 表示简单、中等、困难题比例为 4:4:2" placement="top">
              <el-icon style="color: #909399; cursor: help; line-height: 32px;"><QuestionFilled /></el-icon>
            </el-tooltip>
          </div>
          
          <!-- 必选知识点 -->
          <div style="display: flex; gap: 8px; flex-wrap: wrap; margin-top: 10px; align-items: center;">
            <span style="font-weight: 600; color: #303133; min-width: 100px;">📚 必选知识点：</span>
            <el-input 
              style="flex: 1; max-width: 600px;" 
              type="text" 
              placeholder="输入知识点标签，用逗号分隔，例如: 古诗词鉴赏,阅读理解（可选）" 
              v-model="requiredKnowledgePointsInput">
            </el-input>
            <el-tooltip content="输入必须覆盖的知识点标签（具体文本），用英文逗号分隔。留空表示不限制" placement="top">
              <el-icon style="color: #909399; cursor: help;"><QuestionFilled /></el-icon>
            </el-tooltip>
          </div>
          
          <!-- 按钮区域 + 总分 -->
          <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 15px; padding-top: 15px; border-top: 1px dashed #dcdfe6;">
            <div>
              <el-tag size="small"
                style="display: flex; align-items: center; justify-content: center; text-align: center; height: 36px; padding: 0 16px;">
                <span style="text-align: center; line-height: 36px;">
                  试卷总分: <span style="font-size: 18px; font-weight: bold; margin-left: 4px;">{{ totalScore }}</span>
                </span>
              </el-tag>
            </div>
            <div>
              <el-button @click="goBack" style="margin-right: 10px;">返回列表</el-button>
              <!-- 智能组卷按钮（仅在自动组卷模式显示） -->
              <el-button 
                v-if="zujuanTypes == 1" 
                type="warning" 
                @click="handleGeneticPaper"
                :loading="geneticLoading">
                🧬 智能组卷
              </el-button>
              <!-- 开始组卷/添加考题按钮（仅在手动组卷模式显示） -->
              <el-button 
                v-if="zujuanTypes == 2" 
                type="success" 
                @click="addshiti">
                添加考题
              </el-button>
            </div>
          </div>
        </label>

        <!-- 手动组卷批量设置分数（仅在 zujuanTypes==2 时显示） -->
        <div v-if="zujuanTypes == 2" style="flex: 1; width: 100%;">
          <div style="display: flex; gap: 12px; flex-wrap: wrap; align-items: center; margin-bottom: 12px;">
            <span style="font-weight: 600; color: #303133;">按题型批量设置分数：</span>
            <el-input style="width: 180px" type="number" min="0" max="100" placeholder="单选题分数"
              v-model.number="batchScore.dan">
              <template #prepend>单选题</template>
            </el-input>
            <el-input style="width: 180px" type="number" min="0" max="100" placeholder="多选题分数"
              v-model.number="batchScore.duo">
              <template #prepend>多选题</template>
            </el-input>
            <el-input style="width: 180px" type="number" min="0" max="100" placeholder="判断题分数"
              v-model.number="batchScore.pan">
              <template #prepend>判断题</template>
            </el-input>
            <el-input style="width: 180px" type="number" min="0" max="100" placeholder="填空题分数"
              v-model.number="batchScore.tian">
              <template #prepend>填空题</template>
            </el-input>
            <el-input style="width: 180px" type="number" min="0" max="100" placeholder="简答题分数"
              v-model.number="batchScore.jian">
              <template #prepend>简答题</template>
            </el-input>
            <el-button type="primary" @click="applyBatchScores">应用</el-button>
            <el-button @click="resetBatchScores">重置</el-button>
          </div>
          
          <!-- 手动组卷的按钮区域 + 总分 -->
          <div style="display: flex; justify-content: space-between; align-items: center; padding-top: 15px; border-top: 1px dashed #dcdfe6;">
            <div>
              <el-tag size="small"
                style="display: flex; align-items: center; justify-content: center; text-align: center; height: 36px; padding: 0 16px;">
                <span style="text-align: center; line-height: 36px;">
                  试卷总分: <span style="font-size: 18px; font-weight: bold; margin-left: 4px;">{{ totalScore }}</span>
                </span>
              </el-tag>
            </div>
            <div>
              <el-button @click="goBack" style="margin-right: 10px;">返回列表</el-button>
              <el-button type="success" @click="addshiti">
                添加考题
              </el-button>
            </div>
          </div>
        </div>
      </el-row>


      <!-- 操作按钮 -->
      <div class="operation-container">
        <el-button class="operation-btn" type="primary" :disabled="multipleSelection.length === 0"
          @click="handleBatchDelete">
          批量删除
        </el-button>
      </div>

      <!-- 表格 -->
      <div class="table-scroll-x">
        <el-table :data="dataList" style="width: 100%" @selection-change="handleSelectionChange" border stripe
          row-key="id" :row-class-name="tableRowClassName">
          <el-table-column type="selection" width="55" header-align="center" align="center" />
          <el-table-column label="拖拽排序" width="80" header-align="center" align="center">
            <template #default>
              <el-icon class="drag-handle" style="cursor: move; color: #909399;">
                <DCaret />
              </el-icon>
            </template>
          </el-table-column>
          <!-- <el-table-column type="index" label="序号" width="65" header-align="center" align="center"/> -->
          <el-table-column prop="examPaperName" label="试卷名称" min-width="100" header-align="center" align="center" />
          <el-table-column prop="examQuestionName" label="试题名称" min-width="100" header-align="center" align="center" />
          <el-table-column prop="examQuestionTypes" label="试题类型" min-width="100" header-align="center" align="center">
            <template #default="{ row }">
              {{ getExamQuestionTypeLabel(row) }}
            </template>
          </el-table-column>
          <el-table-column prop="difficultyLevel" label="难度等级" min-width="100" header-align="center" align="center">
            <template #default="{ row }">
              <el-tag :type="getDifficultyTagType(row.difficultyLevel)" size="small">
                {{ getDifficultyLabel(row.difficultyLevel) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="knowledgePoint" label="知识点" min-width="150" header-align="center" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.knowledgePoint" type="info" size="small">{{ row.knowledgePoint }}</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="examPaperTopicNumber" label="试题分数" min-width="100" header-align="center"
            align="center" />
          <el-table-column label="操作" width="300" header-align="center" align="center" fixed="right">
            <template #default="{ row }">
              <div class="table-actions">
                <el-button type="primary" size="small" @click="handleView(row)">详情</el-button>
                <el-button type="success" size="small" @click="handleEdit(row)">修改分数</el-button>
                <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <!-- 添加试题弹窗 -->
      <el-dialog v-model="questionsTableVisible" title="添加试题" width="80%" class="questions-dialog">
        <div class="filter-container">
          <div class="filter-item-group">
            <el-input v-model="questionsSearchForm.examQuestionName" placeholder="试题名称" class="filter-item" clearable>
              <template #prefix>
                <el-icon>
                  <Search />
                </el-icon>
              </template>
            </el-input>
            <el-select v-model="questionsSearchForm.examQuestionTypes" placeholder="请选择试题类型" clearable
              class="filter-item">
              <el-option v-for="(item, index) in examQuestionTypesSelectSearch" :key="index" :label="item.indexName"
                :value="item.codeIndex" />
            </el-select>
          </div>
          <div class="filter-btn-group">
            <el-button type="primary" @click="questionsSearch">查询</el-button>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="operation-container">
          <el-button class="operation-btn" type="success" :disabled="selectedQuestions.length === 0"
            @click="batchAddQuestionsToPaper">
            批量添加
          </el-button>
        </div>

        <!-- 表格 -->
        <div class="table-scroll-x">
          <el-table :data="questionsList" style="width: 100%" @selection-change="handleQuestionsSelectionChange" border
            stripe>
            <el-table-column type="selection" width="55" header-align="center" align="center"
              :selectable="checkSelectable" />
            <el-table-column prop="examQuestionName" label="试题名称" min-width="150" header-align="center" align="center">
              <template #default="{ row }">
                <span
                  style="display: inline-block; white-space: nowrap; width: 100%; overflow: hidden; text-overflow: ellipsis;">
                  {{ row.examQuestionName }}
                </span>
              </template>
            </el-table-column>

            <el-table-column prop="examQuestionAnswer" label="正确答案" min-width="150" header-align="center" align="center">
              <template #default="{ row }">
                <span
                  style="display: inline-block; white-space: nowrap; width: 100%; overflow: hidden; text-overflow: ellipsis;">
                  {{ row.examQuestionAnswer }}
                </span>
              </template>
            </el-table-column>

            <el-table-column prop="examQuestionAnalysis" label="答案解析" min-width="150" header-align="center"
              align="center">
              <template #default="{ row }">
                <span
                  style="display: inline-block; white-space: nowrap; width: 100%; overflow: hidden; text-overflow: ellipsis;">
                  {{ row.examQuestionAnalysis }}
                </span>
              </template>
            </el-table-column>

            <el-table-column prop="examQuestionTypesName" label="试题类型" min-width="120" header-align="center"
              align="center">
              <template #default="{ row }">
                {{ row.examQuestionTypesName || row.examQuestionTypes || "" }}
              </template>
            </el-table-column>

            <el-table-column label="操作" width="150" header-align="center" align="center" fixed="right">
              <template #default="{ row }">
                <div class="table-actions">
                  <el-button type="success" size="small" @click="addQuestionsToPaper(row)"
                    v-if="!isQuestionAdded(row.id)">
                    添加
                  </el-button>
                  <el-button type="info" size="small" disabled v-else>
                    已添加
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 分页 -->
        <el-pagination v-model:current-page="questionsPageIndex" v-model:page-size="questionsContents.pageEachNum"
          :total="questionsPageTotalPage" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper"
          @size-change="questionsSizeChangeHandle" @current-change="questionsCurrentChangeHandle" />
      </el-dialog>


      <!-- 分页 -->
      <el-pagination v-model:current-page="params.page" v-model:page-size="params.limit" :total="total"
        :page-sizes="[5, 10, 15, 20]" layout="total, sizes, prev, pager, next, jumper" @size-change="getDataList"
        @current-change="getDataList" />
    </div>

    <!-- 新增或编辑或查看 -->
    <add-or-update v-if="addOrUpdateFlag" ref="addOrUpdateRef" :readonly="isReadonly"
      @handleSuccess="handleSuccess"></add-or-update>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed, watch } from "vue";
import request from "@/utils/request";
import { ElMessage, ElMessageBox } from "element-plus";
import AddOrUpdate from "./add-or-update.vue";
import utils from "@/utils/utils.js";
import { useRouter } from "vue-router";
import { Search, DCaret, QuestionFilled } from '@element-plus/icons-vue';
import { getExamQuestionTypeOptions } from "@/utils/dictionary.js";
import Sortable from 'sortablejs';


const router = useRouter();


// 数据列表
const dataList = ref([]);
const total = ref(0);
// 多选数据
const multipleSelection = ref([]);
//默认展示表格页面
const showIndexFlag = ref(true);
// 分页属性
const pageParam = ref({
  page: 1,
  limit: 5,

})
// 查询参数
const params = ref({
  ...pageParam.value,
});

// 添加试题弹窗相关状态
const questionsTableVisible = ref(false);
const questionsSearchForm = ref({
  examQuestionName: '',
  kemuTypes: 0,
  examQuestionTypes: '',
});
const examQuestionTypesSelectSearch = ref([]);
const questionsList = ref([]);
const questionsPageIndex = ref(1);
const questionsPageTotalPage = ref(0);
// 选中的试题
const selectedQuestions = ref([]);
// 已经添加的所有试题 id
const allAddQuestionsId = ref([])

const query = router.currentRoute.value.query;

// 拖拽排序相关
let sortableInstance = null;
const tableRowClassName = () => {
  return 'drag-row';
};

// 初始化拖拽排序
const initSortable = () => {
  nextTick(() => {
    const tbody = document.querySelector('.el-table__body > tbody');
    if (tbody && !sortableInstance) {
      sortableInstance = Sortable.create(tbody, {
        handle: '.drag-handle',
        animation: 150,
        ghostClass: 'sortable-ghost',
        dragClass: 'sortable-drag',
        onEnd: async (evt) => {
          const { oldIndex, newIndex } = evt;
          if (oldIndex === newIndex) return;

          // 重新排列 dataList
          const draggedItem = dataList.value[oldIndex];
          dataList.value.splice(oldIndex, 1);
          dataList.value.splice(newIndex, 0, draggedItem);

          // 构建新的排序数据 - 只传递 ID 列表，顺序即为拖拽后的视觉顺序
          const topicSequences = dataList.value.map((item, index) => ({
            id: item.id
            // 不再传递 sequence，后端会根据列表位置重新计算
          }));

          try {
            const response = await request.post('/examPaperTopic/batchUpdateSequence', {
              examPaperId: query.id,
              topicSequences: topicSequences
            });

            if (response.code === 200) {
              ElMessage.success('题目顺序已更新');
            } else {
              ElMessage.error('更新顺序失败');
              // 失败时恢复原顺序
              getDataList();
            }
          } catch (error) {
            console.error('更新顺序失败:', error);
            ElMessage.error('更新顺序失败，请稍后重试');
            // 失败时恢复原顺序
            getDataList();
          }
        }
      });
    }
  });
};

// 弹窗表格配置 - 简化配置，只保留必要的分页设置
const questionsContents = ref({
  pageEachNum: 10,
});

// 题目类型标签转换 - 使用后端字典转换后的值
const getExamQuestionTypeLabel = (row) => {
  return row.examQuestionTypesName || row.examQuestionTypes || "";
};

// 难度等级标签转换
const getDifficultyLabel = (level) => {
  const difficultyMap = {
    1: '简单',
    2: '中等',
    3: '困难'
  };
  return difficultyMap[level] || '-';
};

// 难度等级标签颜色类型
const getDifficultyTagType = (level) => {
  const typeMap = {
    1: 'success',  // 简单 - 绿色
    2: 'warning',  // 中等 - 橙色
    3: 'danger'    // 困难 - 红色
  };
  return typeMap[level] || 'info';
};

// 获取数据列表
const getDataList = () => {
  params.value.examPaperId = query.id;
  request.post("examPaperTopic/page", params.value).then(({ data }) => {

    totalScore.value = data.totalScore || 0;
    // 按题型排序：单选(1)->多选(2)->判断(3)->填空(4)->简答(5)
    dataList.value = (data.list || []).sort((a, b) => {
      const typeA = Number(a.examQuestionTypes) || 0;
      const typeB = Number(b.examQuestionTypes) || 0;
      return typeA - typeB;
    });
    total.value = data.total || 0;
    allAddQuestionsId.value = data.allQuestionId;

    // 初始化拖拽排序
    initSortable();
  });
};

// 查询
const handleSearch = () => {
  params.value.page = 1;
  getDataList();
};

// 新增重置方法
const handleReset = () => {
  params.value = {
    ...pageParam.value,
  }
  handleSearch();
};

// 多选
const handleSelectionChange = (selection) => {
  multipleSelection.value = selection;
};

// 批量删除
const handleBatchDelete = () => {
  if (multipleSelection.value.length === 0) {
    ElMessage.warning("请选择要删除的信息");
    return;
  }
  ElMessageBox.confirm("确定要删除选中的信息吗?", "提示", {
    type: "warning",
  })
    .then(() => {
      const ids = multipleSelection.value.map((item) => Number(item.id));
      console.log("🚀 ~ .then ~ ids:", ids);
      request.post("examPaperTopic/del/batch", { ids: ids, examPaperId: query.id }).then(() => {
        ElMessage.success("删除成功");
        getDataList();
      });
    })
    .catch(() => {
    });
};

// 单个删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除该信息吗?`, "提示", {
    type: "warning",
  })
    .then(() => {
      request.delete(`examPaperTopic/del/${row.id}`).then(() => {
        ElMessage.success("删除成功");
        getDataList();
      });
    })
    .catch(() => {
    });
};

//科目和试卷id传递
const kemuTypes = ref('');
const examPaperId = ref('');

const addOrUpdateFlag = ref(false);
const addOrUpdateRef = ref();
const isReadonly = ref(false);

// 新增
const handleAdd = () => {
  showIndexFlag.value = false;
  addOrUpdateFlag.value = true;
  isReadonly.value = false;
  nextTick(() => {
    addOrUpdateRef.value?.init();
  });
};

// 编辑
const handleEdit = (row) => {
  showIndexFlag.value = false;
  addOrUpdateFlag.value = true;
  isReadonly.value = false;
  nextTick(() => {
    addOrUpdateRef.value?.init(row.id);
  });
};

// 查看（只读模式）
const handleView = (row) => {
  showIndexFlag.value = false;
  addOrUpdateFlag.value = true;
  isReadonly.value = true;
  nextTick(() => {
    addOrUpdateRef.value?.init(row.id);
  });

};



// 成功回调
const handleSuccess = () => {
  addOrUpdateFlag.value = false;
  showIndexFlag.value = true;
  isReadonly.value = false;
  getDataList();
};

const getCount = () => {
  kemuTypes.value = query.kemuTypes;
  request.get(`examQuestion/getCurrentQuestionCounts/${kemuTypes.value}`)
    .then(({ data }) => {
      danXisting.value = data.danXisting;
      duoXisting.value = data.duoXisting;
      panXisting.value = data.panXisting;
      tianXisting.value = data.tianXisting;
      jianXisting.value = data.jianXisting;

    })
    .catch(err => {
      console.error(err);
      ElMessage.error('请求出错，请稍后重试');
    });

}

const getKemuAndPaperId = () => {
  kemuTypes.value = query.kemuTypes;
  examPaperId.value = query.id;
  zujuanTypes.value = query.zujuanTypes;
  examQuestionTypesSelectSearch.value = query.examQuestionTypes;
  getExamQuestionTypes();
};

// 初始化加载数据
onMounted(() => {
  getDictOptions(); // 先获取字典选项
  getDataList();
  getCount();
  getKemuAndPaperId();
});

// 题型现存数量（从接口或父组件传入后赋值）
const danXisting = ref(0);
const duoXisting = ref(0);
const panXisting = ref(0);
const tianXisting = ref(0);
const jianXisting = ref(0);

// 用户输入的数量与分值
const danNum = ref(0); const danFen = ref(0);
const duoNum = ref(0); const duoFen = ref(0);
const panNum = ref(0); const panFen = ref(0);
const tianNum = ref(0); const tianFen = ref(0);
const jianNum = ref(0); const jianFen = ref(0);

// 监控数量输入框，验证整数
watch(danNum, (newVal, oldVal) => {
  if (newVal !== null && newVal !== undefined && newVal !== '' && !Number.isInteger(newVal)) {
    // 显示警告但不修改值
    validateInteger(newVal, '单选题数量');
    // 如果是小数，则重置为之前的值
    danNum.value = oldVal;
  }
});

watch(duoNum, (newVal, oldVal) => {
  if (newVal !== null && newVal !== undefined && newVal !== '' && !Number.isInteger(newVal)) {
    // 显示警告但不修改值
    validateInteger(newVal, '多选题数量');
    // 如果是小数，则重置为之前的值
    duoNum.value = oldVal;
  }
});

watch(panNum, (newVal, oldVal) => {
  if (newVal !== null && newVal !== undefined && newVal !== '' && !Number.isInteger(newVal)) {
    // 显示警告但不修改值
    validateInteger(newVal, '判断题数量');
    // 如果是小数，则重置为之前的值
    panNum.value = oldVal;
  }
});

watch(tianNum, (newVal, oldVal) => {
  if (newVal !== null && newVal !== undefined && newVal !== '' && !Number.isInteger(newVal)) {
    // 显示警告但不修改值
    validateInteger(newVal, '填空题数量');
    // 如果是小数，则重置为之前的值
    tianNum.value = oldVal;
  }
});

watch(jianNum, (newVal, oldVal) => {
  if (newVal !== null && newVal !== undefined && newVal !== '' && !Number.isInteger(newVal)) {
    // 显示警告但不修改值
    validateInteger(newVal, '简答题数量');
    // 如果是小数，则重置为之前的值
    jianNum.value = oldVal;
  }
});

// 监控分数输入框，验证整数
watch(danFen, (newVal, oldVal) => {
  if (newVal !== null && newVal !== undefined && newVal !== '' && !Number.isInteger(newVal)) {
    // 显示警告但不修改值
    validateInteger(newVal, '单选题分数');
    // 如果是小数，则重置为之前的值
    danFen.value = oldVal;
  }
});

watch(duoFen, (newVal, oldVal) => {
  if (newVal !== null && newVal !== undefined && newVal !== '' && !Number.isInteger(newVal)) {
    // 显示警告但不修改值
    validateInteger(newVal, '多选题分数');
    // 如果是小数，则重置为之前的值
    duoFen.value = oldVal;
  }
});

watch(panFen, (newVal, oldVal) => {
  if (newVal !== null && newVal !== undefined && newVal !== '' && !Number.isInteger(newVal)) {
    // 显示警告但不修改值
    validateInteger(newVal, '判断题分数');
    // 如果是小数，则重置为之前的值
    panFen.value = oldVal;
  }
});

watch(tianFen, (newVal, oldVal) => {
  if (newVal !== null && newVal !== undefined && newVal !== '' && !Number.isInteger(newVal)) {
    // 显示警告但不修改值
    validateInteger(newVal, '填空题分数');
    // 如果是小数，则重置为之前的值
    tianFen.value = oldVal;
  }
});

watch(jianFen, (newVal, oldVal) => {
  if (newVal !== null && newVal !== undefined && newVal !== '' && !Number.isInteger(newVal)) {
    // 显示警告但不修改值
    validateInteger(newVal, '简答题分数');
    // 如果是小数，则重置为之前的值
    jianFen.value = oldVal;
  }
});

// 组卷类型（示例：1=自动/批量，其他=单个添加）
const zujuanTypes = ref(0);

// 按钮位置控制对象（根据你原有的 contents 结构）
const contents = ref({ btnAdAllBoxPosition: '2' });

// 总分实时计算
const totalScore = ref(0);

// 手动组卷批量设置分数
const batchScore = ref({
  dan: null,  // 单选题分数
  duo: null,  // 多选题分数
  pan: null,  // 判断题分数
  tian: null, // 填空题分数
  jian: null  // 简答题分数
});

// 智能组卷loading状态
const geneticLoading = ref(false);

// 难度配比（易:中:难）
const difficultyRatio = ref({
  easy: 4,    // 简单题比例
  medium: 4,  // 中等题比例
  hard: 2     // 困难题比例
});

// 必选知识点输入框
const requiredKnowledgePointsInput = ref('');

// 整数验证函数 - 当输入框失去焦点时验证
const validateInteger = (value, fieldName) => {
  // 如果值为空或者是整数，则不处理
  if (value === null || value === undefined || value === '' || Number.isInteger(value)) {
    return value;
  }

  // 如果是小数，仅提示用户，但不修改值
  ElMessage.warning(`${fieldName}必须是整数，请重新输入`);
  return value;
};

// 添加试题弹窗相关方法
const openQuestionsDialog = () => {
  questionsTableVisible.value = true;
  // 初始化查询条件
  questionsSearchForm.value = {
    examQuestionName: '',
    kemuTypes: kemuTypes.value,

  };
  // 获取试题列表
  getQuestionsList();
};

const getExamQuestionTypes = () => {
  if (query.examQuestionTypes) {
    try {
      examQuestionTypesSelectSearch.value = JSON.parse(query.examQuestionTypes);
    } catch (error) {
      console.error('解析试题类型数据失败:', error);
      // 如果解析失败，使用默认值
      examQuestionTypesSelectSearch.value = getDefaultQuestionTypes();
    }
  } else {
    // 如果没有传递，使用默认值
    examQuestionTypesSelectSearch.value = getDefaultQuestionTypes();
  }
};

// 获取默认试题类型 - 从字典接口获取
const getDefaultQuestionTypes = () => {
  // 如果已有选项，使用选项数据
  if (examQuestionTypeOptions.value && examQuestionTypeOptions.value.length > 0) {
    return examQuestionTypeOptions.value.map(item => ({
      codeIndex: String(item.value),
      indexName: item.label
    }));
  }
  // 否则返回空数组，等待数据加载
  return [];
};

// 试题类型选项 - 从字典接口获取
const examQuestionTypeOptions = ref([]);

// 获取字典选项
const getDictOptions = async () => {
  try {
    const typeList = await getExamQuestionTypeOptions();
    examQuestionTypeOptions.value = typeList;
    // 更新默认试题类型
    examQuestionTypesSelectSearch.value = getDefaultQuestionTypes();
  } catch (error) {
    console.error('获取字典选项失败:', error);
  }
};

const getQuestionsList = () => {
  const queryParams = {
    page: questionsPageIndex.value,
    limit: questionsContents.value.pageEachNum,
    ...questionsSearchForm.value,
    examQuestionTypes: questionsSearchForm.value.examQuestionTypes,
    examQuestionName: questionsSearchForm.value.examQuestionName
  };


  // console.log('前端发送的参数:', JSON.stringify(queryParams, null, 2));

  request.post("/examQuestion/page", queryParams)
    .then(({ data }) => {
      questionsList.value = data.list;
      questionsPageTotalPage.value = data.total;
    })
    .catch(err => {
      console.error(err);
      ElMessage.error('获取试题列表失败');
    });
};

//试题查询
const questionsSearch = () => {

  questionsPageIndex.value = 1;
  getQuestionsList();
};

const questionsSizeChangeHandle = (size) => {
  questionsContents.value.pageEachNum = size;
  questionsPageIndex.value = 1;
  getQuestionsList();
};

const questionsCurrentChangeHandle = (page) => {
  questionsPageIndex.value = page;
  getQuestionsList();
};

const handleQuestionsSelectionChange = (selection) => {
  // 处理试题多选
  selectedQuestions.value = selection;
};

// 批量添加试题到试卷
const batchAddQuestionsToPaper = async () => {
  if (selectedQuestions.value.length === 0) {
    ElMessage.warning('请先选择要添加的试题');
    return;
  }

  // 确认是否添加
  try {
    await ElMessageBox.confirm(
      `确定要将选中的 ${selectedQuestions.value.length} 道试题添加到试卷中吗？`,
      '确认批量添加',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    // 提取试题ID数组
    const examQuestionIds = selectedQuestions.value.map(row => row.id);

    // 构造批量添加的请求数据
    const payload = {
      examQuestionIds: examQuestionIds,
      examPaperId: examPaperId.value
    };

    // 调用批量保存接口
    const response = await request.post("/examPaperTopic/saveBatch", payload);

    if (response.code === 200) {
      ElMessage.success(`试题批量添加成功`);
      getDataList(); // 刷新主表格
      getQuestionsList(); // 刷新试题列表

      // 清空选中项
      selectedQuestions.value = [];
    } else {
      ElMessage.error(response.msg || '批量添加失败');
    }
  } catch (error) {
    // 用户取消操作或请求失败
    if (error !== 'cancel') {
      console.error('批量添加失败:', error);
      ElMessage.error('批量添加失败，请稍后重试');
    }
  }
};

// 检查题目是否已添加到试卷
const isQuestionAdded = (questionId) => {
  return allAddQuestionsId.value.some(item => item === questionId);
};

// 检查试题是否可选（用于表格多选框）
const checkSelectable = (row) => {
  // 如果试题已添加，则不可选
  return !isQuestionAdded(row.id);
};

const addQuestionsToPaper = (row) => {

  // 检查题目是否已添加
  if (isQuestionAdded(row.id)) {
    ElMessage.warning('该题目已添加到试卷中');
    return;
  }

  const payload = {
    examPaperId: examPaperId.value,
    examQuestionId: row.id,
    examPaperTopicNumber: 0, // 默认分数，可以根据需要调整
  };

  request.post("/examPaperTopic/save", payload)
    .then((response) => {

      if (response.code === 200) {
        ElMessage.success('添加试题成功');
        getDataList(); // 刷新主表格
        getQuestionsList(); // 刷新试题列表，保持弹窗开启状态
      } else {
        ElMessage.error(response.msg || '添加试题失败');
      }
    })
    .catch(err => {
      console.error(err);
      ElMessage.error('添加试题失败');
    });
};

// 返回上一级试卷列表页
const goBack = () => {
  router.go(-1); // 返回上一页
};

// 校验与提交
const addshiti = () => {
  //判断自动手动组卷
  if (zujuanTypes.value == 1) {

    // 检查试卷是否已经有试题
    if (dataList.value.length > 0) {
      ElMessage.warning('已完成组卷，不可重复组卷');
      return;
    }

    // 简单前端校验
    const checks = [
      [danNum, danXisting, '单选题数量'],
      [duoNum, duoXisting, '多选题数量'],
      [panNum, panXisting, '判断题数量'],
      [tianNum, tianXisting, '填空题数量'],
      [jianNum, jianXisting, '简答题数量'],
    ];
    for (const [numRef, maxRef, label] of checks) {
      if (numRef.value < 0) return ElMessage.warning(`${label}不能小于0`);
      if (numRef.value > maxRef.value) return ElMessage.warning(`${label}不能超过现有数量`);
    }
    const scoreChecks = [
      [danFen, '单选题分数'],
      [duoFen, '多选题分数'],
      [panFen, '判断题分数'],
      [tianFen, '填空题分数'],
      [jianFen, '简答题分数'],
    ];
    for (const [fenRef, label] of scoreChecks) {
      if (fenRef.value < 0) return ElMessage.warning(`${label}不能小于0`);
      if (fenRef.value > 100) return ElMessage.warning(`${label}不能超过100`);
    }

    // 空值判断 - 检查所有必要参数
    const requiredParams = [
      [danFen.value, '单选题分数'],
      [duoFen.value, '多选题分数'],
      [panFen.value, '判断题分数'],
      [tianFen.value, '填空题分数'],
      [jianFen.value, '简答题分数'],
    ];

    // 检查基本参数不为空
    for (const [value, label] of requiredParams) {
      if (value === null || value === undefined || value === '') {
        return ElMessage.warning(`请完善${label}信息`);
      }
    }

    // 检查数量参数（允许为0，但不能为空）
    const numParams = [
      [danNum.value, '单选题数量'],
      [duoNum.value, '多选题数量'],
      [panNum.value, '判断题数量'],
      [tianNum.value, '填空题数量'],
      [jianNum.value, '简答题数量']
    ];

    for (const [value, label] of numParams) {
      if (value === null || value === undefined || value === '') {
        return ElMessage.warning(`请填写${label}信息`);
      }
    }

    // 检查题型分数和数量的逻辑一致性
    if (danFen.value > 0 && danNum.value === 0) {
      return ElMessage.warning('单选题已设置分数，请输入单选题数量');
    }
    if (duoFen.value > 0 && duoNum.value === 0) {
      return ElMessage.warning('多选题已设置分数，请输入多选题数量');
    }
    if (panFen.value > 0 && panNum.value === 0) {
      return ElMessage.warning('判断题已设置分数，请输入判断题数量');
    }
    if (tianFen.value > 0 && tianNum.value === 0) {
      return ElMessage.warning('填空题已设置分数，请输入填空题数量');
    }
    if (jianFen.value > 0 && jianNum.value === 0) {
      return ElMessage.warning('简答题已设置分数，请输入简答题数量');
    }

    // 检查是否所有题型数量都是0
    if (danNum.value === 0 && duoNum.value === 0 && panNum.value === 0 && tianNum.value === 0 && jianNum.value === 0) {
      return ElMessage.warning('请输入题型数量');
    }

    const payload = {
      kemuTypes: kemuTypes.value,
      examPaperId: examPaperId.value,
      danNum: danNum.value, danFen: danFen.value,
      duoNum: duoNum.value, duoFen: duoFen.value,
      panNum: panNum.value, panFen: panFen.value,
      tianNum: tianNum.value, tianFen: tianFen.value,
      jianNum: jianNum.value, jianFen: jianFen.value,
      totalScore: totalScore.value
    };
    // TODO: 在这里调用你的接口或 emit 给父组件
    // request.post('/api/xxx', payload).then(...)
    request.get('/examPaperTopic/autoPaper', { params: payload })
      .then(res => {
        if (res.code === 200) {
          ElMessage.success('组卷成功');
          getDataList(); // 自动刷新选题列表
        } else {
          ElMessage.error(res.msg || '组卷失败');
        }
      })
      .catch(err => {
        console.error(err);
        ElMessage.error('请求出错，请稍后重试');
      });
    ElMessage.success('组卷参数已准备就绪');
  }
  else {
    openQuestionsDialog();
  }
}

// ========== 手动组卷批量设置分数 ==========
/**
 * 应用批量设置的分数
 */
const applyBatchScores = async () => {
  // 检查是否至少设置了一个题型的分数
  const hasScore = Object.values(batchScore.value).some(score => score !== null && score !== undefined && score !== '');
  if (!hasScore) {
    ElMessage.warning('请至少设置一个题型的分数');
    return;
  }

  try {
    await ElMessageBox.confirm(
      '确定要批量更新已添加试题的分数吗？此操作将覆盖现有分数。',
      '确认批量设置',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    // 构建需要更新的题目列表
    const updateList = [];
    dataList.value.forEach(item => {
      const questionType = item.examQuestionTypes;
      let newScore = null;

      // 根据题型匹配对应的分数
      switch (questionType) {
        case 1: // 单选题
          newScore = batchScore.value.dan;
          break;
        case 2: // 多选题
          newScore = batchScore.value.duo;
          break;
        case 3: // 判断题
          newScore = batchScore.value.pan;
          break;
        case 4: // 填空题
          newScore = batchScore.value.tian;
          break;
        case 5: // 简答题
          newScore = batchScore.value.jian;
          break;
      }

      // 如果该题型设置了分数，则加入更新列表
      if (newScore !== null && newScore !== undefined && newScore !== '') {
        updateList.push({
          id: item.id,
          examPaperTopicNumber: Number(newScore)
        });
      }
    });

    if (updateList.length === 0) {
      ElMessage.warning('当前试卷中没有匹配到需要更新的试题');
      return;
    }

    // 批量更新分数
    let successCount = 0;
    let failCount = 0;

    for (const updateItem of updateList) {
      try {
        await request.post('/examPaperTopic/update', {
          id: updateItem.id,
          examPaperTopicNumber: updateItem.examPaperTopicNumber
        });
        successCount++;
      } catch (error) {
        console.error('更新失败:', error);
        failCount++;
      }
    }

    if (failCount === 0) {
      ElMessage.success(`批量设置成功！共更新 ${successCount} 道试题`);
    } else {
      ElMessage.warning(`批量设置完成：成功 ${successCount} 道，失败 ${failCount} 道`);
    }

    // 刷新数据以更新总分显示
    getDataList();

    // 重置批量设置表单
    resetBatchScores();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量设置分数失败:', error);
      ElMessage.error('批量设置分数失败，请稍后重试');
    }
  }
};

/**
 * 重置批量设置分数表单
 */
const resetBatchScores = () => {
  batchScore.value = {
    dan: null,
    duo: null,
    pan: null,
    tian: null,
    jian: null
  };
};

/**
 * 智能组卷 - 使用完整版遗传算法
 */
const handleGeneticPaper = async () => {
  // 检查试卷是否已经有试题
  if (dataList.value.length > 0) {
    ElMessage.warning('已完成组卷，请先删除现有试题后再使用智能组卷');
    return;
  }

  // 基础参数校验
  if (!kemuTypes.value) {
    ElMessage.warning('科目信息缺失');
    return;
  }

  // 计算总题目数和目标总分
  const totalQuestions = 
    (danNum.value || 0) + 
    (duoNum.value || 0) + 
    (panNum.value || 0) + 
    (tianNum.value || 0) + 
    (jianNum.value || 0);

  if (totalQuestions === 0) {
    ElMessage.warning('请至少设置一种题型的数量');
    return;
  }

  const targetScore = 
    (danNum.value || 0) * (danFen.value || 0) +
    (duoNum.value || 0) * (duoFen.value || 0) +
    (panNum.value || 0) * (panFen.value || 0) +
    (tianNum.value || 0) * (tianFen.value || 0) +
    (jianNum.value || 0) * (jianFen.value || 0);

  if (targetScore === 0) {
    ElMessage.warning('试卷总分不能为0，请设置各题型分数');
    return;
  }

  // 构建题型数量和分值配置
  const questionTypeCount = {};
  const questionTypeScore = {};
  
  if (danNum.value > 0) {
    questionTypeCount['1'] = danNum.value;
    questionTypeScore['1'] = danFen.value || 0;
  }
  if (duoNum.value > 0) {
    questionTypeCount['2'] = duoNum.value;
    questionTypeScore['2'] = duoFen.value || 0;
  }
  if (panNum.value > 0) {
    questionTypeCount['3'] = panNum.value;
    questionTypeScore['3'] = panFen.value || 0;
  }
  if (tianNum.value > 0) {
    questionTypeCount['4'] = tianNum.value;
    questionTypeScore['4'] = tianFen.value || 0;
  }
  if (jianNum.value > 0) {
    questionTypeCount['5'] = jianNum.value;
    questionTypeScore['5'] = jianFen.value || 0;
  }

  // 计算难度分布（基于比例）
  const difficultyDistribution = {};
  const totalRatio = (difficultyRatio.value.easy || 0) + 
                     (difficultyRatio.value.medium || 0) + 
                     (difficultyRatio.value.hard || 0);
  
  if (totalRatio > 0) {
    difficultyDistribution['1'] = Math.round(totalQuestions * (difficultyRatio.value.easy || 0) / totalRatio);
    difficultyDistribution['2'] = Math.round(totalQuestions * (difficultyRatio.value.medium || 0) / totalRatio);
    difficultyDistribution['3'] = totalQuestions - difficultyDistribution['1'] - difficultyDistribution['2'];
  }

  // 解析必选知识点（文本标签）
  const requiredKnowledgePoints = [];
  if (requiredKnowledgePointsInput.value && requiredKnowledgePointsInput.value.trim()) {
    const parts = requiredKnowledgePointsInput.value.split(',');
    for (const part of parts) {
      const trimmed = part.trim();
      if (trimmed) {
        requiredKnowledgePoints.push(trimmed);
      }
    }
  }

  // 构建确认信息
  let confirmMessage = `即将使用完整版遗传算法智能组卷：<br/><br/>
    <b>📋 硬约束（必须满足）：</b><br/>
    - 目标题目数：<b>${totalQuestions}</b> 题<br/>
    - 目标总分：<b>${targetScore}</b> 分<br/>`;
  
  if (danNum.value > 0) confirmMessage += `- 单选题：<b>${danNum.value}</b>题 × <b>${danFen.value}</b>分<br/>`;
  if (duoNum.value > 0) confirmMessage += `- 多选题：<b>${duoNum.value}</b>题 × <b>${duoFen.value}</b>分<br/>`;
  if (panNum.value > 0) confirmMessage += `- 判断题：<b>${panNum.value}</b>题 × <b>${panFen.value}</b>分<br/>`;
  if (tianNum.value > 0) confirmMessage += `- 填空题：<b>${tianNum.value}</b>题 × <b>${tianFen.value}</b>分<br/>`;
  if (jianNum.value > 0) confirmMessage += `- 简答题：<b>${jianNum.value}</b>题 × <b>${jianFen.value}</b>分<br/>`;
  
  confirmMessage += `<br/><b>🎯 软约束（尽量满足）：</b><br/>`;
  
  if (totalRatio > 0) {
    confirmMessage += `- 难度配比：简单<b>${difficultyDistribution['1']}</b>题 : 中等<b>${difficultyDistribution['2']}</b>题 : 困难<b>${difficultyDistribution['3']}</b>题<br/>`;
  }
  
  if (requiredKnowledgePoints.length > 0) {
    confirmMessage += `- 必选知识点：<b>${requiredKnowledgePoints.join(', ')}</b><br/>`;
  }
  
  confirmMessage += `<br/><b>🧬 算法参数：</b><br/>
    - 种群大小：<b>50</b><br/>
    - 最大迭代次数：<b>50</b>代<br/>
    - 交叉概率：<b>70%</b><br/>
    - 变异概率：<b>5%</b><br/>
    <br/>
    <span style="color: #E6A23C;">⚠️ 算法将使用选择、交叉、变异操作进化，预计需要 3-10 秒</span>`;

  try {
    await ElMessageBox.confirm(
      confirmMessage,
      '🧬 完整版遗传算法智能组卷',
      {
        confirmButtonText: '开始生成',
        cancelButtonText: '取消',
        type: 'info',
        dangerouslyUseHTMLString: true,
        distinguishCancelAndClose: true
      }
    );

    geneticLoading.value = true;

    // 调用完整版遗传算法接口
    const requestData = {
      paperId: examPaperId.value,
      subjectId: kemuTypes.value,
      targetQuestionCount: totalQuestions,
      targetTotalScore: targetScore,
      questionTypeCount: questionTypeCount,
      questionTypeScore: questionTypeScore,
      difficultyDistribution: Object.keys(difficultyDistribution).length > 0 ? difficultyDistribution : null,
      requiredKnowledgePoints: requiredKnowledgePoints.length > 0 ? requiredKnowledgePoints : null
    };

    console.log('🚀 发送遗传算法请求:', requestData);

    // 调用完整版遗传算法接口
    const response = await request({
      method: 'post',
      url: '/advancedGeneticPaper/quickGenerate',
      data: requestData,
      timeout: 30000
    });

    if (response.code === 0 || response.code === 200) {
      ElMessage.success({
        message: '✅ 完整版遗传算法组卷成功！试卷已生成',
        duration: 3000
      });
      
      // 刷新页面数据
      getDataList();
    } else {
      ElMessage.error(response.msg || '智能组卷失败');
    }
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error('智能组卷失败:', error);
      ElMessage.error('智能组卷失败: ' + (error.message || '请稍后重试'));
    }
  } finally {
    geneticLoading.value = false;
  }
};


</script>
<style lang="scss" scoped>
/* ==================== 查询条件区域 ==================== */
.filter-container {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  padding: var(--card-padding);
  background: var(--card-bg);
  border-radius: var(--card-border-radius);
  box-shadow: var(--card-shadow);
  border-top: var(--card-border-top);
  border-right: var(--card-border-right);
  border-bottom: var(--card-border-bottom);
  border-left: var(--card-border-left);

  .filter-item-group {
    flex: 1;
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
    align-items: center;
  }

  .filter-btn-group {
    display: flex;
    gap: 12px;
    align-items: center;
    flex-shrink: 0;
  }
}

/* ==================== 题量/分值设置条样式 ==================== */
.ad {
  margin: 20px 0;
  padding: var(--card-padding, 20px);
  background: var(--card-bg, #ffffff);
  border-radius: var(--card-border-radius, 8px);
  box-shadow: var(--card-shadow, 0 2px 8px rgba(0, 0, 0, 0.08));
  border-top: var(--card-border-top, 1px solid #e4e7ed);
  border-right: var(--card-border-right, 1px solid #e4e7ed);
  border-bottom: var(--card-border-bottom, 1px solid #e4e7ed);
  border-left: var(--card-border-left, 1px solid #e4e7ed);

  .highlight-num {
    color: #1890ff;
    font-weight: 600;
    margin: 0 4px;
  }

  .el-input {
    .el-input__wrapper {
      border-radius: 8px;
      border: 1px solid #dcdfe6;
      transition: all 0.3s;

      &:hover {
        border-color: #c0c4cc;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
      }

      &.is-focus {
        border-color: #1890ff;
        box-shadow: 0 3px 10px rgba(24, 144, 255, 0.15);
      }
    }
  }

  .el-button {
    border-radius: var(--btn-default-border-radius, 6px);
    font-weight: 500;
    transition: all 0.3s;

    &.el-button--success {
      background: var(--btn-success-bg, #67c23a);
      border-color: var(--btn-success-border-color, #67c23a);
      color: var(--btn-success-color, #fff);

      &:hover {
        background: var(--btn-success-hover-bg, #85ce61);
        border-color: var(--btn-success-hover-border-color, #85ce61);
        color: var(--btn-success-hover-color, #fff);
      }
    }

    &.el-button--default {
      background: var(--btn-default-bg, #fff);
      border-color: var(--btn-default-border-color, #dcdfe6);
      color: var(--btn-default-color, #606266);

      &:hover {
        background: var(--btn-default-hover-bg, #f0f9ff);
        border-color: var(--btn-default-hover-border-color, #1890ff);
        color: var(--btn-default-hover-color, #1890ff);
      }
    }
  }

  .el-tag {
    background: #f0f9ff;
    color: #1890ff;
    border: 1px solid #1890ff;
    border-radius: 6px;
    font-weight: 500;

    span {
      color: #1890ff;
      font-weight: 600;
    }
  }
}

/* ==================== 操作按钮区域 ==================== */
.operation-container {
  margin-bottom: 16px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  padding: var(--card-padding);
  background: var(--card-bg);
  border-radius: var(--card-border-radius);
  box-shadow: var(--card-shadow);
  border-top: var(--card-border-top);
  border-right: var(--card-border-right);
  border-bottom: var(--card-border-bottom);
  border-left: var(--card-border-left);
}

/* ==================== 表格容器 ==================== */
.table-scroll-x {
  margin-bottom: 20px;
  background: var(--card-bg);
  border-radius: var(--card-border-radius);
  padding: var(--card-padding);
  box-shadow: var(--card-shadow);
  border-top: var(--card-border-top);
  border-right: var(--card-border-right);
  border-bottom: var(--card-border-bottom);
  border-left: var(--card-border-left);
}

/* ==================== Element Plus 输入框样式覆盖 ==================== */
:deep(.el-input__wrapper) {
  min-height: 38px;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  border: 1px solid #dcdfe6;
  transition: all 0.3s;

  &:hover {
    border-color: #c0c4cc;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }

  &.is-focus {
    border-color: #1890ff;
    box-shadow: 0 3px 10px rgba(24, 144, 255, 0.15);
  }
}

:deep(.el-input__inner) {
  color: #303133;
  font-size: 14px;

  &::placeholder {
    color: #a8abb2;
  }
}

/* ==================== Element Plus 选择器样式覆盖 ==================== */
:deep(.el-select) {
  width: 100%;
}

:deep(.el-select__wrapper) {
  min-height: 38px;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  border: 1px solid #dcdfe6;
  transition: all 0.3s;

  &:hover {
    border-color: #c0c4cc;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  }

  &.is-focused {
    border-color: #1890ff;
    box-shadow: 0 3px 10px rgba(24, 144, 255, 0.15);
  }
}

/* ==================== Element Plus 按钮样式覆盖 ==================== */
:deep(.el-button) {
  font-weight: 500;

  // 主要按钮
  &.el-button--primary {
    background: var(--btn-primary-bg, #1890ff);
    border-width: var(--btn-primary-border-width, 1px);
    border-style: var(--btn-primary-border-style, solid);
    border-color: var(--btn-primary-border-color, #1890ff);
    border-radius: var(--btn-primary-border-radius, 6px);
    color: var(--btn-primary-color, #fff);

    &:hover {
      background: var(--btn-primary-hover-bg, #40a9ff);
      border-color: var(--btn-primary-hover-border-color, #40a9ff);
      color: var(--btn-primary-hover-color, #fff);
    }

    &.is-disabled {
      background: #a0cfff;
    }
  }

  // 默认按钮
  &.el-button--default {
    background: var(--btn-default-bg, #fff);
    border-width: var(--btn-default-border-width, 1px);
    border-style: var(--btn-default-border-style, solid);
    border-color: var(--btn-default-border-color, #dcdfe6);
    border-radius: var(--btn-default-border-radius, 6px);
    color: var(--btn-default-color, #606266);

    &:hover {
      background: var(--btn-default-hover-bg, #f0f9ff);
      border-color: var(--btn-default-hover-border-color, #1890ff);
      color: var(--btn-default-hover-color, #1890ff);
    }
  }

  // 危险按钮
  &.el-button--danger {
    background: var(--btn-danger-bg, #f56c6c);
    border-width: var(--btn-danger-border-width, 1px);
    border-style: var(--btn-danger-border-style, solid);
    border-color: var(--btn-danger-border-color, #f56c6c);
    border-radius: var(--btn-danger-border-radius, 6px);
    color: var(--btn-danger-color, #fff);

    &:hover {
      background: var(--btn-danger-hover-bg, #f78989);
      border-color: var(--btn-danger-hover-border-color, #f78989);
      color: var(--btn-danger-hover-color, #fff);
    }
  }

  // 成功按钮
  &.el-button--success {
    background: var(--btn-success-bg, #67c23a);
    border-width: var(--btn-success-border-width, 1px);
    border-style: var(--btn-success-border-style, solid);
    border-color: var(--btn-success-border-color, #67c23a);
    border-radius: var(--btn-success-border-radius, 6px);
    color: var(--btn-success-color, #fff);

    &:hover {
      background: var(--btn-success-hover-bg, #85ce61);
      border-color: var(--btn-success-hover-border-color, #85ce61);
      color: var(--btn-success-hover-color, #fff);
    }
  }

  // 警告按钮
  &.el-button--warning {
    background: var(--btn-warning-bg, #e6a23c);
    border-width: var(--btn-warning-border-width, 1px);
    border-style: var(--btn-warning-border-style, solid);
    border-color: var(--btn-warning-border-color, #e6a23c);
    border-radius: var(--btn-warning-border-radius, 6px);
    color: var(--btn-warning-color, #fff);

    &:hover {
      background: var(--btn-warning-hover-bg, #ebb563);
      border-color: var(--btn-warning-hover-border-color, #ebb563);
      color: var(--btn-warning-hover-color, #fff);
    }
  }

  // 信息按钮
  &.el-button--info {
    background: var(--btn-info-bg, #909399);
    border-width: var(--btn-info-border-width, 1px);
    border-style: var(--btn-info-border-style, solid);
    border-color: var(--btn-info-border-color, #909399);
    border-radius: var(--btn-info-border-radius, 6px);
    color: var(--btn-info-color, #fff);

    &:hover {
      background: var(--btn-info-hover-bg, #a6a9ad);
      border-color: var(--btn-info-hover-border-color, #a6a9ad);
      color: var(--btn-info-hover-color, #fff);
    }
  }
}

/* ==================== Element Plus 表格样式覆盖 ==================== */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;

  // 表头样式
  .el-table__header-wrapper {
    .el-table__header {
      th {
        background: var(--table-header-bg, #f5f7fa);
        color: var(--table-header-text-color, #303133);
        font-weight: 600;
        font-size: 14px;
        border-bottom: 2px solid var(--table-header-border-color, #e4e7ed);

        .cell {
          padding: 12px 0;
        }
      }
    }
  }

  // 表体样式
  .el-table__body-wrapper {
    .el-table__body {
      tr {
        transition: all 0.3s;

        &:hover {
          background: var(--table-row-hover-bg, #f0f9ff);

          td {
            background: transparent;
          }
        }

        td {
          color: #303133;
          font-size: 14px;
          border-bottom: 1px solid #ebeef5;

          .cell {
            padding: 12px 0;
          }
        }
      }

      // 斑马纹
      .el-table__row--striped {
        td {
          background: var(--table-row-stripe-bg, #fafafa);
        }
      }
    }
  }

  // 边框
  &.el-table--border {
    border: 1px solid #ebeef5;

    td,
    th {
      border-right: 1px solid #ebeef5;
    }
  }
}

/* ==================== 表格内操作按钮 ==================== */
.table-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
  flex-wrap: wrap;

  .el-button {
    margin: 0;
    padding: 6px 12px;
    font-size: 13px;
  }
}

/* ==================== Element Plus 分页样式覆盖 ==================== */
:deep(.el-pagination) {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 20px 0;
  gap: 8px;

  // 总数
  .el-pagination__total {
    color: #606266;
    font-weight: 500;
  }

  // 每页条数选择器
  .el-pagination__sizes {
    .el-select {
      width: auto;
      min-width: 100px;

      .el-select__wrapper {
        border-radius: 6px;
        padding: 0 8px;
      }

      .el-select__selected-item {
        display: flex;
        align-items: center;
      }
    }
  }

  // 上一页/下一页按钮
  .btn-prev,
  .btn-next {
    background: #fff;
    border: 1px solid #dcdfe6;
    border-radius: 6px;
    color: #606266;
    padding: 0 12px;
    min-width: 32px;
    height: 32px;
    transition: all 0.3s;

    &:hover {
      color: #1890ff;
      border-color: #1890ff;
      background: #f0f9ff;
    }

    &:disabled {
      color: #c0c4cc;
      background: #f5f7fa;
      border-color: #e4e7ed;
      cursor: not-allowed;
    }
  }

  // 页码按钮
  .el-pager {
    li {
      background: #fff;
      border: 1px solid #dcdfe6;
      border-radius: 6px;
      color: #606266;
      min-width: 32px;
      height: 32px;
      line-height: 30px;
      margin: 0 4px;
      transition: all 0.3s;

      &:hover {
        color: #1890ff;
        border-color: #1890ff;
        background: #f0f9ff;
      }

      &.is-active {
        background: #1890ff;
        border-color: transparent;
        color: #fff;
        font-weight: 600;
      }

      &.btn-quicknext,
      &.btn-quickprev {
        border: none;

        &:hover {
          color: #1890ff;
        }
      }
    }
  }

  // 跳转
  .el-pagination__jump {
    color: #606266;
    margin-left: 8px;

    .el-input {
      width: 50px;

      .el-input__wrapper {
        border-radius: 6px;
      }
    }
  }
}

/* ==================== 添加试题弹窗样式 ==================== */
.questions-dialog {
  :deep(.el-dialog__header) {
    background: var(--card-bg, #f5f7fa);
    border-bottom: 2px solid var(--table-header-border-color, #e4e7ed);
    padding: 20px;
    margin: 0;
  }

  :deep(.el-dialog__title) {
    color: var(--table-header-text-color, #303133);
    font-weight: 600;
    font-size: 16px;
  }

  :deep(.el-dialog__body) {
    padding: 20px;
    background: #f5f7fa;
  }

  // 弹窗内的筛选容器
  .filter-container {
    margin-bottom: 16px;
  }

  // 弹窗内的操作按钮容器
  .operation-container {
    margin-bottom: 16px;
  }

  // 弹窗内的表格容器
  .table-scroll-x {
    margin-bottom: 20px;
  }

  // 弹窗内的分页
  :deep(.el-pagination) {
    background: var(--card-bg, #ffffff);
    padding: 16px;
    border-radius: var(--card-border-radius, 8px);
    box-shadow: var(--card-shadow, 0 2px 8px rgba(0, 0, 0, 0.08));
    border: 1px solid #e4e7ed;
  }
}

/* ==================== 通用状态标签 ==================== */
.status-tag {
  font-weight: 500;
  border: none;
}

/* ==================== 暂无数据样式 ==================== */
.no-data {
  color: #909399;
  font-size: 13px;
}

/* ==================== 拖拽排序样式 ==================== */
.drag-handle {
  font-size: 18px;
  color: #909399;
  cursor: move;

  &:hover {
    color: #1890ff;
  }
}

.sortable-ghost {
  opacity: 0.5;
  background: #f0f9ff;
}

.sortable-drag {
  opacity: 1;
  background: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.drag-row {
  cursor: default;

  &:hover {
    background-color: #f5f7fa;
  }
}

/* ==================== 响应式适配 ==================== */
@media (max-width: 768px) {
  .filter-container {
    flex-direction: column;
    padding: 16px;

    .filter-item-group {
      width: 100%;

      .filter-item {
        width: 100%;
      }
    }

    .filter-btn-group {
      width: 100%;
      justify-content: flex-start;
    }
  }

  .ad {
    padding: 16px;

    .el-input {
      width: 100% !important;
    }
  }

  .table-scroll-x {
    padding: 12px;
    overflow-x: auto;
  }

  :deep(.el-pagination) {
    justify-content: center;
    flex-wrap: wrap;
  }
}
</style>
