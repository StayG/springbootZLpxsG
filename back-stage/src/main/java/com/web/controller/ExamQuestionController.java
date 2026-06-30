package com.web.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.web.annotation.IgnoreAuth;
import com.web.domain.ExamPaperTopic;
import com.web.domain.ExamQuestion;
import com.web.domain.Req.ExamQuestionReq;
import com.web.domain.Teachers;
import com.web.domain.dto.ExamQuestionExcelDto;
import com.web.service.ExamInfoService;
import com.web.service.ExamPaperTopicService;
import com.web.service.ExamQuestionService;
import com.web.service.TeachersService;
import com.web.service.RedisCacheService;
import com.web.utils.OperationLogUtil;
import com.web.utils.PageUtils;
import com.web.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 试题 前端控制器
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/examQuestion")
public class ExamQuestionController {

    @Resource
    private ExamQuestionService examQuestionService;

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
    public Result page(@RequestBody ExamQuestionReq req, HttpServletRequest request) {
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
        
        // 生成缓存 key（包含试题名称筛选条件）
        String cacheKey = req.getPage() + ":" + req.getLimit() + ":" + 
                         (req.getKemuTypes() != null ? req.getKemuTypes() : "all") + ":" +
                         (req.getExamQuestionTypes() != null ? req.getExamQuestionTypes() : "all") + ":" +
                         (req.getExamQuestionName() != null && !req.getExamQuestionName().isEmpty() ? req.getExamQuestionName() : "all");
        
        // 先从缓存获取
        Object cachedData = redisCacheService.getQuestionListCache(cacheKey);
        if (cachedData != null) {
            return Result.success(cachedData);
        }
        
        // 缓存未命中，查询数据库
        PageUtils page = examQuestionService.selectPage(req);
        
        // 存入缓存（5分钟）
        redisCacheService.setQuestionListCache(cacheKey, page);
        
        return Result.success(page);
    }

    /**
     * 新增（自动关联教师科目）
     * 
     * @param examQuestion
     * @return
     */
    @PostMapping("/save")
    public Result save(@RequestBody ExamQuestion examQuestion, HttpServletRequest request) {
        // 从 Session 获取当前登录用户信息
        String role = (String) request.getSession().getAttribute("role");
        String tableName = (String) request.getSession().getAttribute("tableName");
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        
        System.out.println("\n========== [试题保存] 开始 ==========");
        System.out.println("[试题保存] 1. Session信息 - role: " + role + ", tableName: " + tableName + ", userId: " + userId);
        System.out.println("[试题保存] 2. 接收到的前端数据 - examQuestionName: " + examQuestion.getExamQuestionName());
        System.out.println("[试题保存] 3. 前端数据中的 kemuTypes: " + examQuestion.getKemuTypes());
        
        // ========== 后端数据校验 ==========
        String validateResult = validateExamQuestion(examQuestion);
        if (validateResult != null) {
            return Result.error(validateResult);
        }
        
        // 如果是教师角色，强制设置科目为教师的任教科目
        if (("teachers".equals(tableName) || "教师".equals(role)) && userId != null) {
            Teachers teacher = teachersService.getById(userId);
            if (teacher != null) {
                examQuestion.setKemuTypes(teacher.getKemuTypes());
                System.out.println("[试题保存] 4. 教师新增试题 - 强制设置kemuTypes: " + teacher.getKemuTypes());
            } else {
                System.out.println("[试题保存] ❌ 警告：未找到教师信息，userId=" + userId);
            }
        }
        
        System.out.println("[试题保存] 5. 最终保存的数据 - kemuTypes: " + examQuestion.getKemuTypes());
        System.out.println("========== [试题保存] 结束 ==========\n");
        
        examQuestionService.save(examQuestion);
        
        // 清除题目列表缓存
        redisCacheService.clearQuestionListCache();
        
        // 记录操作日志
        operationLogUtil.logSuccess(request, "试题管理", "新增", "新增试题：" + examQuestion.getExamQuestionName());
        
        return Result.success();
    }

