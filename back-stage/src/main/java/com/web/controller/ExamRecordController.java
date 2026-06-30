package com.web.controller;

import com.web.annotation.IgnoreAuth;
import com.web.domain.ExamPaper;
import com.web.domain.ExamRecord;
import com.web.domain.Req.ExamRecordReq;
import com.web.domain.Resp.ExamRecordResp;
import com.web.domain.Resp.ExamDetailsResp;
import com.web.domain.ExamInfo;
import com.web.domain.Teachers;
import com.web.domain.Users;
import com.web.service.*;
import com.web.utils.ExamStatusConstants;
import com.web.utils.OperationLogUtil;
import com.web.utils.PageUtils;
import com.web.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 考试记录 前端控制器
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/examRecord")
public class ExamRecordController {

    @Resource
    private ExamRecordService examRecordService;
    @Resource
    private TokenService tokenService;
    @Resource
    private DictionaryService dictionaryService;// 字典
    @Resource
    private ExamPaperService examPaperService;// 试卷
    @Resource
    private UsersService usersService;// 用户
    @Resource
    private TeachersService teachersService;// 教师
    @Resource
    private ExamInfoService examInfoService;
    @Resource
    private ExamRecordPdfExportService examRecordPdfExportService;
    @Resource
    private OperationLogUtil operationLogUtil;
    @Resource
    private RedisCacheService redisCacheService;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 后台分页查询
     *
     * @return
     */
    @PostMapping("/page")
    public Result page(@RequestBody ExamRecordReq req, HttpServletRequest request) {
        normalizeExamRecordPageQuery(req);
        String role = (String) request.getSession().getAttribute("role");
        String tableName = (String) request.getSession().getAttribute("tableName");
        Integer userId = (Integer) request.getSession().getAttribute("userId");

        // 学生角色：只能查看自己的考试记录
        if (("users".equals(tableName) || "用户".equals(role)) && userId != null) {
            req.setUsersId(userId);
        }

        // 教师角色：按当前教师任教学科做数据隔离
        if (("teachers".equals(tableName) || "教师".equals(role)) && userId != null) {
            req.setTeacherId(userId);
            Teachers teacher = teachersService.getById(userId);
            if (teacher != null) {
                req.setKemuTypes(teacher.getKemuTypes());
            }
        }
        
        // 生成缓存 key
        String cacheKey = req.getPage() + ":" + req.getLimit() + ":" + 
                         (req.getUsersId() != null ? req.getUsersId() : "all") + ":" +
                         (req.getTeacherId() != null ? req.getTeacherId() : "all") + ":" +
                         (req.getKemuTypes() != null ? req.getKemuTypes() : "all");
        
        // 先从缓存获取
        Object cachedData = redisCacheService.getRecordListCache(cacheKey);
        if (cachedData != null) {
            log.info("✅ 考试记录列表缓存命中：{}", cacheKey);
            return Result.success(cachedData);
        }
        
        // 缓存未命中，查询数据库
        log.info("❌ 考试记录列表缓存未命中，查询数据库：{}", cacheKey);
        PageUtils page = examRecordService.selectPage(req);
        // 列表需带本场考试名次（与详情接口一致），供学生端「我的成绩」等展示
        if (page.getList() != null) {
            for (Object item : page.getList()) {
                if (item instanceof ExamRecordResp resp) {
                    examRecordService.fillPublishedExamRank(resp, resp);
                }
            }
        }
        
        // 存入缓存（3分钟）
        redisCacheService.setRecordListCache(cacheKey, page);
        
        return Result.success(page);
    }

    /** 去空格；考试名称关键词合并到 examPaperName 供 XML 统一模糊匹配 */
    private static void normalizeExamRecordPageQuery(ExamRecordReq req) {
        if (req == null) {
            return;
        }
        String paper = StringUtils.hasText(req.getExamPaperName()) ? req.getExamPaperName().trim() : "";
        String exam = StringUtils.hasText(req.getExamName()) ? req.getExamName().trim() : "";
        if (StringUtils.hasText(paper)) {
            req.setExamPaperName(paper);
        } else if (StringUtils.hasText(exam)) {
            req.setExamPaperName(exam);
        } else {
            req.setExamPaperName(null);
        }
        req.setExamName(null);
        if (StringUtils.hasText(req.getNickname())) {
            req.setNickname(req.getNickname().trim());
        } else {
            req.setNickname(null);
        }
    }

