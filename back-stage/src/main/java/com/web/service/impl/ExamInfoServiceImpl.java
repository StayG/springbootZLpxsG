package com.web.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.web.domain.ExamInfo;
import com.web.domain.ExamPaperTopic;
import com.web.domain.ExamRecord;
import com.web.domain.ExamPaper;
import com.web.domain.Req.ExamInfoReq;
import com.web.domain.Resp.StudentExamInfoResp;
import com.web.mapper.ExamInfoMapper;
import com.web.service.ExamRecordService;
import com.web.service.ExamInfoService;
import com.web.service.ExamPaperTopicService;
import com.web.service.ExamPaperService;
import com.web.utils.PageUtils;
import com.web.utils.Query;
import com.web.utils.ExamStatusConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 考试信息 Service 实现类
 * </p>
 */
@Slf4j
@Service
public class ExamInfoServiceImpl extends ServiceImpl<ExamInfoMapper, ExamInfo> implements ExamInfoService {

    @Resource
    private ExamPaperService examPaperService;

    @Resource
    private ExamRecordService examRecordService;

    @Resource
    private ExamPaperTopicService examPaperTopicService;

    /** 
     * 考试状态：只存储 0未发布、1已发布
     * "进行中"和"已结束"由 startTime/endTime 动态计算
     */
    private static final int STATUS_UNPUBLISHED = 0;  // 未发布
    private static final int STATUS_PUBLISHED = 1;    // 已发布

    @Override
    public PageUtils selectPage(ExamInfoReq req) {
        // 使用 Query 工具类创建分页对象
        Page<ExamInfo> page = new Query<ExamInfo>(req.getPageInfo()).getPage();
        
        // 执行自定义 SQL 查询（支持数据隔离和关联查询）
        List<ExamInfo> records = baseMapper.selectPageList(req);
        
        // 动态计算考试状态（管理端展示）
        Date now = new Date();
        for (ExamInfo exam : records) {
            if (exam.getStatus() != null && exam.getStatus() == STATUS_PUBLISHED) {
                // 已发布的考试，根据时间动态计算状态
                if (exam.getStartTime() != null && exam.getEndTime() != null) {
                    if (now.before(exam.getStartTime())) {
                        // 未开始：保持已发布状态
                        exam.setStatus(STATUS_PUBLISHED);
                    } else if (now.after(exam.getEndTime())) {
                        // 已结束：设置为状态3
                        exam.setStatus(3);
                    } else {
                        // 进行中：设置为状态2
                        exam.setStatus(2);
                    }
                }
            }
            // 未发布的考试（status=0）保持不变
        }
        
        // 设置查询结果
        page.setRecords(records);
        page.setTotal(records.size());
        
        return new PageUtils(page);
    }

    @Override
    public ExamInfo info(Integer id) {
        return baseMapper.selectById(id);
    }

