package com.web.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.web.annotation.IgnoreAuth;
import com.web.domain.*;
import com.web.domain.Req.ExamPaperTopicReq;
import com.web.domain.Resp.ExamPaperTopicResp;
import com.web.service.*;
import com.web.utils.CommonUtil;
import com.web.utils.ExamStatusConstants;
import com.web.utils.PageUtils;
import com.web.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 试卷选题 前端控制器
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/examPaperTopic")
public class ExamPaperTopicController {

    private static final Logger logger = LoggerFactory.getLogger(ExamPaperTopicController.class);

    private static final String TABLE_NAME = "examPaperTopic";

    @Resource
    private ExamPaperTopicService examPaperTopicService;

    @Resource
    private ExamQuestionService examQuestionService;

    @Resource
    private ExamPaperService examPaperService;

    @Resource
    private DictionaryService dictionaryService;

    @Resource
    private ExamRecordService examRecordService;

    @Resource
    private ExamDetailsService examDetailsService;
    @Resource
    private ExamWrongQuestionService examWrongQuestionService;
    @Resource
    private ExamInfoService examInfoService;
    @Resource
    private RedisCacheService redisCacheService;

    /**
     * 后台分页查询
     * 
     * @return
     */
    @PostMapping("/page")
    public Result page(@RequestBody ExamPaperTopicReq req) {
        PageUtils page = examPaperTopicService.selectPage(req);
        Integer totalScore = examPaperTopicService.selectscore(req.getExamPaperId());
        // 获取已经添加得所有试题id
        List<ExamPaperTopic> allTopicList = examPaperTopicService.lambdaQuery()
                .eq(ExamPaperTopic::getExamPaperId, req.getExamPaperId()).list();

        List<Integer> questionId = new ArrayList<>();
        if (CollUtil.isNotEmpty(allTopicList)) {
            questionId = allTopicList.stream().map(ExamPaperTopic::getExamQuestionId).collect(Collectors.toList());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getList());
        result.put("total", page.getTotal());
        result.put("page", page.getPage());
        result.put("limit", page.getLimit());
        result.put("totalScore", totalScore);
        result.put("allQuestionId", questionId);

        return Result.success(result);
    }

    /**
     * 新增
     * 
     * @param examPaperTopic
     * @return
     */
    @PostMapping("/save")
    public Result save(@RequestBody ExamPaperTopic examPaperTopic) {
        try {
            // 获取试卷id
            Integer examPaperId = examPaperTopic.getExamPaperId();
            
            // 查询当前试卷的所有题目，找到最大顺序号
            List<ExamPaperTopic> existingTopics = examPaperTopicService.lambdaQuery()
                    .eq(ExamPaperTopic::getExamPaperId, examPaperId)
                    .list();
            
            Integer maxSequence = 0;
            if (CollUtil.isNotEmpty(existingTopics)) {
                maxSequence = existingTopics.stream()
                        .mapToInt(ExamPaperTopic::getExamPaperTopicSequence)
                        .max()
                        .orElse(0);
            }
            
            // 设置顺序号为最大值 + 1
            examPaperTopic.setExamPaperTopicSequence(maxSequence + 1);
            
            examPaperTopicService.save(examPaperTopic);

            // 更新分数
            examPaperTopicService.updateScore(examPaperId);
            return Result.success(examPaperTopic);

        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 捕获唯一约束异常
            return Result.error("该题目已存在于试卷中，请勿重复添加");
        } catch (Exception e) {
            // 其他异常
            return Result.error("添加失败：" + e.getMessage());
        }

    }

    /**
     * 批量删除
     * 
     * @param jsonBody
     * @return
     */
    @PostMapping("/saveBatch")
    public Result saveBatch(@RequestBody String jsonBody) {
        JSONObject entries = JSONUtil.parseObj(jsonBody);
        List<Integer> examQuestionIds = entries.getJSONArray("examQuestionIds").toList(Integer.class);
        Integer examPaperId = entries.getInt("examPaperId");

        if (CollUtil.isNotEmpty(examQuestionIds)) {
            // 校验
            List<ExamPaperTopic> list = examPaperTopicService.lambdaQuery()
                    .eq(ExamPaperTopic::getExamPaperId, examPaperId).list();
            if (CollUtil.isNotEmpty(list)) {
                List<Integer> ids = list.stream().map(ExamPaperTopic::getExamQuestionId).collect(Collectors.toList());
                examQuestionIds.removeAll(ids);
            }

            // 如果过滤后没有需要添加的题目，直接返回
            if (CollUtil.isEmpty(examQuestionIds)) {
                // 更新分数
                examPaperTopicService.updateScore(examPaperId);
                return Result.success();
            }

            // 查询试题信息，按题型排序（单选1->多选2->判断3->填空4->简答5）
            // 使用查询条件代替 listByIds，避免可能的类型转换问题
            List<ExamQuestion> questions = examQuestionService.lambdaQuery()
                    .in(ExamQuestion::getId, examQuestionIds)
                    .list();
            
            if (CollUtil.isEmpty(questions)) {
                return Result.error("未找到对应的试题信息");
            }
            
            questions.sort(Comparator.comparingInt(ExamQuestion::getExamQuestionTypes));

            // 获取当前试卷的最大顺序号
            Integer maxSequence = 0;
            if (CollUtil.isNotEmpty(list)) {
                maxSequence = list.stream()
                        .mapToInt(ExamPaperTopic::getExamPaperTopicSequence)
                        .max()
                        .orElse(0);
            }

            List<ExamPaperTopic> addBatch = new ArrayList<>();
            for (ExamQuestion question : questions) {
                maxSequence++;
                ExamPaperTopic examPaperTopic = new ExamPaperTopic();
                examPaperTopic.setExamPaperId(examPaperId);
                examPaperTopic.setExamQuestionId(question.getId());
                examPaperTopic.setExamPaperTopicSequence(maxSequence);
                addBatch.add(examPaperTopic);
            }
            examPaperTopicService.saveBatch(addBatch);
        }

        // 更新分数
        examPaperTopicService.updateScore(examPaperId);
        return Result.success();
    }

    /**
     * 修改
     * 
     * @param examPaperTopic
     * @return
     */
    @PostMapping("/update")
    public Result update(@RequestBody ExamPaperTopic examPaperTopic) {
        examPaperTopicService.updateById(examPaperTopic);
        // 获取试卷id
        Integer examPaperId = examPaperTopicService.getById(examPaperTopic.getId()).getExamPaperId();

        // 更新分数
        examPaperTopicService.updateScore(examPaperId);
        return Result.success();
    }

    /**
     * 根据id查询
     * 
     * @param id
     * @return
     */
    @GetMapping("/info/{id}")
    public Result getById(@PathVariable Integer id) {
        return Result.success(examPaperTopicService.info(id));
    }

