package com.web.controller;

import com.web.annotation.IgnoreAuth;
import com.web.domain.ExamQuestion;
import com.web.domain.ExamWrongQuestion;
import com.web.domain.Req.ExamWrongQuestionRedoReq;
import com.web.domain.Req.ExamWrongQuestionReq;
import com.web.service.ExamQuestionService;
import com.web.service.ExamWrongQuestionService;
import com.web.utils.OperationLogUtil;
import com.web.utils.PageUtils;
import com.web.utils.Result;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 错题 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/examWrongQuestion")
public class ExamWrongQuestionController {

    @Resource
    private ExamWrongQuestionService examWrongQuestionService;

    @Resource
    private ExamQuestionService examQuestionService;
    
    @Resource
    private OperationLogUtil operationLogUtil;

    /**
     * 后台分页查询
     * 
     * @return
     */
    @PostMapping("/page")
    public Result page(@RequestBody ExamWrongQuestionReq req, HttpServletRequest request) {
        Object tableNameObj = request.getSession().getAttribute("tableName");
        String tableName = tableNameObj != null ? String.valueOf(tableNameObj) : null;
        Integer sessionUserId = (Integer) request.getSession().getAttribute("userId");
        // 学生端：仅用会话用户筛选，忽略请求体中的 usersId，防止越权查询他人错题
        if ("users".equals(tableName) && sessionUserId != null) {
            req.setUsersId(sessionUserId);
        }

        PageUtils pageUtils = examWrongQuestionService.selectPage(req);
        // 学生端错题集列表：不直接下发正确答案与解析（由「查看答案/解析」单独拉取）；非学生端列表仍脱敏
        try {
            if (pageUtils != null && pageUtils.getList() != null) {
                boolean isStudent = "users".equals(tableName);
                for (Object item : pageUtils.getList()) {
                    if (item instanceof com.web.domain.Resp.ExamWrongQuestionResp resp) {
                        if (isStudent && sessionUserId != null) {
                            resp.setExamQuestionAnswer(null);
                            resp.setExamQuestionAnalysis(null);
                            resp.setAllowViewAnswer(null);
                        } else {
                            resp.setExamQuestionAnswer(null);
                            resp.setExamQuestionAnalysis(null);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return Result.success(pageUtils);
    }

    /**
     * 新增
     * 
     * @param examWrongQuestion
     * @return
     */
    @PostMapping("/save")
    public Result save(@RequestBody ExamWrongQuestion examWrongQuestion) {
        examWrongQuestionService.save(examWrongQuestion);
        return Result.success();
    }

    /**
     * 修改
     * 
     * @param examWrongQuestion
     * @return
     */
    @PostMapping("/update")
    public Result update(@RequestBody ExamWrongQuestion examWrongQuestion) {
        examWrongQuestionService.updateById(examWrongQuestion);
        return Result.success();
    }

    /**
     * 根据id查询
     * 
     * @param id
     * @return
     */
    @GetMapping("/info/{id}")
    public Result getById(@PathVariable Integer id, jakarta.servlet.http.HttpServletRequest request) {
        Object tableNameObj = request.getSession().getAttribute("tableName");
        String tableName = tableNameObj != null ? String.valueOf(tableNameObj) : null;
        Integer userId = (Integer) request.getSession().getAttribute("userId");

        com.web.domain.Resp.ExamWrongQuestionResp resp = examWrongQuestionService.info(id);
        if (resp == null) {
            return Result.error("记录不存在");
        }

        // 学生只能查看自己的错题
        if ("users".equals(tableName) && userId != null) {
            try {
                if (resp.getUsersId() != null && !String.valueOf(userId).equals(String.valueOf(resp.getUsersId()))) {
                    return Result.error("无权查看该记录");
                }
            } catch (Exception ignored) {
                return Result.error("无权查看该记录");
            }

            // 错题集始终允许查看答案和解析，不受"交卷后显示答案"设置的影响
            // 因为错题集的目的就是帮助学生复习和学习错题
            resp.setAllowViewAnswer(true);
        }

        return Result.success(resp);
    }

    /**
     * 学生重做练习：仅题目与选项，不含上次错误答案、标准答案与解析、考试信息。
     */
    @GetMapping("/practice/{id}")
    public Result practice(@PathVariable Integer id, HttpServletRequest request) {
        Object tableNameObj = request.getSession().getAttribute("tableName");
        String tableName = tableNameObj != null ? String.valueOf(tableNameObj) : null;
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        if (!"users".equals(tableName) || userId == null) {
            return Result.error("请使用学生账号登录后重试");
        }
        com.web.domain.Resp.ExamWrongQuestionResp resp = examWrongQuestionService.info(id);
        if (resp == null) {
            return Result.error("记录不存在");
        }
        try {
            if (resp.getUsersId() != null && !String.valueOf(userId).equals(String.valueOf(resp.getUsersId()))) {
                return Result.error("无权练习该题");
            }
        } catch (Exception e) {
            return Result.error("无权练习该题");
        }
        resp.setExamQuestionAnswer(null);
        resp.setExamQuestionAnalysis(null);
        resp.setExamDetailsMyanswer(null);
        resp.setExamPaperName(null);
        resp.setExamPaperDate(null);
        resp.setExamPaperMyscore(null);
        resp.setExamDetailsMyscore(null);
        resp.setShowAnswerAfterSubmit(null);
        resp.setExamRecordStatus(null);
        resp.setAllowViewAnswer(null);
        return Result.success(resp);
    }

    /**
     * 提交错题重做：客观题自动判分并更新掌握状态；填空/简答不自动判分、不自动变更掌握状态。
     */
    @PostMapping("/redoSubmit")
    public Result redoSubmit(@RequestBody ExamWrongQuestionRedoReq body, HttpServletRequest request) {
        Object tableNameObj = request.getSession().getAttribute("tableName");
        String tableName = tableNameObj != null ? String.valueOf(tableNameObj) : null;
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        if (!"users".equals(tableName) || userId == null) {
            return Result.error("请使用学生账号登录后重试");
        }
        if (body == null || body.getId() == null) {
            return Result.error("缺少错题记录编号");
        }
        ExamWrongQuestion row = examWrongQuestionService.getById(body.getId());
        if (row == null) {
            return Result.error("记录不存在");
        }
        if (row.getUsersId() != null && !String.valueOf(userId).equals(String.valueOf(row.getUsersId()))) {
            return Result.error("无权提交该题");
        }
        if (row.getExamQuestionId() == null || row.getExamQuestionId().trim().isEmpty()) {
            return Result.error("题目数据不完整");
        }
        ExamQuestion question;
        try {
            question = examQuestionService.getById(Integer.valueOf(row.getExamQuestionId().trim()));
        } catch (Exception e) {
            return Result.error("题目不存在或已删除");
        }
        if (question == null) {
            return Result.error("题目不存在或已删除");
        }
        String mine = normalizeRedoAnswer(body.getRedoAnswer());
        if (isBlankAnswer(mine)) {
            return Result.error("请先完成作答后再提交");
        }
        Integer qType = question.getExamQuestionTypes();
        int type = qType != null ? qType : 0;
        String correct = question.getExamQuestionAnswer() != null ? question.getExamQuestionAnswer() : "";
        String analysis = question.getExamQuestionAnalysis() != null ? question.getExamQuestionAnalysis() : "";

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("yourAnswer", mine);
        out.put("correctAnswer", correct);
        out.put("analysis", analysis);

        if (type == 1 || type == 3) {
            boolean ok = singleAnswerEquals(correct, mine);
            row.setMasteryStatus(ok ? 1 : 0);
            examWrongQuestionService.updateById(row);
            out.put("objective", true);
            out.put("correct", ok);
            out.put("masteryStatus", row.getMasteryStatus());
            
            // 记录操作日志
            String questionName = question.getExamQuestionName() != null ? question.getExamQuestionName() : "题目ID:" + question.getId();
            operationLogUtil.logSuccess(request, "错题本", "重做", "重做错题：" + questionName + "，结果：" + (ok ? "正确" : "错误"));
            
            return Result.success(out);
        }
        if (type == 2) {
            boolean ok = multiAnswerEquals(correct, mine);
            row.setMasteryStatus(ok ? 1 : 0);
            examWrongQuestionService.updateById(row);
            out.put("objective", true);
            out.put("correct", ok);
            out.put("masteryStatus", row.getMasteryStatus());
            
            // 记录操作日志
            String questionName = question.getExamQuestionName() != null ? question.getExamQuestionName() : "题目ID:" + question.getId();
            operationLogUtil.logSuccess(request, "错题本", "重做", "重做错题：" + questionName + "，结果：" + (ok ? "正确" : "错误"));
            
            return Result.success(out);
        }
        out.put("objective", false);
        out.put("subjective", true);
        out.put("referenceAnswer", correct);
        
        // 记录操作日志（主观题）
        String questionName = question.getExamQuestionName() != null ? question.getExamQuestionName() : "题目ID:" + question.getId();
        operationLogUtil.logSuccess(request, "错题本", "重做", "重做错题（主观题）：" + questionName);
        
        return Result.success(out);
    }

    private static String normalizeRedoAnswer(String raw) {
        if (raw == null) {
            return "";
        }
        String s = String.valueOf(raw).trim();
        if ("null".equalsIgnoreCase(s) || "undefined".equalsIgnoreCase(s)) {
            return "";
        }
        return s;
    }

    private static boolean isBlankAnswer(String s) {
        return s == null || s.isEmpty() || "未作答".equals(s);
    }

    private static boolean singleAnswerEquals(String correct, String mine) {
        if (correct == null) {
            correct = "";
        }
        if (mine == null) {
            mine = "";
        }
        return correct.trim().equals(mine.trim());
    }

    private static boolean multiAnswerEquals(String correct, String mine) {
        List<String> ca = splitChoiceTokens(correct);
        List<String> ma = splitChoiceTokens(mine);
        if (ca.size() != ma.size()) {
            return false;
        }
        ca.sort(String::compareTo);
        ma.sort(String::compareTo);
        return ca.equals(ma);
    }

    private static List<String> splitChoiceTokens(String s) {
        if (s == null || s.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String[] parts = s.split("[,，]");
        List<String> out = new ArrayList<>();
        for (String p : parts) {
            String t = p != null ? p.trim() : "";
            if (!t.isEmpty()) {
                out.add(t);
            }
        }
        return out;
    }

    /**
     * 根据id删除
     * 
     * @param id
     * @return
     */
    @DeleteMapping("/del/{id}")
    public Result delete(@PathVariable Integer id, HttpServletRequest request) {
        // 查询错题信息用于日志
        ExamWrongQuestion wrongQuestion = examWrongQuestionService.getById(id);
        String questionInfo = "ID:" + id;
        if (wrongQuestion != null && wrongQuestion.getExamQuestionId() != null) {
            try {
                ExamQuestion question = examQuestionService.getById(Integer.valueOf(wrongQuestion.getExamQuestionId()));
                if (question != null && question.getExamQuestionName() != null) {
                    questionInfo = question.getExamQuestionName();
                }
            } catch (Exception ignored) {
            }
        }
        
        boolean success = examWrongQuestionService.removeById(id);
        
        // 记录操作日志
        if (success) {
            operationLogUtil.logSuccess(request, "错题本", "删除", "删除错题：" + questionInfo);
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
        boolean success = examWrongQuestionService.removeBatchByIds(Arrays.asList(ids));
        
        // 记录操作日志
        if (success) {
            operationLogUtil.logSuccess(request, "错题本", "删除", "批量删除错题，共 " + ids.length + " 条");
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
    public Result list(@RequestBody ExamWrongQuestionReq req) {
        PageUtils page = examWrongQuestionService.selectPage(req);
        try {
            if (page != null && page.getList() != null) {
                for (Object item : page.getList()) {
                    if (item instanceof com.web.domain.Resp.ExamWrongQuestionResp resp) {
                        resp.setExamQuestionAnswer(null);
                        resp.setExamQuestionAnalysis(null);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return Result.success(page);
    }

    /**
     * 获取某道题的历史错误记录（用于查看历史作答）
     */
    @GetMapping("/history/{id}")
    public Result getHistory(@PathVariable Integer id, HttpServletRequest request) {
        Object tableNameObj = request.getSession().getAttribute("tableName");
        String tableName = tableNameObj != null ? String.valueOf(tableNameObj) : null;
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        
        if (!"users".equals(tableName) || userId == null) {
            return Result.error("请使用学生账号登录后重试");
        }
        
        // 先获取当前错题记录，确认权限和获取题目ID
        com.web.domain.Resp.ExamWrongQuestionResp current = examWrongQuestionService.info(id);
        if (current == null) {
            return Result.error("记录不存在");
        }
        
        // 验证权限
        try {
            if (current.getUsersId() != null && !String.valueOf(userId).equals(String.valueOf(current.getUsersId()))) {
                return Result.error("无权查看该记录");
            }
        } catch (Exception e) {
            return Result.error("无权查看该记录");
        }
        
        if (current.getExamQuestionId() == null || current.getExamQuestionId().trim().isEmpty()) {
            return Result.error("题目数据不完整");
        }
        
        // 查询该题的所有历史错误记录
        List<com.web.domain.Resp.ExamWrongQuestionResp> history = 
            examWrongQuestionService.getHistoryByUserAndQuestion(
                String.valueOf(userId), 
                current.getExamQuestionId()
            );
        
        return Result.success(history);
    }

    /**
     * 加载所有
     * 
     * @return
     */
    @GetMapping("/loadAll")
    public Result loadAll() {
        return Result.success(examWrongQuestionService.list());
    }

}