    @Override
    public String publish(Integer id) {
        log.info("========== [发布考试] 开始 ==========");
        log.info("[发布考试] 考试ID: {}", id);

        // 1. 查询考试信息
        ExamInfo examInfo = baseMapper.selectById(id);
        if (examInfo == null) {
            log.error("[发布考试] ❌ 考试不存在，ID: {}", id);
            return "考试不存在";
        }

        // 2. 校验状态：只能发布未发布（状态=0）的考试
        if (examInfo.getStatus() != 0) {
            log.error("[发布考试] ❌ 考试状态不正确，当前状态: {}，只有未发布状态才能发布", examInfo.getStatus());
            return "考试状态不正确，只有未发布状态才能发布";
        }

        // 3. 校验关联试卷是否存在
        if (examInfo.getExamPaperId() == null) {
            log.error("[发布考试] ❌ 未关联试卷");
            return "未关联试卷";
        }

        ExamPaper examPaper = examPaperService.getById(examInfo.getExamPaperId());
        if (examPaper == null) {
            log.error("[发布考试] ❌ 关联的试卷不存在，试卷ID: {}", examInfo.getExamPaperId());
            return "关联的试卷不存在";
        }
        log.info("[发布考试] ✅ 关联试卷验证通过 - 试卷名称: {}, 总分: {}", examPaper.getExamPaperName(), examPaper.getExamPaperMyscore());

        // 4. 校验考试时间
        if (examInfo.getStartTime() == null || examInfo.getEndTime() == null) {
            log.error("[发布考试] ❌ 考试时间未设置");
            return "考试时间未设置";
        }

        Date now = new Date();
        if (examInfo.getEndTime().before(examInfo.getStartTime())) {
            log.error("[发布考试] ❌ 结束时间早于开始时间");
            return "结束时间早于开始时间";
        }

        log.info("[发布考试] 开始时间: {}, 结束时间: {}", examInfo.getStartTime(), examInfo.getEndTime());

        // 5. 【双重保险】检测与已发布考试的时间冲突
        String conflictMessage = checkTimeConflictForPublish(examInfo.getId(), examInfo.getStartTime(), 
                examInfo.getEndTime(), examInfo.getKemuTypes());
        if (conflictMessage != null) {
            log.error("[发布考试] ❌ 时间冲突检测失败: {}", conflictMessage);
            return conflictMessage;
        }
        log.info("[发布考试] ✅ 时间冲突检测通过");

        // 6. 更新状态为已发布（状态=1）
        examInfo.setStatus(1);
        examInfo.setUpdateTime(now);

        int result = baseMapper.updateById(examInfo);
        log.info("[发布考试] {} 发布考试 - 考试名称: {}, 最终状态: {}", 
                result > 0 ? "✅" : "❌", examInfo.getExamName(), examInfo.getStatus());
        log.info("========== [发布考试] 结束 ==========\n");

        return result > 0 ? null : "发布失败，数据库更新失败";
    }

    @Override
    public boolean unpublish(Integer id) {
        log.info("========== [取消发布考试] 开始 ==========");
        log.info("[取消发布] 考试ID: {}", id);

        // 1. 查询考试信息
        ExamInfo examInfo = baseMapper.selectById(id);
        if (examInfo == null) {
            log.error("[取消发布] ❌ 考试不存在，ID: {}", id);
            return false;
        }

        // 2. 校验状态：只能取消已发布（状态=1）且未开始的考试
        if (examInfo.getStatus() != 1) {
            log.error("[取消发布] ❌ 考试状态不正确，当前状态: {}，只有已发布状态才能取消", examInfo.getStatus());
            return false;
        }

        // 3. 校验是否已开始：如果已经开始则不允许取消
        Date now = new Date();
        if (examInfo.getStartTime() != null && now.after(examInfo.getStartTime())) {
            log.error("[取消发布] ❌ 考试已开始，无法取消发布，开始时间: {}", examInfo.getStartTime());
            return false;
        }

        // 4. 更新状态为未发布（状态=0）
        examInfo.setStatus(0);
        examInfo.setUpdateTime(now);

        int result = baseMapper.updateById(examInfo);
        log.info("[取消发布] {} 取消发布 - 考试名称: {}, 最终状态: {}", 
                result > 0 ? "✅" : "❌", examInfo.getExamName(), examInfo.getStatus());
        log.info("========== [取消发布考试] 结束 ==========\n");

        return result > 0;
    }

