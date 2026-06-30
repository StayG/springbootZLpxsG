package com.web.controller;

import com.web.domain.Req.GradesStatisticsReq;
import com.web.domain.Resp.GradesStatisticsVO;
import com.web.domain.Teachers;
import com.web.service.ExamRecordService;
import com.web.service.RedisCacheService;
import com.web.service.TeachersService;
import com.web.domain.Resp.ExamGradeTableRow;
import com.web.utils.OperationLogUtil;
import com.web.utils.Result;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 成绩与统计（独立路径，避免与静态资源 /** 映射冲突）
 */
@Slf4j
@RestController
@RequestMapping("/gradesStatistics")
public class GradesStatisticsController {

    @Resource
    private ExamRecordService examRecordService;
    @Resource
    private TeachersService teachersService;
    @Resource
    private OperationLogUtil operationLogUtil;
    @Resource
    private RedisCacheService redisCacheService;

    @PostMapping
    public Result overview(@RequestBody GradesStatisticsReq req, HttpServletRequest request) {
        applyTeacherScope(req, request);
        
        // 生成缓存 key
        String cacheKey = buildCacheKey(req);
        
        // 先从缓存获取
        Object cachedData = redisCacheService.getStatisticsCache(cacheKey);
        if (cachedData != null) {
            log.info("✅ 统计数据缓存命中：{}", cacheKey);
            return Result.success(cachedData);
        }
        
        // 缓存未命中，查询数据库
        log.info("❌ 统计数据缓存未命中，查询数据库：{}", cacheKey);
        GradesStatisticsVO statistics = examRecordService.gradesStatistics(req);
        
        // 存入缓存（5分钟）
        redisCacheService.setStatisticsCache(cacheKey, statistics);
        
        return Result.success(statistics);
    }

    /**
     * 导出用：当前筛选下全部「考试成绩列表」行（与页面统计口径一致）
     */
    @PostMapping("/exportTable")
    public Result<List<ExamGradeTableRow>> exportTable(@RequestBody GradesStatisticsReq req, HttpServletRequest request) {
        applyTeacherScope(req, request);
        List<ExamGradeTableRow> data = examRecordService.gradesStatisticsExportTable(req);
        
        // 记录操作日志
        String filterInfo = buildFilterInfo(req);
        operationLogUtil.logSuccess(request, "成绩与统计", "导出", "导出成绩统计数据" + filterInfo + "，共 " + (data != null ? data.size() : 0) + " 条");
        
        return Result.success(data);
    }
    
    /**
     * 构建筛选条件信息用于日志
     */
    private String buildFilterInfo(GradesStatisticsReq req) {
        if (req == null) {
            return "";
        }
        StringBuilder info = new StringBuilder();
        if (req.getExamInfoId() != null) {
            info.append("（考试ID:").append(req.getExamInfoId()).append("）");
        }
        if (req.getKemuTypes() != null) {
            info.append("（科目:").append(req.getKemuTypes()).append("）");
        }
        return info.toString();
    }
    
    /**
     * 构建缓存 Key
     */
    private String buildCacheKey(GradesStatisticsReq req) {
        if (req == null) {
            return "default";
        }
        StringBuilder key = new StringBuilder();
        key.append(req.getExamInfoId() != null ? req.getExamInfoId() : "all");
        key.append(":");
        key.append(req.getKemuTypes() != null ? req.getKemuTypes() : "all");
        key.append(":");
        key.append(req.getDateStart() != null ? req.getDateStart() : "all");
        key.append(":");
        key.append(req.getDateEnd() != null ? req.getDateEnd() : "all");
        key.append(":");
        key.append(req.getTrendRange() != null ? req.getTrendRange() : "all");
        key.append(":");
        key.append(req.getPage() != null ? req.getPage() : "1");
        key.append(":");
        key.append(req.getLimit() != null ? req.getLimit() : "10");
        return key.toString();
    }

    private void applyTeacherScope(GradesStatisticsReq req, HttpServletRequest request) {
        String role = (String) request.getSession().getAttribute("role");
        String tableName = (String) request.getSession().getAttribute("tableName");
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        if (("teachers".equals(tableName) || "教师".equals(role)) && userId != null) {
            Teachers teacher = teachersService.getById(userId);
            if (teacher != null) {
                req.setKemuTypes(teacher.getKemuTypes());
            }
        }
    }
    
    /**
     * 测试统计数据缓存功能
     * 
     * @param kemuTypes 科目类型（可选）
     * @return 测试结果
     */
    @GetMapping("/testCache")
    public Result testCache(@RequestParam(required = false) Integer kemuTypes) {
        log.info("\n========== 开始测试统计数据缓存 ==========");
        
        try {
            // 测试1：统计数据查询（无缓存）
            GradesStatisticsReq req1 = new GradesStatisticsReq();
            req1.setPage(1);
            req1.setLimit(10);
            if (kemuTypes != null) {
                req1.setKemuTypes(kemuTypes);
            }
            
            long start1 = System.currentTimeMillis();
            GradesStatisticsVO stats1 = examRecordService.gradesStatistics(req1);
            long time1 = System.currentTimeMillis() - start1;
            log.info("第1次查询（无缓存）：耗时 {}ms", time1);
            
            // 测试2：统计数据查询（缓存命中）
            GradesStatisticsReq req2 = new GradesStatisticsReq();
            req2.setPage(1);
            req2.setLimit(10);
            if (kemuTypes != null) {
                req2.setKemuTypes(kemuTypes);
            }
            
            long start2 = System.currentTimeMillis();
            GradesStatisticsVO stats2 = examRecordService.gradesStatistics(req2);
            long time2 = System.currentTimeMillis() - start2;
            log.info("第2次查询（缓存命中）：耗时 {}ms", time2);
            
            // 计算性能提升
            double improvement = time2 > 0 ? (double) time1 / time2 : 0;
            
            log.info("========== 统计数据缓存测试完成 ==========\n");
            
            // 返回测试结果
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("统计数据第1次查询耗时(ms)", time1);
            result.put("统计数据第2次查询耗时(ms)", time2);
            result.put("统计数据性能提升", String.format("%.1f倍", improvement));
            result.put("测试说明", "第1次查询从数据库获取，第2次查询从Redis缓存获取");
            
            // 添加统计数据摘要
            if (stats1 != null && stats1.getSummary() != null) {
                result.put("参与人数", stats1.getSummary().getParticipants() != null ? 
                    stats1.getSummary().getParticipants().getValue() : "0");
                result.put("平均分", stats1.getSummary().getAvgScore() != null ? 
                    stats1.getSummary().getAvgScore().getValue() : "0");
            }
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("测试统计数据缓存失败", e);
            return Result.error("测试失败：" + e.getMessage());
        }
    }
}
