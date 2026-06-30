package com.web.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.web.domain.ExamInfo;
import com.web.domain.ExamPaper;
import com.web.domain.ExamRecord;
import com.web.domain.Req.ExamInfoReq;
import com.web.domain.Teachers;
import com.web.service.ExamInfoService;
import com.web.service.ExamPaperService;
import com.web.service.ExamRecordService;
import com.web.service.TeachersService;
import com.web.utils.PageUtils;
import com.web.utils.Result;
import com.web.utils.ExamStatusConstants;
import com.web.utils.OperationLogUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import com.web.domain.ExamDetails;
import com.web.domain.ExamPaperTopic;
import com.web.domain.ExamQuestion;
import com.web.service.ExamDetailsService;
import com.web.service.ExamPaperTopicService;
import com.web.service.ExamQuestionService;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 考试信息 前端控制器
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/examInfo")
public class ExamInfoController {

    @Resource
    private ExamInfoService examInfoService;

    @Resource
    private TeachersService teachersService;

    @Resource
    private ExamPaperService examPaperService;

    @Resource
    private ExamRecordService examRecordService;

    @Resource
    private ExamPaperTopicService examPaperTopicService;

    @Resource
    private ExamDetailsService examDetailsService;

    @Resource
    private ExamQuestionService examQuestionService;
    
    @Resource
    private OperationLogUtil operationLogUtil;
    
    @Resource
    private ExamPaperTopicController examPaperTopicController;
    
    @Resource
    private com.web.service.RedisCacheService redisCacheService;
    
    @Resource
    private com.web.utils.RedisUtil redisUtil;

    /**
     * 后台分页查询（支持数据隔离）
     *
     * @return
     */
    @PostMapping("/page")
    public Result page(@RequestBody ExamInfoReq req, HttpServletRequest request) {
        // 从 Session 获取当前登录用户信息
        String role = (String) request.getSession().getAttribute("role"); // 中文角色名
        String tableName = (String) request.getSession().getAttribute("tableName"); // 英文表名
        Integer userId = (Integer) request.getSession().getAttribute("userId");

        // 注入角色信息
        req.setRole(role);

        // 如果是教师角色，注入教师 ID 实现数据隔离
        if (("teachers".equals(tableName) || "教师".equals(role)) && userId != null) {
            req.setTeacherId(userId);
        }

        // 生成缓存 key
        String cacheKey = req.getPage() + ":" + req.getLimit() + ":" + 
                         (req.getKemuTypes() != null ? req.getKemuTypes() : "all") + ":" +
                         (req.getTeacherId() != null ? req.getTeacherId() : "all") + ":" +
                         (req.getStatus() != null ? req.getStatus() : "all");
        
        // 先从缓存获取
        Object cachedData = redisCacheService.getExamListCache(cacheKey);
        if (cachedData != null) {
            log.info("✅ 考试列表缓存命中：{}", cacheKey);
            return Result.success(cachedData);
        }
        
        // 缓存未命中，查询数据库
        log.info("❌ 考试列表缓存未命中，查询数据库：{}", cacheKey);
        PageUtils page = examInfoService.selectPage(req);
        
        // 存入缓存（5分钟）
        redisCacheService.setExamListCache(cacheKey, page);

        return Result.success(page);
    }

    /**
     * 新增（自动关联教师信息和科目）
     *
     * @param examInfo
     * @return
     */
    @PostMapping("/save")
    public Result save(@RequestBody ExamInfo examInfo, HttpServletRequest request) {
        // 从 Session 获取当前登录用户信息
        String role = (String) request.getSession().getAttribute("role");
        String tableName = (String) request.getSession().getAttribute("tableName");
        Integer userId = (Integer) request.getSession().getAttribute("userId");

        System.out.println("\n========== [考试保存] 开始 ==========");
        System.out.println("[考试保存] 1. Session信息 - role: " + role + ", tableName: " + tableName + ", userId: " + userId);
        System.out.println("[考试保存] 2. 接收到的前端数据 - examName: " + examInfo.getExamName());
        System.out.println(
                "[考试保存] 3. 前端数据中的 kemuTypes: " + examInfo.getKemuTypes() + ", teacherId: " + examInfo.getTeacherId());

        if (examInfo.getAllowRetake() != null && examInfo.getAllowRetake() == 1) {
            if (examInfo.getMaxRetakeCount() == null || examInfo.getMaxRetakeCount() < 1) {
                return Result.error("开启允许重考后，最多重考次数至少为 1");
            }
        } else {
            examInfo.setAllowRetake(0);
            examInfo.setMaxRetakeCount(0);
        }

        // 如果是教师角色，强制设置教师ID和科目为教师的任教科目
        if (("teachers".equals(tableName) || "教师".equals(role)) && userId != null) {
            Teachers teacher = teachersService.getById(userId);
            if (teacher != null) {
                examInfo.setTeacherId(userId);
                examInfo.setKemuTypes(teacher.getKemuTypes());
                System.out.println(
                        "[考试保存] 4. 教师新增考试 - 强制设置teacherId: " + userId + ", kemuTypes: " + teacher.getKemuTypes());
            } else {
                System.out.println("[考试保存] ❌ 警告：未找到教师信息，userId=" + userId);
                return Result.error("教师信息不存在，无法创建考试");
            }
        } else if ("managers".equals(tableName) || "管理员".equals(role)) {
            // 管理员新增考试：teacherId 设为 null（表示系统/管理员创建）
            examInfo.setTeacherId(null);
            System.out.println("[考试保存] 4. 管理员新增考试 - teacherId 设为 null（系统创建）");
            System.out.println("[考试保存] 4.1. 保持前端传入的 kemuTypes: " + examInfo.getKemuTypes());
        } else {
            // 其他角色：teacherId 设为 null
            examInfo.setTeacherId(null);
            System.out.println("[考试保存] 4. 其他角色新增考试 - teacherId 设为 null");
        }

        // 校验关联的试卷是否存在且属于该科目
        if (examInfo.getExamPaperId() != null) {
            ExamPaper examPaper = examPaperService.getById(examInfo.getExamPaperId());
            if (examPaper == null) {
                return Result.error("关联的试卷不存在");
            }
            if (!examPaper.getKemuTypes().equals(examInfo.getKemuTypes())) {
                return Result.error("关联的试卷科目与考试科目不一致");
            }
            System.out.println("[考试保存] 5. 关联试卷验证通过 - 试卷名称: " + examPaper.getExamPaperName() + ", 总分: "
                    + examPaper.getExamPaperMyscore());
        }

        // 检测考试时间冲突
        if (examInfo.getStartTime() != null && examInfo.getEndTime() != null) {
            String conflictMessage = checkTimeConflict(null, examInfo.getStartTime(), examInfo.getEndTime(), examInfo.getKemuTypes());
            if (conflictMessage != null) {
                System.out.println("[考试保存] ❌ 时间冲突检测失败: " + conflictMessage);
                return Result.error(conflictMessage);
            }
            System.out.println("[考试保存] 5.5. 时间冲突检测通过");
        }

        // 设置默认状态为"未开始"
        if (examInfo.getStatus() == null) {
            examInfo.setStatus(0);
        }

        // 设置创建时间
        examInfo.setCreateTime(new Date());

        System.out.println("[考试保存] 6. 最终保存的数据 - kemuTypes: " + examInfo.getKemuTypes() + ", teacherId: "
                + examInfo.getTeacherId());
        System.out.println("========== [考试保存] 结束 ==========\n");

        examInfoService.save(examInfo);
        
        // 清除考试列表缓存
        redisCacheService.clearExamListCache();
        log.info("🗑️  清除考试列表缓存（新增考试）");
        
        // 记录操作日志
        operationLogUtil.logSuccess(request, "考试管理", "新增", "新增考试：" + examInfo.getExamName());
        
        return Result.success();
    }