    @Override
    public PageUtils selectStudentPage(ExamInfoReq req) {
        // 1. 查询所有符合条件的记录（不进行物理分页，因为需要动态计算状态）
        List<StudentExamInfoResp> allRecords = baseMapper.selectStudentPageList(req);
        
        // 2. 动态计算前端展示状态
        Date now = new Date();
        for (StudentExamInfoResp resp : allRecords) {
            boolean allowRetake = resp.getAllowRetake() != null && resp.getAllowRetake() == 1;
            int maxRetakeCount = resp.getMaxRetakeCount() != null ? resp.getMaxRetakeCount() : 0;
            boolean hasRecord = resp.getExamRecordUuidNumber() != null && !resp.getExamRecordUuidNumber().isBlank();
            Integer recordStatus = resp.getExamRecordStatus();
            long attemptCountLong = req.getUsersId() == null ? 0L : examRecordService.lambdaQuery()
                    .eq(ExamRecord::getExamInfoId, resp.getId())
                    .eq(ExamRecord::getUsersId, req.getUsersId())
                    .count();
            int attemptCount = Math.toIntExact(attemptCountLong);
            int usedRetakeCount = Math.max(0, attemptCount - 1);
            int remainingRetakeCount = allowRetake ? Math.max(0, maxRetakeCount - usedRetakeCount) : 0;

            resp.setAttemptCount(attemptCount);
            resp.setRemainingRetakeCount(remainingRetakeCount);

            if (now.before(resp.getStartTime())) {
                // 未开始
                resp.setStatus(ExamStatusConstants.EXAM_NOT_STARTED);
                resp.setCanRetake(false);
                resp.setActionType("not_started");
                resp.setActionText("未开始");
            } else if (now.after(resp.getEndTime())) {
                // 考试已结束（最高优先级）：结束后不允许再进入/重考
                if (resp.getExamRecordUuidNumber() != null) {
                    // 有考试记录：待出分看答卷，已发布看成绩
                    resp.setStatus(ExamStatusConstants.EXAM_FINISHED);
                    resp.setCanRetake(false);
                    if (resp.getPendingReviewCount() != null && resp.getPendingReviewCount() > 0) {
                        resp.setActionType("view_answer_sheet");
                        resp.setActionText("查看答卷");
                    } else {
                        resp.setActionType("view_result");
                        resp.setActionText("查看成绩");
                    }
                } else {
                    // 无考试记录：已结束（未参加）
                    resp.setStatus(ExamStatusConstants.EXAM_ENDED);
                    resp.setCanRetake(false);
                    resp.setActionType("ended");
                    resp.setActionText("已结束");
                }
            } else if (recordStatus != null && recordStatus == ExamStatusConstants.RECORD_IN_PROGRESS) {
                // 考试中
                resp.setStatus(ExamStatusConstants.EXAM_ONGOING);
                resp.setCanRetake(false);
                resp.setActionType("resume_exam");
                resp.setActionText("继续考试");
            } else if (recordStatus != null && (recordStatus == ExamStatusConstants.RECORD_SUBMITTED
                    || recordStatus == ExamStatusConstants.RECORD_FORCED_SUBMIT
                    || recordStatus == ExamStatusConstants.RECORD_COMPLETED)) {
                // 已提交/强制交卷/已完成
                if (allowRetake && remainingRetakeCount > 0) {
                    resp.setStatus(ExamStatusConstants.EXAM_ONGOING);
                    resp.setCanRetake(true);
                    resp.setActionType("retake_exam");
                    resp.setActionText("重新考试");
                } else {
                    resp.setStatus(ExamStatusConstants.EXAM_FINISHED);
                    resp.setCanRetake(false);
                    if (resp.getPendingReviewCount() != null && resp.getPendingReviewCount() > 0) {
                        resp.setActionType("view_answer_sheet");
                        resp.setActionText("查看答卷");
                    } else {
                        resp.setActionType("view_result");
                        resp.setActionText(recordStatus == ExamStatusConstants.RECORD_FORCED_SUBMIT ? "查看成绩（强制交卷）" : "查看成绩");
                    }
                }
            } else {
                // 其他情况：进入考试或继续考试
                resp.setStatus(ExamStatusConstants.EXAM_ONGOING);
                resp.setCanRetake(false);
                resp.setActionType(hasRecord ? "resume_exam" : "enter_exam");
                resp.setActionText(hasRecord ? "继续考试" : "进入考试");
            }
        }

        // 3. 排序：进行中 > 未开始 > 已结束（含已完成/强制交卷/可重考）
        // 同组内统一按开始时间倒序（开始越晚越靠前）
        // 注意：必须在分页前排序，否则前端会出现“翻页后才看到目标状态”的错觉。
        Comparator<StudentExamInfoResp> comparator = (a, b) -> {
            int ga = mapStudentSortGroup(a);
            int gb = mapStudentSortGroup(b);
            int diff = Integer.compare(ga, gb);
            if (diff != 0) return diff;

            Date aStart = a.getStartTime();
            Date bStart = b.getStartTime();
            // 同组内：开始时间倒序（越晚越靠前）
            return compareDateDescNullLast(aStart, bStart);
        };
        allRecords.sort(comparator);

        // 4. 状态筛选：req.status 约定 0未开始/1进行中/2已结束
        // 后端 resp.status 可能为 0/1/2/3；这里按“分组”解释。
        Integer filterStatus = req.getStatus();
        List<StudentExamInfoResp> filtered = allRecords;
        if (filterStatus != null) {
            filtered = new ArrayList<>();
            for (StudentExamInfoResp r : allRecords) {
                int g = mapStudentGroup(r);
                if (filterStatus == 0 && g == 0) filtered.add(r);
                else if (filterStatus == 1 && g == 1) filtered.add(r);
                else if (filterStatus == 2 && g == 2) filtered.add(r);
            }
        }

        // 5. 执行内存分页
        Page<StudentExamInfoResp> page = new Query<StudentExamInfoResp>(req.getPageInfo()).getPage();
        int total = filtered.size();
        int currentPage = req.getPage();
        int pageSize = req.getLimit();
        
        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        
        if (fromIndex < total) {
            page.setRecords(filtered.subList(fromIndex, toIndex));
        } else {
            page.setRecords(new ArrayList<>());
        }
        page.setTotal(total);
        
        return new PageUtils(page);
    }

