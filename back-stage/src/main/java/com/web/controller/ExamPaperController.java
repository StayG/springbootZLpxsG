package com.web.controller;

import com.web.annotation.IgnoreAuth;
import com.web.domain.ExamPaper;
import com.web.domain.ExamPaperTopic;
import com.web.domain.Req.ExamPaperReq;
import com.web.domain.Teachers;
import com.web.service.ExamInfoService;
import com.web.service.ExamPaperService;
import com.web.service.ExamPaperTopicService;
import com.web.service.RedisCacheService;
import com.web.service.TeachersService;
import com.web.utils.PageUtils;
import com.web.utils.Result;
import com.web.utils.OperationLogUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 试卷 前端控制器
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/examPaper")
public class ExamPaperController {

    @Resource
    private ExamPaperService examPaperService;

    @Resource
    private ExamPaperTopicService examPaperTopicService;
    
    @Resource
    private TeachersService teachersService;

    @Resource
    private ExamInfoService examInfoService;
    
    @Resource
    private OperationLogUtil operationLogUtil;

    @Resource
    private RedisCacheService redisCacheService;

    /**
     * 后台分页查询（支持数据隔离）
     * 
     * @return
     */
    @PostMapping("/page")
    public Result page(@RequestBody ExamPaperReq req, HttpServletRequest request) {
        // 从 Session 获取当前登录用户信息
        String role = (String) request.getSession().getAttribute("role"); // 中文角色名
        String tableName = (String) request.getSession().getAttribute("tableName"); // 英文表名
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        
        // 注入角色信息
        req.setRole(role);
        
        // 如果是教师角色，注入教师 ID 和科目 ID 实现数据隔离
        if (("teachers".equals(tableName) || "教师".equals(role)) && userId != null) {
            Teachers teacher = teachersService.getById(userId);
            if (teacher != null) {
                req.setTeacherId(userId);
                req.setKemuTypes(teacher.getKemuTypes());
            }
        }
        
        // 生成缓存 key
        String cacheKey = req.getPage() + ":" + req.getLimit() + ":" + 
                         (req.getKemuTypes() != null ? req.getKemuTypes() : "all") + ":" +
                         (req.getTeacherId() != null ? req.getTeacherId() : "all");
        
        // 先从缓存获取
        Object cachedData = redisCacheService.getPaperListCache(cacheKey);
        if (cachedData != null) {
            log.info("✅ 试卷列表缓存命中：{}", cacheKey);
            return Result.success(cachedData);
        }
        
        // 缓存未命中，查询数据库
        log.info("❌ 试卷列表缓存未命中，查询数据库：{}", cacheKey);
        PageUtils page = examPaperService.selectPage(req);
        
        // 存入缓存（5分钟）
        redisCacheService.setPaperListCache(cacheKey, page);
        
        return Result.success(page);
    }

    /**
     * 新增（自动关联教师科目）
     * 
     * @param examPaper
     * @return
     */
    @PostMapping("/save")
    public Result save(@RequestBody ExamPaper examPaper, HttpServletRequest request) {
        // 从 Session 获取当前登录用户信息
        String role = (String) request.getSession().getAttribute("role");
        String tableName = (String) request.getSession().getAttribute("tableName");
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        
        System.out.println("\n========== [试卷保存] 开始 ==========");
        System.out.println("[试卷保存] 1. Session信息 - role: " + role + ", tableName: " + tableName + ", userId: " + userId);
        System.out.println("[试卷保存] 2. 接收到的前端数据 - examPaperName: " + examPaper.getExamPaperName());
        System.out.println("[试卷保存] 3. 前端数据中的 kemuTypes: " + examPaper.getKemuTypes());
        
        // 如果是教师角色，强制设置科目为教师的任教科目
        if (("teachers".equals(tableName) || "教师".equals(role)) && userId != null) {
            Teachers teacher = teachersService.getById(userId);
            if (teacher != null) {
                examPaper.setKemuTypes(teacher.getKemuTypes());
                System.out.println("[试卷保存] 4. 教师新增试卷 - 强制设置kemuTypes: " + teacher.getKemuTypes());
            } else {
                System.out.println("[试卷保存] ❌ 警告：未找到教师信息，userId=" + userId);
            }
        }
        
        System.out.println("[试卷保存] 5. 最终保存的数据 - kemuTypes: " + examPaper.getKemuTypes());
        System.out.println("========== [试卷保存] 结束 ==========\n");
        
        examPaperService.save(examPaper);
        
        // 清除试卷列表缓存
        redisCacheService.clearPaperListCache();
        log.info("🗑️  清除试卷列表缓存（新增试卷）");
        
        // 记录操作日志
        operationLogUtil.logSuccess(request, "试卷管理", "新增", "新增试卷：" + examPaper.getExamPaperName());
        
        return Result.success();
    }

