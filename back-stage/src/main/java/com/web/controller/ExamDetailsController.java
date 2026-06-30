package com.web.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.web.annotation.IgnoreAuth;
import com.web.domain.ExamDetails;
import com.web.domain.ExamPaperTopic;
import com.web.domain.ExamQuestion;
import com.web.domain.ExamRecord;
import com.web.domain.ExamWrongQuestion;
import com.web.domain.Req.ExamDetailsReq;
import com.web.service.ExamDetailsService;
import com.web.service.ExamPaperTopicService;
import com.web.service.ExamQuestionService;
import com.web.service.ExamRecordService;
import com.web.service.ExamWrongQuestionService;
import com.web.utils.ExamStatusConstants;
import com.web.utils.OperationLogUtil;
import com.web.utils.PageUtils;
import com.web.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 答题详情 前端控制器
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/examDetails")
public class ExamDetailsController {

    @Resource
    private ExamDetailsService examDetailsService;

    @Resource
    private ExamQuestionService examQuestionService;

    @Resource
    private ExamRecordService examRecordService;

    @Resource
    private com.web.service.ExamInfoService examInfoService;

    @Resource
    private ExamWrongQuestionService examWrongQuestionService;

    @Resource
    private ExamPaperTopicService examPaperTopicService;
    
    @Resource
    private OperationLogUtil operationLogUtil;
    
    @Resource
    private com.web.service.RedisCacheService redisCacheService;

    /**
     * 后台分页查询
     * 
     * @return
     */
    @PostMapping("/page")
    public Result page(@RequestBody ExamDetailsReq req, HttpServletRequest request) {
        Object tableNameObj = request.getSession().getAttribute("tableName");
        String tableName = tableNameObj != null ? String.valueOf(tableNameObj) : null;
        Integer sessionUserId = (Integer) request.getSession().getAttribute("userId");
        
        // 学生端：仅用会话用户筛选，忽略请求体中的 usersId，防止越权查询他人答题详情
        if ("users".equals(tableName) && sessionUserId != null) {
            req.setUsersId(sessionUserId);
        }

        PageUtils page = examDetailsService.selectPage(req);
        return Result.success(page);
    }

    /**
     * 新增
     * 
     * @param examDetails
     * @return
     */
    @PostMapping("/save")
    public Result save(@RequestBody ExamDetails examDetails, HttpServletRequest request) {
        examDetailsService.save(examDetails);
        
        // 记录操作日志
        operationLogUtil.logSuccess(request, "考试记录", "新增", "新增答题详情，ID：" + examDetails.getId());
        
        return Result.success();
    }

    /**
     * 修改
     * 
     * @param examDetails
     * @return
     */
    @PostMapping("/update")
    public Result update(@RequestBody ExamDetails examDetails, HttpServletRequest request) {
        examDetailsService.updateById(examDetails);
        
        // 记录操作日志
        operationLogUtil.logSuccess(request, "考试记录", "修改", "修改答题详情，ID：" + examDetails.getId());
        
        return Result.success();
    }

    /**
     * 根据id查询
     * 
     * @param id
     * @return
     */
    @GetMapping("/info/{id}")
    public Result getById(@PathVariable Integer id, HttpServletRequest request) {
        Object tableNameObj = request.getSession().getAttribute("tableName");
        String tableName = tableNameObj != null ? String.valueOf(tableNameObj) : null;
        Integer userId = (Integer) request.getSession().getAttribute("userId");

        com.web.domain.Resp.ExamDetailsResp resp = examDetailsService.info(id);
        if (resp == null) {
            return Result.error("记录不存在");
        }

        // 学生只能查看自己的答题详情
        if ("users".equals(tableName) && userId != null) {
            try {
                if (resp.getUsersId() != null && !String.valueOf(userId).equals(String.valueOf(resp.getUsersId()))) {
                    return Result.error("无权查看该记录");
                }
            } catch (Exception ignored) {
                return Result.error("无权查看该记录");
            }
        }

        return Result.success(resp);
    }

    /**
     * 根据id删除
     * 
     * @param id
     * @return
     */
    @DeleteMapping("/del/{id}")
    public Result delete(@PathVariable Integer id, HttpServletRequest request) {
        boolean success = examDetailsService.removeById(id);
        
        // 记录操作日志
        if (success) {
            operationLogUtil.logSuccess(request, "考试记录", "删除", "删除答题详情，ID：" + id);
        }
        
        return Result.success(success);
    }