    /**
     * 修改（保持教师科目不变）
     * 
     * @param examQuestion
     * @return
     */
    @PostMapping("/update")
    public Result update(@RequestBody ExamQuestion examQuestion, HttpServletRequest request) {
        // 从 Session 获取当前登录用户信息
        String role = (String) request.getSession().getAttribute("role");
        String tableName = (String) request.getSession().getAttribute("tableName");
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        
        System.out.println("\n========== [试题更新] 开始 ==========");
        System.out.println("[试题更新] 1. Session信息 - role: " + role + ", tableName: " + tableName + ", userId: " + userId);
        System.out.println("[试题更新] 2. 接收到的前端数据 - examQuestion.id: " + examQuestion.getId() + ", examQuestionName: " + examQuestion.getExamQuestionName());
        System.out.println("[试题更新] 3. 前端传入的 kemuTypes: " + examQuestion.getKemuTypes());
        
        // 查询数据库中原始的试题信息（用于对比）
        ExamQuestion originalQuestion = examQuestionService.getById(examQuestion.getId());
        if (originalQuestion != null) {
            System.out.println("[试题更新] 3.5. 数据库中的原始 kemuTypes: " + originalQuestion.getKemuTypes());
        }
        
        // ========== 后端数据校验 ==========
        String validateResult = validateExamQuestion(examQuestion);
        if (validateResult != null) {
            return Result.error(validateResult);
        }
        
        // 如果是教师角色，强制保持科目不变（防止越权修改）
        if (("teachers".equals(tableName) || "教师".equals(role)) && userId != null) {
            Teachers teacher = teachersService.getById(userId);
            if (teacher != null) {
                Integer frontendKemuTypes = examQuestion.getKemuTypes();
                Integer teacherKemuTypes = teacher.getKemuTypes();
                
                // 强制设置科目为教师的任教科目，无论前端传入什么值
                examQuestion.setKemuTypes(teacherKemuTypes);
                
                // 检测是否有篡改行为
                if (frontendKemuTypes != null && !frontendKemuTypes.equals(teacherKemuTypes)) {
                    System.out.println("[试题更新] ⚠️  检测到前端尝试篡改科目: " + frontendKemuTypes + " -> " + teacherKemuTypes + " (已强制修正)");
                }
                
                System.out.println("[试题更新] 4. 教师更新试题 - 强制覆盖kemuTypes: " + frontendKemuTypes + " -> " + teacherKemuTypes);
                System.out.println("[试题更新] 4.1. 教师姓名: " + teacher.getRealName() + ", 任教科目ID: " + teacherKemuTypes);
            } else {
                System.out.println("[试题更新] ❌ 警告：未找到教师信息，userId=" + userId);
                return Result.error("教师信息不存在，无法更新试题");
            }
        } else if ("managers".equals(tableName) || "管理员".equals(role)) {
            System.out.println("[试题更新] 4. 管理员更新试题 - 允许修改任意科目");
            System.out.println("[试题更新] 4.1. 保持前端传入的 kemuTypes: " + examQuestion.getKemuTypes());
        } else {
            System.out.println("[试题更新] 4. 未知角色 - 保持前端传入的 kemuTypes: " + examQuestion.getKemuTypes());
        }
        
        System.out.println("[试题更新] 5. 最终保存的数据 - kemuTypes: " + examQuestion.getKemuTypes());
        System.out.println("========== [试题更新] 结束 ==========\n");
        
        examQuestionService.updateById(examQuestion);
        
        // 清除题目缓存
        redisCacheService.deleteQuestionCache(examQuestion.getId());
        redisCacheService.clearQuestionListCache();
        
        // 记录操作日志
        operationLogUtil.logSuccess(request, "试题管理", "修改", "修改试题：" + examQuestion.getExamQuestionName());
        
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
        Object cachedQuestion = redisCacheService.getQuestionCache(id);
        if (cachedQuestion != null) {
            return Result.success(cachedQuestion);
        }
        
        // 缓存未命中，查询数据库
        Object questionInfo = examQuestionService.info(id);
        
        // 存入缓存（1小时）
        if (questionInfo != null) {
            redisCacheService.setQuestionCache(id, questionInfo);
        }
        
        return Result.success(questionInfo);
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
        
        // 查询要删除的试题
        ExamQuestion examQuestion = examQuestionService.getById(id);
        if (examQuestion == null) {
            return Result.error("试题不存在");
        }
        
        // 如果是教师角色，只能删除自己科目的试题
        if (("teachers".equals(tableName) || "教师".equals(role)) && userId != null) {
            Teachers teacher = teachersService.getById(userId);
            if (teacher != null && !teacher.getKemuTypes().equals(examQuestion.getKemuTypes())) {
                return Result.error("无权操作：只能删除自己科目的试题");
            }
        }

        // 检查试题是否被试卷使用
        List<ExamPaperTopic> examPaperTopicslist = examPaperTopicService.lambdaQuery()
                .eq(ExamPaperTopic::getExamQuestionId, id).list();
        if (examPaperTopicslist != null && examPaperTopicslist.size() > 0) {
            return Result.error("该试题已被 " + examPaperTopicslist.size() + " 个试卷使用，无法删除");
        }

        if (examInfoService.hasReleasedOrOngoingExamUsingQuestion(id)) {
            return Result.error("存在「已发布」或「进行中」的考试关联含该试题的试卷，无法删除");
        }
        
        // 删除试题
        String questionName = examQuestion.getExamQuestionName();
        examQuestionService.removeById(id);
        
        // 清除题目缓存
        redisCacheService.deleteQuestionCache(id);
        redisCacheService.clearQuestionListCache();

        // 记录操作日志
        operationLogUtil.logSuccess(request, "试题管理", "删除", "删除试题：" + questionName);

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
        if (ids == null || ids.length == 0) {
            return Result.error("请选择要删除的试题");
        }
        // 从 Session 获取当前登录用户信息
        String role = (String) request.getSession().getAttribute("role");
        String tableName = (String) request.getSession().getAttribute("tableName");
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        
        // 如果是教师角色，只能删除自己科目的试题
        if (("teachers".equals(tableName) || "教师".equals(role)) && userId != null) {
            Teachers teacher = teachersService.getById(userId);
            if (teacher != null) {
                for (Integer id : ids) {
                    ExamQuestion examQuestion = examQuestionService.getById(id);
                    if (examQuestion == null) {
                        return Result.error("试题不存在：ID=" + id);
                    }
                    if (!teacher.getKemuTypes().equals(examQuestion.getKemuTypes())) {
                        return Result.error("无权操作：只能删除自己科目的试题（ID=" + id + "）");
                    }
                }
            }
        }

        // 检查试题是否被试卷使用
        for (Integer id : ids) {
            List<ExamPaperTopic> examPaperTopicslist = examPaperTopicService.lambdaQuery()
                    .eq(ExamPaperTopic::getExamQuestionId, id).list();
            if (examPaperTopicslist != null && examPaperTopicslist.size() > 0) {
                ExamQuestion examQuestion = examQuestionService.getById(id);
                String questionName = examQuestion != null ? examQuestion.getExamQuestionName() : "ID=" + id;
                return Result.error("试题「" + questionName + "」已被 " + examPaperTopicslist.size() + " 个试卷使用，无法删除");
            }
        }

        for (Integer id : ids) {
            if (id != null && examInfoService.hasReleasedOrOngoingExamUsingQuestion(id)) {
                return Result.error("存在「已发布」或「进行中」的考试关联含所选试题的试卷（试题ID=" + id + "），无法删除");
            }
        }
        
        // 批量删除试题
        examQuestionService.removeBatchByIds(Arrays.asList(ids));
        
        // 清除题目缓存
        redisCacheService.clearQuestionListCache();

        // 记录操作日志
        operationLogUtil.logSuccess(request, "试题管理", "删除", "批量删除试题，共 " + ids.length + " 条");

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
    public Result list(@RequestBody ExamQuestionReq req) {
        PageUtils page = examQuestionService.selectPage(req);
        return Result.success(page);
    }

    /**
     * 加载所有
     * 
     * @return
     */
    @GetMapping("/loadAll")
    public Result loadAll() {
        return Result.success(examQuestionService.list());
    }

    /**
     * 获取试题数量
     * 
     * @return
     */
    @GetMapping("/getCurrentQuestionCounts/{kemuTypes}")
    public Result getCurrentQuestionCounts(@PathVariable String kemuTypes) {
        return Result.success(examQuestionService.getCurrentQuestionCounts(kemuTypes));
    }

    /**
     * Excel 批量导入试题
     * 
     * @param file Excel 文件
     * @return 导入结果
     */
    @PostMapping("/importExcel")
    public Result importExcel(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file == null || file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        // ========== 文件安全校验 ==========
        String validationError = validateExcelFile(file);
        if (validationError != null) {
            return Result.error(validationError);
        }

        try {
            Map<String, Object> result = examQuestionService.importExcel(file);
            
            // 记录操作日志
            Integer successCount = (Integer) result.get("successCount");
            operationLogUtil.logSuccess(request, "试题管理", "导入", "导入试题，成功 " + successCount + " 条");
            
            return Result.success(result);
        } catch (Exception e) {
            // 记录失败日志
            operationLogUtil.logFailure(request, "试题管理", "导入", "导入试题失败：" + e.getMessage());
            return Result.error("导入失败：" + e.getMessage());
        }
    }

    /**
     * 下载 Excel 模板
     * 
     * @param response HTTP 响应
     */
    @IgnoreAuth
    @GetMapping("/downloadTemplate")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        try {
            // 设置响应头
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");

            // 文件名编码处理，防止中文乱码
            String fileName = URLEncoder.encode("试题导入模板.xls", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName);

            // 使用 EasyExcel 直接写入响应
            EasyExcel.write(response.getOutputStream(), ExamQuestionExcelDto.class)
                    .sheet("模板")
                    .doWrite(getTemplateData());
        } catch (Exception e) {
            log.error("下载 Excel 模板失败：{}", e.getMessage(), e);
            try {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":500,\"msg\":\"下载模板失败：" + e.getMessage() + "\"}");
            } catch (IOException ex) {
                log.error("写入错误响应失败：{}", ex.getMessage(), ex);
            }
        }
    }

    /**
     * Excel 批量导出试题
     * 
     * @param req      查询条件
     * @param response HTTP 响应
     */
    @PostMapping("/exportExcel")
    public void exportExcel(@RequestBody(required = false) ExamQuestionReq req, HttpServletResponse response,
            HttpServletRequest request) throws IOException {
        try {
            // 设置响应头
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");

            // 文件名编码处理，防止中文乱码
            String fileName = URLEncoder.encode("试题数据_" + System.currentTimeMillis() + ".xls", "UTF-8")
                    .replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName);

            // 如果 req 为 null，创建一个新的对象
            if (req == null) {
                req = new ExamQuestionReq();
            }

            // 调用 Service 导出
            byte[] excelData = examQuestionService.exportExcel(req);
            response.getOutputStream().write(excelData);
            response.getOutputStream().flush();
            
            // 记录操作日志
            operationLogUtil.logSuccess(request, "试题管理", "导出", "导出试题数据");
        } catch (Exception e) {
            log.error("导出 Excel 失败：{}", e.getMessage(), e);
            // 记录失败日志
            operationLogUtil.logFailure(request, "试题管理", "导出", "导出试题失败：" + e.getMessage());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":500,\"msg\":\"导出失败：" + e.getMessage() + "\"}");
        }
    }

    /**
     * 获取模板数据
     */
    private List<ExamQuestionExcelDto> getTemplateData() {
        return CollUtil.newArrayList(
                createTemplateData("示例单选题", "{\"A\":\"选项 A\",\"B\":\"选项 B\",\"C\":\"选项 C\",\"D\":\"选项 D\"}", "A",
                        "这是答案解析", 1, 2, "基础概念", 1, 1),
                createTemplateData("示例多选题", "{\"A\":\"选项 A\",\"B\":\"选项 B\",\"C\":\"选项 C\",\"D\":\"选项 D\"}", "A,B",
                        "这是答案解析", 2, 2, "核心知识点", 2, 2),
                createTemplateData("示例判断题", null, "A", "这是答案解析（A:正确 B:错误）", 3, 2, "基本概念", 1, 3),
                createTemplateData("示例填空题", null, "北京", "这是答案解析", 4, 3, "地理常识", 3, 4),
                createTemplateData("示例简答题", null, "", "参考答案：这是一个简答题的参考答案，用于人工批改", 5, 2, "综合应用", 1, 5));
    }

    /**
     * 创建模板示例数据
     */
    private ExamQuestionExcelDto createTemplateData(String name, String options, String answer,
            String analysis, Integer type, Integer difficulty, String knowledgePoint, Integer kemu, Integer sequence) {
        ExamQuestionExcelDto dto = new ExamQuestionExcelDto();
        dto.setExamQuestionName(name);
        dto.setExamQuestionOptions(options);
        dto.setExamQuestionAnswer(answer);
        dto.setExamQuestionAnalysis(analysis);
        dto.setExamQuestionTypes(type);
        dto.setDifficultyLevel(difficulty);
        dto.setKnowledgePoint(knowledgePoint);
        dto.setKemuTypes(kemu);
        dto.setExamQuestionSequence(sequence);
        return dto;
    }

    /**
     * 后端试题数据校验逻辑
     * 
     * @param examQuestion 试题对象
     * @return 校验失败时返回错误信息，校验通过时返回 null
     */
    private String validateExamQuestion(ExamQuestion examQuestion) {
        if (examQuestion == null) {
            return "试题数据不能为空";
        }

        // 1. 基础字段校验
        if (examQuestion.getExamQuestionName() == null || examQuestion.getExamQuestionName().trim().isEmpty()) {
            return "试题名称不能为空";
        }

        if (examQuestion.getKemuTypes() == null) {
            return "请选择科目";
        }

        if (examQuestion.getExamQuestionTypes() == null) {
            return "请选择试题类型";
        }

        Integer type = examQuestion.getExamQuestionTypes();
        String answer = examQuestion.getExamQuestionAnswer();
        String optionsJson = examQuestion.getExamQuestionOptions();

        // 2. 按题型校验
        switch (type) {
            case 1: // 单选题
                if (answer == null || answer.trim().isEmpty()) {
                    return "单选题必须设置正确答案";
                }
                if (answer.length() != 1) {
                    return "单选题答案只能包含一个选项（如 A）";
                }
                if (optionsJson != null && !optionsJson.equals("[]")) {
                    try {
                        JSONArray optionsArray = JSONUtil.parseArray(optionsJson);
                        if (optionsArray.size() < 2) {
                            return "单选题至少需要 2 个选项";
                        }
                    } catch (Exception e) {
                        return "选项格式解析失败";
                    }
                }
                break;

            case 2: // 多选题
                if (answer == null || answer.trim().isEmpty()) {
                    return "多选题必须设置正确答案";
                }
                // 多选题答案至少包含一个选项
                String[] answers = answer.split(",");
                if (answers.length < 1) {
                    return "多选题至少需要选择一个正确答案";
                }
                if (optionsJson != null && !optionsJson.equals("[]")) {
                    try {
                        JSONArray optionsArray = JSONUtil.parseArray(optionsJson);
                        if (optionsArray.size() < 2) {
                            return "多选题至少需要 2 个选项";
                        }
                    } catch (Exception e) {
                        return "选项格式解析失败";
                    }
                }
                break;

            case 3: // 判断题
                if (answer == null || answer.trim().isEmpty()) {
                    return "判断题必须设置正确答案";
                }
                if (!answer.equals("A") && !answer.equals("B")) {
                    return "判断题答案只能是 'A' (对) 或 'B' (错)";
                }
                break;

            case 4: // 填空题
                if (answer == null || answer.trim().isEmpty()) {
                    return "填空题必须设置正确答案";
                }
                break;

            case 5: // 简答题
                // 简答题的参考答案是选填的，不做校验
                break;

            default:
                return "不支持的试题类型";
        }

        // 校验通过
        return null;
    }

    /**
     * Excel 文件上传安全校验
     * 
     * @param file 上传的 Excel 文件
     * @return 校验失败返回错误信息，校验通过返回 null
     */
    private String validateExcelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "上传文件不能为空";
        }

        // 1. 文件名校验
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return "文件名不能为空";
        }

        // 2. 文件名安全校验（防止路径穿越攻击）
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            return "文件名包含非法字符";
        }

        // 3. 文件扩展名校验
        if (!originalFilename.contains(".")) {
            return "文件必须包含扩展名";
        }

        String fileExt = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        
        // Excel 文件白名单
        if (!fileExt.equals("xls") && !fileExt.equals("xlsx")) {
            return "只允许上传 Excel 文件（xls、xlsx）";
        }

        // 4. 文件大小校验
        long fileSize = file.getSize();
        long maxSize = 10 * 1024 * 1024; // Excel 最大 10MB
        
        if (fileSize == 0) {
            return "文件大小不能为 0";
        }
        
        if (fileSize > maxSize) {
            return "Excel 文件大小不能超过 10MB";
        }

        // 5. 文件内容类型校验（MIME 类型）
        String contentType = file.getContentType();
        if (contentType == null || contentType.trim().isEmpty()) {
            return "无法识别文件类型";
        }

        // Excel MIME 类型白名单
        boolean isValidMime = contentType.equals("application/vnd.ms-excel") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                contentType.equals("application/octet-stream");
        
        if (!isValidMime) {
            return "文件类型不是有效的 Excel 格式";
        }

        // 校验通过
        return null;
    }

    /**
     * 测试题目缓存功能
     * 
     * @param kemuTypes 科目类型（可选）
     * @return
     */
    @GetMapping("/testCache")
    public Result testCache(@RequestParam(required = false) Integer kemuTypes) {
        try {
            // 构建查询条件
            ExamQuestionReq req = new ExamQuestionReq();
            req.setPage(1);
            req.setLimit(10);
            if (kemuTypes != null) {
                req.setKemuTypes(kemuTypes);
            }
            
            // 生成缓存 key
            String cacheKey = req.getPage() + ":" + req.getLimit() + ":" + 
                             (req.getKemuTypes() != null ? req.getKemuTypes() : "all") + ":all";
            
            long startTime1 = System.currentTimeMillis();
            
            // 第一次查询（应该从数据库查询）
            PageUtils page1 = examQuestionService.selectPage(req);
            
            long endTime1 = System.currentTimeMillis();
            long time1 = endTime1 - startTime1;
            
            // 模拟缓存写入
            redisCacheService.setQuestionListCache(cacheKey, page1);
            
            // 等待一小段时间
            Thread.sleep(100);
            
            long startTime2 = System.currentTimeMillis();
            
            // 第二次查询（应该从缓存获取）
            Object cachedData = redisCacheService.getQuestionListCache(cacheKey);
            
            long endTime2 = System.currentTimeMillis();
            long time2 = endTime2 - startTime2;
            
            // 检查 Redis 中是否有缓存
            boolean hasCached = cachedData != null;
            
            // 构建测试结果
            java.util.Map<String, Object> testResult = new java.util.HashMap<>();
            testResult.put("测试类型", "题目列表缓存测试");
            testResult.put("科目类型", kemuTypes != null ? kemuTypes : "全部");
            testResult.put("第一次查询耗时", time1 + "ms（从数据库）");
            testResult.put("第二次查询耗时", time2 + "ms（从缓存）");
            testResult.put("性能提升", time1 > 0 ? String.format("%.1f倍", (double)time1 / time2) : "N/A");
            testResult.put("缓存状态", hasCached ? "✅ 已缓存" : "❌ 未缓存");
            testResult.put("数据条数", page1.getList() != null ? page1.getList().size() : 0);
            testResult.put("总记录数", page1.getTotal());
            testResult.put("缓存Key", "question:list:" + cacheKey);
            testResult.put("缓存过期时间", "5分钟");
            
            if (time2 < time1 && hasCached) {
                testResult.put("测试结果", "✅ 缓存生效！第二次查询明显更快");
            } else if (!hasCached) {
                testResult.put("测试结果", "⚠️ 缓存未生效，请检查 Redis 连接");
            } else {
                testResult.put("测试结果", "⚠️ 性能提升不明显，可能是数据量太小");
            }
            
            // 应用场景说明
            testResult.put("应用场景", "题目列表查询、题库管理、试卷选题等");
            testResult.put("优化效果", "减少数据库查询，提升列表加载速度");
            
            return Result.success(testResult);
        } catch (Exception e) {
            e.printStackTrace();
            java.util.Map<String, Object> errorResult = new java.util.HashMap<>();
            errorResult.put("测试结果", "❌ 测试失败");
            errorResult.put("错误信息", e.getMessage());
            return Result.error("缓存测试失败：" + e.getMessage());
        }
    }

}