    /**
     * 修改（保持教师科目不变）
     *
     * @param examInfo
     * @return
     */
    @PostMapping("/update")
    public Result update(@RequestBody ExamInfo examInfo, HttpServletRequest request) {
        // 从 Session 获取当前登录用户信息
        String role = (String) request.getSession().getAttribute("role");
        String tableName = (String) request.getSession().getAttribute("tableName");
        Integer userId = (Integer) request.getSession().getAttribute("userId");

        System.out.println("\n========== [考试更新] 开始 ==========");
        System.out.println("[考试更新] 1. Session信息 - role: " + role + ", tableName: " + tableName + ", userId: " + userId);
        System.out.println(
                "[考试更新] 2. 接收到的前端数据 - examInfo.id: " + examInfo.getId() + ", examName: " + examInfo.getExamName());
        System.out.println("[考试更新] 3. 前端传入的 kemuTypes: " + examInfo.getKemuTypes());

        if (examInfo.getAllowRetake() != null && examInfo.getAllowRetake() == 1) {
            if (examInfo.getMaxRetakeCount() == null || examInfo.getMaxRetakeCount() < 1) {
                return Result.error("开启允许重考后，最多重考次数至少为 1");
            }
        } else {
            examInfo.setAllowRetake(0);
            examInfo.setMaxRetakeCount(0);
        }

        // 查询数据库中原始的考试信息（用于对比）
        ExamInfo originalExam = examInfoService.getById(examInfo.getId());
        if (originalExam == null) {
            return Result.error("考试不存在");
        }
        System.out.println("[考试更新] 3.5. 数据库中的原始 kemuTypes: " + originalExam.getKemuTypes());

        // 如果是教师角色，只能修改自己创建的考试，且强制保持科目不变
        if (("teachers".equals(tableName) || "教师".equals(role)) && userId != null) {
            // 权限校验：只能修改自己创建的考试
            if (!originalExam.getTeacherId().equals(userId)) {
                return Result.error("无权操作：只能修改自己创建的考试");
            }

            Teachers teacher = teachersService.getById(userId);
            if (teacher != null) {
                Integer frontendKemuTypes = examInfo.getKemuTypes();
                Integer teacherKemuTypes = teacher.getKemuTypes();

                // 强制设置科目为教师的任教科目，无论前端传入什么值
                examInfo.setKemuTypes(teacherKemuTypes);
                examInfo.setTeacherId(userId);

                // 检测是否有篡改行为
                if (frontendKemuTypes != null && !frontendKemuTypes.equals(teacherKemuTypes)) {
                    System.out.println(
                            "[考试更新] ⚠️  检测到前端尝试篡改科目: " + frontendKemuTypes + " -> " + teacherKemuTypes + " (已强制修正)");
                }

                System.out
                        .println("[考试更新] 4. 教师更新考试 - 强制覆盖kemuTypes: " + frontendKemuTypes + " -> " + teacherKemuTypes);
                System.out.println("[考试更新] 4.1. 教师姓名: " + teacher.getRealName() + ", 任教科目ID: " + teacherKemuTypes);
            } else {
                System.out.println("[考试更新] ❌ 警告：未找到教师信息，userId=" + userId);
                return Result.error("教师信息不存在，无法更新考试");
            }
        } else if ("managers".equals(tableName) || "管理员".equals(role)) {
            System.out.println("[考试更新] 4. 管理员更新考试 - 允许修改任意科目");
            System.out.println("[考试更新] 4.1. 保持前端传入的 kemuTypes: " + examInfo.getKemuTypes());
        } else {
            System.out.println("[考试更新] 4. 未知角色 - 保持前端传入的 kemuTypes: " + examInfo.getKemuTypes());
        }

        // 校验关联的试卷是否存在且属于该科目
        if (examInfo.getExamPaperId() != null) {
            ExamPaper examPaper = examPaperService.getById(examInfo.getExamPaperId());
            if (examPaper == null) {
                return Result.error("关联的试卷不存在");
            }
            if (!examPaper.getKemuTypes().equals(examInfo.getKemuTypes())) {
                return Result.error("关联的试卷科目与考试科目不一致");
            }
            System.out.println("[考试更新] 5. 关联试卷验证通过 - 试卷名称: " + examPaper.getExamPaperName());
        }

        // 检测考试时间冲突（排除当前考试自身）
        if (examInfo.getStartTime() != null && examInfo.getEndTime() != null) {
            String conflictMessage = checkTimeConflict(examInfo.getId(), examInfo.getStartTime(), examInfo.getEndTime(), examInfo.getKemuTypes());
            if (conflictMessage != null) {
                System.out.println("[考试更新] ❌ 时间冲突检测失败: " + conflictMessage);
                return Result.error(conflictMessage);
            }
            System.out.println("[考试更新] 5.5. 时间冲突检测通过");
        }

        // 设置更新时间
        examInfo.setUpdateTime(new Date());

        System.out.println("[考试更新] 6. 最终保存的数据 - kemuTypes: " + examInfo.getKemuTypes());
        System.out.println("========== [考试更新] 结束 ==========\n");

        examInfoService.updateById(examInfo);
        
        // 清除考试缓存
        redisCacheService.deleteExamInfoCache(examInfo.getId());
        redisCacheService.clearExamListCache();
        log.info("🗑️  清除考试缓存（更新考试 ID={}）", examInfo.getId());
        
        // 记录操作日志
        operationLogUtil.logSuccess(request, "考试管理", "修改", "修改考试：" + examInfo.getExamName());
        
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
        // 先从缓存获取
        Object cachedData = redisCacheService.getExamInfoCache(id);
        if (cachedData != null) {
            log.info("✅ 考试详情缓存命中：ID={}", id);
            return Result.success(cachedData);
        }
        
        // 缓存未命中，查询数据库
        log.info("❌ 考试详情缓存未命中，查询数据库：ID={}", id);
        ExamInfo examInfo = examInfoService.info(id);
        if (examInfo == null) {
            return Result.error("考试不存在");
        }
        
        // 存入缓存（10分钟）
        redisCacheService.setExamInfoCache(id, examInfo);
        
        return Result.success(examInfo);
    }