    /**
     * 根据id删除
     * 
     * @param id 试题试卷关联表
     * @return
     */
    @DeleteMapping("/del/{id}")
    public Result delete(@PathVariable Integer id) {

        // 获取试卷id
        Integer examPaperId = examPaperTopicService.getById(id).getExamPaperId();

        // 2. 删除关联表数据
        examPaperTopicService.removeById(id);

        // 更新分数
        examPaperTopicService.updateScore(examPaperId);
        return Result.success();

    }

    /**
     * 批量删除
     * 
     * @param jsonBody
     * @return
     */
    @RequestMapping("/del/batch")
    public Result deleteBatch(@RequestBody String jsonBody) {
        JSONObject entries = JSONUtil.parseObj(jsonBody);
        List<Integer> ids = entries.getJSONArray("ids").toList(Integer.class);
        Integer examPaperId = entries.getInt("examPaperId");

        examPaperTopicService.removeBatchByIds(ids);
        // 更新分数
        examPaperTopicService.updateScore(examPaperId);
        return Result.success();
    }

    /**
     * 分页查询
     * 
     * @param req
     * @return
     */
    @IgnoreAuth
    @PostMapping("/list")
    public Result list(@RequestBody ExamPaperTopicReq req) {
        PageUtils page = examPaperTopicService.selectPage(req);
        return Result.success(page);
    }

    /**
     * 加载所有
     * 
     * @return
     */
    @GetMapping("/loadAll")
    public Result loadAll() {
        return Result.success(examPaperTopicService.list());
    }

    /**
     * 调整题目顺序 - 单个调整
     * 
     * @param jsonBody 包含 id 和 examPaperTopicSequence
     * @return
     */
    @PostMapping("/updateSequence")
    public Result updateSequence(@RequestBody String jsonBody) {
        JSONObject entries = JSONUtil.parseObj(jsonBody);
        Integer id = entries.getInt("id");
        Integer examPaperTopicSequence = entries.getInt("examPaperTopicSequence");

        if (id == null || examPaperTopicSequence == null) {
            return Result.error("参数错误");
        }

        ExamPaperTopic examPaperTopic = examPaperTopicService.getById(id);
        if (examPaperTopic == null) {
            return Result.error("题目不存在");
        }

        examPaperTopic.setExamPaperTopicSequence(examPaperTopicSequence);
        examPaperTopicService.updateById(examPaperTopic);

        return Result.success();
    }