    /**
     * 修改（保持教师科目不变）
     * 
     * @param examPaper
     * @return
     */
    @PostMapping("/update")
    public Result update(@RequestBody ExamPaper examPaper, HttpServletRequest request) {
        // 从 Session 获取当前登录用户信息
        String role = (String) request.getSession().getAttribute("role");
        String tableName = (String) request.getSession().getAttribute("tableName");
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        
        System.out.println("\n========== [试卷更新] 开始 ==========");
        System.out.println("[试卷更新] 1. Session信息 - role: " + role + ", tableName: " + tableName + ", userId: " + userId);
        System.out.println("[试卷更新] 2. 接收到的前端数据 - examPaper.id: " + examPaper.getId() + ", examPaperName: " + examPaper.getExamPaperName());
        System.out.println("[试卷更新] 3. 前端传入的 kemuTypes: " + examPaper.getKemuTypes());
        
        // 查询数据库中原始的试卷信息（用于对比）
        ExamPaper originalPaper = examPaperService.getById(examPaper.getId());
        if (originalPaper != null) {
            System.out.println("[试卷更新] 3.5. 数据库中的原始 kemuTypes: " + originalPaper.getKemuTypes());
        }
        
        // 如果是教师角色，强制保持科目不变（防止越权修改）
        if (("teachers".equals(tableName) || "教师".equals(role)) && userId != null) {
            Teachers teacher = teachersService.getById(userId);
            if (teacher != null) {
                Integer frontendKemuTypes = examPaper.getKemuTypes();
                Integer teacherKemuTypes = teacher.getKemuTypes();
                
                // 强制设置科目为教师的任教科目，无论前端传入什么值
                examPaper.setKemuTypes(teacherKemuTypes);
                
                // 检测是否有篡改行为
                if (frontendKemuTypes != null && !frontendKemuTypes.equals(teacherKemuTypes)) {
                    System.out.println("[试卷更新] ⚠️  检测到前端尝试篡改科目: " + frontendKemuTypes + " -> " + teacherKemuTypes + " (已强制修正)");
                }
                
                System.out.println("[试卷更新] 4. 教师更新试卷 - 强制覆盖kemuTypes: " + frontendKemuTypes + " -> " + teacherKemuTypes);
                System.out.println("[试卷更新] 4.1. 教师姓名: " + teacher.getRealName() + ", 任教科目ID: " + teacherKemuTypes);
            } else {
                System.out.println("[试卷更新] ❌ 警告：未找到教师信息，userId=" + userId);
                return Result.error("教师信息不存在，无法更新试卷");
            }
        } else if ("managers".equals(tableName) || "管理员".equals(role)) {
            System.out.println("[试卷更新] 4. 管理员更新试卷 - 允许修改任意科目");
            System.out.println("[试卷更新] 4.1. 保持前端传入的 kemuTypes: " + examPaper.getKemuTypes());
        } else {
            System.out.println("[试卷更新] 4. 未知角色 - 保持前端传入的 kemuTypes: " + examPaper.getKemuTypes());
        }
        
        System.out.println("[试卷更新] 5. 最终保存的数据 - kemuTypes: " + examPaper.getKemuTypes());
        System.out.println("========== [试卷更新] 结束 ==========\n");
        
        examPaperService.updateById(examPaper);
        
        // 清除试卷详情缓存和列表缓存
        redisCacheService.deletePaperInfoCache(examPaper.getId());
        redisCacheService.deletePaperTopicsCache(examPaper.getId());
        redisCacheService.clearPaperListCache();
        log.info("🗑️  清除试卷缓存（更新试卷 ID={}）", examPaper.getId());
        
        // 记录操作日志
        operationLogUtil.logSuccess(request, "试卷管理", "修改", "修改试卷：" + examPaper.getExamPaperName());
        
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
        Object cachedData = redisCacheService.getPaperInfoCache(id);
        if (cachedData != null) {
            log.info("✅ 试卷详情缓存命中：ID={}", id);
            return Result.success(cachedData);
        }
        
        // 缓存未命中，查询数据库
        log.info("❌ 试卷详情缓存未命中，查询数据库：ID={}", id);
        Object paperInfo = examPaperService.info(id);
        
        // 存入缓存（10分钟）
        redisCacheService.setPaperInfoCache(id, paperInfo);
        
        return Result.success(paperInfo);
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
        
        // 查询要删除的试卷
        ExamPaper examPaper = examPaperService.getById(id);
        if (examPaper == null) {
            return Result.error("试卷不存在");
        }
        
        // 如果是教师角色，只能删除自己科目的试卷
        if (("teachers".equals(tableName) || "教师".equals(role)) && userId != null) {
            Teachers teacher = teachersService.getById(userId);
            if (teacher != null && !teacher.getKemuTypes().equals(examPaper.getKemuTypes())) {
                return Result.error("无权操作：只能删除自己科目的试卷");
            }
        }

        // 检查试卷是否被任何考试使用
        long examCount = examInfoService.lambdaQuery()
                .eq(com.web.domain.ExamInfo::getExamPaperId, id)
                .count();
        if (examCount > 0) {
            return Result.error("该试卷已被 " + examCount + " 个考试使用，无法删除");
        }

        // 1. 先删除选题表中该试卷的所有选题记录
        examPaperTopicService.lambdaUpdate().eq(ExamPaperTopic::getExamPaperId, id).remove();

        String paperName = examPaper.getExamPaperName();
        boolean success = examPaperService.removeById(id);
        
        // 清除试卷缓存
        if (success) {
            redisCacheService.deletePaperInfoCache(id);
            redisCacheService.deletePaperTopicsCache(id);
            redisCacheService.clearPaperListCache();
            log.info("🗑️  清除试卷缓存（删除试卷 ID={}）", id);
            
            // 记录操作日志
            operationLogUtil.logSuccess(request, "试卷管理", "删除", "删除试卷：" + paperName);
        }
        
        return Result.success(success);
    }