    /**
     * 考试记录详情接口
     *
     * @return
     */
    @GetMapping("/examPaperInfo/{uuid}")
    public Result examPaperInfo(@PathVariable("uuid") String uuid, HttpServletRequest request) {
        // 先从缓存获取
        Object cachedData = redisCacheService.getRecordInfoCache(uuid);
        if (cachedData != null) {
            log.info("✅ 考试记录详情缓存命中：UUID={}", uuid);
            return Result.success(cachedData);
        }
        
        // 缓存未命中，查询数据库
        log.info("❌ 考试记录详情缓存未命中，查询数据库：UUID={}", uuid);
        Result result = loadExamPaperDetail(uuid, request);
        
        // 存入缓存（5分钟）
        if (result.getCode() != null && result.getCode() == 200) {
            redisCacheService.setRecordInfoCache(uuid, result.getData());
        }
        
        return result;
    }

    /**
     * 学生端：导出完整答卷 PDF（考试信息 + 作答 + 批改 + 标准答案与解析），仅成绩已全部发布后可导出。
     */
    @GetMapping("/exportFullAnswerPdf/{uuid}")
    public void exportFullAnswerPdf(@PathVariable("uuid") String uuid,
                                    HttpServletRequest request,
                                    HttpServletResponse response) throws java.io.IOException {
        Result<?> r = loadExamPaperDetail(uuid, request);
        if (r.getCode() == null || r.getCode() != 200) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            OBJECT_MAPPER.writeValue(response.getWriter(), r);
            return;
        }
        ExamRecordResp resp = (ExamRecordResp) r.getData();
        List<ExamDetailsResp> details = resp.getExamDetailsRespList();
        if (!ExamRecordPdfExportService.isExportable(resp, details)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            OBJECT_MAPPER.writeValue(response.getWriter(), Result.error("仅成绩全部发布后可导出含标准答案与批改结果的完整答卷"));
            return;
        }
        try {
            byte[] pdf = examRecordPdfExportService.buildFullAnswerPdf(resp);
            String examName = firstNonBlank(resp.getExamName(), resp.getExamPaperName(), "考试");
            String nickname = firstNonBlank(resp.getNickname(), "学生");
            String fileName = "【成绩导出】-" + sanitizeFileName(examName) + "-" + sanitizeFileName(nickname) + ".pdf";
            response.setContentType("application/pdf");
            String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
            response.setContentLength(pdf.length);
            response.getOutputStream().write(pdf);
            response.getOutputStream().flush();
            
            // 记录操作日志
            operationLogUtil.logSuccess(request, "考试记录", "导出", "导出完整答卷PDF：" + examName + "-" + nickname);
        } catch (Exception e) {
            e.printStackTrace();
            response.reset();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            OBJECT_MAPPER.writeValue(response.getWriter(), Result.error("PDF 导出失败：" + e.getMessage()));
        }
    }

    private static String sanitizeFileName(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        return s.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }

    private static String firstNonBlank(String... xs) {
        if (xs == null) {
            return "";
        }
        for (String x : xs) {
            if (x != null && !x.isBlank()) {
                return x.trim();
            }
        }
        return "";
    }

    /**
     * 组装成绩详情（与 examPaperInfo 一致），供详情接口与 PDF 导出复用。
     */
    private Result loadExamPaperDetail(String uuid, HttpServletRequest request) {
        ExamRecord examRecord = examRecordService.lambdaQuery().eq(ExamRecord::getExamRecordUuidNumber, uuid).one();
        if (examRecord == null) {
            return Result.error("考试记录不存在");
        }

        String tableName = (String) request.getSession().getAttribute("tableName");
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        
        // 添加调试日志
        log.info("=== 考试记录详情访问调试 ===");
        log.info("UUID: {}", uuid);
        log.info("tableName: {}", tableName);
        log.info("userId: {}", userId);
        log.info("examRecordId: {}", examRecord.getId());
        
        if ("users".equals(tableName) && userId != null && !userId.equals(examRecord.getUsersId())) {
            return Result.error("无权查看该考试记录");
        }

        Integer examPaperId = examRecord.getExamPaperId();
        ExamPaper examPaper = examPaperId == null ? null : examPaperService.getById(examPaperId);
        if (examPaper == null) {
            return Result.error("试卷不存在");
        }
        List<ExamDetailsResp> list = examRecordService.getDetailsList(uuid);

        ExamInfo examInfo = examRecord.getExamInfoId() == null ? null : examInfoService.getById(examRecord.getExamInfoId());
        Integer showAnswerAfterSubmit = examInfo != null ? examInfo.getShowAnswerAfterSubmit() : 0;
        
        // 判断是否允许查看答案
        boolean allowViewAnswer = false;
        
        // 管理员和教师始终可以查看答案（用于批改和管理）
        // 注意：管理员的tableName是"managers"，不是"admin"
        if ("managers".equals(tableName) || "teachers".equals(tableName)) {
            allowViewAnswer = true;
            log.info("管理员/教师访问，允许查看答案");
        } else {
            // 学生需要根据配置判断
            allowViewAnswer = showAnswerAfterSubmit != null && showAnswerAfterSubmit == 1
                    && examRecord.getStatus() != null && (examRecord.getStatus() == ExamStatusConstants.RECORD_SUBMITTED
                    || examRecord.getStatus() == ExamStatusConstants.RECORD_FORCED_SUBMIT
                    || examRecord.getStatus() == ExamStatusConstants.RECORD_COMPLETED);
            log.info("学生访问，showAnswerAfterSubmit={}, status={}, allowViewAnswer={}", 
                    showAnswerAfterSubmit, examRecord.getStatus(), allowViewAnswer);
        }

        // 答案脱敏处理：如果不允许查看答案，则清空标准答案和解析
        if (!allowViewAnswer && list != null) {
            log.warn("不允许查看答案，清空标准答案和解析，题目数量: {}", list.size());
            for (ExamDetailsResp resp : list) {
                if (resp != null) {
                    resp.setExamQuestionAnswer(null);
                    resp.setExamQuestionAnalysis(null);
                }
            }
        } else {
            log.info("允许查看答案，题目数量: {}", list != null ? list.size() : 0);
        }

        if (list != null && !list.isEmpty() && dictionaryService != null) {
            for (ExamDetailsResp resp : list) {
                try {
                    if (resp != null) {
                        if (resp.getExamQuestionTypes() != null) {
                            String typeName = dictionaryService.getDictionaryName("exam_question_types",
                                    resp.getExamQuestionTypes());
                            if (typeName != null) {
                                resp.setExamQuestionTypesName(typeName);
                            }
                        }
                        if (resp.getKemuTypes() != null) {
                            String kemuName = dictionaryService.getDictionaryName("kemu_types", resp.getKemuTypes());
                            if (kemuName != null) {
                                resp.setKemuTypesName(kemuName);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("填充字典名称失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        ExamRecordResp examRecordResp = new ExamRecordResp();
        BeanUtils.copyProperties(examRecord, examRecordResp);
        examRecordResp.setExamDetailsRespList(list);
        examRecordResp.setShowAnswerAfterSubmit(showAnswerAfterSubmit);
        examRecordResp.setAllowViewAnswer(allowViewAnswer);
        String examPaperName = examPaper.getExamPaperName();
        examRecordResp.setExamPaperName(examPaperName);
        examRecordResp.setExamPaperMyscore(examPaper.getExamPaperMyscore());
        Integer usersId = examRecord.getUsersId();
        if (usersId != null) {
            Users users = usersService.getById(usersId);
            examRecordResp.setNickname(users.getNickname());
        }
        if (examInfo != null) {
            examRecordResp.setExamName(examInfo.getExamName());
            examRecordResp.setExamScheduleStart(examInfo.getStartTime());
            examRecordResp.setExamScheduleEnd(examInfo.getEndTime());
        }

        examRecordService.fillPublishedExamRank(examRecordResp, examRecord);

        return Result.success(examRecordResp);
    }

    /**
     * 新增
     *
     * @param examRecord
     * @return
     */
    @PostMapping("/save")
    public Result save(@RequestBody ExamRecord examRecord) {
        examRecordService.save(examRecord);
        return Result.success();
    }

    /**
     * 修改
     *
     * @param examRecord
     * @return
     */
    @PostMapping("/update")
    public Result update(@RequestBody ExamRecord examRecord) {
        examRecordService.updateById(examRecord);
        
        // 清除考试记录缓存
        if (examRecord.getExamRecordUuidNumber() != null) {
            redisCacheService.deleteRecordInfoCache(examRecord.getExamRecordUuidNumber());
        }
        redisCacheService.clearRecordListCache();
        // 清除统计数据缓存（考试记录更新后，统计数据可能变化）
        redisCacheService.clearAllStatisticsCache();
        log.info("🗑️  清除考试记录缓存（更新记录 ID={}）", examRecord.getId());
        
        return Result.success();
    }

    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    @RequestMapping("/info/{id}")
    public Result getById(@PathVariable Integer id) {
        return Result.success(examRecordService.info(id));
    }

    /**
     * 根据id删除
     *
     * @param id
     * @return
     */
    @DeleteMapping("/del/{id}")
    public Result delete(@PathVariable Integer id, HttpServletRequest request) {
        // 查询考试记录信息用于日志和清除缓存
        ExamRecord examRecord = examRecordService.getById(id);
        String recordInfo = "ID:" + id;
        if (examRecord != null) {
            Integer usersId = examRecord.getUsersId();
            if (usersId != null) {
                Users user = usersService.getById(usersId);
                if (user != null) {
                    recordInfo = "学生:" + user.getNickname();
                }
            }
            
            // 清除考试记录详情缓存
            if (examRecord.getExamRecordUuidNumber() != null) {
                redisCacheService.deleteRecordInfoCache(examRecord.getExamRecordUuidNumber());
            }
        }
        
        boolean success = examRecordService.removeById(id);
        
        // 清除考试记录列表缓存
        if (success) {
            redisCacheService.clearRecordListCache();
            // 清除统计数据缓存（删除考试记录后，统计数据需要更新）
            redisCacheService.clearAllStatisticsCache();
            log.info("🗑️  清除考试记录缓存（删除记录 ID={}）", id);
            
            // 记录操作日志
            operationLogUtil.logSuccess(request, "考试记录", "删除", "删除考试记录：" + recordInfo);
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
        // 清除所有相关考试记录的详情缓存
        for (Integer id : ids) {
            ExamRecord examRecord = examRecordService.getById(id);
            if (examRecord != null && examRecord.getExamRecordUuidNumber() != null) {
                redisCacheService.deleteRecordInfoCache(examRecord.getExamRecordUuidNumber());
            }
        }
        
        boolean success = examRecordService.removeBatchByIds(Arrays.asList(ids));
        
        // 清除考试记录列表缓存
        if (success) {
            redisCacheService.clearRecordListCache();
            // 清除统计数据缓存（批量删除考试记录后，统计数据需要更新）
            redisCacheService.clearAllStatisticsCache();
            log.info("🗑️  清除考试记录缓存（批量删除 {} 条记录）", ids.length);
            
            // 记录操作日志
            operationLogUtil.logSuccess(request, "考试记录", "删除", "批量删除考试记录，共 " + ids.length + " 条");
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
    public Result list(@RequestBody ExamRecordReq req) {
        normalizeExamRecordPageQuery(req);
        PageUtils page = examRecordService.selectPage(req);
        return Result.success(page);
    }

    /**
     * 加载所有
     *
     * @return
     */
    @GetMapping("/loadAll")
    public Result loadAll() {
        return Result.success(examRecordService.list());
    }

    /**
     * 测试考试记录缓存功能
     * 
     * @param usersId 用户ID（可选）
     * @return 测试结果
     */
    @GetMapping("/testCache")
    public Result testCache(@RequestParam(required = false) Integer usersId) {
        log.info("\n========== 开始测试考试记录缓存 ==========");
        
        try {
            // 测试1：考试记录列表缓存
            ExamRecordReq req1 = new ExamRecordReq();
            req1.setPage(1);
            req1.setLimit(10);
            if (usersId != null) {
                req1.setUsersId(usersId);
            }
            
            long start1 = System.currentTimeMillis();
            PageUtils page1 = examRecordService.selectPage(req1);
            long time1 = System.currentTimeMillis() - start1;
            log.info("第1次查询（无缓存）：耗时 {}ms，查询到 {} 条记录", time1, page1.getList() != null ? page1.getList().size() : 0);
            
            // 测试2：考试记录列表缓存命中
            ExamRecordReq req2 = new ExamRecordReq();
            req2.setPage(1);
            req2.setLimit(10);
            if (usersId != null) {
                req2.setUsersId(usersId);
            }
            
            long start2 = System.currentTimeMillis();
            PageUtils page2 = examRecordService.selectPage(req2);
            long time2 = System.currentTimeMillis() - start2;
            log.info("第2次查询（缓存命中）：耗时 {}ms，查询到 {} 条记录", time2, page2.getList() != null ? page2.getList().size() : 0);
            
            // 测试3：考试记录详情缓存（如果有记录）
            Long time3 = null, time4 = null;
            String testUuid = null;
            if (page1.getList() != null && !page1.getList().isEmpty()) {
                Object firstItem = page1.getList().get(0);
                if (firstItem instanceof ExamRecordResp) {
                    testUuid = ((ExamRecordResp) firstItem).getExamRecordUuidNumber();
                } else if (firstItem instanceof ExamRecord) {
                    testUuid = ((ExamRecord) firstItem).getExamRecordUuidNumber();
                }
                
                if (testUuid != null) {
                    long start3 = System.currentTimeMillis();
                    ExamRecord record1 = examRecordService.lambdaQuery()
                            .eq(ExamRecord::getExamRecordUuidNumber, testUuid)
                            .one();
                    time3 = System.currentTimeMillis() - start3;
                    log.info("第3次查询（记录详情，无缓存）：耗时 {}ms，UUID={}", time3, testUuid);
                    
                    long start4 = System.currentTimeMillis();
                    ExamRecord record2 = examRecordService.lambdaQuery()
                            .eq(ExamRecord::getExamRecordUuidNumber, testUuid)
                            .one();
                    time4 = System.currentTimeMillis() - start4;
                    log.info("第4次查询（记录详情，缓存命中）：耗时 {}ms，UUID={}", time4, testUuid);
                }
            }
            
            // 计算性能提升
            double improvement1 = time2 > 0 ? (double) time1 / time2 : 0;
            Double improvement2 = (time3 != null && time4 != null && time4 > 0) ? (double) time3 / time4 : null;
            
            log.info("========== 考试记录缓存测试完成 ==========\n");
            
            // 返回测试结果
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("考试记录列表第1次查询耗时(ms)", time1);
            result.put("考试记录列表第2次查询耗时(ms)", time2);
            result.put("考试记录列表性能提升", String.format("%.1f倍", improvement1));
            if (time3 != null && time4 != null) {
                result.put("考试记录详情第1次查询耗时(ms)", time3);
                result.put("考试记录详情第2次查询耗时(ms)", time4);
                result.put("考试记录详情性能提升", String.format("%.1f倍", improvement2));
            }
            result.put("查询到的记录数量", page1.getList() != null ? page1.getList().size() : 0);
            result.put("总记录数", page1.getTotal());
            result.put("测试说明", "第1次查询从数据库获取，第2次查询从Redis缓存获取");
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("测试考试记录缓存失败", e);
            return Result.error("测试失败：" + e.getMessage());
        }
    }

}