    /**
     * 批量调整题目顺序
     * 
     * @param jsonBody 包含 examPaperId 和 topicSequences (list of {id, sequence})
     * @return
     */
    @PostMapping("/batchUpdateSequence")
    public Result batchUpdateSequence(@RequestBody String jsonBody) {
        JSONObject entries = JSONUtil.parseObj(jsonBody);
        Integer examPaperId = entries.getInt("examPaperId");

        if (examPaperId == null) {
            return Result.error("参数错误");
        }

        try {
            // 获取 topicSequences 数组
            cn.hutool.json.JSONArray jsonArray = entries.getJSONArray("topicSequences");
            if (jsonArray == null || jsonArray.isEmpty()) {
                return Result.error("参数错误");
            }

            // 关键修复：根据前端传递的顺序列表，重新计算连续的 sequence 值
            // 前端传递的顺序就是用户拖拽后的视觉顺序（从上到下）
            // 我们需要将其转换为连续的递增序列号（1, 2, 3...）
            List<ExamPaperTopic> updateList = new ArrayList<>();
            
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                if (item != null) {
                    Integer id = item.getInt("id");
                    
                    if (id != null) {
                        ExamPaperTopic examPaperTopic = examPaperTopicService.getById(id);
                        if (examPaperTopic != null && examPaperTopic.getExamPaperId().equals(examPaperId)) {
                            // 根据在列表中的位置重新计算 sequence（从1开始递增）
                            // i=0 表示第一题，sequence=1；i=1 表示第二题，sequence=2；以此类推
                            examPaperTopic.setExamPaperTopicSequence(i + 1);
                            updateList.add(examPaperTopic);
                        }
                    }
                }
            }
            
            // 批量更新，确保原子性
            if (!updateList.isEmpty()) {
                examPaperTopicService.updateBatchById(updateList);
                log.info("[批量更新顺序] 成功更新 {} 道题目的顺序 - 试卷ID: {}", updateList.size(), examPaperId);
            }
            
            return Result.success();
        } catch (Exception e) {
            logger.error("批量更新题目顺序失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @RequestMapping("/autoPaper")
    public Result autoPaper(@RequestParam Map<String, Object> req, HttpServletRequest request) {
        // 试卷科目
        Integer kemuTypes = Integer.valueOf(String.valueOf(req.get("kemuTypes")));
        // 试卷数据信息
        ExamPaper examPaper = new ExamPaper();
        examPaper.setId(Integer.valueOf(String.valueOf(req.get("examPaperId"))));
        // ============判断该试卷是否已组卷 ============
        Integer examPaperId = Integer.valueOf(String.valueOf(req.get("examPaperId")));
        long topicCount = examPaperTopicService.lambdaQuery()
                .eq(ExamPaperTopic::getExamPaperId, examPaperId)
                .count();
        if (topicCount > 0) {
            return Result.error("该试卷已组卷，不可重复组卷！请先清空试卷");
        }
        examPaper.setExamPaperMyscore(0);

        // 单选数量和分数
        Integer danNum = Integer.valueOf(String.valueOf(req.get("danNum")));
        Integer danFen = Integer.valueOf(String.valueOf(req.get("danFen")));
        ArrayList<ExamQuestion> danList = new ArrayList<>();
        // 多选数量和分数

        Integer duoNum = Integer.valueOf(String.valueOf(req.get("duoNum")));
        Integer duoFen = Integer.valueOf(String.valueOf(req.get("duoFen")));
        ArrayList<ExamQuestion> duoList = new ArrayList<>();
        // 判断数量和分数
        Integer panNum = Integer.valueOf(String.valueOf(req.get("panNum")));
        Integer panFen = Integer.valueOf(String.valueOf(req.get("panFen")));
        ArrayList<ExamQuestion> panList = new ArrayList<>();
        // 填空数量和分数
        Integer tianNum = Integer.valueOf(String.valueOf(req.get("tianNum")));
        Integer tianFen = Integer.valueOf(String.valueOf(req.get("tianFen")));
        ArrayList<ExamQuestion> tianList = new ArrayList<>();
        // 简答题数量和分数
        Integer jianNum = Integer.valueOf(String.valueOf(req.get("jianNum")));
        Integer jianFen = Integer.valueOf(String.valueOf(req.get("jianFen")));
        ArrayList<ExamQuestion> jianList = new ArrayList<>();
        // 存储已经筛选的数据
        ArrayList<ExamPaperTopic> examPaperTopics = new ArrayList<>();
        // 随机数
        Random random = new Random();
        // 题目顺序计数器
        int sequence = 0;

        // 查询全部试题信息
        QueryWrapper<ExamQuestion> queryWrapper = new QueryWrapper<ExamQuestion>().eq("kemu_types", kemuTypes);
        logger.info("sql语句:" + queryWrapper.getSqlSegment());
        List<ExamQuestion> examPaperTopicLists = examQuestionService.list(queryWrapper);
        for (ExamQuestion examQuestion : examPaperTopicLists) {
            // 将对应类型数据放入对应的list集合中
            if (examQuestion.getExamQuestionTypes() == 1) {// 单选题
                danList.add(examQuestion);
            } else if (examQuestion.getExamQuestionTypes() == 2) {// 多选题
                duoList.add(examQuestion);
            } else if (examQuestion.getExamQuestionTypes() == 3) {// 判断题
                panList.add(examQuestion);
            } else if (examQuestion.getExamQuestionTypes() == 4) {// 填空题
                tianList.add(examQuestion);
            } else if (examQuestion.getExamQuestionTypes() == 5) {// 简答题
                jianList.add(examQuestion);
            }
        }

        // 判断前台传来的题数值是否为空
        if (danNum != null && danNum > 0 && danFen != null) {// 单选
            if (danList.size() < danNum)
                return Result.error("单选题数量" + danNum + "超过数据库中存在的最大数量" + danList.size());
            for (int i = 0; i < danNum; i++) {
                ExamPaperTopic examPaperTopic = new ExamPaperTopic();
                int intRandom = random.nextInt(danList.size());
                sequence++;
                examPaper.setExamPaperMyscore(examPaper.getExamPaperMyscore() + danFen);
                examPaperTopic.setExamQuestionId(danList.get(intRandom).getId());
                examPaperTopic.setExamPaperTopicNumber(danFen);
                examPaperTopic.setExamPaperId(examPaper.getId());
                examPaperTopic.setExamPaperTopicSequence(sequence);
                examPaperTopics.add(examPaperTopic);
                danList.remove(intRandom);
            }
        }
        if (duoNum != null && duoNum > 0 && duoFen != null) {// 多选
            if (duoList.size() < duoNum)
                return Result.error("多选题数量" + duoNum + "超过数据库中存在的最大数量" + duoList.size());
            for (int i = 0; i < duoNum; i++) {
                ExamPaperTopic examPaperTopic = new ExamPaperTopic();
                int intRandom = random.nextInt(duoList.size());
                sequence++;
                examPaper.setExamPaperMyscore(examPaper.getExamPaperMyscore() + duoFen);
                examPaperTopic.setExamQuestionId(duoList.get(intRandom).getId());
                examPaperTopic.setExamPaperTopicNumber(duoFen);
                examPaperTopic.setExamPaperId(examPaper.getId());
                examPaperTopic.setExamPaperTopicSequence(sequence);
                examPaperTopics.add(examPaperTopic);
                duoList.remove(intRandom);
            }
        }
        if (panNum != null && panNum > 0 && panFen != null) {// 判断
            if (panList.size() < panNum)
                return Result.error("判断题数量" + panNum + "超过数据库中存在的最大数量" + panList.size());
            for (int i = 0; i < panNum; i++) {
                ExamPaperTopic examPaperTopic = new ExamPaperTopic();
                int intRandom = random.nextInt(panList.size());
                sequence++;
                examPaper.setExamPaperMyscore(examPaper.getExamPaperMyscore() + panFen);
                examPaperTopic.setExamQuestionId(panList.get(intRandom).getId());
                examPaperTopic.setExamPaperTopicNumber(panFen);
                examPaperTopic.setExamPaperId(examPaper.getId());
                examPaperTopic.setExamPaperTopicSequence(sequence);
                examPaperTopics.add(examPaperTopic);
                panList.remove(intRandom);
            }
        }
        if (tianNum != null && tianNum > 0 && tianFen != null) {// 填空
            if (tianList.size() < tianNum)
                return Result.error("填空题数量" + tianNum + "超过数据库中存在的最大数量" + tianList.size());
            for (int i = 0; i < tianNum; i++) {
                ExamPaperTopic examPaperTopic = new ExamPaperTopic();
                int intRandom = random.nextInt(tianList.size());
                sequence++;
                examPaper.setExamPaperMyscore(examPaper.getExamPaperMyscore() + tianFen);
                examPaperTopic.setExamQuestionId(tianList.get(intRandom).getId());
                examPaperTopic.setExamPaperTopicNumber(tianFen);
                examPaperTopic.setExamPaperId(examPaper.getId());
                examPaperTopic.setExamPaperTopicSequence(sequence);
                examPaperTopics.add(examPaperTopic);
                tianList.remove(intRandom);
            }
        }
        if (jianNum != null && jianNum > 0 && jianFen != null) {// 简答
            if (jianList.size() < jianNum)
                return Result.error("简答题数量" + jianNum + "超过数据库中存在的最大数量" + jianList.size());
            for (int i = 0; i < jianNum; i++) {
                ExamPaperTopic examPaperTopic = new ExamPaperTopic();
                int intRandom = random.nextInt(jianList.size());
                sequence++;
                examPaper.setExamPaperMyscore(examPaper.getExamPaperMyscore() + jianFen);
                examPaperTopic.setExamQuestionId(jianList.get(intRandom).getId());
                examPaperTopic.setExamPaperTopicNumber(jianFen);
                examPaperTopic.setExamPaperId(examPaper.getId());
                examPaperTopic.setExamPaperTopicSequence(sequence);
                examPaperTopics.add(examPaperTopic);
                jianList.remove(intRandom);
            }
        }

        examPaperService.updateById(examPaper);
        examPaperTopicService.saveBatch(examPaperTopics);

        // 更新分数
        examPaperTopicService.updateScore(examPaperId);
        return Result.success();

    }

    /**
     * 获取试卷试题
     * 
     * @param examPaperId
     * @param request
     * @return
     */
    @GetMapping("/getExamQuestion")
    public Result getExamQuestion(Integer examPaperId, Integer examInfoId, HttpServletRequest request) {
        // 验证试卷状态：只有启用状态（examPaperTypes = 1）的试卷才能开始考试
        if (examPaperId != null) {
            ExamPaper examPaper = examPaperService.getById(examPaperId);
            if (examPaper == null) {
                return Result.error("试卷不存在");
            }
            if (examPaper.getExamPaperTypes() == null || examPaper.getExamPaperTypes() != 1) {
                return Result.error("该试卷未启用，无法进行考试");
            }
        }

        // 获取当前试卷的选题信息
        ExamPaperTopicReq req = new ExamPaperTopicReq();
        req.setLimit(9999);
        req.setExamPaperId(examPaperId);
        PageUtils page = examPaperTopicService.selectPage(req);
        List<ExamPaperTopicResp> examPaperTopicResp = (List<ExamPaperTopicResp>) page.getList();

        // 试题id存放list
        List<Integer> examPaperTopicId = new ArrayList<>();
        // 循环选题信息获取试题id并存放起来
        for (ExamPaperTopic papertopic : examPaperTopicResp) {
            examPaperTopicId.add(papertopic.getExamQuestionId());
        }
        // 查询试题数据
        List<ExamQuestion> examQuestion = examQuestionService.listByIds(examPaperTopicId);
        // 创建试题ID到试题对象的映射
        Map<Integer, ExamQuestion> examQuestionMap = new HashMap<>();
        for (ExamQuestion item : examQuestion) {
            examQuestionMap.put(item.getId(), item);
        }
        // 试题数据存放到选题view中
        for (ExamPaperTopicResp resp : examPaperTopicResp) {
            try {
                ExamQuestion question = examQuestionMap.get(resp.getExamQuestionId());
                if (question != null) {
                    BeanUtils.copyProperties(question, resp);
                }
                // 填充字典名称（试题类型、科目）
                if (dictionaryService != null && resp != null) {
                    try {
                        // 填充试题类型名称
                        if (resp.getExamQuestionTypes() != null) {
                            String typeName = dictionaryService.getDictionaryName("exam_question_types",
                                    resp.getExamQuestionTypes());
                            if (typeName != null) {
                                resp.setExamQuestionTypesName(typeName);
                            }
                        }
                        // 填充科目类型名称
                        if (resp.getKemuTypes() != null) {
                            String kemuName = dictionaryService.getDictionaryName("kemu_types", resp.getKemuTypes());
                            if (kemuName != null) {
                                resp.setKemuTypesName(kemuName);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("填充字典名称失败: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                // 选项乱序逻辑：如果是客观题且开启了乱序，则进行打乱
                if (examInfoId != null) {
                    ExamInfo examInfo = examInfoService.getById(examInfoId);
                    if (examInfo != null && examInfo.getOptionShuffle() != null && examInfo.getOptionShuffle() == 1) {
                        if (resp.getExamQuestionTypes() != null && 
                            (resp.getExamQuestionTypes() == 1 || resp.getExamQuestionTypes() == 2 || resp.getExamQuestionTypes() == 3)) {
                            shuffleOptions(resp);
                        }
                    }
                }
            } catch (Exception e) {
                // 处理失败不影响主流程，只记录错误
                System.err.println("处理试题数据失败: " + e.getMessage());
            }
        }

        return Result.success(examPaperTopicResp);

    }

    /**
     * 对题目选项进行随机打乱
     */
    private void shuffleOptions(ExamPaperTopicResp resp) {
        if (StrUtil.isBlank(resp.getExamQuestionOptions())) return;

        try {
            JSONArray optionsArray = JSONUtil.parseArray(resp.getExamQuestionOptions());
            List<Object> optionList = optionsArray.toList(Object.class);
            
            // 随机打乱
            Collections.shuffle(optionList);
            
            // 重新组装 JSON
            resp.setExamQuestionOptions(JSONUtil.toJsonStr(optionList));
        } catch (Exception e) {
            log.warn("选项乱序处理失败: {}", e.getMessage());
        }
    }

    /**
     * 提交试卷
     * 
     * @param req
     * @param request
     * @return
     */
    @RequestMapping("/submit")
    public Result submit(@RequestBody Map<String, Object> req, HttpServletRequest request) {
        log.info("\n========== [提交试卷] 开始 ==========");
        
        // 答题用户
        Integer usersId = (Integer) request.getSession().getAttribute("userId");
        if (usersId == null) {
            log.error("[提交试卷] ❌ 用户未登录");
            return Result.error("请先登录");
        }
        
        // 试卷id
        Integer examPaperId = Integer.valueOf(String.valueOf(req.get("examPaperId")));
        // 考试id（新增）
        Integer examInfoId = req.get("examInfoId") != null ? Integer.valueOf(String.valueOf(req.get("examInfoId"))) : null;
        boolean forceSubmit = req.get("forceSubmit") != null && Boolean.parseBoolean(String.valueOf(req.get("forceSubmit")));

        log.info("[提交试卷] 用户ID: {}, 试卷ID: {}, 考试ID: {}", usersId, examPaperId, examInfoId);

        // 验证试卷状态：只有启用状态（examPaperTypes = 1）的试卷才能提交答案
        if (examPaperId != null) {
            ExamPaper examPaper = examPaperService.getById(examPaperId);
            if (examPaper == null) {
                log.error("[提交试卷] ❌ 试卷不存在");
                return Result.error("试卷不存在");
            }
            if (examPaper.getExamPaperTypes() == null || examPaper.getExamPaperTypes() != 1) {
                log.error("[提交试卷] ❌ 试卷未启用");
                return Result.error("该试卷未启用，无法提交答案");
            }
        }

        // 答题信息
        List<Map<String, Object>> answerList = (List<Map<String, Object>>) JSONUtil
                .parse(String.valueOf(req.get("answerList")));
        // 答题map
        HashMap<String, String> answerMap = new HashMap<>();
        for (Map<String, Object> map : answerList) {
            String qId = String.valueOf(map.get("examQuestionId"));
            String ans = String.valueOf(map.get("answer"));
            answerMap.put(qId, ans);
        }
        
        // 试题信息
        HashMap<String, Object> map = new HashMap<>();
        map.put("limit", "9999");
        map.put("examPaperId", examPaperId);
        CommonUtil.checkMap(map);
        PageUtils page = examPaperTopicService.queryPage(map);
        List<ExamPaperTopicResp> examPaperTopicList = (List<ExamPaperTopicResp>) page.getList();

        if (examPaperTopicList == null || examPaperTopicList.size() == 0) {
            log.error("[提交试卷] ❌ 试卷中没有题目");
            return Result.error("试卷中没有题目");
        }

        List<ExamDetails> examDetailsList = new ArrayList<>();
        List<ExamDetails> updateExamDetailsList = new ArrayList<>();
        List<ExamWrongQuestion> examWrongQuestionList = new ArrayList<>();

        String examRecordUuid = req.get("examRecordUuid") != null ? String.valueOf(req.get("examRecordUuid")) : null;
        ExamRecord examRecord = getOrCreateActiveExamRecord(examPaperId, examInfoId, usersId, examRecordUuid);
        if (examRecord.getStatus() != null && examRecord.getStatus() == ExamStatusConstants.RECORD_SUBMITTED) {
            return Result.error("试卷已提交，请勿重复交卷");
        }
        String uuid = examRecord.getExamRecordUuidNumber();

        Map<Integer, ExamDetails> existingDetailMap = examDetailsService.lambdaQuery()
                .eq(ExamDetails::getExamRecordId, examRecord.getId())
                .list()
                .stream()
                .collect(Collectors.toMap(ExamDetails::getExamQuestionId, detail -> detail, (a, b) -> a));
        
        log.info("[提交试卷] 开始处理 {} 道题目的判分", examPaperTopicList.size());

        // 初始化客观题自动判分总和
        int autoScore = 0;

        // 判断用户的答案
        for (ExamPaperTopicResp examPaperTopic : examPaperTopicList) {
            boolean cuoti = false;
            ExamDetails examDetails = existingDetailMap.get(examPaperTopic.getExamQuestionId());
            boolean isNewDetail = examDetails == null;
            if (isNewDetail) {
                examDetails = new ExamDetails();
                examDetails.setExamDetailsUuidNumber(uuid);
                examDetails.setExamRecordId(examRecord.getId());
                examDetails.setUsersId(usersId);
                examDetails.setExamQuestionId(examPaperTopic.getExamQuestionId());
                examDetails.setCreateTime(new Date());
            }
            examDetails.setExamPaperTopicNumber(examPaperTopic.getExamPaperTopicNumber());
            examDetails.setExamPaperTopicSequence(examPaperTopic.getExamPaperTopicSequence());

            ExamWrongQuestion examWrongQuestion = new ExamWrongQuestion();
            examWrongQuestion.setUsersId(String.valueOf(usersId));
            examWrongQuestion.setInsertTime(new Date());
            examWrongQuestion.setCreateTime(new Date());
            examWrongQuestion.setExamPaperId(String.valueOf(examPaperId));
            examWrongQuestion.setExamQuestionId(String.valueOf(examPaperTopic.getExamQuestionId()));
            examWrongQuestion.setExamRecordId(examRecord.getId());

            String qId = String.valueOf(examPaperTopic.getExamQuestionId());
            String myAnswer = answerMap.containsKey(qId)
                    ? normalizeAnswer(answerMap.get(qId))
                    : normalizeAnswer(examDetails.getExamDetailsMyanswer());
            // 简答题（类型5）不参与自动判分，直接设置基础信息；错题本在教师批阅后由 ExamDetailsController.syncWrongQuestionBySubjectiveResult 写入
            if (examPaperTopic.getExamQuestionTypes() == 5) {
                examDetails.setExamDetailsMyanswer(myAnswer);
                examWrongQuestion.setExamDetailsMyanswer(myAnswer);
                // 简答题分数为0，待人工批改
                examDetails.setExamDetailsMyscore(0);
                examDetails.setTeacherScore(0); // 教师评分初始为0
                examDetails.setReviewStatus(0); // 标记为待批阅
                // 注意：不能 continue，必须继续执行后续的 add 操作
            } else
            // 判断是否答题
            if (!"未作答".equals(myAnswer)) {
                examDetails.setExamDetailsMyanswer(myAnswer);
                examWrongQuestion.setExamDetailsMyanswer(myAnswer);
                examDetails.setTeacherScore(0);

                // 多选题判断对错
                if (examPaperTopic.getExamQuestionTypes() == 2) {
                    // 正确答案
                    List<String> split = Arrays.asList(examPaperTopic.getExamQuestionAnswer().split(","));
                    ArrayList<String> splitLs = new ArrayList<>();
                    splitLs.addAll(split);
                    // 用户答案
                    List<String> answer = Arrays.asList(myAnswer.split(","));
                    List<String> answerLs = new ArrayList<>();
                    answerLs.addAll(answer);
                    // 判断用户是否回答正确
                    if (splitLs.size() == answerLs.size()) {// 判断用户回答信息的长度是否正确答案的长度一样
                        splitLs.retainAll(answerLs);
                        if (splitLs.size() == answerLs.size()) {// 判断内容是否正确
                            autoScore += examPaperTopic.getExamPaperTopicNumber();
                            examDetails.setExamDetailsMyscore(examPaperTopic.getExamPaperTopicNumber());
                        } else {// 错误
                            cuoti = true;
                            examDetails.setExamDetailsMyscore(0);
                        }
                    } else {// 错误
                        cuoti = true;
                        examDetails.setExamDetailsMyscore(0);
                    }
                } else {
                    // 其他题判断对错
                    if (myAnswer.equals(examPaperTopic.getExamQuestionAnswer())) {// 正确
                        examDetails.setExamDetailsMyscore(examPaperTopic.getExamPaperTopicNumber());
                        autoScore += examPaperTopic.getExamPaperTopicNumber();
                    } else {// 错误
                        cuoti = true;
                        examDetails.setExamDetailsMyscore(0);
                    }
                }
                // 客观题自动批阅完成，设置批阅状态为已批阅
                examDetails.setReviewStatus(1);
            } else {// 未作答
                cuoti = true;
                examWrongQuestion.setExamDetailsMyanswer("未作答");
                examDetails.setExamDetailsMyanswer("未作答");
                examDetails.setExamDetailsMyscore(0);
                // 未作答的客观题也标记为已批阅
                examDetails.setReviewStatus(1);
            }
            if (cuoti) {
                examWrongQuestionList.add(examWrongQuestion);
                cuoti = false;
            }
            if (isNewDetail) {
                examDetailsList.add(examDetails);
            } else {
                updateExamDetailsList.add(examDetails);
            }
        }

        log.info("[提交试卷] 判分完成 - 客观题得分: {}", autoScore);

        // 设置考试记录的精细化分数字段
        examRecord.setAutoScore(autoScore); // 客观题自动判分
        examRecord.setTeacherScore(0); // 主观题初始为0
        examRecord.setTotalScore(autoScore); // 总分 = 客观题 + 主观题
        examRecord.setEndTime(new Date()); // 记录交卷时间
        
        // 检查是否有主观题，决定 pass_status
        boolean hasSubjectiveQuestions = examPaperTopicList.stream()
                .anyMatch(topic -> topic.getExamQuestionTypes() == 5);
        
        if (!hasSubjectiveQuestions) {
            // 纯客观题考试，立即判定及格状态
            Integer passingScore = null;
            if (examInfoId != null) {
                ExamInfo examInfo = examInfoService.getById(examInfoId);
                if (examInfo != null) {
                    passingScore = examInfo.getPassingScore();
                }
            }
            
            if (passingScore != null && passingScore > 0) {
                if (autoScore >= passingScore) {
                    examRecord.setPassStatus(1); // 及格
                    log.info("[提交试卷] 纯客观题考试 - 及格状态: 及格 ({} >= {})", autoScore, passingScore);
                } else {
                    examRecord.setPassStatus(2); // 不及格
                    log.info("[提交试卷] 纯客观题考试 - 及格状态: 不及格 ({} < {})", autoScore, passingScore);
                }
            } else {
                examRecord.setPassStatus(0); // 待判定（未设置及格分）
                log.info("[提交试卷] 纯客观题考试 - 及格状态: 待判定（未设置及格分）");
            }
        } else {
            // 含主观题考试，等待教师批阅后判定
            examRecord.setPassStatus(0); // 待判定
            log.info("[提交试卷] 含主观题考试 - 及格状态: 待判定（等待教师批阅）");
        }

        if (!examDetailsList.isEmpty()) {
            examDetailsService.saveBatch(examDetailsList);
        }
        if (!updateExamDetailsList.isEmpty()) {
            examDetailsService.updateBatchById(updateExamDetailsList);
        }
        examWrongQuestionService.remove(new QueryWrapper<ExamWrongQuestion>()
                .eq("exam_record_id", examRecord.getId()));
        if (!examWrongQuestionList.isEmpty()) {
            examWrongQuestionService.saveBatch(examWrongQuestionList);
        }
        
        log.info("[提交试卷] 已新增 {} 条、更新 {} 条答题详情，{} 条错题记录", examDetailsList.size(), updateExamDetailsList.size(), examWrongQuestionList.size());
        
        // 更新考试记录状态：1-正常交卷，2-强制交卷
        examRecord.setEndTime(new Date());
        examRecord.setStatus(forceSubmit ? ExamStatusConstants.RECORD_FORCED_SUBMIT : ExamStatusConstants.RECORD_SUBMITTED); // 1-正常交卷 2-强制交卷
        examRecordService.updateById(examRecord);
        
        // 清除缓存（提交试卷后，考试记录和列表都需要更新）
        redisCacheService.deleteRecordInfoCache(examRecord.getExamRecordUuidNumber());
        redisCacheService.clearExamListCache();
        log.info("🗑️  清除缓存（提交试卷 UUID={}）", examRecord.getExamRecordUuidNumber());
        
        log.info("[提交试卷] ✅ 考试记录状态已更新为'{}' - Record ID: {}, autoScore: {}, teacherScore: {}, totalScore: {}, passStatus: {}",
                forceSubmit ? "强制交卷" : "已提交",
                examRecord.getId(), examRecord.getAutoScore(), examRecord.getTeacherScore(), examRecord.getTotalScore(), examRecord.getPassStatus());
        log.info("========== [提交试卷] 结束 ==========\n");
        
        // 返回成绩数据
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("score", examRecord.getTotalScore());
        resultData.put("examRecordId", examRecord.getId());
        resultData.put("examRecordUuid", examRecord.getExamRecordUuidNumber());
        return Result.success(resultData);
    }

    /**
     * 实时保存答案（支持断点续考）
     * 
     * @param req {examPaperId, examInfoId, answerList: [{examQuestionId, answer}]}
     * @param request
     * @return
     */
    @PostMapping("/saveAnswer")
    public Result saveAnswer(@RequestBody Map<String, Object> req, HttpServletRequest request) {
        try {
            // 答题用户
            Integer usersId = (Integer) request.getSession().getAttribute("userId");
            if (usersId == null) {
                return Result.error("请先登录");
            }
            
            // 试卷id
            Integer examPaperId = parseNullableInteger(req.get("examPaperId"));
            if (examPaperId == null) {
                return Result.error("试卷ID不能为空");
            }
            // 考试id
            Integer examInfoId = parseNullableInteger(req.get("examInfoId"));
            String examRecordUuid = normalizeNullableString(req.get("examRecordUuid"));
            
            // 答题信息
            List<Map<String, Object>> answerList = (List<Map<String, Object>>) req.get("answerList");
            if (answerList == null || answerList.isEmpty()) {
                return Result.error("没有需要保存的答案");
            }
            
            ExamRecord examRecord = getOrCreateActiveExamRecord(examPaperId, examInfoId, usersId, examRecordUuid);
            if (examRecord.getStatus() != null && examRecord.getStatus() == ExamStatusConstants.RECORD_SUBMITTED) {
                return Result.error("试卷已提交，无法继续保存答案");
            }

            List<ExamPaperTopic> topicList = examPaperTopicService.lambdaQuery()
                    .eq(ExamPaperTopic::getExamPaperId, examPaperId)
                    .list();
            Map<Integer, ExamPaperTopic> topicMap = topicList.stream()
                    .collect(Collectors.toMap(ExamPaperTopic::getExamQuestionId, topic -> topic, (a, b) -> a));
            Map<Integer, ExamQuestion> questionMap = topicList.isEmpty()
                    ? new HashMap<>()
                    : examQuestionService.listByIds(topicList.stream().map(ExamPaperTopic::getExamQuestionId).collect(Collectors.toList()))
                            .stream()
                            .collect(Collectors.toMap(ExamQuestion::getId, question -> question, (a, b) -> a));
            Map<Integer, ExamDetails> detailMap = examDetailsService.lambdaQuery()
                    .eq(ExamDetails::getExamRecordId, examRecord.getId())
                    .list()
                    .stream()
                    .collect(Collectors.toMap(ExamDetails::getExamQuestionId, detail -> detail, (a, b) -> a));

            List<ExamDetails> insertList = new ArrayList<>();
            List<ExamDetails> updateList = new ArrayList<>();
            for (Map<String, Object> answerMap : answerList) {
                Integer examQuestionId = parseNullableInteger(answerMap.get("examQuestionId"));
                if (examQuestionId == null) {
                    continue;
                }
                String answer = normalizeAnswer(answerMap.get("answer") != null ? String.valueOf(answerMap.get("answer")) : null);
                ExamDetails existingDetail = detailMap.get(examQuestionId);

                if (existingDetail != null) {
                    existingDetail.setExamDetailsMyanswer(answer);
                    existingDetail.setExamDetailsMyscore(0);
                    updateList.add(existingDetail);
                } else {
                    ExamPaperTopic topic = topicMap.get(examQuestionId);
                    ExamQuestion question = questionMap.get(examQuestionId);
                    ExamDetails newDetail = new ExamDetails();
                    newDetail.setExamDetailsUuidNumber(examRecord.getExamRecordUuidNumber());
                    newDetail.setExamRecordId(examRecord.getId());
                    newDetail.setUsersId(usersId);
                    newDetail.setExamQuestionId(examQuestionId);
                    newDetail.setExamDetailsMyanswer(answer);
                    newDetail.setExamDetailsMyscore(0);
                    newDetail.setTeacherScore(0);
                    if (topic != null) {
                        newDetail.setExamPaperTopicNumber(topic.getExamPaperTopicNumber());
                        newDetail.setExamPaperTopicSequence(topic.getExamPaperTopicSequence());
                    }
                    newDetail.setReviewStatus(question != null && question.getExamQuestionTypes() != null && question.getExamQuestionTypes() == 5 ? 0 : 1);
                    newDetail.setCreateTime(new Date());
                    insertList.add(newDetail);
                }
            }

            examRecord.setStatus(ExamStatusConstants.RECORD_IN_PROGRESS);
            if (examInfoId != null && examRecord.getExamInfoId() == null) {
                examRecord.setExamInfoId(examInfoId);
            }
            examRecordService.updateById(examRecord);
            if (!insertList.isEmpty()) {
                examDetailsService.saveBatch(insertList);
            }
            if (!updateList.isEmpty()) {
                examDetailsService.updateBatchById(updateList);
            }
            
            // 清除考试列表缓存（保存答案后，考试记录状态可能变化，需要更新缓存）
            redisCacheService.clearExamListCache();
            // 清除考试记录详情缓存（保存答案后，答题详情已变化，必须清除缓存）
            redisCacheService.deleteRecordInfoCache(examRecord.getExamRecordUuidNumber());
            log.info("🗑️  清除缓存（保存答案 UUID={}）", examRecord.getExamRecordUuidNumber());
            
            log.info("[实时保存] 成功保存 {} 道题的答案 - Record ID: {}", answerList.size(), examRecord.getId());
            Map<String, Object> result = new HashMap<>();
            result.put("examRecordId", examRecord.getId());
            result.put("examRecordUuid", examRecord.getExamRecordUuidNumber());
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("[实时保存] 失败: {}", e.getMessage(), e);
            return Result.error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 触发强制交卷判分（由切屏监控调用）
     * 
     * @param examRecord 考试记录
     * @param userId 用户ID
     */
    public void triggerForceSubmitGrading(ExamRecord examRecord, Integer userId) {
        log.info("\n========== [强制交卷判分] 开始 ==========");
        log.info("[强制交卷判分] Record ID: {}, UUID: {}, 用户ID: {}", examRecord.getId(), examRecord.getExamRecordUuidNumber(), userId);
        
        try {
            // 获取试卷信息
            Integer examPaperId = examRecord.getExamPaperId();
            Integer examInfoId = examRecord.getExamInfoId();
            
            if (examPaperId == null) {
                log.error("[强制交卷判分] ❌ 试卷ID为空");
                return;
            }
            
            // 获取试卷题目
            HashMap<String, Object> map = new HashMap<>();
            map.put("limit", "9999");
            map.put("examPaperId", examPaperId);
            CommonUtil.checkMap(map);
            PageUtils page = examPaperTopicService.queryPage(map);
            List<ExamPaperTopicResp> examPaperTopicList = (List<ExamPaperTopicResp>) page.getList();

            if (examPaperTopicList == null || examPaperTopicList.isEmpty()) {
                log.warn("[强制交卷判分] ⚠️ 试卷中没有题目");
                return;
            }

            // 获取已有的答题详情
            Map<Integer, ExamDetails> existingDetailMap = examDetailsService.lambdaQuery()
                    .eq(ExamDetails::getExamRecordId, examRecord.getId())
                    .list()
                    .stream()
                    .collect(Collectors.toMap(ExamDetails::getExamQuestionId, detail -> detail, (a, b) -> a));
            
            List<ExamDetails> updateExamDetailsList = new ArrayList<>();
            List<ExamWrongQuestion> examWrongQuestionList = new ArrayList<>();
            
            // 初始化客观题自动判分总和
            int autoScore = 0;

            // 判断用户的答案
            for (ExamPaperTopicResp examPaperTopic : examPaperTopicList) {
                boolean cuoti = false;
                ExamDetails examDetails = existingDetailMap.get(examPaperTopic.getExamQuestionId());
                
                if (examDetails == null) {
                    // 如果没有答题详情，创建一个"未作答"的记录
                    examDetails = new ExamDetails();
                    examDetails.setExamDetailsUuidNumber(examRecord.getExamRecordUuidNumber());
                    examDetails.setExamRecordId(examRecord.getId());
                    examDetails.setUsersId(userId);
                    examDetails.setExamQuestionId(examPaperTopic.getExamQuestionId());
                    examDetails.setExamPaperTopicNumber(examPaperTopic.getExamPaperTopicNumber());
                    examDetails.setExamPaperTopicSequence(examPaperTopic.getExamPaperTopicSequence());
                    examDetails.setExamDetailsMyanswer("未作答");
                    examDetails.setExamDetailsMyscore(0);
                    examDetails.setTeacherScore(0);
                    examDetails.setReviewStatus(examPaperTopic.getExamQuestionTypes() == 5 ? 0 : 1);
                    examDetails.setCreateTime(new Date());
                    examDetailsService.save(examDetails);
                }

                ExamWrongQuestion examWrongQuestion = new ExamWrongQuestion();
                examWrongQuestion.setUsersId(String.valueOf(userId));
                examWrongQuestion.setInsertTime(new Date());
                examWrongQuestion.setCreateTime(new Date());
                examWrongQuestion.setExamPaperId(String.valueOf(examPaperId));
                examWrongQuestion.setExamQuestionId(String.valueOf(examPaperTopic.getExamQuestionId()));
                examWrongQuestion.setExamRecordId(examRecord.getId());

                String myAnswer = normalizeAnswer(examDetails.getExamDetailsMyanswer());
                
                // 简答题（类型5）不参与自动判分
                if (examPaperTopic.getExamQuestionTypes() == 5) {
                    examDetails.setExamDetailsMyanswer(myAnswer);
                    examWrongQuestion.setExamDetailsMyanswer(myAnswer);
                    examDetails.setExamDetailsMyscore(0);
                    examDetails.setTeacherScore(0);
                    examDetails.setReviewStatus(0); // 标记为待批阅
                } else if (!"未作答".equals(myAnswer)) {
                    // 客观题判分
                    examDetails.setExamDetailsMyanswer(myAnswer);
                    examWrongQuestion.setExamDetailsMyanswer(myAnswer);
                    examDetails.setTeacherScore(0);

                    // 多选题判断对错
                    if (examPaperTopic.getExamQuestionTypes() == 2) {
                        List<String> split = Arrays.asList(examPaperTopic.getExamQuestionAnswer().split(","));
                        ArrayList<String> splitLs = new ArrayList<>(split);
                        List<String> answer = Arrays.asList(myAnswer.split(","));
                        List<String> answerLs = new ArrayList<>(answer);
                        
                        if (splitLs.size() == answerLs.size()) {
                            splitLs.retainAll(answerLs);
                            if (splitLs.size() == answerLs.size()) {
                                autoScore += examPaperTopic.getExamPaperTopicNumber();
                                examDetails.setExamDetailsMyscore(examPaperTopic.getExamPaperTopicNumber());
                            } else {
                                cuoti = true;
                                examDetails.setExamDetailsMyscore(0);
                            }
                        } else {
                            cuoti = true;
                            examDetails.setExamDetailsMyscore(0);
                        }
                    } else {
                        // 其他题判断对错
                        if (myAnswer.equals(examPaperTopic.getExamQuestionAnswer())) {
                            examDetails.setExamDetailsMyscore(examPaperTopic.getExamPaperTopicNumber());
                            autoScore += examPaperTopic.getExamPaperTopicNumber();
                        } else {
                            cuoti = true;
                            examDetails.setExamDetailsMyscore(0);
                        }
                    }
                    examDetails.setReviewStatus(1);
                } else {
                    // 未作答
                    cuoti = true;
                    examWrongQuestion.setExamDetailsMyanswer("未作答");
                    examDetails.setExamDetailsMyanswer("未作答");
                    examDetails.setExamDetailsMyscore(0);
                    examDetails.setReviewStatus(1);
                }
                
                if (cuoti) {
                    examWrongQuestionList.add(examWrongQuestion);
                }
                updateExamDetailsList.add(examDetails);
            }

            log.info("[强制交卷判分] 判分完成 - 客观题得分: {}", autoScore);

            // 更新答题详情
            if (!updateExamDetailsList.isEmpty()) {
                examDetailsService.updateBatchById(updateExamDetailsList);
            }
            
            // 更新错题记录
            examWrongQuestionService.remove(new QueryWrapper<ExamWrongQuestion>()
                    .eq("exam_record_id", examRecord.getId()));
            if (!examWrongQuestionList.isEmpty()) {
                examWrongQuestionService.saveBatch(examWrongQuestionList);
            }
            
            log.info("[强制交卷判分] 已更新 {} 条答题详情，{} 条错题记录", updateExamDetailsList.size(), examWrongQuestionList.size());

            // 设置考试记录的精细化分数字段
            examRecord.setAutoScore(autoScore);
            examRecord.setTeacherScore(0);
            examRecord.setTotalScore(autoScore);
            
            // 检查是否有主观题，决定 pass_status
            boolean hasSubjectiveQuestions = examPaperTopicList.stream()
                    .anyMatch(topic -> topic.getExamQuestionTypes() == 5);
            
            if (!hasSubjectiveQuestions) {
                // 纯客观题考试，立即判定及格状态
                Integer passingScore = null;
                if (examInfoId != null) {
                    ExamInfo examInfo = examInfoService.getById(examInfoId);
                    if (examInfo != null) {
                        passingScore = examInfo.getPassingScore();
                    }
                }
                
                if (passingScore != null && passingScore > 0) {
                    if (autoScore >= passingScore) {
                        examRecord.setPassStatus(1); // 及格
                        log.info("[强制交卷判分] 纯客观题考试 - 及格状态: 及格 ({} >= {})", autoScore, passingScore);
                    } else {
                        examRecord.setPassStatus(2); // 不及格
                        log.info("[强制交卷判分] 纯客观题考试 - 及格状态: 不及格 ({} < {})", autoScore, passingScore);
                    }
                } else {
                    examRecord.setPassStatus(0); // 待判定
                    log.info("[强制交卷判分] 纯客观题考试 - 及格状态: 待判定（未设置及格分）");
                }
            } else {
                // 含主观题考试，等待教师批阅后判定
                examRecord.setPassStatus(0);
                log.info("[强制交卷判分] 含主观题考试 - 及格状态: 待判定（等待教师批阅）");
            }
            
            // 更新考试记录（状态已在reportScreenSwitch中设置为RECORD_FORCED_SUBMIT）
            examRecordService.updateById(examRecord);
            
            log.info("[强制交卷判分] ✅ 完成 - autoScore: {}, teacherScore: {}, totalScore: {}, passStatus: {}",
                    examRecord.getAutoScore(), examRecord.getTeacherScore(), examRecord.getTotalScore(), examRecord.getPassStatus());
            log.info("========== [强制交卷判分] 结束 ==========\n");
            
        } catch (Exception e) {
            log.error("[强制交卷判分] ❌ 异常: {}", e.getMessage(), e);
            throw new RuntimeException("强制交卷判分失败: " + e.getMessage(), e);
        }
    }

    private ExamRecord getOrCreateActiveExamRecord(Integer examPaperId, Integer examInfoId, Integer usersId, String examRecordUuid) {
        ExamRecord examRecord = null;
        if (StrUtil.isNotBlank(examRecordUuid)) {
            examRecord = examRecordService.lambdaQuery()
                    .eq(ExamRecord::getExamRecordUuidNumber, examRecordUuid)
                    .eq(ExamRecord::getUsersId, usersId)
                    .last("LIMIT 1")
                    .one();
        }

        if (examRecord == null) {
            QueryWrapper<ExamRecord> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("exam_paper_id", examPaperId)
                    .eq("users_id", usersId)
                    .eq("status", 0)
                    .orderByDesc("id")
                    .last("LIMIT 1");
            if (examInfoId != null) {
                queryWrapper.eq("exam_info_id", examInfoId);
            }
            examRecord = examRecordService.getOne(queryWrapper);
        }

        if (examRecord == null) {
            String uuid = String.valueOf(System.currentTimeMillis());
            examRecord = new ExamRecord();
            examRecord.setExamRecordUuidNumber(uuid);
            examRecord.setTotalScore(0);
            examRecord.setAutoScore(0);
            examRecord.setTeacherScore(0);
            examRecord.setPassStatus(0);
            examRecord.setStatus(ExamStatusConstants.RECORD_IN_PROGRESS);
            examRecord.setIsLatest(1);
            examRecord.setExamPaperId(examPaperId);
            examRecord.setExamInfoId(examInfoId);
            examRecord.setUsersId(usersId);
            examRecord.setInsertTime(new Date());
            examRecord.setCreateTime(new Date());
            examRecordService.save(examRecord);
            log.info("[考试记录] 创建新的考试记录 - ID: {}, UUID: {}", examRecord.getId(), uuid);
        } else if (examInfoId != null && examRecord.getExamInfoId() == null) {
            examRecord.setExamInfoId(examInfoId);
            examRecordService.updateById(examRecord);
        }
        return examRecord;
    }

    private String normalizeAnswer(String answer) {
        if (StrUtil.isBlank(answer) || "null".equalsIgnoreCase(answer) || "未作答".equals(answer)) {
            return "未作答";
        }
        return answer;
    }

    private Integer parseNullableInteger(Object value) {
        String normalized = normalizeNullableString(value);
        if (normalized == null) {
            return null;
        }
        return Integer.valueOf(normalized);
    }

    private String normalizeNullableString(Object value) {
        if (value == null) {
            return null;
        }
        String str = String.valueOf(value).trim();
        if (StrUtil.isBlank(str) || "null".equalsIgnoreCase(str) || "undefined".equalsIgnoreCase(str)) {
            return null;
        }
        return str;
    }

}