    /**
     * 根据id删除（带权限校验）
     *
     * @param id
     * @return
     */
    @DeleteMapping("/del/{id}")
    public Result delete(@PathVariable Integer id, HttpServletRequest request) {
        // 从 Session 获取当前登录用户信息
        String role = (String) request.getSession().getAttribute("role");
        String tableName = (String) request.getSession().getAttribute("tableName");
        Integer userId = (Integer) request.getSession().getAttribute("userId");

        // 查询要删除的考试
        ExamInfo examInfo = examInfoService.getById(id);
        if (examInfo == null) {
            return Result.error("考试不存在");
        }

        // 如果是教师角色，只能删除自己创建的考试
        if (("teachers".equals(tableName) || "教师".equals(role)) && userId != null) {
            if (!examInfo.getTeacherId().equals(userId)) {
                return Result.error("无权操作：只能删除自己创建的考试");
            }
        }

        String examName = examInfo.getExamName();
        examInfoService.removeById(id);
        
        // 清除所有相关缓存
        // 1. 清除考试缓存
        redisCacheService.deleteExamInfoCache(id);
        redisCacheService.clearExamListCache();
        
        // 2. 清除考试记录缓存（考试记录列表和详情）
        redisCacheService.clearRecordListCache();
        redisUtil.deleteByPattern("record:info:*");
        
        // 3. 清除统计数据缓存
        redisCacheService.clearAllStatisticsCache();
        
        log.info("🗑️  清除所有相关缓存（删除考试 ID={}, 名称={}）", id, examName);
        
        // 记录操作日志
        operationLogUtil.logSuccess(request, "考试管理", "删除", "删除考试：" + examName);
        
        return Result.success();
    }

    /**
     * 批量删除（带权限校验）
     *
     * @param ids
     * @return
     */
    @RequestMapping("/del/batch")
    public Result deleteBatch(@RequestBody Integer[] ids, HttpServletRequest request) {
        // 从 Session 获取当前登录用户信息
        String role = (String) request.getSession().getAttribute("role");
        String tableName = (String) request.getSession().getAttribute("tableName");
        Integer userId = (Integer) request.getSession().getAttribute("userId");

        // 校验参数
        if (ids == null || ids.length == 0) {
            return Result.error("请选择要删除的记录");
        }

        // 遍历校验每条记录
        Date now = new Date();
        for (Integer id : ids) {
            ExamInfo examInfo = examInfoService.getById(id);
            if (examInfo == null) {
                return Result.error("考试不存在：ID=" + id);
            }

            // 已发布且未结束的考试不可删除（动态判断）
            if (examInfo.getStatus() == 1) {
                if (examInfo.getEndTime() == null || now.before(examInfo.getEndTime())) {
                    return Result.error("选中的记录中包含进行中或未结束的考试，无法删除");
                }
            }

            // 如果是教师角色，只能删除自己创建的考试
            if (("teachers".equals(tableName) || "教师".equals(role)) && userId != null) {
                if (!examInfo.getTeacherId().equals(userId)) {
                    return Result.error("无权操作：只能删除自己创建的考试（ID=" + id + "）");
                }
            }
        }

        // 执行批量删除
        boolean result = examInfoService.removeBatchByIds(Arrays.asList(ids));
        if (!result) {
            return Result.error("删除失败");
        }
        
        // 清除所有相关缓存
        // 1. 清除考试缓存
        for (Integer id : ids) {
            redisCacheService.deleteExamInfoCache(id);
        }
        redisCacheService.clearExamListCache();
        
        // 2. 清除考试记录缓存（考试记录列表和详情）
        redisCacheService.clearRecordListCache();
        redisUtil.deleteByPattern("record:info:*");
        
        // 3. 清除统计数据缓存
        redisCacheService.clearAllStatisticsCache();
        
        log.info("🗑️  清除所有相关缓存（批量删除考试，数量={}）", ids.length);
        log.info("🗑️  清除考试缓存（批量删除 {} 个考试）", ids.length);
        
        // 记录操作日志
        operationLogUtil.logSuccess(request, "考试管理", "删除", "批量删除考试，共 " + ids.length + " 条");
        
        return Result.success();
    }

