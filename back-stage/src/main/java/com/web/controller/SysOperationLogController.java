package com.web.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.web.domain.SysOperationLog;
import com.web.domain.Teachers;
import com.web.domain.Req.SysOperationLogReq;
import com.web.domain.dto.SysOperationLogExcelDto;
import com.web.service.SysOperationLogService;
import com.web.service.TeachersService;
import com.web.utils.PageUtils;
import com.web.utils.Result;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理端操作日志：记录管理员与教师在后台的关键操作；教师仅可查看本人相关日志。
 */
@RestController
@RequestMapping("/operationLog")
public class SysOperationLogController {

    @Resource
    private SysOperationLogService sysOperationLogService;
    @Resource
    private TeachersService teachersService;
    @Resource
    private com.web.task.OperationLogCleanupTask operationLogCleanupTask;

    private static boolean isManager(HttpServletRequest request) {
        String tableName = (String) request.getSession().getAttribute("tableName");
        String role = (String) request.getSession().getAttribute("role");
        return "managers".equals(tableName) || "管理员".equals(role);
    }

    private static boolean isTeacher(HttpServletRequest request) {
        String tableName = (String) request.getSession().getAttribute("tableName");
        String role = (String) request.getSession().getAttribute("role");
        return "teachers".equals(tableName) || "教师".equals(role);
    }

    private static boolean canAccess(HttpServletRequest request) {
        return isManager(request) || isTeacher(request);
    }

    /**
     * 教师端强制数据范围；管理员可按筛选条件查询。
     */
    private void applyAccessScope(SysOperationLogReq req, HttpServletRequest request) {
        req.setScopeTeacherDisplayName(null);
        if (!isTeacher(request)) {
            return;
        }
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        if (userId == null) {
            return;
        }
        Teachers teacher = teachersService.getById(userId);
        String display = teacher != null && StrUtil.isNotBlank(teacher.getRealName())
                ? teacher.getRealName().trim()
                : null;
        if (StrUtil.isBlank(display)) {
            Object userName = request.getSession().getAttribute("userName");
            display = userName != null ? String.valueOf(userName).trim() : null;
        }
        req.setScopeTeacherDisplayName(StrUtil.isBlank(display) ? null : display);
    }

    @PostMapping("/page")
    public Result page(@RequestBody SysOperationLogReq req, HttpServletRequest request) {
        if (!canAccess(request)) {
            return Result.error(403, "无权访问操作日志");
        }
        applyAccessScope(req, request);
        PageUtils page = sysOperationLogService.selectPage(req);
        return Result.success(page);
    }

    @GetMapping("/options")
    public Result<Map<String, Object>> options(
            @RequestParam(required = false) String beginTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String operationModule,
            @RequestParam(required = false) String actionType,
            HttpServletRequest request) {
        if (!canAccess(request)) {
            return Result.error(403, "无权访问操作日志");
        }
        SysOperationLogReq scopeReq = new SysOperationLogReq();
        scopeReq.setBeginTime(StrUtil.isBlank(beginTime) ? null : beginTime.trim());
        scopeReq.setEndTime(StrUtil.isBlank(endTime) ? null : endTime.trim());
        scopeReq.setOperationModule(StrUtil.isBlank(operationModule) ? null : operationModule.trim());
        scopeReq.setActionType(StrUtil.isBlank(actionType) ? null : actionType.trim());
        applyAccessScope(scopeReq, request);
        List<String> admins = sysOperationLogService.listDistinctAdminNames(scopeReq);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("adminNames", admins);
        // 与管理员侧栏菜单顺序一致，便于筛选
        map.put("modules", List.of(
                "公告信息",
                "用户管理",
                "教师管理",
                "科目管理",
                "试题管理",
                "试卷管理",
                "考试管理",
                "考试记录",
                "阅卷管理",
                "错题本",
                "成绩与统计"));
        map.put("actionTypes", List.of("新增", "修改", "删除", "发布", "导入", "启用", "禁用", "阅卷", "导出"));
        return Result.success(map);
    }

    @PostMapping("/export")
    public void export(@RequestBody SysOperationLogReq req, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        if (!canAccess(request)) {
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"msg\":\"无权访问操作日志\"}");
            return;
        }
        applyAccessScope(req, request);
        req.setPage(1);
        req.setLimit(5000);
        List<SysOperationLog> rows = sysOperationLogService.listForExport(req);
        List<SysOperationLogExcelDto> excelRows = rows.stream()
                .map(SysOperationLogController::toExcelRow)
                .collect(Collectors.toList());

        String fileName = URLEncoder.encode("操作日志_" + System.currentTimeMillis() + ".xlsx", StandardCharsets.UTF_8)
                .replace("+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-disposition", "attachment;filename=" + fileName);

        EasyExcel.write(response.getOutputStream(), SysOperationLogExcelDto.class)
                .sheet("操作日志")
                .doWrite(excelRows);
        response.getOutputStream().flush();
    }

    private static SysOperationLogExcelDto toExcelRow(SysOperationLog r) {
        SysOperationLogExcelDto dto = new SysOperationLogExcelDto();
        dto.setOperationTime(r.getOperationTime());
        dto.setAdminName(r.getAdminName());
        dto.setOperationModule(r.getOperationModule());
        dto.setActionType(r.getActionType());
        dto.setContent(r.getContent());
        dto.setIp(r.getIp());
        dto.setOperationResult(r.getSuccess() != null && r.getSuccess() == 1 ? "成功" : "失败");
        return dto;
    }

    /**
     * 获取日志清理配置信息
     */
    @GetMapping("/cleanup/config")
    public Result getCleanupConfig(HttpServletRequest request) {
        if (!isManager(request)) {
            return Result.error(403, "只有管理员可以查看日志清理配置");
        }
        
        String configInfo = operationLogCleanupTask.getConfigInfo();
        return Result.success(configInfo);
    }

    /**
     * 手动触发日志清理
     */
    @PostMapping("/cleanup/manual")
    public Result manualCleanup(HttpServletRequest request) {
        if (!isManager(request)) {
            return Result.error(403, "只有管理员可以手动清理日志");
        }
        
        try {
            operationLogCleanupTask.manualCleanup();
            return Result.success("日志清理任务已触发，请查看后台日志了解清理结果");
        } catch (Exception e) {
            return Result.error("触发日志清理失败：" + e.getMessage());
        }
    }
}