    /**
     * 批量删除
     * 
     * @param ids
     * @return
     */
    @RequestMapping("/del/batch")
    public Result deleteBatch(@RequestBody Integer[] ids, HttpServletRequest request) {
        boolean success = examDetailsService.removeBatchByIds(Arrays.asList(ids));
        
        // 记录操作日志
        if (success) {
            operationLogUtil.logSuccess(request, "考试记录", "删除", "批量删除答题详情，共 " + ids.length + " 条");
        }
        
        return Result.success(success);
    }

    /**
     * 分页查询
     * 
     * @param req
     * @return
     */
    @IgnoreAuth
    @PostMapping("/list")
    public Result list(@RequestBody ExamDetailsReq req) {
        PageUtils page = examDetailsService.selectPage(req);
        return Result.success(page);
    }

    /**
     * 加载所有
     * 
     * @return
     */
    @GetMapping("/loadAll")
    public Result loadAll() {
        return Result.success(examDetailsService.list());
    }

    /**
     * 教师批阅主观题（简答题）
     * 
     * @param jsonBody {examDetailsId, teacherScore, teacherComment, reviewStatus(0/1, optional), recalculate(optional)}
     * @return
     */
    @PostMapping("/gradeSubjective")
    public Result gradeSubjective(@RequestBody String jsonBody, HttpServletRequest request) {
        try {
            JSONObject entries = JSONUtil.parseObj(jsonBody);
            Integer examDetailsId = entries.getInt("examDetailsId");
            Integer teacherScore = entries.getInt("teacherScore");
            String teacherComment = entries.getStr("teacherComment");
            Integer reviewStatus = entries.getInt("reviewStatus"); // 0-待批阅 1-已批阅
            Boolean recalculate = entries.getBool("recalculate");

            // 1. 参数校验
            if (examDetailsId == null || teacherScore == null) {
                return Result.error("参数错误：缺少必要参数");
            }

            if (teacherScore < 0) {
                return Result.error("分数不能为负数");
            }
            if (reviewStatus != null && reviewStatus != 0 && reviewStatus != 1) {
                return Result.error("参数错误：reviewStatus 只能为 0 或 1");
            }

            // 2. 查询答题详情
            ExamDetails examDetails = examDetailsService.getById(examDetailsId);
            if (examDetails == null) {
                return Result.error("答题记录不存在");
            }

            // 3. 验证是否为简答题
            ExamQuestion question = examQuestionService.getById(examDetails.getExamQuestionId());
            if (question == null) {
                return Result.error("试题不存在");
            }

            if (question.getExamQuestionTypes() != 5) {
                return Result.error("只能批阅简答题（类型5）");
            }

            // 4. 更新考试记录的阅卷状态（首次批阅时）
            Integer teacherId = (Integer) request.getSession().getAttribute("userId");
            String tableName = (String) request.getSession().getAttribute("tableName");
            
            // 5. 更新批阅信息
            examDetails.setTeacherScore(teacherScore);
            examDetails.setTeacherComment(teacherComment);
            // reviewStatus 不传则按“批阅提交”默认置为 1，传 0 则仅保存不改变待批阅状态
            if (reviewStatus == null) {
                reviewStatus = 1;
            }
            examDetails.setReviewStatus(reviewStatus);
            examDetails.setExamDetailsMyscore(teacherScore); // 主观题得分同步到明细得分字段（总分汇总依赖）

            boolean updated = examDetailsService.updateById(examDetails);
            if (!updated) {
                return Result.error("批阅失败，请稍后重试");
            }

            // 6. 错题本：仅在「已批阅」时按得分写入或移除；「待批阅」草稿不写错题本，并清除该题已有错题记录（避免曾确认后又改草稿仍留在学生错题本）。
            if (reviewStatus != null && reviewStatus == 1) {
                syncWrongQuestionBySubjectiveResult(examDetails, teacherScore);
            } else if (reviewStatus != null && reviewStatus == 0) {
                removeSubjectiveWrongQuestionForDetail(examDetails);
            }

            // 7. 重新计算该考生的总分（仅在“确认批阅/提交”时触发）
            boolean shouldRecalculate = Boolean.TRUE.equals(recalculate) || (recalculate == null && reviewStatus == 1);
            if (shouldRecalculate) {
                recalculateTotalScore(examDetails.getExamDetailsUuidNumber());
            }

            log.info("教师保存/批阅成功：答题ID={}, 分数={}, reviewStatus={}, recalculate={}", examDetailsId, teacherScore, reviewStatus, shouldRecalculate);
            
            // 清除考试记录详情缓存（批阅后数据已变化，必须清除缓存）
            if (examDetails.getExamDetailsUuidNumber() != null) {
                redisCacheService.deleteRecordInfoCache(examDetails.getExamDetailsUuidNumber());
                log.info("🗑️  清除考试记录详情缓存（批阅 UUID={}）", examDetails.getExamDetailsUuidNumber());
            }
            // 清除考试记录列表缓存
            redisCacheService.clearRecordListCache();
            // 清除统计数据缓存（批阅后成绩变化，统计数据需要更新）
            redisCacheService.clearAllStatisticsCache();
            log.info("🗑️  清除统计数据缓存（批阅后成绩已变化）");
            
            // 记录操作日志（仅在确认批阅时记录）
            if (reviewStatus != null && reviewStatus == 1) {
                operationLogUtil.logSuccess(request, "阅卷管理", "阅卷", "批阅简答题，答题ID：" + examDetailsId + "，得分：" + teacherScore);
            }
            
            return Result.success("保存成功");

        } catch (Exception e) {
            log.error("批阅失败：{}", e.getMessage(), e);
            return Result.error("批阅失败：" + e.getMessage());
        }
    }