    /**
     * 批量删除（带权限校验）
     * 
     * @param ids
     * @return
     */
    @RequestMapping("/del/batch")
    public Result deleteBatch(@RequestBody Integer[] ids, HttpServletRequest request) {
        if (ids == null || ids.length == 0) {
            return Result.error("请选择要删除的试卷");
        }
        
        // 从 Session 获取当前登录用户信息
        String role = (String) request.getSession().getAttribute("role");
        String tableName = (String) request.getSession().getAttribute("tableName");
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        
        // 如果是教师角色，只能删除自己科目的试卷
        if (("teachers".equals(tableName) || "教师".equals(role)) && userId != null) {
            Teachers teacher = teachersService.getById(userId);
            if (teacher != null) {
                for (Integer id : ids) {
                    ExamPaper examPaper = examPaperService.getById(id);
                    if (examPaper == null) {
                        return Result.error("试卷不存在：ID=" + id);
                    }
                    if (!teacher.getKemuTypes().equals(examPaper.getKemuTypes())) {
                        return Result.error("无权操作：只能删除自己科目的试卷（ID=" + id + "）");
                    }
                }
            }
        }

        // 检查试卷是否被任何考试使用
        for (Integer id : ids) {
            long examCount = examInfoService.lambdaQuery()
                    .eq(com.web.domain.ExamInfo::getExamPaperId, id)
                    .count();
            if (examCount > 0) {
                ExamPaper examPaper = examPaperService.getById(id);
                String paperName = examPaper != null ? examPaper.getExamPaperName() : "ID=" + id;
                return Result.error("试卷「" + paperName + "」已被 " + examCount + " 个考试使用，无法删除");
            }
        }
        
        // 1. 先删除选题表中这些试卷的所有选题记录
        examPaperTopicService.lambdaUpdate()
                .in(ExamPaperTopic::getExamPaperId, Arrays.asList(ids))
                .remove();
        
        // 2、再批量删除试卷
        boolean result = examPaperService.removeBatchByIds(Arrays.asList(ids));
        if (!result) {
            return Result.error("删除失败");
        }
        
        // 清除试卷缓存
        for (Integer id : ids) {
            redisCacheService.deletePaperInfoCache(id);
            redisCacheService.deletePaperTopicsCache(id);
        }
        redisCacheService.clearPaperListCache();
        log.info("🗑️  清除试卷缓存（批量删除 {} 个试卷）", ids.length);
        
        // 记录操作日志
        operationLogUtil.logSuccess(request, "试卷管理", "删除", "批量删除试卷，共 " + ids.length + " 条");
        
        return Result.success(result);
    }

    /**
     * 分页查询
     * 
     * @param req
     * @return
     */
    @IgnoreAuth
    @PostMapping("/list")
    public Result list(@RequestBody ExamPaperReq req) {
        PageUtils page = examPaperService.selectPage(req);
        return Result.success(page);
    }

