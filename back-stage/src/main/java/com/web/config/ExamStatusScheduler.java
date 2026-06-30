package com.web.config;

import com.web.domain.ExamInfo;
import com.web.domain.ExamRecord;
import com.web.service.ExamInfoService;
import com.web.service.ExamRecordService;
import com.web.utils.ExamStatusConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 考试状态自动流转定时任务
 * <p>
 * 功能：
 * 1. 自动更新 exam_info 表的状态（已发布 → 已结束）
 * 2. 强制交卷：考试结束后，将所有"考试中"的记录自动标记为"强制交卷"
 * </p>
 */
@Slf4j
@Component
public class ExamStatusScheduler {

    @Resource
    private ExamInfoService examInfoService;

    @Resource
    private ExamRecordService examRecordService;

    /**
     * 定时更新考试状态和处理强制交卷
     * 每10秒执行一次（cron 表达式：秒 分 时 日 月 周）
     * 
     * 状态流转：
     * 1. 已发布(1) → 进行中(2)：当前时间 >= 开始时间 && 当前时间 < 结束时间
     * 2. 已发布(1) → 已结束(3)：当前时间 >= 结束时间（跳过进行中状态）
     * 3. 进行中(2) → 已结束(3)：当前时间 >= 结束时间
     */
    @Scheduled(cron = "0/10 * * * * ?")  // 每10秒执行一次
    public void updateExamStatusAndForceSubmit() {
        log.debug("========== [考试状态更新定时任务] 开始执行 ==========");

        try {
            Date now = new Date();

            // 1. 查询所有已发布和进行中的考试（status=1 或 2）
            List<ExamInfo> activeExams = examInfoService.lambdaQuery()
                    .in(ExamInfo::getStatus, 1, 2)  // 查询已发布和进行中的考试
                    .isNotNull(ExamInfo::getStartTime)
                    .isNotNull(ExamInfo::getEndTime)
                    .list();

            if (activeExams == null || activeExams.isEmpty()) {
                log.debug("[考试状态更新定时任务] 没有需要处理的考试，跳过");
                return;
            }

            int toOngoingCount = 0;    // 已发布 → 进行中
            int toFinishedCount = 0;   // 已发布/进行中 → 已结束
            int forceSubmitCount = 0;  // 强制交卷数量

            // 2. 遍历检查考试状态
            for (ExamInfo exam : activeExams) {
                Integer currentStatus = exam.getStatus();
                
                // 2.1 判断考试是否已结束
                if (now.after(exam.getEndTime())) {
                    // 更新为"已结束"(3)
                    boolean statusUpdated = examInfoService.lambdaUpdate()
                            .eq(ExamInfo::getId, exam.getId())
                            .in(ExamInfo::getStatus, 1, 2)  // 从"已发布"或"进行中"
                            .set(ExamInfo::getStatus, 3)     // 更新为"已结束"
                            .update();
                    
                    if (statusUpdated) {
                        toFinishedCount++;
                        log.info("[考试状态更新定时任务] ✅ 考试 [{}] 状态已更新：{}({}) → 已结束(3)", 
                                exam.getExamName(),
                                currentStatus == 1 ? "已发布" : "进行中",
                                currentStatus);
                    }
                    
                    // 强制交卷：将该考试下所有"考试中"的记录强制交卷
                    int updated = examRecordService.lambdaUpdate()
                            .eq(ExamRecord::getExamInfoId, exam.getId())
                            .eq(ExamRecord::getStatus, ExamStatusConstants.RECORD_IN_PROGRESS)
                            .set(ExamRecord::getStatus, ExamStatusConstants.RECORD_FORCED_SUBMIT)
                            .set(ExamRecord::getEndTime, now)
                            .update() ? 1 : 0;
                    
                    if (updated > 0) {
                        forceSubmitCount += updated;
                        log.info("[考试状态更新定时任务] ✅ 考试 [{}] 强制交卷 {} 条记录", 
                                exam.getExamName(), updated);
                    }
                }
                // 2.2 判断考试是否正在进行中
                else if (currentStatus == 1 && !now.before(exam.getStartTime())) {
                    // 当前时间 >= 开始时间 && 当前时间 < 结束时间
                    // 更新为"进行中"(2)
                    boolean statusUpdated = examInfoService.lambdaUpdate()
                            .eq(ExamInfo::getId, exam.getId())
                            .eq(ExamInfo::getStatus, 1)      // 从"已发布"
                            .set(ExamInfo::getStatus, 2)     // 更新为"进行中"
                            .update();
                    
                    if (statusUpdated) {
                        toOngoingCount++;
                        log.info("[考试状态更新定时任务] ✅ 考试 [{}] 状态已更新：已发布(1) → 进行中(2)", 
                                exam.getExamName());
                    }
                }
            }

            if (toOngoingCount > 0 || toFinishedCount > 0 || forceSubmitCount > 0) {
                log.info("[考试状态更新定时任务] 执行完成 - 进行中 {} 个，已结束 {} 个，强制交卷 {} 条记录", 
                        toOngoingCount, toFinishedCount, forceSubmitCount);
            }

        } catch (Exception e) {
            log.error("[考试状态更新定时任务] ❌ 执行异常", e);
        }

        log.debug("========== [考试状态更新定时任务] 执行结束 ==========");
    }
}