    /**
     * 查询有待批阅简答题的考试记录
     * 
     * @param req 查询条件
     * @return
     */
    @PostMapping("/pendingReviewList")
    public Result getPendingReviewList(@RequestBody ExamDetailsReq req) {
        try {
            // 使用原生 SQL 查询包含未批阅简答题的考试记录
            List<ExamDetails> pendingList = examDetailsService.lambdaQuery()
                    .exists("SELECT 1 FROM exam_question eq " +
                            "WHERE eq.id = exam_details.exam_question_id " +
                            "AND eq.exam_question_types = 5 " +
                            "AND (exam_details.review_status = 0 OR exam_details.review_status IS NULL)")
                    .orderByDesc(ExamDetails::getCreateTime)
                    .list();

            return Result.success(new PageUtils(pendingList, pendingList.size(), 1, 10));
        } catch (Exception e) {
            log.error("查询待批阅列表失败：{}", e.getMessage(), e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 重新计算考生总分并更新及格状态
     * 
     * @param uuidNumber 考试编号
     */
    private void recalculateTotalScore(String uuidNumber) {
        try {
            // 1. 先通过 UUID 找到考试记录 ID
            ExamRecord record = examRecordService.lambdaQuery()
                    .eq(ExamRecord::getExamRecordUuidNumber, uuidNumber)
                    .one();
            
            if (record == null) {
                log.warn("未找到考试编号 {} 对应的考试记录", uuidNumber);
                return;
            }
            
            // 2. 根据 exam_record_id 查询该考生所有答题详情
            List<ExamDetails> detailsList = examDetailsService.lambdaQuery()
                    .eq(ExamDetails::getExamRecordId, record.getId())
                    .list();

            if (detailsList == null || detailsList.isEmpty()) {
                log.warn("未找到考试记录 ID {} 的答题记录", record.getId());
                return;
            }

            // 3. 分别累加客观题和主观题得分
            int autoScore = 0;
            int teacherScore = 0;
            
            for (ExamDetails detail : detailsList) {
                // 查询题目类型
                ExamQuestion question = examQuestionService.getById(detail.getExamQuestionId());
                if (question != null) {
                    if (question.getExamQuestionTypes() == 5) {
                        // 简答题：累加到 teacherScore
                        teacherScore += (detail.getExamDetailsMyscore() != null ? detail.getExamDetailsMyscore() : 0);
                    } else {
                        // 客观题：累加到 autoScore
                        autoScore += (detail.getExamDetailsMyscore() != null ? detail.getExamDetailsMyscore() : 0);
                    }
                }
            }
            
            // 4. 更新考试记录的精细化分数字段
            Integer oldAutoScore = record.getAutoScore();
            Integer oldTeacherScore = record.getTeacherScore();
            Integer oldTotalScore = record.getTotalScore();
            
            record.setAutoScore(autoScore);
            record.setTeacherScore(teacherScore);
            record.setTotalScore(autoScore + teacherScore);
            
            // 5. 检查是否所有主观题都已批阅
            long pendingCount = detailsList.stream()
                    .filter(d -> {
                        ExamQuestion q = examQuestionService.getById(d.getExamQuestionId());
                        return q != null && q.getExamQuestionTypes() == 5 && 
                               (d.getReviewStatus() == null || d.getReviewStatus() == ExamStatusConstants.REVIEW_PENDING);
                    })
                    .count();
            
            // 6. 如果所有主观题都已批阅，更新 pass_status、status 和 reviewStatus
            if (pendingCount == 0 && record.getExamInfoId() != null) {
                com.web.domain.ExamInfo examInfo = examInfoService.getById(record.getExamInfoId());
                if (examInfo != null && examInfo.getPassingScore() != null && examInfo.getPassingScore() > 0) {
                    Integer passingScore = examInfo.getPassingScore();
                    Integer totalScore = record.getTotalScore();
                    
                    if (totalScore >= passingScore) {
                        record.setPassStatus(1); // 及格
                        log.info("[及格判定] 考试记录 {} - 及格 ({} >= {})", record.getId(), totalScore, passingScore);
                    } else {
                        record.setPassStatus(2); // 不及格
                        log.info("[及格判定] 考试记录 {} - 不及格 ({} < {})", record.getId(), totalScore, passingScore);
                    }
                } else {
                    record.setPassStatus(0); // 待判定（未设置及格分）
                    log.info("[及格判定] 考试记录 {} - 待判定（未设置及格分）", record.getId());
                }
                
                // 状态流转：1/2 -> 3 (已完成)
                if (record.getStatus() == ExamStatusConstants.RECORD_SUBMITTED
                        || record.getStatus() == ExamStatusConstants.RECORD_FORCED_SUBMIT) {
                    record.setStatus(ExamStatusConstants.RECORD_COMPLETED);
                    log.info("[状态流转] 考试记录 {} - 已提交/强制交卷 -> 已完成", record.getId());
                }
            }
            
            // 7. 数据一致性校验
            int calculatedTotal = record.getAutoScore() + record.getTeacherScore();
            if (calculatedTotal != (record.getTotalScore() != null ? record.getTotalScore() : 0)) {
                log.error("⚠️ 数据不一致警告 - Record ID: {}, autoScore: {}, teacherScore: {}, totalScore: {}, calculated: {}",
                        record.getId(), record.getAutoScore(), record.getTeacherScore(), record.getTotalScore(), calculatedTotal);
                // 自动修复
                record.setTotalScore(calculatedTotal);
            }
            
            // 8. 更新考试记录
            examRecordService.updateById(record);
            
            log.info("总分重算完成：考试编号={}, 记录ID={}, 原autoScore={}, 新autoScore={}, 原teacherScore={}, 新teacherScore={}, 原总分={}, 新总分={}, passStatus={}",
                    uuidNumber, record.getId(), oldAutoScore, record.getAutoScore(), 
                    oldTeacherScore, record.getTeacherScore(), oldTotalScore, record.getTotalScore(), record.getPassStatus());
        } catch (Exception e) {
            log.error("总分重算失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 待批阅草稿：删除该生该卷该题在当前考试记录中的错题本记录（不写新纪录）。
     */
    private void removeSubjectiveWrongQuestionForDetail(ExamDetails examDetails) {
        try {
            Integer examPaperId = resolveExamPaperIdForWrongQuestion(examDetails);
            if (examPaperId == null || examDetails.getExamQuestionId() == null || examDetails.getUsersId() == null) {
                return;
            }
            String usersId = String.valueOf(examDetails.getUsersId());
            // 注意：必须加上 exam_record_id 条件，避免删除其他考试中的错题记录
            examWrongQuestionService.remove(new LambdaQueryWrapper<ExamWrongQuestion>()
                    .eq(ExamWrongQuestion::getUsersId, usersId)
                    .eq(ExamWrongQuestion::getExamPaperId, String.valueOf(examPaperId))
                    .eq(ExamWrongQuestion::getExamQuestionId, String.valueOf(examDetails.getExamQuestionId()))
                    .eq(ExamWrongQuestion::getExamRecordId, examDetails.getExamRecordId())); // 新增：限定当前考试记录
        } catch (Exception e) {
            log.error("清除待批阅简答题错题记录失败，examDetailsId={}", examDetails.getId(), e);
        }
    }

    private Integer resolveExamPaperIdForWrongQuestion(ExamDetails examDetails) {
        if (examDetails.getExamRecordId() != null) {
            ExamRecord record = examRecordService.getById(examDetails.getExamRecordId());
            if (record != null) {
                return record.getExamPaperId();
            }
        }
        if (examDetails.getExamDetailsUuidNumber() != null) {
            ExamRecord record = examRecordService.lambdaQuery()
                    .eq(ExamRecord::getExamRecordUuidNumber, examDetails.getExamDetailsUuidNumber())
                    .one();
            if (record != null) {
                return record.getExamPaperId();
            }
        }
        return null;
    }

    /**
     * 根据简答题批阅结果同步错题本
     */
    private void syncWrongQuestionBySubjectiveResult(ExamDetails examDetails, Integer teacherScore) {
        try {
            String usersId = String.valueOf(examDetails.getUsersId());

            Integer examPaperId = resolveExamPaperIdForWrongQuestion(examDetails);
            if (examPaperId == null) {
                log.warn("错题本同步跳过：未找到 examPaperId, examDetailsId={}", examDetails.getId());
                return;
            }

            int fullScore = resolveSubjectiveFullScore(examDetails, examPaperId);
            if (fullScore <= 0) {
                log.warn("错题本同步跳过：无法解析该题满分 examDetailsId={}, examPaperId={}", examDetails.getId(), examPaperId);
                return;
            }

            // 先清理该题在当前考试记录中的错题记录，避免重复
            // 注意：必须加上 exam_record_id 条件，避免删除其他考试中的错题记录
            examWrongQuestionService.remove(new LambdaQueryWrapper<ExamWrongQuestion>()
                    .eq(ExamWrongQuestion::getUsersId, usersId)
                    .eq(ExamWrongQuestion::getExamPaperId, String.valueOf(examPaperId))
                    .eq(ExamWrongQuestion::getExamQuestionId, String.valueOf(examDetails.getExamQuestionId()))
                    .eq(ExamWrongQuestion::getExamRecordId, examDetails.getExamRecordId())); // 新增：限定当前考试记录

            int scored = teacherScore != null ? teacherScore : 0;
            // 未得满分：全错(0) 与 部分错误(0<scored<full) 均写入错题本
            if (scored < fullScore) {
                ExamWrongQuestion wrongQuestion = new ExamWrongQuestion();
                wrongQuestion.setUsersId(usersId);
                wrongQuestion.setExamPaperId(String.valueOf(examPaperId));
                wrongQuestion.setExamQuestionId(String.valueOf(examDetails.getExamQuestionId()));
                wrongQuestion.setExamRecordId(examDetails.getExamRecordId());
                wrongQuestion.setExamDetailsMyanswer(
                        examDetails.getExamDetailsMyanswer() != null ? examDetails.getExamDetailsMyanswer() : "未作答");
                Date now = new Date();
                wrongQuestion.setInsertTime(now);
                wrongQuestion.setCreateTime(now);
                examWrongQuestionService.save(wrongQuestion);
            }
        } catch (Exception e) {
            log.error("同步错题本失败，examDetailsId={}", examDetails.getId(), e);
        }
    }

    /**
     * 简答题满分：以答题明细为准，为空或 0 时从试卷选题表兜底。
     */
    private int resolveSubjectiveFullScore(ExamDetails examDetails, Integer examPaperId) {
        Integer fromDetail = examDetails.getExamPaperTopicNumber();
        if (fromDetail != null && fromDetail > 0) {
            return fromDetail;
        }
        if (examPaperId == null || examDetails.getExamQuestionId() == null) {
            return 0;
        }
        ExamPaperTopic topic = examPaperTopicService.lambdaQuery()
                .eq(ExamPaperTopic::getExamPaperId, examPaperId)
                .eq(ExamPaperTopic::getExamQuestionId, examDetails.getExamQuestionId())
                .one();
        if (topic != null && topic.getExamPaperTopicNumber() != null && topic.getExamPaperTopicNumber() > 0) {
            return topic.getExamPaperTopicNumber();
        }
        return 0;
    }

}