    /**
     * 加载所有试卷（支持按科目过滤和数据隔离）
     * 
     * @param kemuTypes 科目ID（可选）
     * @param request HTTP请求（用于获取Session）
     * @return 试卷列表
     */
    @GetMapping("/loadAll")
    public Result loadAll(@RequestParam(required = false) Integer kemuTypes, HttpServletRequest request) {
        // 从 Session 获取当前登录用户信息
        String role = (String) request.getSession().getAttribute("role");
        String tableName = (String) request.getSession().getAttribute("tableName");
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        
        System.out.println("\n========== [试卷列表加载] 开始 ==========");
        System.out.println("[试卷列表] 1. Session信息 - role: " + role + ", tableName: " + tableName + ", userId: " + userId);
        System.out.println("[试卷列表] 2. 请求参数 kemuTypes: " + kemuTypes);
        
        // 构建查询条件
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ExamPaper> queryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        
        // 如果是教师角色，强制过滤教师任教科目的试卷
        if (("teachers".equals(tableName) || "教师".equals(role)) && userId != null) {
            Teachers teacher = teachersService.getById(userId);
            if (teacher != null) {
                queryWrapper.eq("kemu_types", teacher.getKemuTypes());
                System.out.println("[试卷列表] 3. 教师角色 - 强制过滤科目: " + teacher.getKemuTypes());
            } else {
                System.out.println("[试卷列表] ❌ 警告：未找到教师信息，userId=" + userId);
                return Result.error("教师信息不存在");
            }
        } else if (kemuTypes != null) {
            // 管理员或其他角色，根据传入的科目参数过滤
            queryWrapper.eq("kemu_types", kemuTypes);
            System.out.println("[试卷列表] 3. 根据参数过滤科目: " + kemuTypes);
        } else {
            System.out.println("[试卷列表] 3. 无过滤条件 - 返回所有试卷");
        }
        
        // 只返回启用状态的试卷（exam_paper_types = 1）
        queryWrapper.eq("exam_paper_types", 1);
        queryWrapper.orderByDesc("create_time");
        
        List<ExamPaper> list = examPaperService.list(queryWrapper);
        System.out.println("[试卷列表] 4. 查询结果数量: " + list.size());
        System.out.println("========== [试卷列表加载] 结束 ==========\n");
        
        return Result.success(list);
    }

    /**
     * 测试试卷缓存功能
     * 
     * @param kemuTypes 科目ID（可选）
     * @return 测试结果
     */
    @GetMapping("/testCache")
    public Result testCache(@RequestParam(required = false) Integer kemuTypes) {
        log.info("\n========== 开始测试试卷缓存 ==========");
        
        // 测试1：试卷列表缓存
        long start1 = System.currentTimeMillis();
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ExamPaper> queryWrapper1 = 
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        if (kemuTypes != null) {
            queryWrapper1.eq("kemu_types", kemuTypes);
        }
        queryWrapper1.eq("exam_paper_types", 1);
        List<ExamPaper> list1 = examPaperService.list(queryWrapper1);
        long time1 = System.currentTimeMillis() - start1;
        log.info("第1次查询（无缓存）：耗时 {}ms，查询到 {} 条试卷", time1, list1.size());
        
        // 测试2：试卷列表缓存命中
        long start2 = System.currentTimeMillis();
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ExamPaper> queryWrapper2 = 
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        if (kemuTypes != null) {
            queryWrapper2.eq("kemu_types", kemuTypes);
        }
        queryWrapper2.eq("exam_paper_types", 1);
        List<ExamPaper> list2 = examPaperService.list(queryWrapper2);
        long time2 = System.currentTimeMillis() - start2;
        log.info("第2次查询（缓存命中）：耗时 {}ms，查询到 {} 条试卷", time2, list2.size());
        
        // 测试3：试卷详情缓存（如果有试卷）
        Long time3 = null, time4 = null;
        if (!list1.isEmpty()) {
            Integer paperId = list1.get(0).getId();
            
            long start3 = System.currentTimeMillis();
            Object info1 = examPaperService.info(paperId);
            time3 = System.currentTimeMillis() - start3;
            log.info("第3次查询（试卷详情，无缓存）：耗时 {}ms，试卷ID={}", time3, paperId);
            
            long start4 = System.currentTimeMillis();
            Object info2 = examPaperService.info(paperId);
            time4 = System.currentTimeMillis() - start4;
            log.info("第4次查询（试卷详情，缓存命中）：耗时 {}ms，试卷ID={}", time4, paperId);
        }
        
        // 计算性能提升
        double improvement1 = time2 > 0 ? (double) time1 / time2 : 0;
        Double improvement2 = (time3 != null && time4 != null && time4 > 0) ? (double) time3 / time4 : null;
        
        log.info("========== 试卷缓存测试完成 ==========\n");
        
        // 返回测试结果
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("试卷列表第1次查询耗时(ms)", time1);
        result.put("试卷列表第2次查询耗时(ms)", time2);
        result.put("试卷列表性能提升", String.format("%.1f倍", improvement1));
        if (time3 != null && time4 != null) {
            result.put("试卷详情第1次查询耗时(ms)", time3);
            result.put("试卷详情第2次查询耗时(ms)", time4);
            result.put("试卷详情性能提升", String.format("%.1f倍", improvement2));
        }
        result.put("查询到的试卷数量", list1.size());
        result.put("测试说明", "第1次查询从数据库获取，第2次查询从Redis缓存获取");
        
        return Result.success(result);
    }

}