    /**
     * 加载所有（用于下拉选择，支持数据隔离）
     *
     * @return
     */
    @GetMapping("/loadAll")
    public Result loadAll(HttpServletRequest request) {
        // 从 Session 获取当前登录用户信息
        String role = (String) request.getSession().getAttribute("role");
        String tableName = (String) request.getSession().getAttribute("tableName");
        Integer userId = (Integer) request.getSession().getAttribute("userId");

        // 如果是教师角色，返回自己创建的 OR 与自己任教科目相同的考试
        if (("teachers".equals(tableName) || "教师".equals(role)) && userId != null) {
            Teachers teacher = teachersService.getById(userId);
            if (teacher != null && teacher.getKemuTypes() != null) {
                return Result.success(examInfoService.lambdaQuery()
                        .and(wrapper -> wrapper
                                .eq(ExamInfo::getTeacherId, userId)
                                .or()
                                .eq(ExamInfo::getKemuTypes, teacher.getKemuTypes())
                        )
                        .orderByDesc(ExamInfo::getCreateTime)
                        .list());
            }
        }

        // 管理员返回所有
        return Result.success(examInfoService.list());
    }

    /**
     * 发布考试（状态 0 -> 1）
     *
     * @param id 考试ID
     * @return
     */
    @PostMapping("/publish/{id}")
    public Result publish(@PathVariable Integer id, HttpServletRequest request) {
        log.info("\n========== [发布考试接口] 开始 ==========");

        // 从 Session 获取当前登录用户信息
        String role = (String) request.getSession().getAttribute("role");
        String tableName = (String) request.getSession().getAttribute("tableName");
        Integer userId = (Integer) request.getSession().getAttribute("userId");

        log.info("[发布考试] Session信息 - role: {}, tableName: {}, userId: {}", role, tableName, userId);

        // 查询要发布的考试
        ExamInfo examInfo = examInfoService.getById(id);
        if (examInfo == null) {
            log.error("[发布考试] ❌ 考试不存在，ID: {}", id);
            return Result.error("考试不存在");
        }

        // 权限校验：教师只能发布自己创建的考试
        if (("teachers".equals(tableName) || "教师".equals(role)) && userId != null) {
            if (!examInfo.getTeacherId().equals(userId)) {
                log.error("[发布考试] ❌ 权限不足，教师ID: {}, 考试创建者ID: {}", userId, examInfo.getTeacherId());
                return Result.error("无权操作：只能发布自己创建的考试");
            }
            log.info("[发布考试] ✅ 教师权限校验通过");
        }

        // 调用 Service 层发布逻辑
        String errorMessage = examInfoService.publish(id);

        log.info("[发布考试] {} 发布结果: {}", errorMessage == null ? "✅" : "❌", id);
        log.info("========== [发布考试接口] 结束 ==========\n");

        if (errorMessage == null) {
            // 清除考试缓存（发布状态改变）
            redisCacheService.deleteExamInfoCache(id);
            redisCacheService.clearExamListCache();
            log.info("🗑️  清除考试缓存（发布考试 ID={}）", id);
            
            // 记录操作日志
            operationLogUtil.logSuccess(request, "考试管理", "发布", "发布考试：" + examInfo.getExamName());
            return Result.success("发布成功");
        } else {
            return Result.error(errorMessage);
        }
    }

    /**
     * 取消发布考试（状态 1 -> 0）
     *
     * @param id 考试ID
     * @return
     */
    @PostMapping("/unpublish/{id}")
    public Result unpublish(@PathVariable Integer id, HttpServletRequest request) {
        log.info("\n========== [取消发布考试接口] 开始 ==========");

        // 从 Session 获取当前登录用户信息
        String role = (String) request.getSession().getAttribute("role");
        String tableName = (String) request.getSession().getAttribute("tableName");
        Integer userId = (Integer) request.getSession().getAttribute("userId");

        log.info("[取消发布] Session信息 - role: {}, tableName: {}, userId: {}", role, tableName, userId);

        // 查询要取消发布的考试
        ExamInfo examInfo = examInfoService.getById(id);
        if (examInfo == null) {
            log.error("[取消发布] ❌ 考试不存在，ID: {}", id);
            return Result.error("考试不存在");
        }

        // 权限校验：教师只能取消自己创建的考试
        if (("teachers".equals(tableName) || "教师".equals(role)) && userId != null) {
            if (!examInfo.getTeacherId().equals(userId)) {
                log.error("[取消发布] ❌ 权限不足，教师ID: {}, 考试创建者ID: {}", userId, examInfo.getTeacherId());
                return Result.error("无权操作：只能取消自己创建的考试");
            }
            log.info("[取消发布] ✅ 教师权限校验通过");
        }

        // 调用 Service 层取消发布逻辑
        boolean success = examInfoService.unpublish(id);

        log.info("[取消发布] {} 取消发布结果: {}", success ? "✅" : "❌", id);
        log.info("========== [取消发布考试接口] 结束 ==========\n");

        if (success) {
            // 清除考试缓存（发布状态改变）
            redisCacheService.deleteExamInfoCache(id);
            redisCacheService.clearExamListCache();
            log.info("🗑️  清除考试缓存（取消发布考试 ID={}）", id);
            
            // 记录操作日志
            operationLogUtil.logSuccess(request, "考试管理", "发布", "取消发布考试：" + examInfo.getExamName());
            return Result.success("取消发布成功");
        } else {
            return Result.error("取消发布失败");
        }
    }

    /**
     * 学生端分页查询考试列表
     *
     * @return
     */
    @PostMapping("/studentPage")
    public Result studentPage(@RequestBody ExamInfoReq req, HttpServletRequest request) {
        // 从 Session 获取当前登录用户 ID
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }
        req.setUsersId(userId);

