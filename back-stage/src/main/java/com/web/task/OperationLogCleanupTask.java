package com.web.task;

import com.web.service.SysOperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 操作日志定期清理任务
 * 
 * 功能：
 * 1. 定期清理超过指定天数的操作日志
 * 2. 默认保留90天的日志
 * 3. 每天凌晨2点执行清理任务
 * 
 * 配置：
 * - operation-log.retention-days: 日志保留天数（默认90天）
 * - operation-log.cleanup.enabled: 是否启用自动清理（默认true）
 */
@Slf4j
@Component
public class OperationLogCleanupTask {

    @Resource
    private SysOperationLogService sysOperationLogService;

    /**
     * 日志保留天数，默认90天
     */
    @Value("${operation-log.retention-days:90}")
    private int retentionDays;

    /**
     * 是否启用自动清理，默认启用
     */
    @Value("${operation-log.cleanup.enabled:true}")
    private boolean cleanupEnabled;

    /**
     * 定期清理操作日志
     * 
     * 执行时间：每天凌晨2点
     * Cron表达式：0 0 2 * * ?
     * - 秒：0
     * - 分：0
     * - 时：2（凌晨2点）
     * - 日：*（每天）
     * - 月：*（每月）
     * - 周：?（不指定）
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldLogs() {
        if (!cleanupEnabled) {
            log.info("操作日志自动清理功能已禁用");
            return;
        }

        log.info("\n========== [操作日志清理] 开始 ==========");
        log.info("清理策略：删除 {} 天前的操作日志", retentionDays);

        try {
            // 计算截止日期
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
            String cutoffDateStr = cutoffDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            log.info("截止日期：{}", cutoffDateStr);

            // 查询要删除的日志数量
            long countToDelete = sysOperationLogService.lambdaQuery()
                    .lt(com.web.domain.SysOperationLog::getOperationTime, 
                        java.sql.Timestamp.valueOf(cutoffDate))
                    .count();

            if (countToDelete == 0) {
                log.info("没有需要清理的操作日志");
                log.info("========== [操作日志清理] 结束 ==========\n");
                return;
            }

            log.info("发现 {} 条需要清理的操作日志", countToDelete);

            // 执行删除
            boolean success = sysOperationLogService.lambdaUpdate()
                    .lt(com.web.domain.SysOperationLog::getOperationTime, 
                        java.sql.Timestamp.valueOf(cutoffDate))
                    .remove();

            if (success) {
                log.info("✅ 成功清理 {} 条操作日志", countToDelete);
                
                // 查询剩余日志数量
                long remainingCount = sysOperationLogService.count();
                log.info("剩余操作日志数量：{} 条", remainingCount);
            } else {
                log.error("❌ 清理操作日志失败");
            }

        } catch (Exception e) {
            log.error("❌ 清理操作日志时发生异常", e);
        }

        log.info("========== [操作日志清理] 结束 ==========\n");
    }

    /**
     * 手动触发清理（用于测试）
     * 
     * 可以通过Controller调用此方法进行手动清理
     */
    public void manualCleanup() {
        log.info("手动触发操作日志清理");
        cleanupOldLogs();
    }

    /**
     * 获取当前配置信息
     */
    public String getConfigInfo() {
        return String.format(
            "操作日志清理配置：\n" +
            "- 自动清理：%s\n" +
            "- 保留天数：%d 天\n" +
            "- 执行时间：每天凌晨2点\n" +
            "- 当前日志数量：%d 条",
            cleanupEnabled ? "已启用" : "已禁用",
            retentionDays,
            sysOperationLogService.count()
        );
    }
}