    private static int mapStudentGroup(StudentExamInfoResp resp) {
        // 用于筛选：约定 0未开始/1进行中/2已结束（含已完成/强制交卷/可重考）
        if (resp == null || resp.getStatus() == null) return 0;
        int s = resp.getStatus();
        if (s == ExamStatusConstants.EXAM_NOT_STARTED) return 0;
        if (s == ExamStatusConstants.EXAM_ONGOING) return 1;
        return 2;
    }

    private static int mapStudentSortGroup(StudentExamInfoResp resp) {
        // 用于排序：进行中(0) -> 未开始(1) -> 已结束(2)
        if (resp == null) return 1;
        int filterGroup = mapStudentGroup(resp); // 0未开始/1进行中/2已结束
        if (filterGroup == 1) return 0; // ongoing
        if (filterGroup == 0) return 1; // not_started
        return 2; // ended
    }

    private static int compareDateDescNullLast(Date a, Date b) {
        if (a == null && b == null) return 0;
        if (a == null) return 1;
        if (b == null) return -1;
        return b.compareTo(a);
    }

    @Override
    public boolean hasReleasedOrOngoingExamUsingPaper(Integer examPaperId) {
        if (examPaperId == null) {
            return false;
        }
        
        // 查询所有已发布的考试
        List<ExamInfo> publishedExams = this.lambdaQuery()
                .eq(ExamInfo::getExamPaperId, examPaperId)
                .eq(ExamInfo::getStatus, STATUS_PUBLISHED)
                .list();
        
        if (publishedExams.isEmpty()) {
            return false;
        }
        
        // 动态判断是否有进行中或未结束的考试
        Date now = new Date();
        for (ExamInfo exam : publishedExams) {
            if (exam.getEndTime() == null || now.before(exam.getEndTime())) {
                // 未结束的考试
                return true;
            }
        }
        
        return false;
    }

    @Override
    public boolean hasReleasedOrOngoingExamUsingKemuTypes(Integer kemuTypes) {
        if (kemuTypes == null) {
            return false;
        }
        
        // 查询所有已发布的考试
        List<ExamInfo> publishedExams = this.lambdaQuery()
                .eq(ExamInfo::getKemuTypes, kemuTypes)
                .eq(ExamInfo::getStatus, STATUS_PUBLISHED)
                .list();
        
        if (publishedExams.isEmpty()) {
            return false;
        }
        
        // 动态判断是否有进行中或未结束的考试
        Date now = new Date();
        for (ExamInfo exam : publishedExams) {
            if (exam.getEndTime() == null || now.before(exam.getEndTime())) {
                // 未结束的考试
                return true;
            }
        }
        
        return false;
    }

