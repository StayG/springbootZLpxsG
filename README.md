# 在线考试系统项目文档

## 目录
1. [项目概述](#项目概述)
2. [技术栈](#技术栈)
3. [项目结构](#项目结构)
4. [核心功能模块](#核心功能模块)
5. [数据库设计](#数据库设计)
6. [系统配置](#系统配置)
7. [API接口说明](#api接口说明)
8. [安全机制](#安全机制)
9. [部署说明](#部署说明)
10. [开发指南](#开发指南)
11. [常见问题](#常见问题)
12. [附录](#附录)

---

## 项目概述

**项目名称**: 在线考试管理系统
**项目版本**: 0.0.1-SNAPSHOT
**开发时间**: 2026年
**项目类型**: 前后端分离的在线考试管理平台

本项目是一个完整的在线考试管理系统，支持**管理员、教师、学生**三种角色，提供试题管理、试卷管理、考试管理、在线答题、自动/人工阅卷、成绩统计、错题本、公告管理等完整功能。

### 核心特色功能 ⭐

1. **密码加密存储（BCrypt）**
   - 使用 BCrypt 算法对所有用户密码进行加密存储
   - 自动生成随机盐值，同一密码每次加密结果不同
   - 抵抗暴力破解和彩虹表攻击

2. **遗传算法智能组卷**
   - 使用遗传算法自动从题库中选择最优题目组合
   - 同时满足题型、分值、难度、知识点等多维度约束
   - 支持自定义算法参数（种群大小、迭代次数等）
   - 通常在数秒内完成组卷，适应度可达95分以上

### 系统组成

| 子系统 | 说明 | 端口 |
|--------|------|------|
| back-stage | Spring Boot 后端服务 | 9999 |
| admin | 管理端前端（管理员 + 教师） | 7777 |
| client | 学生端前端 | 5555 |

### 访问地址

| 地址 | 说明 |
|------|------|
| http://localhost:7777 | 管理端（管理员/教师登录） |
| http://localhost:5555 | 学生端（学生登录） |
| http://localhost:9999/springbootZLpxsG/swagger-ui/index.html | API 文档 |

---

## 技术栈

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.4.3 | 核心框架 |
| Java | 17 | 开发语言 |
| MyBatis-Plus | 3.5.15 | ORM 框架，含分页插件 |
| MySQL | 8.0.33 | 关系型数据库 |
| Redis | - | 缓存（Token、考试数据） |
| JWT (jjwt) | 0.12.5 | 无状态身份认证 |
| SpringDoc OpenAPI | 2.5.0 | Swagger API 文档 |
| EasyExcel | 3.3.2 | Excel 导入导出 |
| Apache POI | 5.2.5 | Excel 底层处理 |
| iTextPDF | 5.5.13.3 | PDF 答卷导出（含中文支持） |
| Hutool | 5.8.28 | Java 工具类库 |
| Lombok | 1.18.42 | 代码简化注解 |
| Spring AOP | - | 操作日志切面 |
| Spring Scheduler | - | 定时任务（强制交卷） |
| Spring Security Crypto | 6.2.1 | 密码加密（BCrypt） |
| Jenetics | 7.2.0 | 遗传算法库（智能组卷） |

### 管理端前端技术栈（admin）

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.22 | 前端框架 |
| Vite | 6.0.5 | 构建工具 |
| Element Plus | 2.9.3 | UI 组件库 |
| Vue Router | 4.x | 路由管理 |
| Pinia | 2.3.1 | 状态管理（含持久化） |
| Axios | 1.7.9 | HTTP 客户端 |
| ECharts | 5.6.0 | 成绩统计数据可视化 |
| WangEditor | 5.1.23 | 富文本编辑器（公告编辑） |
| XLSX | 0.18.5 | Excel 前端处理 |
| Sortable.js | 1.15.7 | 试题拖拽排序 |
| Sass | 1.83.4 | CSS 预处理器 |

### 学生端前端技术栈（client）

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.13 | 前端框架 |
| Vite | 6.0.5 | 构建工具 |
| Element Plus | 2.9.3 | UI 组件库 |
| Vue Router | 4.x | 路由管理 |
| Pinia | 2.3.1 | 状态管理（含持久化） |
| Axios | 1.7.9 | HTTP 客户端 |
| KaTeX | 0.16.11 | 数学/物理/化学公式渲染 |
| Sass | 1.83.4 | CSS 预处理器 |

---

## 项目结构

### 后端项目结构

```
back-stage/
├── src/main/java/com/web/
│   ├── annotation/
│   │   └── IgnoreAuth.java          # 忽略 JWT 认证注解
│   ├── aop/
│   │   ├── LogAnnotation.java       # 日志注解定义
│   │   └── LoginInfoAspect.java     # 登录信息 AOP 切面
│   ├── config/
│   │   ├── CorsConfig.java          # 跨域配置
│   │   ├── ExamStatusScheduler.java # 定时任务：考试结束强制交卷
│   │   ├── InterceptorConfig.java   # 拦截器注册配置
│   │   ├── MybatisPlusConfig.java   # MyBatis-Plus 分页插件
│   │   ├── MyMetaObjectHandler.java # createTime/updateTime 自动填充
│   │   ├── RedisConfig.java         # Redis 序列化配置（Jackson JSON）
│   │   └── SwaggerConfig.java       # SpringDoc API 文档配置
│   ├── controller/                  # RESTful 控制器（17个）
│   │   ├── AdvancedGeneticPaperController.java  # 遗传算法智能组卷 ⭐新增
│   │   ├── DictionaryController.java
│   │   ├── ExamDetailsController.java
│   │   ├── ExamInfoController.java
│   │   ├── ExamPaperController.java
│   │   ├── ExamPaperTopicController.java
│   │   ├── ExamQuestionController.java
│   │   ├── ExamRecordController.java
│   │   ├── ExamWrongQuestionController.java
│   │   ├── FileController.java
│   │   ├── GradesStatisticsController.java
│   │   ├── ListController.java
│   │   ├── ManagersController.java
│   │   ├── NoticesController.java
│   │   ├── RedisTestController.java
│   │   ├── SysOperationLogController.java
│   │   ├── TeachersController.java
│   │   └── UsersController.java
│   ├── domain/                      # 实体类
│   │   ├── dto/                     # 数据传输对象
│   │   ├── Req/                     # 请求封装对象
│   │   ├── Resp/                    # 响应封装对象
│   │   ├── Config.java
│   │   ├── Dictionary.java
│   │   ├── ExamDetails.java
│   │   ├── ExamInfo.java
│   │   ├── ExamPaper.java
│   │   ├── ExamPaperTopic.java
│   │   ├── ExamQuestion.java
│   │   ├── ExamRecord.java
│   │   ├── ExamWrongQuestion.java
│   │   ├── GeneticAlgorithmConfig.java  # 遗传算法配置 ⭐新增
│   │   ├── Chromosome.java              # 染色体类 ⭐新增
│   │   ├── Managers.java
│   │   ├── Notices.java
│   │   ├── SysOperationLog.java
│   │   ├── Teachers.java
│   │   ├── Token.java
│   │   └── Users.java
│   ├── mapper/                      # MyBatis-Plus Mapper 接口（16个）
│   ├── service/                     # 业务逻辑层（含 ExamRecordPdfExportService、RedisCacheService、AdvancedGeneticPaperService ⭐新增）
│   ├── utils/
│   │   ├── ExamStatusConstants.java # 考试状态常量定义
│   │   ├── OperationLogUtil.java    # 操作日志工具类
│   │   ├── PasswordUtil.java        # 密码加密工具类 ⭐新增
│   │   ├── PasswordMigrationUtil.java  # 密码迁移工具 ⭐新增
│   │   ├── JwtUtil.java             # JWT工具类
│   │   └── ...
│   └── BackStageApplication.java   # Spring Boot 启动类
└── src/main/resources/
    ├── application.yml              # 主配置文件
    ├── static/upload/               # 文件上传目录（开发环境）
    └── mapper/                      # MyBatis XML 映射文件
```

### 管理端前端结构（admin）

```
admin/
├── src/
│   ├── components/
│   │   ├── FileUpload/              # 文件上传组件
│   │   ├── ImageView/               # 图片预览组件
│   │   ├── ModuleSelect/            # 模块选择组件
│   │   └── RichEditor/              # WangEditor 富文本封装
│   ├── config/config.js             # 全局配置（baseUrl、角色 key 等）
│   ├── layouts/                     # 布局组件
│   ├── router/router.js             # 路由配置（含路由守卫）
│   ├── stores/
│   │   ├── theme.js                 # 主题状态
│   │   └── user.js                  # 用户登录状态（含 Token）
│   ├── styles/                      # SCSS 样式（Element Plus 主题覆盖）
│   ├── utils/
│   │   ├── request.js               # Axios 封装（自动携带 Token）
│   │   ├── dictionary.js            # 字典转换工具
│   │   ├── examFormulaDefaults.js   # 公式快捷栏默认配置
│   │   ├── menu.js                  # 菜单工具
│   │   ├── storage.js               # localStorage 封装
│   │   └── utils.js                 # 通用工具函数
│   └── views/                       # 页面组件
│       ├── home/                    # 首页（数据概览）
│       ├── examQuestion/            # 试题管理（增删改查、Excel 导入导出）
│       ├── examPaper/               # 试卷管理
│       ├── examPaperTopic/          # 试卷选题（拖拽排序、分值设置）
│       ├── examInfo/                # 考试管理（创建、发布、规则配置）
│       ├── examRecord/              # 考试记录
│       ├── examDetails/             # 考试答题详情
│       ├── examGrading/             # 阅卷管理（待阅卷列表）
│       ├── examGradingDetails/      # 批改试卷（主观题评分）
│       ├── examGradingView/         # 阅卷详情查看
│       ├── examWrongQuestion/       # 错题本管理
│       ├── gradesStatistics/        # 成绩统计（ECharts 图表）
│       ├── notices/                 # 公告管理（富文本编辑）
│       ├── subject/                 # 科目管理
│       ├── teachers/                # 教师管理（含批量导入）
│       ├── users/                   # 学生管理（含批量导入）
│       ├── operationLog/            # 操作日志
│       ├── login.vue                # 登录页
│       ├── personal-center.vue      # 个人中心
│       └── reset-password.vue       # 修改密码
├── index.html
├── package.json
└── vite.config.js                   # 含开发代理配置
```

### 学生端前端结构（client）

```
client/
├── src/
│   ├── config/config.js             # 全局配置（导航菜单、角色 key 等）
│   ├── router/router.js             # 路由配置（含切屏监控守卫）
│   ├── stores/                      # Pinia 状态管理
│   ├── utils/
│   │   ├── examScreenSwitchSession.js  # 切屏监控工具
│   │   └── ...
│   └── views/
│       ├── home/                    # 首页
│       ├── notices/                 # 公告列表 + 详情
│       ├── examCenter/              # 考试中心（可参加的考试列表）
│       ├── examPaper/exam.vue       # 在线答题页面（核心）
│       ├── examRecord/              # 考试记录列表
│       ├── studentExamRecord/       # 学生成绩详情
│       ├── wrongQuestion/           # 错题本
│       ├── wrongQuestionRedo/       # 错题重做
│       ├── helpCenter/              # 帮助中心（文章列表 + 详情）
│       ├── personal-center/         # 个人中心
│       ├── login.vue                # 登录页
│       ├── register.vue             # 注册页
│       └── reset-password-page.vue  # 密码重置页
├── package.json
└── vite.config.js
```
---

## 核心功能模块

### 1. 用户认证模块

#### 角色体系

| 角色 | 登录入口 | 权限范围 |
|------|---------|---------|
| 管理员 (managers) | 管理端 7777 | 全部功能，含用户管理、系统配置 |
| 教师 (teachers) | 管理端 7777 | 试题/试卷/考试/阅卷/成绩管理 |
| 学生 (users) | 学生端 5555 | 参加考试、查看成绩、错题本 |

---

## 核心功能模块


#### 认证流程

**登录流程**:
1. 用户提交账号密码
2. 后端验证用户信息（根据角色查询不同表）
3. 生成 JWT Token（有效期 7 天）
4. Token 存入 Redis（key: `token_角色_用户ID`）
5. 前端存储 Token 到 localStorage
6. 后续请求通过 `Authorization: Bearer <token>` 携带

**Token 验证**:
- 拦截器 `InterceptorConfig` 拦截所有请求
- 从请求头提取 Token
- 验证 Token 签名和有效期
- 从 Redis 查询 Token 是否存在
- 解析 Token 获取用户信息（userId、role）
- 注解 `@IgnoreAuth` 可跳过验证（如登录、注册接口）

**权限控制**:
- 前端路由守卫：根据 `role` 控制页面访问
- 后端接口：通过 Token 中的 `role` 字段判断权限
- 操作日志：仅记录管理员和教师的操作（`OperationLogUtil.java`）

---

### 2. 试题管理模块

#### 试题类型

| 类型代码 | 类型名称 | 说明 |
|---------|---------|------|
| 1 | 单选题 | 单个正确答案 |
| 2 | 多选题 | 多个正确答案 |
| 3 | 判断题 | 对/错 |
| 4 | 填空题 | 多个空格，支持公式 |
| 5 | 简答题 | 主观题，需人工阅卷 |

#### 核心功能

**试题 CRUD**:
- 支持富文本题干（含图片、公式）
- 选项管理（单选/多选）
- 答案设置（客观题自动判分）
- 解析说明（学生查看错题时显示）
- 难度等级（简单/中等/困难）
- 科目分类

**批量导入**:
- Excel 模板导入（EasyExcel）
- 支持批量创建试题
- 导入时自动校验数据格式

**批量导出**:
- 导出试题库为 Excel
- 可按科目、难度筛选导出

**公式支持**:
- 前端使用 KaTeX 渲染数学公式
- 支持 LaTeX 语法（如 `$E=mc^2$`）
- 管理端提供公式快捷栏（`examFormulaDefaults.js`）

---

### 3. 试卷管理模块

#### 试卷组成

```
试卷 (exam_paper)
  ├── 基本信息（名称、科目、总分、时长）
  └── 试卷选题 (exam_paper_topic)
        ├── 题目 1（关联 exam_question）
        ├── 题目 2
        └── ...
```

#### 核心功能

**试卷创建方式**:

**方式一：手动选题**
1. 填写试卷基本信息（名称、科目、总分、考试时长）
2. 进入选题页面（`examPaperTopic`）
3. 从试题库筛选题目（按科目、类型、难度）
4. 拖拽排序（Sortable.js）
5. 设置每题分值
6. 保存试卷

**方式二：智能组卷（遗传算法）** ⭐ 新功能
1. 填写试卷基本信息
2. 点击"智能组卷"按钮
3. 配置组卷参数：
   - 题型分布（单选10题、多选5题等）
   - 每题分值（单选2分、多选4分等）
   - 难度分布（简单10题、中等10题、困难5题）
   - 必选知识点（可选）
4. 系统自动使用遗传算法生成最优试卷
5. 预览并保存

**试卷预览**:
- 查看完整试卷结构
- 显示题目数量、总分、预计时长
- 支持编辑和删除

**试卷复用**:
- 可复制已有试卷
- 修改后另存为新试卷

---

### 3.1 智能组卷模块（遗传算法）⭐ 新功能

#### 功能概述

使用**遗传算法（Genetic Algorithm）**自动从题库中选择最优题目组合，生成符合多维度约束的试卷。

**核心优势**:
- 自动化：无需手动逐题选择
- 多约束：同时满足题型、分值、难度、知识点等多个约束
- 最优化：通过进化算法找到最佳题目组合
- 高效性：通常在数秒内完成组卷（50次迭代）

#### 遗传算法原理

**算法流程**:
```
1. 初始化种群（随机生成N份试卷）
   ↓
2. 计算适应度（评估每份试卷的质量）
   ↓
3. 选择（保留优秀试卷）
   ↓
4. 交叉（优秀试卷互相组合）
   ↓
5. 变异（随机替换或交换题目）
   ↓
6. 重复步骤2-5，直到达到最大迭代次数或找到最优解
   ↓
7. 返回最优试卷
```

**关键概念**:
- **染色体（Chromosome）**: 一份试卷（题目ID列表）
- **基因（Gene）**: 试卷中的一道题目ID
- **适应度（Fitness）**: 试卷质量评分（0-100分）
- **种群（Population）**: 多份试卷的集合
- **交叉（Crossover）**: 两份试卷互相交换部分题目
- **变异（Mutation）**: 随机替换或交换题目

#### 算法参数配置

| 参数 | 说明 | 默认值 | 推荐范围 |
|------|------|--------|---------|
| populationSize | 种群大小（每代试卷数量） | 50 | 30-100 |
| maxGenerations | 最大迭代次数 | 50 | 30-200 |
| crossoverRate | 交叉概率 | 0.7 | 0.6-0.9 |
| mutationRate | 变异概率 | 0.05 | 0.01-0.1 |
| eliteSize | 精英保留数量 | 10 | 5-20 |
| tournamentSize | 锦标赛选择大小 | 5 | 3-7 |

**参数说明**:
- **种群大小越大**：搜索空间越广，但计算时间越长
- **迭代次数越多**：越可能找到最优解，但耗时越长
- **交叉概率高**：加快收敛速度，但可能陷入局部最优
- **变异概率高**：增加多样性，但可能破坏优秀解

**自动调整机制**:
- 当题库规模小（≤10题）时，自动降低种群大小和迭代次数
- 超时保护：单代最多5秒，总体最多20秒
- 提前终止：适应度达到95分以上时提前结束

#### 约束条件

**硬约束（必须满足，否则适应度大幅降低）**:

| 约束 | 说明 | 不满足的惩罚 |
|------|------|------------|
| 题目数量 | 试卷题目总数 = 目标题数 | -1000分 |
| 题目去重 | 不能有重复题目 | -1000分 |
| 题型分布 | 每种题型数量必须精确匹配 | -500分 |

**软约束（尽量满足，影响适应度评分）**:

| 约束 | 权重 | 说明 |
|------|------|------|
| 总分接近度 | 35% | 试卷总分与目标总分的接近程度 |
| 难度分布 | 30% | 实际难度分布与期望分布的接近程度 |
| 知识点覆盖 | 25% | 必选知识点的覆盖率 |
| 题型分值一致性 | 10% | 同题型的题目分值是否一致 |

**适应度计算公式**:
```
适应度 = 100（基础分）
       - 硬约束惩罚（0 或 -500/-1000）
       - 总分偏差 × 35%
       - 难度偏差 × 30%
       + 知识点覆盖率 × 25%
       + 分值一致性 × 10%
```

#### 遗传算子详解

**1. 选择算子：锦标赛选择（Tournament Selection）**

从种群中随机选取K个个体，选择其中适应度最高的作为父代。

```java
// K = tournamentSize（默认5）
for (int i = 0; i < K; i++) {
    随机选择一个个体
    if (该个体适应度 > 当前最优) {
        当前最优 = 该个体
    }
}
return 当前最优
```

**优点**: 保持种群多样性，避免早熟收敛

**2. 交叉算子：顺序交叉（Order Crossover, OX）**

两个父代试卷交换部分题目，生成两个子代试卷。

```
父代1: [Q1, Q2, Q3, Q4, Q5]
父代2: [Q6, Q7, Q8, Q9, Q10]

随机选择交叉点: [2, 4)

子代1: [Q6, Q7, Q3, Q4, Q10]  (保留父代1的Q3-Q4，其余从父代2按顺序填充)
子代2: [Q1, Q2, Q8, Q9, Q5]   (保留父代2的Q8-Q9，其余从父代1按顺序填充)
```

**优点**: 保持题目顺序特征，适合组卷场景

**3. 变异算子：两种策略**

**策略一：替换变异（保持题型）**
```
随机选择一道题目，从同题型的其他题目中随机选择一道替换
例如: 将试卷中的"单选题Q5"替换为另一道"单选题Q20"
```

**策略二：交换变异**
```
随机选择试卷中的两道题目，交换它们的位置
例如: 将Q3和Q7的位置互换
```

**优点**: 增加种群多样性，跳出局部最优

#### API接口

**完整版接口**: `/advancedGeneticPaper/generate`

**请求方式**: POST

**请求体示例**:
```json
{
  "paperId": 1,
  "subjectId": 1,
  "targetQuestionCount": 25,
  "targetTotalScore": 100,
  "questionTypeCount": {
    "1": 10,
    "2": 5,
    "3": 5,
    "4": 3,
    "5": 2
  },
  "questionTypeScore": {
    "1": 2,
    "2": 4,
    "3": 2,
    "4": 5,
    "5": 10
  },
  "difficultyDistribution": {
    "1": 10,
    "2": 10,
    "3": 5
  },
  "requiredKnowledgePoints": ["古诗词鉴赏", "阅读理解"],
  "populationSize": 50,
  "maxGenerations": 50,
  "crossoverRate": 0.7,
  "mutationRate": 0.05
}
```

**快速接口**: `/advancedGeneticPaper/quickGenerate`

**请求方式**: POST

**请求体示例**（使用默认算法参数）:
```json
{
  "paperId": 1,
  "subjectId": 1,
  "targetQuestionCount": 25,
  "targetTotalScore": 100,
  "questionTypeCount": {
    "1": 10,
    "2": 5,
    "3": 5,
    "4": 3,
    "5": 2
  },
  "questionTypeScore": {
    "1": 2,
    "2": 4,
    "3": 2,
    "4": 5,
    "5": 10
  }
}
```

**响应示例**:
```json
{
  "code": 0,
  "msg": "操作成功",
  "data": {
    "message": "智能组卷成功",
    "questionCount": 25,
    "questions": [
      {
        "id": 1,
        "examQuestionName": "以下哪个是Java关键字？",
        "examQuestionTypes": 1,
        "difficultyLevel": 1,
        "knowledgePoint": "Java基础"
      },
      ...
    ]
  }
}
```

#### 核心代码文件

| 文件 | 说明 |
|------|------|
| `AdvancedGeneticPaperController.java` | 智能组卷控制器 |
| `AdvancedGeneticPaperServiceImpl.java` | 遗传算法实现 |
| `GeneticAlgorithmConfig.java` | 算法配置类 |
| `Chromosome.java` | 染色体类（试卷） |

#### 使用示例

**场景：生成一份语文试卷**

**要求**:
- 科目：语文（ID=1）
- 总题数：25题
- 总分：100分
- 题型分布：
  - 单选题：10题，每题2分（共20分）
  - 多选题：5题，每题4分（共20分）
  - 判断题：5题，每题2分（共10分）
  - 填空题：3题，每题5分（共15分）
  - 简答题：2题，每题10分（共20分）
- 难度分布：简单10题、中等10题、困难5题
- 必选知识点：古诗词鉴赏、阅读理解、文言文

**前端调用**:
```javascript
axios.post('/springbootZLpxsG/advancedGeneticPaper/quickGenerate', {
  paperId: 1,
  subjectId: 1,
  targetQuestionCount: 25,
  targetTotalScore: 100,
  questionTypeCount: {
    1: 10,
    2: 5,
    3: 5,
    4: 3,
    5: 2
  },
  questionTypeScore: {
    1: 2,
    2: 4,
    3: 2,
    4: 5,
    5: 10
  },
  difficultyDistribution: {
    1: 10,
    2: 10,
    3: 5
  },
  requiredKnowledgePoints: ['古诗词鉴赏', '阅读理解', '文言文']
})
.then(response => {
  if (response.data.code === 0) {
    console.log('组卷成功！');
    console.log('已选题目：', response.data.data.questions);
  }
});
```

**执行日志**:
```
🧬 开始遗传算法组卷
📚 题库总大小: 150, 目标题数: 25
✅ 初始化种群完成 - 种群大小: 50
🎯 初始最优适应度: 78.50
🔄 开始第0代进化...
📊 第0代: 最优适应度=82.30, 平均适应度=65.40, 耗时=120ms
🔄 开始第10代进化...
📊 第10代: 最优适应度=91.20, 平均适应度=78.60, 耗时=115ms
🔄 开始第20代进化...
📊 第20代: 最优适应度=95.80, 平均适应度=85.30, 耗时=118ms
🎉 达到最优解，提前终止在第23代 - 适应度: 96.50
✅ 遗传算法组卷完成 - 耗时: 2850ms, 最优适应度: 96.50
✅ 已保存25道题目到试卷ID=1
```

#### 性能优化

**1. 小题库优化**:
- 题库≤10题时，自动降低种群大小（20）和迭代次数（30）
- 避免在小数据集上浪费计算资源

**2. 超时保护**:
- 单代最多5秒
- 总体最多20秒
- 超时后返回当前最优解

**3. 提前终止**:
- 适应度≥95分时提前终止
- 避免无意义的迭代

**4. 精英保留**:
- 每代保留前10名优秀个体
- 加快收敛速度

#### 常见问题

**Q1: 题库不足怎么办？**
- 系统会在组卷前验证题库
- 如果某种题型数量不足，会报错提示

**Q2: 为什么组卷结果每次不同？**
- 遗传算法是随机算法，每次执行结果略有差异
- 但都会满足约束条件，且适应度相近

**Q3: 如何提高组卷质量？**
- 增加种群大小（如100）
- 增加迭代次数（如200）
- 调整约束权重（修改适应度函数）

**Q4: 组卷速度慢怎么办？**
- 降低种群大小（如30）
- 降低迭代次数（如30）
- 题库规模很大时会自动优化

**Q5: 如何保证知识点覆盖？**
- 在 `requiredKnowledgePoints` 中指定必选知识点
- 系统会优先选择包含这些知识点的题目

---

### 4. 考试管理模块

#### 考试状态

考试状态由 **考试时间动态计算**，不依赖定时任务更新 `status` 字段。

| 状态码 | 状态名称 | 计算规则 |
|-------|---------|---------|
| 0 | 未开始 | 当前时间 < 开始时间 |
| 1 | 进行中 | 开始时间 ≤ 当前时间 < 结束时间 |
| 2 | 已结束 | 当前时间 ≥ 结束时间 |

**状态常量定义**: `ExamStatusConstants.java`

```java
public class ExamStatusConstants {
    public static final Integer NOT_STARTED = 0;  // 未开始
    public static final Integer IN_PROGRESS = 1;  // 进行中
    public static final Integer ENDED = 2;        // 已结束
}
```

#### 定时任务

**ExamStatusScheduler.java**:
- 每分钟执行一次
- 查询所有"进行中"且已超时的考试
- 强制交卷未提交的考试记录
- 更新 `exam_record.submit_time` 和 `exam_record.status`

#### 考试规则配置

| 配置项 | 说明 |
|-------|------|
| 开始时间 | 考试开始时间 |
| 结束时间 | 考试结束时间 |
| 考试时长 | 单位：分钟 |
| 及格分数 | 用于成绩统计 |
| 允许切屏次数 | 0 表示不允许切屏 |
| 是否公开成绩 | 控制学生是否可见成绩 |
| 是否显示答案 | 考试结束后是否显示正确答案 |

#### 考试发布流程

1. 创建考试（选择试卷、设置时间和规则）
2. 发布考试（学生端可见）
3. 学生在规定时间内参加考试
4. 考试结束后自动/人工阅卷
5. 发布成绩（学生可查看）

---

### 5. 在线答题模块

#### 答题页面核心功能

**页面**: `client/src/views/examPaper/exam.vue`

**功能特性**:
- 题目列表导航（左侧）
- 答题区域（右侧）
- 倒计时显示（自动交卷）
- 答题进度提示
- 暂存答案（防止意外关闭）
- 切屏监控（`examScreenSwitchSession.js`）
- 公式渲染（KaTeX）
- 图片查看（ImageView 组件）

#### 切屏监控

**工具类**: `client/src/utils/examScreenSwitchSession.js`

**监控逻辑**:
1. 监听 `visibilitychange` 事件
2. 检测页面失焦（切换标签页、最小化窗口）
3. 记录切屏次数到 `sessionStorage`
4. 超过允许次数时：
   - 弹窗警告
   - 强制交卷（调用后端接口）
   - 跳转到考试记录页

**后端记录**:
- `exam_record.screen_switch_count` 字段记录切屏次数
- 教师可在阅卷时查看切屏记录

#### 断点续考

**实现方式**:
- 答案实时保存到后端（`exam_details` 表）
- 学生重新进入考试时，自动加载已保存的答案
- 倒计时根据已用时间计算剩余时间

---

### 6. 阅卷模块

#### 自动阅卷

**适用题型**: 单选题、多选题、判断题、填空题

**判分逻辑**:
- 单选题：答案完全匹配得分，否则 0 分
- 多选题：答案完全匹配得分，否则 0 分（不支持部分得分）
- 判断题：答案匹配得分
- 填空题：每个空格独立判分，支持多个正确答案（用 `|` 分隔）

**触发时机**:
- 学生提交试卷时自动判分
- 客观题得分立即计算
- 主观题得分初始为 0，等待人工阅卷

#### 人工阅卷

**适用题型**: 简答题

**阅卷流程**:
1. 教师进入"阅卷管理"页面
2. 查看待阅卷列表（含主观题的考试记录）
3. 点击"批改试卷"进入详情页
4. 查看学生答案
5. 给出分数和评语
6. 保存评分
7. 系统自动更新总分

**阅卷页面**: `admin/src/views/examGradingDetails/index.vue`

---

### 7. 成绩统计模块

#### 统计维度

| 维度 | 说明 |
|------|------|
| 考试维度 | 某次考试的整体成绩分布 |
| 学生维度 | 某个学生的历史成绩趋势 |
| 科目维度 | 某个科目的平均分、及格率 |

#### 统计指标

- 平均分
- 最高分 / 最低分
- 及格率
- 优秀率（≥90分）
- 成绩分布（0-59、60-69、70-79、80-89、90-100）

#### 数据可视化

**技术**: ECharts 5.6.0

**图表类型**:
- 柱状图：成绩分布
- 折线图：成绩趋势
- 饼图：及格率/不及格率
- 雷达图：多科目对比

**页面**: `admin/src/views/gradesStatistics/`

---

### 8. 错题本模块

#### 错题收集

**触发条件**:
- 考试结束后，系统自动将答错的题目加入错题本
- 表: `exam_wrong_question`
- 字段: `user_id`, `question_id`, `exam_record_id`, `user_answer`, `correct_answer`

#### 错题查看

**学生端功能**:
- 查看错题列表（按科目、时间筛选）
- 查看错题详情（题目、我的答案、正确答案、解析）
- 标记"已掌握"（从错题本移除）

**页面**: `client/src/views/wrongQuestion/`

#### 错题重做

**功能**:
- 将错题组成模拟试卷
- 学生重新作答
- 系统自动判分
- 答对后自动从错题本移除

**页面**: `client/src/views/wrongQuestionRedo/`

---

### 9. 公告管理模块

#### 公告类型

| 类型 | 说明 |
|------|------|
| 系统公告 | 全站公告（所有用户可见） |
| 考试公告 | 针对某次考试的说明 |

#### 核心功能

**管理端**:
- 富文本编辑器（WangEditor 5.1.23）
- 支持图片上传、文字格式化
- 公告发布/撤回
- 置顶功能

**学生端**:
- 公告列表（按发布时间倒序）
- 公告详情（富文本渲染）
- 未读标记

**页面**:
- 管理端: `admin/src/views/notices/`
- 学生端: `client/src/views/notices/`

---

### 10. 文件管理模块

#### 文件上传

**支持类型**:
- 图片: `.jpg`, `.jpeg`, `.png`, `.gif`（用于题目、公告）
- Excel: `.xls`, `.xlsx`（用于批量导入）

**上传流程**:
1. 前端选择文件
2. 调用 `/file/upload` 接口
3. 后端保存到 `static/upload/` 目录
4. 返回文件访问 URL

**组件**: `admin/src/components/FileUpload/index.vue`

#### 文件访问

**开发环境**:
- 文件存储路径: `back-stage/src/main/resources/static/upload/`
- 访问 URL: `http://localhost:9999/springbootZLpxsG/upload/文件名`

**生产环境**:
- 建议使用 Nginx 代理静态资源
- 或使用对象存储服务（OSS、S3）

---

### 11. 操作日志模块

#### 日志记录

**工具类**: `OperationLogUtil.java`

**记录范围**:
- 仅记录管理员和教师的操作
- 不记录学生操作（避免日志过多）

**记录内容**:
- 操作人（用户 ID、用户名、角色）
- 操作时间
- 操作类型（新增、修改、删除、查询）
- 操作模块（试题、试卷、考试等）
- 操作详情（如"删除试题 ID=123"）
- IP 地址

**实现方式**:
- AOP 切面 `LoginInfoAspect.java`
- 注解 `@LogAnnotation`
- 异步写入数据库（不影响主业务）

#### 日志查询

**管理端功能**:
- 按时间范围查询
- 按操作人查询
- 按操作类型查询
- 导出日志为 Excel

**页面**: `admin/src/views/operationLog/index.vue`

---

## 数据库设计

### 数据库概览

**数据库名称**: `springbootzlpxsg`
**数据库名称**: `springbootzlpxsg`
**字符集**: `utf8mb3` / `utf8mb4`
**排序规则**: `utf8mb3_general_ci` / `utf8mb4_0900_ai_ci`
**表数量**: 11 张
**引擎**: InnoDB

### 核心表结构

#### 1. users（学生表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键，自增 |
| user_name | varchar(50) | 用户名（唯一，非空） |
| password | varchar(255) | 密码（BCrypt加密，非空） |
| avatar | varchar(255) | 头像URL |
| nickname | varchar(255) | 姓名 |
| balance | double(10,2) | 余额，默认0.00 |
| email | varchar(100) | 邮箱（唯一） |
| phone | varchar(20) | 电话（唯一） |
| gender | varchar(50) | 性别 |
| create_time | timestamp | 创建时间，默认CURRENT_TIMESTAMP |
| update_time | timestamp | 修改时间，自动更新 |
| status | int | 状态（1启用 0禁用），默认1 |

**索引**:
- PRIMARY KEY (`id`)
- UNIQUE KEY (`user_name`)
- UNIQUE KEY (`email`)
- UNIQUE KEY (`phone`)

---

#### 2. teachers（教师表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键，自增 |
| user_name | varchar(50) | 用户名（唯一，非空） |
| password | varchar(255) | 密码（BCrypt加密，非空） |
| real_name | varchar(50) | 真实姓名 |
| avatar | varchar(255) | 头像URL |
| email | varchar(100) | 邮箱（唯一） |
| phone | varchar(20) | 电话（唯一） |
| gender | varchar(50) | 性别 |
| kemu_types | int | 任教科目ID（关联dictionary表） |
| title | varchar(50) | 职称 |
| create_time | timestamp | 创建时间，默认CURRENT_TIMESTAMP |
| update_time | timestamp | 修改时间，自动更新 |
| status | int | 状态（1启用 0禁用），默认1 |

**索引**:
- PRIMARY KEY (`id`)
- UNIQUE KEY (`user_name`)
- UNIQUE KEY (`email`)
- UNIQUE KEY (`phone`)

---

#### 3. managers（管理员表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键，自增 |
| user_name | varchar(255) | 账号（非空） |
| password | varchar(255) | 密码（BCrypt加密，非空） |
| real_name | varchar(255) | 姓名（非空） |
| avatar | varchar(255) | 头像URL |
| phone | varchar(255) | 手机号（非空） |
| email | varchar(255) | 邮箱（非空） |
| create_time | datetime | 创建时间（非空） |
| role | varchar(255) | 角色标识 |

**索引**:
- PRIMARY KEY (`id`)

---

#### 4. exam_question（试题表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键，自增 |
| exam_question_name | longtext | 试题题干（支持富文本HTML） |
| kemu_types | int | 科目ID（关联dictionary表） |
| exam_question_options | longtext | 选项（JSON字符串） |
| exam_question_answer | varchar(200) | 正确答案 |
| exam_question_analysis | longtext | 答案解析 |
| exam_question_types | int | 试题类型（1单选 2多选 3判断 4填空 5简答），默认0 |
| difficulty_level | int | 难度等级（1简单 2中等 3困难），默认2 |
| knowledge_point | varchar(200) | 知识点标签 |
| formula_type | varchar(32) | 公式快捷栏类型（none/math/physics/chemistry/biology/geography），默认none |
| exam_question_sequence | int | 试题排序，值越大越靠前，默认100 |
| create_time | timestamp | 创建时间，默认CURRENT_TIMESTAMP |
| update_time | timestamp | 修改时间 |

**索引**:
- PRIMARY KEY (`id`)

**说明**:
- `formula_type` 字段用于学生端答题时显示对应学科的公式快捷栏
- `knowledge_point` 用于智能组卷的知识点覆盖约束

---

#### 5. exam_paper（试卷表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键，自增 |
| exam_paper_name | varchar(200) | 试卷名称（非空） |
| exam_paper_myscore | int | 试卷总分数（非空），默认0 |
| kemu_types | int | 科目ID（关联dictionary表） |
| exam_paper_types | int | 试卷状态（非空），默认0 |
| zujuan_types | int | 组卷方式（1自动组卷 2手动组卷） |
| create_time | timestamp | 创建时间，默认CURRENT_TIMESTAMP |
| update_time | timestamp | 修改时间 |

**索引**:
- PRIMARY KEY (`id`)

---

#### 6. exam_paper_topic（试卷选题表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键，自增 |
| exam_paper_id | int | 试卷ID（外键，非空） |
| exam_question_id | int | 试题ID（外键，非空） |
| exam_paper_topic_number | int | 试题分数（非空），默认0 |
| exam_paper_topic_sequence | int | 试题排序，默认100 |
| create_time | timestamp | 创建时间，默认CURRENT_TIMESTAMP |

**索引**:
- PRIMARY KEY (`id`)
- UNIQUE KEY `uk_paper_question` (`exam_paper_id`, `exam_question_id`)
- FOREIGN KEY (`exam_question_id`) REFERENCES `exam_question` (`id`) ON DELETE CASCADE

---

#### 7. exam_info（考试信息表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键，自增 |
| exam_name | varchar(200) | 考试名称（非空） |
| kemu_types | int | 科目ID（非空，关联dictionary表） |
| teacher_id | int | 创建教师ID（可为NULL，管理员创建时） |
| exam_paper_id | int | 关联试卷ID（非空） |
| start_time | datetime | 考试开始时间（非空） |
| end_time | datetime | 考试结束时间（非空） |
| duration | int | 考试时长（分钟） |
| passing_score | int | 及格分数，默认0 |
| allow_screen_switch | tinyint(1) | 是否允许切屏（0不允许 1允许），默认0 |
| max_screen_switch | int | 最大切屏次数，默认3 |
| allow_copy_paste | tinyint(1) | 是否允许复制粘贴（0不允许 1允许），默认0 |
| option_shuffle | tinyint(1) | 选项乱序（0不乱序 1乱序），默认0 |
| exam_password | varchar(50) | 考试密码（可选） |
| show_answer_after_submit | tinyint(1) | 交卷后显示答案（0不显示 1显示），默认0 |
| allow_retake | tinyint(1) | 允许重考（0不允许 1允许），默认0 |
| max_retake_count | int | 最多允许重考次数，默认0 |
| status | tinyint(1) | 考试状态（0未发布 1已发布 2进行中 3已结束），默认0 |
| create_time | datetime | 创建时间，默认CURRENT_TIMESTAMP |
| update_time | datetime | 更新时间，自动更新 |

**索引**:
- PRIMARY KEY (`id`)
- INDEX `idx_kemu_types` (`kemu_types`)
- INDEX `idx_teacher_id` (`teacher_id`)
- INDEX `idx_exam_paper_id` (`exam_paper_id`)
- INDEX `idx_status` (`status`)
- INDEX `idx_start_time` (`start_time`)

**说明**: 考试状态由开始时间和结束时间动态计算

---

#### 8. exam_record（考试记录表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键，自增 |
| exam_record_uuid_number | varchar(200) | 考试编号（唯一） |
| users_id | int | 考试用户ID（非空） |
| exam_paper_id | int | 所属试卷ID（非空） |
| exam_info_id | int | 所属考试ID（外键） |
| total_score | int | 所得总分 |
| auto_score | int | 客观题自动判分总和，默认0 |
| teacher_score | int | 主观题教师批改得分总和，默认0 |
| pass_status | tinyint | 及格状态（0待判定 1及格 2不及格），默认0 |
| status | int | 考试状态（0考试中 1已提交 2强制交卷 3教师已批阅），默认0 |
| insert_time | datetime | 考试开始时间（非空） |
| end_time | datetime | 交卷时间/强制结束时间 |
| create_time | datetime | 创建时间，默认CURRENT_TIMESTAMP |
| screen_switch_count | int | 切屏次数，默认0 |
| screen_switch_times | text | 切屏时间列表JSON |
| is_latest | tinyint(1) | 是否为当前有效记录（1当前有效 0历史作废），默认1 |

**索引**:
- PRIMARY KEY (`id`)
- INDEX (`exam_paper_id`)
- INDEX (`exam_record_uuid_number`)
- FOREIGN KEY `fk_exam_record_exam_info` (`exam_info_id`) REFERENCES `exam_info` (`id`) ON DELETE CASCADE
- FOREIGN KEY (`exam_paper_id`) REFERENCES `exam_paper` (`id`) ON DELETE CASCADE

---

#### 9. exam_details（答题详情表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键，自增 |
| exam_details_uuid_number | varchar(200) | 试卷编号（外键） |
| users_id | int | 用户ID（非空） |
| exam_record_id | int | 考试记录ID（外键，非空） |
| exam_question_id | int | 试题ID（外键，非空） |
| exam_paper_topic_number | int | 题目分值 |
| exam_paper_topic_sequence | int | 题目在试卷中的排序 |
| exam_details_myanswer | varchar(200) | 考生答案 |
| exam_details_myscore | int | 试题得分，默认0 |
| teacher_score | int | 教师评分（主观题） |
| teacher_comment | varchar(500) | 教师评语（主观题） |
| review_status | int | 批阅状态（0待批阅 1已批阅，仅用于简答题），默认0 |
| create_time | timestamp | 创建时间，默认CURRENT_TIMESTAMP |
| update_time | timestamp | 更新时间，自动更新 |

**索引**:
- PRIMARY KEY (`id`)
- UNIQUE INDEX `idx_exam_ques` (`exam_record_id`, `exam_question_id`)
- INDEX (`exam_question_id`)
- INDEX (`exam_details_uuid_number`)
- INDEX `idx_exam_record_sequence` (`exam_record_id`, `exam_paper_topic_sequence`)
- FOREIGN KEY (`exam_question_id`) REFERENCES `exam_question` (`id`) ON DELETE CASCADE
- FOREIGN KEY (`exam_details_uuid_number`) REFERENCES `exam_record` (`exam_record_uuid_number`) ON DELETE CASCADE
- FOREIGN KEY `fk_exam_record_id` (`exam_record_id`) REFERENCES `exam_record` (`id`) ON DELETE CASCADE

---

#### 10. exam_wrong_question（错题表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键，自增 |
| users_id | int | 用户ID（非空） |
| exam_paper_id | int | 试卷ID（外键，非空） |
| exam_question_id | int | 试题ID（外键，非空） |
| exam_details_myanswer | varchar(200) | 考生作答 |
| mastery_status | tinyint | 掌握状态（0未掌握 1已掌握），默认0 |
| exam_record_id | int | 关联考试记录ID |
| insert_time | timestamp | 记录时间，默认CURRENT_TIMESTAMP |
| create_time | timestamp | 创建时间，默认CURRENT_TIMESTAMP |

**索引**:
- PRIMARY KEY (`id`)
- UNIQUE INDEX `uk_user_record_question` (`users_id`, `exam_record_id`, `exam_question_id`)
- INDEX (`exam_paper_id`)
- INDEX (`exam_question_id`)
- FOREIGN KEY (`exam_paper_id`) REFERENCES `exam_paper` (`id`) ON DELETE CASCADE
- FOREIGN KEY (`exam_question_id`) REFERENCES `exam_question` (`id`) ON DELETE CASCADE

---

#### 11. notices（公告表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键，自增 |
| title | varchar(255) | 标题（非空） |
| content | text | 内容（富文本） |
| pictures | varchar(255) | 图片URL |
| teacher_id | int | 发布教师ID |
| kemu_id | int | 科目ID |
| create_time | timestamp | 创建时间，默认CURRENT_TIMESTAMP |
| update_time | timestamp | 修改时间 |

**索引**:
- PRIMARY KEY (`id`)
- INDEX `idx_teacher_id` (`teacher_id`)
- INDEX `idx_kemu_id` (`kemu_id`)

---

#### 12. sys_operation_log（管理员操作日志表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键，自增 |
| operation_time | datetime | 操作时间（非空） |
| admin_name | varchar(64) | 管理员名称（非空） |
| operation_module | varchar(64) | 操作模块（非空） |
| action_type | varchar(32) | 操作类型（新增/修改/删除/发布/导入/禁用），非空 |
| content | varchar(512) | 操作内容（非空） |
| ip | varchar(64) | IP地址（非空） |
| success | tinyint | 操作结果（1成功 0失败），默认1 |

**索引**:
- PRIMARY KEY (`id`)
- INDEX `idx_op_time` (`operation_time`)
- INDEX `idx_admin` (`admin_name`)
- INDEX `idx_module` (`operation_module`)

---

#### 13. dictionary（字典表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键，自增 |
| dic_code | varchar(200) | 字段（字典编码） |
| dic_name | varchar(200) | 字段名（字典名称） |
| code_index | int | 编码（排序索引） |
| index_name | varchar(200) | 编码名字（字典值） |
| super_id | int | 父字段ID |
| remark | varchar(200) | 备注 |
| create_time | timestamp | 创建时间 |

**索引**:
- PRIMARY KEY (`id`)

**用途**: 存储系统字典数据（性别、科目、试卷状态、题型等）

**字典类型示例**:
- `sex_types`: 性别类型（1男 2女）
- `kemu_types`: 科目（1语文 2数学 3英语 4物理 5化学 6生物 7政治 8历史 9地理）
- `exam_question_types`: 试题类型（1单选题 2多选题 3判断题 4填空题 5简答题）
- `exam_paper_types`: 试卷状态（1启用 2禁用）
- `zujuan_types`: 组卷方式（1自动组卷 2手动组卷）

---

#### 14. token（登录凭证表）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 主键，自增 |
| user_id | int | 用户ID（非空） |
| user_name | varchar(100) | 用户名（非空） |
| table_name | varchar(100) | 表名（users/teachers/managers） |
| role | varchar(100) | 角色标识 |
| token | text | JWT Token凭证 |
| create_time | timestamp | 新增时间，默认CURRENT_TIMESTAMP |
| expirated_time | timestamp | 过期时间，默认CURRENT_TIMESTAMP |
| is_blacklisted | int | 是否在黑名单中（0正常 1已失效），默认0 |

**索引**:
- PRIMARY KEY (`id`)
- INDEX `idx_user_id` (`user_id`)
- INDEX `idx_token_blacklist` (`token`(255), `is_blacklisted`)

**说明**: 用于存储JWT Token和实现Token黑名单机制

---

### 考试状态码说明

| 状态码 | 状态名称 | 适用表 | 说明 |
|-------|---------|--------|------|
| 0 | 未开始 | exam_info（动态计算） | 当前时间 < 开始时间 |
| 1 | 进行中 | exam_info（动态计算） | 开始时间 ≤ 当前时间 < 结束时间 |
| 2 | 已结束 | exam_info（动态计算） | 当前时间 ≥ 结束时间 |
| 0 | 答题中 | exam_record | 学生正在答题 |
| 1 | 已提交 | exam_record | 学生已提交，等待阅卷 |
| 2 | 已阅卷 | exam_record | 阅卷完成，成绩已发布 |

---

## 系统配置

### 后端配置文件

**文件路径**: `back-stage/src/main/resources/application.yml`



```yaml
server:
  port: 9999
  servlet:
    context-path: /springbootZLpxsG

spring:
  application:
    name: back-stage
  
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/exam_system?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8
    username: root
    password: 123456
  
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      timeout: 5000ms
  
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.web.domain
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

# JWT 配置
jwt:
  secret: your-secret-key-here-change-in-production
  expiration: 604800000  # 7天（毫秒）

# 文件上传路径
file:
  upload-path: src/main/resources/static/upload/
```

**关键配置说明**:
- **端口**: 9999
- **上下文路径**: `/springbootZLpxsG`（所有接口前缀）
- **数据库**: MySQL 8.0.33，数据库名 `exam_system`
- **Redis**: 本地 6379 端口，用于 Token 缓存
- **文件上传**: 最大 10MB，存储在 `static/upload/`
- **JWT**: 有效期 7 天，生产环境需修改 `secret`

---

### 管理端配置文件

**文件路径**: `admin/src/config/config.js`

```javascript
export default {
  // 后端 API 基础地址
  baseUrl: 'http://localhost:9999/springbootZLpxsG',
  
  // 角色标识
  role: {
    管理员: 'managers',
    教师: 'teachers',
    学生: 'users'
  },
  
  // 导航菜单配置
  indexNav: [
    { name: '首页', url: '/home' },
    { name: '试题管理', url: '/examQuestion' },
    { name: '试卷管理', url: '/examPaper' },
    { name: '考试管理', url: '/examInfo' },
    { name: '阅卷管理', url: '/examGrading' },
    { name: '成绩统计', url: '/gradesStatistics' },
    { name: '公告管理', url: '/notices' },
    { name: '用户管理', url: '/users' },
    { name: '教师管理', url: '/teachers' },
    { name: '科目管理', url: '/subject' },
    { name: '操作日志', url: '/operationLog' }
  ]
}
```

---

### 学生端配置文件

**文件路径**: `client/src/config/config.js`

```javascript
export default {
  // 后端 API 基础地址
  baseUrl: 'http://localhost:9999/springbootZLpxsG',
  
  // 角色标识
  role: {
    学生: 'users'
  },
  
  // 导航菜单配置
  indexNav: [
    { name: '首页', url: '/home' },
    { name: '考试中心', url: '/examCenter' },
    { name: '考试记录', url: '/examRecord' },
    { name: '错题本', url: '/wrongQuestion' },
    { name: '公告', url: '/notices' },
    { name: '帮助中心', url: '/helpCenter' }
  ]
}
```

---

### Vite 代理配置

**管理端**: `admin/vite.config.js`

```javascript
export default defineConfig({
  server: {
    port: 7777,
    proxy: {
      '/springbootZLpxsG': {
        target: 'http://localhost:9999',
        changeOrigin: true
      }
    }
  }
})
```

**学生端**: `client/vite.config.js`

```javascript
export default defineConfig({
  server: {
    port: 5555,
    proxy: {
      '/springbootZLpxsG': {
        target: 'http://localhost:9999',
        changeOrigin: true
      }
    }
  }
})
```

---

## API接口说明

### 接口规范

**Base URL**: `http://localhost:9999/springbootZLpxsG`

**请求头**:
```
Content-Type: application/json
Authorization: Bearer <token>
```

**响应格式**:
```json
{
  "code": 0,           // 0成功，非0失败
  "msg": "操作成功",
  "data": {}           // 响应数据
}
```

---

### 主要接口列表

#### 用户认证接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/users/login` | POST | 学生登录 |
| `/teachers/login` | POST | 教师登录 |
| `/managers/login` | POST | 管理员登录 |
| `/users/register` | POST | 学生注册 |
| `/users/logout` | POST | 退出登录 |
| `/users/info` | GET | 获取当前用户信息 |
| `/users/updatePassword` | POST | 修改密码 |

---

#### 试题管理接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/examQuestion/page` | GET | 分页查询试题 |
| `/examQuestion/info/{id}` | GET | 获取试题详情 |
| `/examQuestion/save` | POST | 新增试题 |
| `/examQuestion/update` | POST | 修改试题 |
| `/examQuestion/delete` | POST | 删除试题 |
| `/examQuestion/import` | POST | Excel 批量导入 |
| `/examQuestion/export` | GET | 导出试题为 Excel |

---

#### 试卷管理接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/examPaper/page` | GET | 分页查询试卷 |
| `/examPaper/info/{id}` | GET | 获取试卷详情 |
| `/examPaper/save` | POST | 新增试卷 |
| `/examPaper/update` | POST | 修改试卷 |
| `/examPaper/delete` | POST | 删除试卷 |
| `/examPaperTopic/list/{paperId}` | GET | 获取试卷题目列表 |
| `/examPaperTopic/save` | POST | 添加题目到试卷 |
| `/examPaperTopic/delete` | POST | 从试卷移除题目 |

---

#### 考试管理接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/examInfo/page` | GET | 分页查询考试 |
| `/examInfo/info/{id}` | GET | 获取考试详情 |
| `/examInfo/save` | POST | 创建考试 |
| `/examInfo/update` | POST | 修改考试 |
| `/examInfo/delete` | POST | 删除考试 |
| `/examInfo/availableExams` | GET | 学生端：获取可参加的考试 |
| `/examInfo/startExam/{examId}` | POST | 学生开始考试 |

---

#### 答题接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/examRecord/save` | POST | 创建考试记录 |
| `/examRecord/submit` | POST | 提交试卷 |
| `/examDetails/saveAnswer` | POST | 保存答案（断点续考） |
| `/examDetails/getAnswers/{recordId}` | GET | 获取已保存的答案 |

---

#### 阅卷接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/examRecord/pendingGrading` | GET | 获取待阅卷列表 |
| `/examGrading/grade` | POST | 批改主观题 |
| `/examGrading/publish` | POST | 发布成绩 |

---

#### 成绩统计接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/gradesStatistics/examStats/{examId}` | GET | 获取考试统计数据 |
| `/gradesStatistics/studentStats/{userId}` | GET | 获取学生成绩趋势 |
| `/gradesStatistics/export/{examId}` | GET | 导出成绩为 Excel |

---

#### 错题本接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/examWrongQuestion/list` | GET | 获取错题列表 |
| `/examWrongQuestion/detail/{id}` | GET | 获取错题详情 |
| `/examWrongQuestion/markMastered` | POST | 标记已掌握 |

---

#### 公告接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/notices/page` | GET | 分页查询公告 |
| `/notices/info/{id}` | GET | 获取公告详情 |
| `/notices/save` | POST | 发布公告 |
| `/notices/update` | POST | 修改公告 |
| `/notices/delete` | POST | 删除公告 |

---

#### 文件上传接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/file/upload` | POST | 上传文件（图片、Excel） |

**请求格式**: `multipart/form-data`

**响应示例**:
```json
{
  "code": 0,
  "msg": "上传成功",
  "data": {
    "url": "http://localhost:9999/springbootZLpxsG/upload/20260510_123456_image.jpg"
  }
}
```

---

### Swagger API 文档

**访问地址**: http://localhost:9999/springbootZLpxsG/swagger-ui/index.html

**功能**:
- 查看所有接口定义
- 在线测试接口
- 查看请求/响应示例

---

## 安全机制

### 1. JWT 身份认证

**Token 生成**:
- 使用 `jjwt` 库生成 JWT Token
- Payload 包含: `userId`, `username`, `role`
- 签名算法: HS256
- 有效期: 7 天

**Token 存储**:
- 后端: Redis（key: `token_角色_用户ID`）
- 前端: localStorage（key: `token`）

**Token 验证**:
- 拦截器拦截所有请求
- 验证 Token 签名和有效期
- 从 Redis 查询 Token 是否存在
- 解析 Token 获取用户信息

**Token 刷新**:
- 当前实现: Token 过期后需重新登录
- 建议优化: 实现 Refresh Token 机制

---

### 2. 密码加密存储（BCrypt）

#### 加密算法

**技术**: Spring Security Crypto - BCrypt
**版本**: 6.2.1

**BCrypt 特点**:
- 自适应哈希算法，计算成本可配置
- 每次加密自动生成随机盐（Salt）
- 同一密码每次加密结果不同
- 单向加密，不可逆向解密
- 抵抗暴力破解和彩虹表攻击

#### 密码加密工具类

**工具类路径**: `back-stage/src/main/java/com/web/utils/PasswordUtil.java`

**核心方法**:

```java
// 1. 加密密码
public static String encode(String rawPassword)

// 2. 验证密码
public static boolean matches(String rawPassword, String encodedPassword)

// 3. 检查密码是否已加密
public static boolean isEncoded(String password)
```

**加密示例**:
```java
// 明文密码
String rawPassword = "123456";

// 加密后的密码（60字符，包含算法版本、成本因子、盐值和哈希值）
String encoded = PasswordUtil.encode(rawPassword);
// 结果示例: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl.GYa7Y7W
```

**验证示例**:
```java
// 用户登录时验证密码
String inputPassword = "123456";
String dbPassword = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl.GYa7Y7W";

boolean isValid = PasswordUtil.matches(inputPassword, dbPassword);
// 返回 true 表示密码正确
```

#### 应用场景

**1. 用户注册**
```java
// UsersController.register()
Users user = new Users();
user.setUserName(username);
user.setPassword(PasswordUtil.encode(password));  // 加密存储
usersService.save(user);
```

**2. 用户登录**
```java
// UsersController.login()
Users user = usersService.getOne(
    Wrappers.lambdaQuery(Users.class)
        .eq(Users::getUserName, userName)
);

// 验证密码
if (user == null || !PasswordUtil.matches(password, user.getPassword())) {
    return Result.error("账号或密码错误");
}
```

**3. 修改密码**
```java
// UsersController.updatePassword()
Users user = usersService.getById(userId);

// 验证旧密码
if (!PasswordUtil.matches(oldPassword, user.getPassword())) {
    return Result.error("原密码错误");
}

// 加密新密码
user.setPassword(PasswordUtil.encode(newPassword));
usersService.updateById(user);
```

**4. 批量导入用户**
```java
// TeachersController.importExcel()
teacher.setPassword(PasswordUtil.encode("123456"));  // 默认密码加密
teachersService.save(teacher);
```

#### 密码迁移工具

**工具类**: `PasswordMigrationUtil.java`

**功能**: 将数据库中的明文密码批量转换为 BCrypt 加密密码

**使用场景**: 系统升级时，将旧的明文密码迁移到加密存储

```java
// 执行迁移
Map<String, Object> result = PasswordMigrationUtil.migrateAllPasswords();

// 返回结果
{
    "users": 150,      // 迁移的学生数量
    "teachers": 20,    // 迁移的教师数量
    "managers": 5,     // 迁移的管理员数量
    "total": 175       // 总计
}
```

#### 密码存储格式

**数据库字段**:
- `users.password` (VARCHAR 100)
- `teachers.password` (VARCHAR 100)
- `managers.password` (VARCHAR 100)

**BCrypt 密码格式**:
```
$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl.GYa7Y7W
 │   │  │                                                    │
 │   │  └─ 22字符盐值                                        │
 │   └─ 成本因子（10 = 2^10 次迭代）                         │
 └─ 算法版本（$2a$ = BCrypt）                               │
                                                             └─ 31字符哈希值
```

**长度**: 60 字符（固定）

#### 安全性分析

**优势**:
- 自适应：可通过提高成本因子增强安全性
- 抗暴力破解：成本因子为10时，单次验证约需 0.1 秒
- 抗彩虹表：每个密码使用不同的盐值
- 抗时序攻击：验证时间恒定

**成本因子对比**:
| 成本因子 | 迭代次数 | 单次验证时间 |
|---------|---------|-------------|
| 10 | 1024 | ~0.1 秒 |
| 12 | 4096 | ~0.4 秒 |
| 14 | 16384 | ~1.6 秒 |

**当前配置**: 成本因子 = 10（默认值）

#### 密码强度要求

**建议规则**:
- 最小长度: 6 位
- 包含字母和数字
- 避免常见弱密码（如 123456、password）

**前端校验**（可选）:
```javascript
// 密码强度校验
function validatePassword(password) {
  if (password.length < 6) {
    return '密码至少6位';
  }
  if (!/[a-zA-Z]/.test(password) || !/[0-9]/.test(password)) {
    return '密码必须包含字母和数字';
  }
  return true;
}
```

#### 注意事项

1. **不要在日志中输出密码**（无论明文还是密文）
2. **不要通过 GET 请求传递密码**（使用 POST + HTTPS）
3. **定期提醒用户修改密码**（建议 3-6 个月）
4. **限制密码重试次数**（防止暴力破解）
5. **生产环境务必使用 HTTPS**（防止中间人攻击）

---

### 3. 切屏监控

**目的**: 防止考试作弊

**实现**:
- 监听 `visibilitychange` 事件
- 检测页面失焦（切换标签页、最小化窗口）
- 记录切屏次数
- 超过允许次数时强制交卷

**工具类**: `client/src/utils/examScreenSwitchSession.js`

---

### 4. 数据安全

**SQL 注入防护**:
- 使用 MyBatis-Plus 参数化查询
- 避免拼接 SQL 字符串

**XSS 防护**:
- 富文本内容使用 `v-html` 渲染前需过滤
- 建议使用 DOMPurify 库

**CSRF 防护**:
- 使用 JWT Token 代替 Session
- 跨域请求需携带 Token

**文件上传安全**:
- 限制文件类型（白名单）
- 限制文件大小（10MB）
- 文件名随机化（防止覆盖）

---

### 5. 权限控制

**前端权限**:
- 路由守卫: 根据 `role` 控制页面访问
- 菜单显示: 根据角色动态生成菜单

**后端权限**:
- 拦截器: 验证 Token 中的 `role` 字段
- 接口级权限: 根据角色判断是否有权限操作

---

## 部署说明

### 环境要求

| 组件 | 版本要求 |
|------|---------|
| Java | 17+ |
| Node.js | 16+ |
| MySQL | 8.0+ |
| Redis | 5.0+ |
| Maven | 3.6+ |

---

### 后端部署

#### 1. 数据库初始化

```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE exam_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入数据库脚本（假设有 exam_system.sql 文件）
mysql -u root -p exam_system < exam_system.sql
```

#### 2. 修改配置文件

编辑 `back-stage/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://your-db-host:3306/exam_system
    username: your-db-username
    password: your-db-password
  
  data:
    redis:
      host: your-redis-host
      port: 6379

jwt:
  secret: your-production-secret-key-change-this
```

#### 3. 打包运行

```bash
cd back-stage

# 使用 Maven 打包
mvn clean package -DskipTests

# 运行 JAR 包
java -jar target/back-stage-0.0.1-SNAPSHOT.jar

# 或使用 nohup 后台运行
nohup java -jar target/back-stage-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```

**访问地址**: http://your-server-ip:9999/springbootZLpxsG

---

### 前端部署

#### 1. 修改配置文件

**管理端** (`admin/src/config/config.js`):
```javascript
export default {
  baseUrl: 'http://your-server-ip:9999/springbootZLpxsG'
}
```

**学生端** (`client/src/config/config.js`):
```javascript
export default {
  baseUrl: 'http://your-server-ip:9999/springbootZLpxsG'
}
```

#### 2. 打包构建

```bash
# 管理端
cd admin
npm install
npm run build

# 学生端
cd client
npm install
npm run build
```

构建产物:
- 管理端: `admin/dist/`
- 学生端: `client/dist/`

#### 3. Nginx 配置

```nginx
# 管理端
server {
    listen 80;
    server_name admin.example.com;
    
    root /var/www/admin/dist;
    index index.html;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # 代理后端 API
    location /springbootZLpxsG {
        proxy_pass http://localhost:9999;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}

# 学生端
server {
    listen 80;
    server_name student.example.com;
    
    root /var/www/client/dist;
    index index.html;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    location /springbootZLpxsG {
        proxy_pass http://localhost:9999;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}

# 静态资源（文件上传）
server {
    listen 80;
    server_name files.example.com;
    
    location /upload {
        alias /path/to/back-stage/src/main/resources/static/upload;
        autoindex off;
    }
}
```

#### 4. 重启 Nginx

```bash
sudo nginx -t
sudo systemctl restart nginx
```

---

### Docker 部署（可选）

#### Dockerfile（后端）

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/back-stage-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9999
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### docker-compose.yml

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: 123456
      MYSQL_DATABASE: exam_system
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
  
  redis:
    image: redis:7
    ports:
      - "6379:6379"
  
  backend:
    build: ./back-stage
    ports:
      - "9999:9999"
    depends_on:
      - mysql
      - redis
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/exam_system
      SPRING_REDIS_HOST: redis

volumes:
  mysql-data:
```

---

## 开发指南

### 后端开发规范

#### 1. 代码结构

```
controller  → 接收请求，参数校验
   ↓
service     → 业务逻辑处理
   ↓
mapper      → 数据库操作
```

#### 2. 命名规范

- **Controller**: `XxxController.java`
- **Service**: `XxxService.java` + `XxxServiceImpl.java`
- **Mapper**: `XxxMapper.java` + `XxxMapper.xml`
- **Domain**: `Xxx.java`（实体类）

#### 3. 接口返回规范

```java
// 成功
return R.ok().put("data", result);

// 失败
return R.error("错误信息");
```

#### 4. 异常处理

```java
try {
    // 业务逻辑
} catch (Exception e) {
    log.error("操作失败", e);
    return R.error("操作失败：" + e.getMessage());
}
```

#### 5. 日志规范

```java
@Slf4j
public class XxxService {
    public void method() {
        log.info("开始执行...");
        log.error("发生错误", e);
    }
}
```

---

### 前端开发规范

#### 1. 组件命名

- **页面组件**: `kebab-case`（如 `exam-question/index.vue`）
- **公共组件**: `PascalCase`（如 `FileUpload/index.vue`）

#### 2. API 调用

```javascript
import request from '@/utils/request'

// GET 请求
request.get('/examQuestion/page', { params: { page: 1 } })

// POST 请求
request.post('/examQuestion/save', data)
```

#### 3. 状态管理

```javascript
// stores/user.js
import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    userInfo: {}
  }),
  actions: {
    setToken(token) {
      this.token = token
    }
  },
  persist: true  // 持久化到 localStorage
})
```

#### 4. 路由守卫

```javascript
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.meta.requiresAuth && !userStore.token) {
    next('/login')
  } else {
    next()
  }
})
```

---

### 新增功能开发流程

#### 后端

1. **创建实体类** (`domain/Xxx.java`)
2. **创建 Mapper** (`mapper/XxxMapper.java` + `mapper/XxxMapper.xml`)
3. **创建 Service** (`service/XxxService.java` + `service/impl/XxxServiceImpl.java`)
4. **创建 Controller** (`controller/XxxController.java`)
5. **测试接口**（Swagger 或 Postman）

#### 前端

1. **创建页面组件** (`views/xxx/index.vue`)
2. **添加路由** (`router/router.js`)
3. **添加菜单** (`config/config.js`)
4. **调用 API**（`utils/request.js`）
5. **测试功能**

---

## 常见问题

### 1. 后端启动失败

**问题**: `Cannot load driver class: com.mysql.cj.jdbc.Driver`

**解决**:
- 检查 MySQL 驱动依赖是否正确
- 检查 `application.yml` 中的数据库配置

---

### 2. 前端跨域问题

**问题**: `Access-Control-Allow-Origin` 错误

**解决**:
- 检查后端 `CorsConfig.java` 是否配置正确
- 检查 `vite.config.js` 代理配置

---

### 3. Token 验证失败

**问题**: 接口返回 `401 Unauthorized`

**解决**:
- 检查 Token 是否过期
- 检查 Redis 是否正常运行
- 检查请求头是否携带 `Authorization`

---

### 4. 文件上传失败

**问题**: 上传文件后无法访问

**解决**:
- 检查 `file.upload-path` 配置
- 检查文件夹权限
- 生产环境建议使用 Nginx 代理静态资源

---

### 5. 考试状态不更新

**问题**: 考试时间已到，状态仍为"未开始"

**解决**:
- 考试状态由时间动态计算，不存储在数据库
- 检查前端是否正确调用状态计算逻辑
- 检查服务器时间是否正确

---

### 6. 切屏监控不生效

**问题**: 切换标签页后未触发警告

**解决**:
- 检查 `examScreenSwitchSession.js` 是否正确引入
- 检查路由守卫是否启用监控
- 某些浏览器可能不支持 `visibilitychange` 事件

---

### 7. 公式渲染失败

**问题**: 数学公式显示为源代码

**解决**:
- 检查 KaTeX 依赖是否安装（`npm install katex`）
- 检查公式语法是否正确（LaTeX 格式）
- 检查是否引入 KaTeX CSS 文件

---

## 附录

### 相关文档

- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Vue 3 官方文档](https://vuejs.org/)
- [Element Plus 文档](https://element-plus.org/)
- [MyBatis-Plus 文档](https://baomidou.com/)
- [KaTeX 文档](https://katex.org/)
- [ECharts 文档](https://echarts.apache.org/)

---

### 开发工具推荐

| 工具 | 用途 |
|------|------|
| IntelliJ IDEA | Java 后端开发 |
| VS Code | 前端开发 |
| Navicat / DBeaver | 数据库管理 |
| Redis Desktop Manager | Redis 可视化 |
| Postman | API 测试 |
| Git | 版本控制 |

---

### 技术支持

如有问题，请联系开发团队或提交 Issue。

---

**文档版本**: v1.0
**最后更新**: 2026-05-10
**维护者**: 开发团队