        PageUtils page = examInfoService.selectStudentPage(req);
        return Result.success(page);
    }

    /**
     * 上报切屏行为
     *
     * @param examRecordUuid 考试记录UUID
     * @return
     */
    @PostMapping("/reportScreenSwitch")
    public Result reportScreenSwitch(@RequestParam String examRecordUuid, HttpServletRequest request) {
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        if (userId == null) {
            return Result.error("请先登录");
        }

        ExamRecord record = examRecordService.lambdaQuery()
                .eq(ExamRecord::getExamRecordUuidNumber, examRecordUuid)
                .one();

        if (record == null) {
            return Result.error("考试记录不存在");
        }

        if (!userId.equals(record.getUsersId())) {
            return Result.error("无权操作该考试记录");
        }

        // 已交卷/已完成则不再累计切屏
        if (record.getStatus() != null && (record.getStatus() == ExamStatusConstants.RECORD_SUBMITTED
                || record.getStatus() == ExamStatusConstants.RECORD_FORCED_SUBMIT
                || record.getStatus() == ExamStatusConstants.RECORD_COMPLETED)) {
            Map<String, Object> result = new HashMap<>();
            result.put("screenSwitchCount", record.getScreenSwitchCount() != null ? record.getScreenSwitchCount() : 0);
            result.put("maxScreenSwitch", null);
            result.put("exceeded", record.getStatus() == ExamStatusConstants.RECORD_FORCED_SUBMIT);
            result.put("forced", record.getStatus() == ExamStatusConstants.RECORD_FORCED_SUBMIT);
            return Result.success(result);
        }

        // 获取关联的考试配置
        if (record.getExamInfoId() != null) {
            ExamInfo examInfo = examInfoService.getById(record.getExamInfoId());
            if (examInfo != null && examInfo.getAllowScreenSwitch() != null && examInfo.getAllowScreenSwitch() == 0) {
                int currentCount = record.getScreenSwitchCount() != null ? record.getScreenSwitchCount() : 0;
                currentCount++;
                record.setScreenSwitchCount(currentCount);
                JSONArray switchTimes = new JSONArray();
                if (StrUtil.isNotBlank(record.getScreenSwitchTimes())) {
                    try {
                        switchTimes = JSONUtil.parseArray(record.getScreenSwitchTimes());
                    } catch (Exception e) {
                        log.warn("[切屏监控] 解析 screenSwitchTimes 失败，将丢弃旧数据重建: {}", e.getMessage());
                    }
                }
                switchTimes.add(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
                record.setScreenSwitchTimes(switchTimes.toString());
                // 考试过程中始终保持“考试中”，这里只累计切屏次数。
                if (record.getStatus() == null) {
                    record.setStatus(ExamStatusConstants.RECORD_IN_PROGRESS);
                }

                boolean exceeded = examInfo.getMaxScreenSwitch() != null && currentCount >= examInfo.getMaxScreenSwitch();
                if (exceeded) {
                    log.warn("[切屏监控] ⚠️ 切屏次数超限！用户: {}, 次数: {}/{}", userId, currentCount, examInfo.getMaxScreenSwitch());
                    
                    // 超限：标记为强制交卷并触发自动判分
                    record.setStatus(ExamStatusConstants.RECORD_FORCED_SUBMIT);
                    record.setEndTime(new Date());
                    examRecordService.updateById(record);
                    
                    // 触发自动判分和错题更新
                    try {
                        log.info("[切屏监控] 开始触发强制交卷判分逻辑...");
                        examPaperTopicController.triggerForceSubmitGrading(record, userId);
                        log.info("[切屏监控] ✅ 强制交卷判分完成");
                    } catch (Exception e) {
                        log.error("[切屏监控] ❌ 强制交卷判分失败: {}", e.getMessage(), e);
                        // 即使判分失败，也要返回超限状态，让前端知道已经强制交卷
                    }
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("screenSwitchCount", currentCount);
                    result.put("maxScreenSwitch", examInfo.getMaxScreenSwitch());
                    result.put("exceeded", true);
                    result.put("forced", true);
                    result.put("message", "切屏次数超限，已强制交卷");
                    return Result.success(result);
                }
                
                examRecordService.updateById(record);

                log.info("[切屏监控] 用户 {} 切屏次数: {}/{}", userId, currentCount, examInfo.getMaxScreenSwitch());
                Map<String, Object> result = new HashMap<>();
                result.put("screenSwitchCount", currentCount);
                result.put("maxScreenSwitch", examInfo.getMaxScreenSwitch());
                result.put("exceeded", false);
                result.put("forced", false);
                return Result.success(result);
            }
        }

        return Result.success();
    }

    /**
     * 校验并进入考试
     *
     * @param id 考试ID
     * @return
     */
    @GetMapping("/enter/{id}")
    public Result enterExam(@PathVariable Integer id, HttpServletRequest request) {
        log.info("\n========== [进入考试] 开始 ==========");
        
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        if (userId == null) {
            log.error("[进入考试] ❌ 用户未登录");
            return Result.error("请先登录");
        }

        ExamInfo examInfo = examInfoService.getById(id);
        if (examInfo == null) {
            log.error("[进入考试] ❌ 考试不存在，ID: {}", id);
            return Result.error("考试不存在");
        }

        // 1. 获取当前考试最新的一条考试记录，统一判断是断点续考、已交卷还是可重考
        ExamRecord latestRecord = examRecordService.lambdaQuery()
                .eq(ExamRecord::getExamInfoId, id)
                .eq(ExamRecord::getUsersId, userId)
                .orderByDesc(ExamRecord::getId)
                .last("LIMIT 1")
                .one();

        // 仅“考试中”的记录允许断点续考
        ExamRecord existingRecord = latestRecord != null && latestRecord.getStatus() != null
                && latestRecord.getStatus() == ExamStatusConstants.RECORD_IN_PROGRESS
                ? latestRecord
                : null;

        long attemptCount = examRecordService.lambdaQuery()
                .eq(ExamRecord::getExamInfoId, id)
                .eq(ExamRecord::getUsersId, userId)
                .count();
        int maxRetakeCount = examInfo.getMaxRetakeCount() != null ? examInfo.getMaxRetakeCount() : 0;
        int maxAttemptCount = (examInfo.getAllowRetake() != null && examInfo.getAllowRetake() == 1) ? maxRetakeCount + 1 : 1;

        // 已交卷/强制交卷时，只有允许重考且未超过次数的考试才能重新进入
        if (latestRecord != null && latestRecord.getStatus() != null
                && (latestRecord.getStatus() == ExamStatusConstants.RECORD_SUBMITTED
                || latestRecord.getStatus() == ExamStatusConstants.RECORD_FORCED_SUBMIT
                || latestRecord.getStatus() == ExamStatusConstants.RECORD_COMPLETED)
                && (examInfo.getAllowRetake() == null || examInfo.getAllowRetake() != 1)) {
            log.warn("[进入考试] ⚠️ 考试已完成且不允许重考，UUID: {}", latestRecord.getExamRecordUuidNumber());
            return Result.error("该考试不允许重考，您已完成本次考试");
        }
        if (latestRecord != null && latestRecord.getStatus() != null
                && (latestRecord.getStatus() == ExamStatusConstants.RECORD_SUBMITTED
                || latestRecord.getStatus() == ExamStatusConstants.RECORD_FORCED_SUBMIT
                || latestRecord.getStatus() == ExamStatusConstants.RECORD_COMPLETED)
                && examInfo.getAllowRetake() != null && examInfo.getAllowRetake() == 1
                && attemptCount >= maxAttemptCount) {
            log.warn("[进入考试] ⚠️ 已达到最大考试次数，examInfoId: {}, userId: {}, attemptCount: {}, maxAttemptCount: {}",
                    id, userId, attemptCount, maxAttemptCount);
            return Result.error("已达到最大重考次数，无法再次进入考试");
        }

        // 如果是断点续考，豁免密码校验；重新考试仍需重新校验密码
        if (existingRecord == null) {
            // 2. 首次进入，进行考试密码校验
            if (StrUtil.isNotBlank(examInfo.getExamPassword())) {
                String inputPassword = request.getParameter("password");
                if (!examInfo.getExamPassword().equals(inputPassword)) {
                    log.warn("[进入考试] ❌ 密码错误");
                    return Result.error("考试密码错误");
                }
            }
        } else {
            log.info("[进入考试] ✅ 检测到有效考试记录，豁免密码校验，UUID: {}", existingRecord.getExamRecordUuidNumber());
        }

        log.info("[进入考试] 考试信息 - 名称: {}, 试卷ID: {}", examInfo.getExamName(), examInfo.getExamPaperId());

        // 1. 校验考试发布状态
        if (examInfo.getStatus() == null || examInfo.getStatus() == 0) {
            log.warn("[进入考试] ⚠️ 考试未发布，status: {}", examInfo.getStatus());
            return Result.error("考试未发布，无法进入");
        }

        // 2. 校验考试时间
        Date now = new Date();
        Date startTime = examInfo.getStartTime();
        Date endTime = examInfo.getEndTime();
        
        if (startTime == null || endTime == null) {
            log.error("[进入考试] ❌ 考试时间配置错误");
            return Result.error("考试时间配置错误");
        }
        
        if (now.before(startTime)) {
            log.warn("[进入考试] ⚠️ 考试尚未开始，开始时间: {}", startTime);
            return Result.error("考试尚未开始，开始时间：" + startTime);
        }
        
        if (now.after(endTime)) {
            log.warn("[进入考试] ⚠️ 考试已结束，结束时间: {}", endTime);
            return Result.error("考试已结束，无法进入");
        }

        // 3. 获取试卷信息
        ExamPaper examPaper = examPaperService.getById(examInfo.getExamPaperId());
        if (examPaper == null) {
            log.error("[进入考试] ❌ 关联试卷不存在");
            return Result.error("关联试卷不存在");
        }

        // --- 核心倒计时计算逻辑 ---
        Integer duration = examInfo.getDuration(); // 考试总时长（分钟）

        // 2. 新开一场时：从「当前时刻」到考试结束，与配置时长取小（分钟）
        long remainingToEndTimeMillis = endTime.getTime() - now.getTime();
        int remainingToEndTime = (int) Math.ceil(remainingToEndTimeMillis / (1000d * 60));
        if (remainingToEndTime < 0) {
            remainingToEndTime = 0;
        }
        int sessionCapForNewAttempt;
        if (duration == null || duration <= 0) {
            sessionCapForNewAttempt = remainingToEndTime;
            log.info("[进入考试] 未设置总时长，新开场次可用: {} 分钟", sessionCapForNewAttempt);
        } else {
            sessionCapForNewAttempt = Math.min(remainingToEndTime, duration);
            log.info("[进入考试] 新开场次 — 距结束: {} 分钟, 配置时长: {} 分钟, 本场上限: {} 分钟",
                    remainingToEndTime, duration, sessionCapForNewAttempt);
        }
        if (sessionCapForNewAttempt <= 0) {
            return Result.error("考试时间已到，无法开始作答");
        }

        // 检查是否已有进行中的答卷（如果上面已经查过，这里直接复用对象，避免重复查询）
        if (existingRecord != null) {
            // 检查考试记录的时间合理性：如果已用时间超过总时长，则视为异常记录
            Date recordStartTime = existingRecord.getInsertTime();
            if (recordStartTime != null) {
                // 断点续考：截止时刻必须只依赖「记录开始时间 + 本场固定上限」，不能用每次请求时的「距结束剩余分钟」
                // 否则 finalDuration 随时间变小，多次 enter 会把 deadline 算得越来越短，前端倒计时逐渐错误。
                int sessionCapForExisting = computeSessionCapMinutesFromStart(recordStartTime, endTime, duration);
                long elapsedMinutes = (now.getTime() - recordStartTime.getTime()) / (1000 * 60);
                log.info("[进入考试] 断点续考 — 记录起点起本场上限: {} 分钟, 已作答: {} 分钟", sessionCapForExisting, elapsedMinutes);

                if (elapsedMinutes >= sessionCapForExisting) {
                    log.warn("[进入考试] ⚠️ 检测到异常历史记录（已超时），将创建新记录");
                    // 删除旧记录
                    examRecordService.removeById(existingRecord.getId());
                    // 继续执行下面的创建新记录逻辑
                } else {
                    // 正常断点续考，返回时间信息
                    initExamDetailsIfAbsent(existingRecord, userId);
                    log.info("[进入考试] ✅ 检测到有效考试记录，UUID: {}", existingRecord.getExamRecordUuidNumber());
                    
                    java.util.Map<String, Object> resultData = new java.util.HashMap<>();
                    resultData.put("examRecordUuid", existingRecord.getExamRecordUuidNumber());
                    resultData.put("examPaperDate", sessionCapForExisting);
                    resultData.put("insertTime", recordStartTime);
                    long recordDeadline = recordStartTime.getTime() + (long) sessionCapForExisting * 60L * 1000L;
                    resultData.put("serverTimestamp", now.getTime());
                    resultData.put("endTimestamp", Math.min(recordDeadline, endTime.getTime()));
                    resultData.put("screenSwitchCount", existingRecord.getScreenSwitchCount() != null ? existingRecord.getScreenSwitchCount() : 0);
                    resultData.put("allowScreenSwitch", examInfo.getAllowScreenSwitch());
                    resultData.put("maxScreenSwitch", examInfo.getMaxScreenSwitch());
                    resultData.put("allowCopyPaste", examInfo.getAllowCopyPaste());
                    
                    log.info("========== [进入考试] 结束 ==========\n");
                    return Result.success(resultData);
                }
            }
        }

        // 如果没有可继续的记录，则创建新的考试记录（首次考试或允许重考后的新尝试）
        if (latestRecord != null && latestRecord.getStatus() != null
                && (latestRecord.getStatus() == ExamStatusConstants.RECORD_SUBMITTED
                || latestRecord.getStatus() == ExamStatusConstants.RECORD_FORCED_SUBMIT
                || latestRecord.getStatus() == ExamStatusConstants.RECORD_COMPLETED)
                && examInfo.getAllowRetake() != null && examInfo.getAllowRetake() == 1) {
            log.info("[进入考试] 检测到已完成记录且允许重考，将创建新的考试记录。上一条UUID: {}",
                    latestRecord.getExamRecordUuidNumber());
        } else {
            log.info("[进入考试] 首次进入考试，创建新的考试记录...");
        }
        if (latestRecord != null) {
            examRecordService.lambdaUpdate()
                    .eq(ExamRecord::getExamInfoId, id)
                    .eq(ExamRecord::getUsersId, userId)
                    .set(ExamRecord::getIsLatest, 0)
                    .update();
        }

        String uuid = String.valueOf(System.currentTimeMillis());
        
        ExamRecord newRecord = new ExamRecord();
        newRecord.setExamRecordUuidNumber(uuid);
        newRecord.setExamPaperId(examInfo.getExamPaperId());
        newRecord.setUsersId(userId);
        newRecord.setExamInfoId(id); // 关联当前考试 ID
        newRecord.setTotalScore(0); // 初始分数为0
        newRecord.setStatus(ExamStatusConstants.RECORD_IN_PROGRESS); // 状态：考试中
        newRecord.setIsLatest(1); // 当前有效记录
        newRecord.setInsertTime(now); // 记录开始时间
        newRecord.setCreateTime(now);
        
        boolean saved = examRecordService.save(newRecord);
        if (!saved) {
            log.error("[进入考试] ❌ 创建考试记录失败");
            log.info("========== [进入考试] 结束 ==========\n");
            return Result.error("创建考试记录失败");
        }
        initExamDetailsIfAbsent(newRecord, userId);
        
        // 清除考试列表缓存（新创建考试记录后，学生端列表需要更新以显示"继续考试"）
        redisCacheService.clearExamListCache();
        log.info("🗑️  清除考试列表缓存（创建考试记录 UUID={}）", uuid);
        
        log.info("[进入考试] ✅ 成功创建考试记录，UUID: {}, examInfoId: {}", uuid, id);
        log.info("========== [进入考试] 结束 ==========\n");
        
        // 返回完整信息（首次进入）
        java.util.Map<String, Object> resultData = new java.util.HashMap<>();
        resultData.put("examRecordUuid", uuid);
        resultData.put("examPaperDate", sessionCapForNewAttempt); // 本场作答上限（分钟）
        resultData.put("insertTime", now); // 开始时间
        long recordDeadline = now.getTime() + (long) sessionCapForNewAttempt * 60L * 1000L;
        resultData.put("serverTimestamp", now.getTime());
        resultData.put("endTimestamp", Math.min(recordDeadline, endTime.getTime()));
        resultData.put("screenSwitchCount", 0);
        resultData.put("allowScreenSwitch", examInfo.getAllowScreenSwitch());
        resultData.put("maxScreenSwitch", examInfo.getMaxScreenSwitch());
        resultData.put("allowCopyPaste", examInfo.getAllowCopyPaste());
        
        return Result.success(resultData);
    }

    /**
     * 从 sessionStart 到考试结束，本场最多可作答的分钟数（与配置单次时长取小）。
     * 仅依赖「记录开始」与「考试结束」，不依赖当前时刻，避免断点续考每次 enter 用 shrinking 的「距结束剩余」重算 deadline。
     */
    private static int computeSessionCapMinutesFromStart(Date sessionStartTime, Date examEndTime, Integer configuredDurationMinutes) {
        if (sessionStartTime == null || examEndTime == null) {
            return 0;
        }
        long spanMillis = examEndTime.getTime() - sessionStartTime.getTime();
        int maxMinutesFromStart = (int) Math.ceil(spanMillis / (1000d * 60));
        if (maxMinutesFromStart < 0) {
            maxMinutesFromStart = 0;
        }
        if (configuredDurationMinutes != null && configuredDurationMinutes > 0) {
            return Math.min(maxMinutesFromStart, configuredDurationMinutes);
        }
        return maxMinutesFromStart;
    }

    private void initExamDetailsIfAbsent(ExamRecord record, Integer userId) {
        long existingCount = examDetailsService.lambdaQuery()
                .eq(ExamDetails::getExamRecordId, record.getId())
                .count();
        if (existingCount > 0) {
            return;
        }

        List<ExamPaperTopic> topics = examPaperTopicService.lambdaQuery()
                .eq(ExamPaperTopic::getExamPaperId, record.getExamPaperId())
                .orderByAsc(ExamPaperTopic::getExamPaperTopicSequence)
                .list();
        if (topics == null || topics.isEmpty()) {
            log.warn("[进入考试] 试卷 {} 没有题目，跳过答题明细初始化", record.getExamPaperId());
            return;
        }

        List<Integer> questionIds = topics.stream()
                .map(ExamPaperTopic::getExamQuestionId)
                .collect(Collectors.toList());
        Map<Integer, ExamQuestion> questionMap = examQuestionService.listByIds(questionIds).stream()
                .collect(Collectors.toMap(ExamQuestion::getId, q -> q));

        Date now = new Date();
        List<ExamDetails> detailList = new ArrayList<>();
        for (ExamPaperTopic topic : topics) {
            ExamQuestion question = questionMap.get(topic.getExamQuestionId());

            ExamDetails detail = new ExamDetails();
            detail.setExamDetailsUuidNumber(record.getExamRecordUuidNumber());
            detail.setExamRecordId(record.getId());
            detail.setUsersId(userId);
            detail.setExamQuestionId(topic.getExamQuestionId());
            detail.setExamPaperTopicNumber(topic.getExamPaperTopicNumber());
            detail.setExamPaperTopicSequence(topic.getExamPaperTopicSequence());
            detail.setExamDetailsMyanswer("未作答");
            detail.setExamDetailsMyscore(0);
            detail.setTeacherScore(0);
            detail.setReviewStatus(question != null && question.getExamQuestionTypes() != null && question.getExamQuestionTypes() == 5 ? 0 : 1);
            detail.setCreateTime(now);
            detailList.add(detail);
        }

        examDetailsService.saveBatch(detailList);
        log.info("[进入考试] 已初始化考试记录 {} 的 {} 条答题明细", record.getId(), detailList.size());
    }

    /**
     * 检测考试时间冲突
     * 
     * @param excludeExamId 排除的考试ID（更新时使用，新增时传null）
     * @param startTime 考试开始时间
     * @param endTime 考试结束时间
     * @param kemuTypes 科目类型
     * @return 冲突提示信息，无冲突返回null
     */
    private String checkTimeConflict(Integer excludeExamId, Date startTime, Date endTime, Integer kemuTypes) {
        if (startTime == null || endTime == null) {
            return null;
        }

        // 时间合理性校验
        if (!startTime.before(endTime)) {
            return "考试开始时间必须早于结束时间";
        }

        // 查询同一科目下已发布但未结束的考试
        // 状态：1-已发布，2-进行中（排除0-未发布和3-已结束）
        List<ExamInfo> existingExams = examInfoService.lambdaQuery()
                .eq(ExamInfo::getKemuTypes, kemuTypes)
                .in(ExamInfo::getStatus, 1, 2) // 只检查已发布(1)和进行中(2)的考试，排除已结束(3)
                .isNotNull(ExamInfo::getStartTime)
                .isNotNull(ExamInfo::getEndTime)
                .list();

        // 排除当前考试自身（更新时）
        if (excludeExamId != null) {
            existingExams = existingExams.stream()
                    .filter(exam -> !exam.getId().equals(excludeExamId))
                    .collect(Collectors.toList());
        }

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
                    "考试时间与已有考试冲突：\n考试名称：%s\n冲突时间：%s ~ %s",
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

    /**
     * 测试考试缓存功能
     * 
     * @param kemuTypes 科目ID（可选）
     * @return 测试结果
     */
    @GetMapping("/testCache")
    public Result testCache(@RequestParam(required = false) Integer kemuTypes) {
        log.info("\n========== 开始测试考试缓存 ==========");
        
        try {
            // 测试1：考试列表缓存
            ExamInfoReq req1 = new ExamInfoReq();
            req1.setPage(1);
            req1.setLimit(10);
            if (kemuTypes != null) {
                req1.setKemuTypes(kemuTypes);
            }
            
            long start1 = System.currentTimeMillis();
            PageUtils page1 = examInfoService.selectPage(req1);
            long time1 = System.currentTimeMillis() - start1;
            log.info("第1次查询（无缓存）：耗时 {}ms，查询到 {} 条考试", time1, page1.getList() != null ? page1.getList().size() : 0);
            
            // 测试2：考试列表缓存命中
            ExamInfoReq req2 = new ExamInfoReq();
            req2.setPage(1);
            req2.setLimit(10);
            if (kemuTypes != null) {
                req2.setKemuTypes(kemuTypes);
            }
            
            long start2 = System.currentTimeMillis();
            PageUtils page2 = examInfoService.selectPage(req2);
            long time2 = System.currentTimeMillis() - start2;
            log.info("第2次查询（缓存命中）：耗时 {}ms，查询到 {} 条考试", time2, page2.getList() != null ? page2.getList().size() : 0);
            
            // 测试3：考试详情缓存（如果有考试）
            Long time3 = null, time4 = null;
            if (page1.getList() != null && !page1.getList().isEmpty()) {
                Object firstItem = page1.getList().get(0);
                Integer examId = null;
                if (firstItem instanceof ExamInfo) {
                    examId = ((ExamInfo) firstItem).getId();
                }
                
                if (examId != null) {
                    long start3 = System.currentTimeMillis();
                    ExamInfo exam1 = examInfoService.getById(examId);
                    time3 = System.currentTimeMillis() - start3;
                    log.info("第3次查询（考试详情，无缓存）：耗时 {}ms，考试ID={}", time3, examId);
                    
                    long start4 = System.currentTimeMillis();
                    ExamInfo exam2 = examInfoService.getById(examId);
                    time4 = System.currentTimeMillis() - start4;
                    log.info("第4次查询（考试详情，缓存命中）：耗时 {}ms，考试ID={}", time4, examId);
                }
            }
            
            // 计算性能提升
            double improvement1 = time2 > 0 ? (double) time1 / time2 : 0;
            Double improvement2 = (time3 != null && time4 != null && time4 > 0) ? (double) time3 / time4 : null;
            
            log.info("========== 考试缓存测试完成 ==========\n");
            
            // 返回测试结果
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("考试列表第1次查询耗时(ms)", time1);
            result.put("考试列表第2次查询耗时(ms)", time2);
            result.put("考试列表性能提升", String.format("%.1f倍", improvement1));
            if (time3 != null && time4 != null) {
                result.put("考试详情第1次查询耗时(ms)", time3);
                result.put("考试详情第2次查询耗时(ms)", time4);
                result.put("考试详情性能提升", String.format("%.1f倍", improvement2));
            }
            result.put("查询到的考试数量", page1.getList() != null ? page1.getList().size() : 0);
            result.put("总考试数", page1.getTotal());
            result.put("测试说明", "第1次查询从数据库获取，第2次查询从Redis缓存获取");
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("测试考试缓存失败", e);
            return Result.error("测试失败：" + e.getMessage());
        }
    }
}