    @Override
    public boolean hasReleasedOrOngoingExamUsingQuestion(Integer examQuestionId) {
        if (examQuestionId == null) {
            return false;
        }
        
        // 查询包含该试题的所有试卷ID
        List<Integer> paperIds = examPaperTopicService.lambdaQuery()
                .eq(ExamPaperTopic::getExamQuestionId, examQuestionId)
                .list()
                .stream()
                .map(ExamPaperTopic::getExamPaperId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        
        if (paperIds.isEmpty()) {
            return false;
        }
        
        // 查询所有已发布的考试
        List<ExamInfo> publishedExams = this.lambdaQuery()
                .in(ExamInfo::getExamPaperId, paperIds)
                .eq(ExamInfo::getStatus, STATUS_PUBLISHED)
                .list();
        
        if (publishedExams.isEmpty()) {
            return false;
        }
        
        // 动态判断是否有进行中或未结束的考试
        Date now = new Date();
        for (ExamInfo exam : publishedExams) {
            if (exam.getEndTime() == null || now.before(exam.getEndTime())) {
                // 未结束的考试
                return true;
            }
        }
        
        return false;
    }

    /**
     * 【双重保险】发布时检测考试时间冲突
     * 
     * @param excludeExamId 排除的考试ID（当前要发布的考试）
     * @param startTime 考试开始时间
     * @param endTime 考试结束时间
     * @param kemuTypes 科目类型
     * @return 冲突提示信息，无冲突返回null
     */
    private String checkTimeConflictForPublish(Integer excludeExamId, Date startTime, Date endTime, Integer kemuTypes) {
        if (startTime == null || endTime == null) {
            return null;
        }

        // 时间合理性校验
        if (!startTime.before(endTime)) {
            return "考试开始时间必须早于结束时间";
        }

        // 查询同一科目下已发布但未结束的考试（排除当前考试自身）
        // 状态：1-已发布，2-进行中（排除0-未发布和3-已结束）
        List<ExamInfo> existingExams = this.lambdaQuery()
                .eq(ExamInfo::getKemuTypes, kemuTypes)
                .in(ExamInfo::getStatus, STATUS_PUBLISHED, 2) // 只检查已发布(1)和进行中(2)的考试，排除已结束(3)
                .isNotNull(ExamInfo::getStartTime)
                .isNotNull(ExamInfo::getEndTime)
                .ne(ExamInfo::getId, excludeExamId) // 排除当前考试自身
                .list();

        // 检测时间重叠
        for (ExamInfo existingExam : existingExams) {
            Date existingStart = existingExam.getStartTime();
            Date existingEnd = existingExam.getEndTime();

            // 时间重叠判断：
            // 新考试开始时间在已有考试时间范围内，或
            // 新考试结束时间在已有考试时间范围内，或
            // 新考试完全包含已有考试
            boolean isOverlap = (startTime.before(existingEnd) && endTime.after(existingStart));

            if (isOverlap) {
                String conflictInfo = String.format(
                    "考试时间与已发布的考试冲突：\n考试名称：%s\n冲突时间：%s ~ %s",
                    existingExam.getExamName(),
                    cn.hutool.core.date.DateUtil.formatDateTime(existingStart),
                    cn.hutool.core.date.DateUtil.formatDateTime(existingEnd)
                );
                log.warn("[时间冲突检测] 检测到冲突 - 新考试: {} ~ {}, 已有考试: {} ({} ~ {})",
                        cn.hutool.core.date.DateUtil.formatDateTime(startTime),
                        cn.hutool.core.date.DateUtil.formatDateTime(endTime),
                        existingExam.getExamName(),
                        cn.hutool.core.date.DateUtil.formatDateTime(existingStart),
                        cn.hutool.core.date.DateUtil.formatDateTime(existingEnd));
                return conflictInfo;
            }
        }

        log.info("[时间冲突检测] 通过 - 科目: {}, 时间: {} ~ {}, 检查了 {} 个未结束的考试",
                kemuTypes,
                cn.hutool.core.date.DateUtil.formatDateTime(startTime),
                cn.hutool.core.date.DateUtil.formatDateTime(endTime),
                existingExams.size());
        return null;
    }
}
