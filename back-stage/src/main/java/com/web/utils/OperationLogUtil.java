package com.web.utils;

import cn.hutool.core.util.StrUtil;
import com.web.domain.Managers;
import com.web.domain.SysOperationLog;
import com.web.domain.Teachers;
import com.web.service.ManagersService;
import com.web.service.SysOperationLogService;
import com.web.service.TeachersService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Date;

/**
 * 操作日志工具类
 * 用于记录管理员和教师的所有关键操作
 */
@Slf4j
@Component
public class OperationLogUtil {

    @Resource
    private SysOperationLogService sysOperationLogService;
    
    @Resource
    private ManagersService managersService;
    
    @Resource
    private TeachersService teachersService;

    /**
     * 记录操作日志
     *
     * @param request         HTTP请求对象
     * @param operationModule 操作模块（如：公告信息、用户管理、教师管理等）
     * @param actionType      操作类型（如：新增、修改、删除、导入、导出、启用、禁用、阅卷等）
     * @param content         操作内容描述
     * @param success         操作是否成功（1-成功，0-失败）
     */
    public void log(HttpServletRequest request, String operationModule, String actionType, String content, Integer success) {
        try {
            // 从Session获取当前登录用户信息
            String role = (String) request.getSession().getAttribute("role");
            String tableName = (String) request.getSession().getAttribute("tableName");
            String userName = (String) request.getSession().getAttribute("userName");
            Integer userId = (Integer) request.getSession().getAttribute("userId");

            // 只记录管理员和教师的操作
            if (!"managers".equals(tableName) && !"teachers".equals(tableName) &&
                !"管理员".equals(role) && !"教师".equals(role)) {
                return;
            }

            // 获取操作人真实姓名
            String adminName = getRealName(tableName, role, userId, userName);

            // 获取IP地址
            String ip = getIpAddress(request);

            // 创建操作日志对象
            SysOperationLog operationLog = new SysOperationLog();
            operationLog.setOperationTime(new Date());
            operationLog.setAdminName(adminName);
            operationLog.setOperationModule(operationModule);
            operationLog.setActionType(actionType);
            operationLog.setContent(content);
            operationLog.setIp(ip);
            operationLog.setSuccess(success != null ? success : 1);

            // 异步保存日志，避免影响主业务
            sysOperationLogService.save(operationLog);

            log.info("操作日志记录成功：{} - {} - {} - {}", adminName, operationModule, actionType, content);
        } catch (Exception e) {
            // 日志记录失败不影响主业务
            log.error("操作日志记录失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 记录成功的操作日志
     */
    public void logSuccess(HttpServletRequest request, String operationModule, String actionType, String content) {
        log(request, operationModule, actionType, content, 1);
    }

    /**
     * 记录失败的操作日志
     */
    public void logFailure(HttpServletRequest request, String operationModule, String actionType, String content) {
        log(request, operationModule, actionType, content, 0);
    }

    /**
     * 获取操作人真实姓名
     * 
     * @param tableName 表名（managers/teachers）
     * @param role 角色名（管理员/教师）
     * @param userId 用户ID
     * @param userName 用户名（备用）
     * @return 真实姓名
     */
    private String getRealName(String tableName, String role, Integer userId, String userName) {
        try {
            // 管理员
            if ("managers".equals(tableName) || "管理员".equals(role)) {
                if (userId != null) {
                    Managers manager = managersService.getById(userId);
                    if (manager != null && StrUtil.isNotBlank(manager.getRealName())) {
                        return manager.getRealName();
                    }
                }
            }
            // 教师
            else if ("teachers".equals(tableName) || "教师".equals(role)) {
                if (userId != null) {
                    Teachers teacher = teachersService.getById(userId);
                    if (teacher != null && StrUtil.isNotBlank(teacher.getRealName())) {
                        return teacher.getRealName();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取真实姓名失败，将使用用户名：{}", e.getMessage());
        }
        
        // 如果无法获取真实姓名，使用用户名
        if (StrUtil.isNotBlank(userName)) {
            return userName;
        }
        
        return "未知用户";
    }

    /**
     * 获取客户端IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况，取第一个
        if (StrUtil.isNotBlank(ip) && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
