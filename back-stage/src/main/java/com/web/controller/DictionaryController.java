package com.web.controller;

import com.web.annotation.IgnoreAuth;
import com.web.domain.Dictionary;
import com.web.domain.Req.DictionaryReq;
import com.web.service.DictionaryService;
import com.web.service.ExamInfoService;
import com.web.service.ExamQuestionService;
import com.web.service.RedisCacheService;
import com.web.utils.PageUtils;
import com.web.utils.Result;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 字典
 * </p>
 */
@RestController
@RequestMapping("/dictionary")
public class DictionaryController {

    @Resource
    private DictionaryService dictionaryService;

    @Resource
    private ExamInfoService examInfoService;

    @Resource
    private ExamQuestionService examQuestionService;

    @Resource
    private RedisCacheService redisCacheService;

    /**
     * 后台分页查询
     * 
     * @return
     */
    @PostMapping("/page")
    public Result page(@RequestBody DictionaryReq req) {
        PageUtils page = dictionaryService.selectPage(req);
        return Result.success(page);
    }

    /**
     * 新增
     * 
     * @param dictionary
     * @return
     */
    @PostMapping("/save")
    public Result save(@RequestBody Dictionary dictionary) {
        // 验证科目名称是否已存在
        if ("kemu_types".equals(dictionary.getDicCode())) {
            if (dictionary.getIndexName() == null || dictionary.getIndexName().trim().isEmpty()) {
                return Result.error("科目名称不能为空");
            }
            if (dictionaryService.isSubjectNameExists(dictionary.getIndexName(), null)) {
                return Result.error("科目名称「" + dictionary.getIndexName() + "」已存在，不能重复添加");
            }
        }
        dictionaryService.save(dictionary);
        
        // 清除字典缓存
        redisCacheService.deleteDictionaryCache(dictionary.getDicCode());
        redisCacheService.clearAllDictionaryCache();
        
        return Result.success();
    }

    /**
     * 修改
     * 
     * @param dictionary
     * @return
     */
    @PostMapping("/update")
    public Result update(@RequestBody Dictionary dictionary) {
        // 验证科目名称是否已存在（排除当前记录）
        if ("kemu_types".equals(dictionary.getDicCode())) {
            if (dictionary.getIndexName() == null || dictionary.getIndexName().trim().isEmpty()) {
                return Result.error("科目名称不能为空");
            }
            if (dictionaryService.isSubjectNameExists(dictionary.getIndexName(), dictionary.getId())) {
                return Result.error("科目名称「" + dictionary.getIndexName() + "」已存在，不能重复");
            }
        }
        dictionaryService.updateById(dictionary);
        
        // 清除字典缓存
        redisCacheService.deleteDictionaryCache(dictionary.getDicCode());
        redisCacheService.clearAllDictionaryCache();
        
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
        return Result.success(dictionaryService.info(id));
    }

    /**
     * 根据id删除
     * 
     * @param id
     * @return
     */
    @DeleteMapping("/del/{id}")
    public Result delete(@PathVariable Integer id) {
        Dictionary dict = dictionaryService.getById(id);
        if (dict == null) {
            return Result.error("记录不存在");
        }
        
        // 检查科目是否有关联的试题
        if ("kemu_types".equals(dict.getDicCode()) && dict.getCodeIndex() != null) {
            if (examQuestionService.hasQuestionsForSubject(dict.getCodeIndex())) {
                return Result.error("科目「" + (dict.getIndexName() != null ? dict.getIndexName() : dict.getCodeIndex())
                        + "」已有关联的试题，无法删除");
            }
            // 检查是否有已发布或进行中的考试使用该科目
            if (examInfoService.hasReleasedOrOngoingExamUsingKemuTypes(dict.getCodeIndex())) {
                return Result.error("存在「已发布」或「进行中」的考试使用该科目，无法删除");
            }
        }
        
        boolean success = dictionaryService.removeById(id);
        
        // 清除字典缓存
        if (success) {
            redisCacheService.deleteDictionaryCache(dict.getDicCode());
            redisCacheService.clearAllDictionaryCache();
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
    public Result deleteBatch(@RequestBody Integer[] ids) {
        if (ids == null || ids.length == 0) {
            return Result.error("请选择要删除的记录");
        }
        for (Integer id : ids) {
            if (id == null) {
                continue;
            }
            Dictionary dict = dictionaryService.getById(id);
            if (dict == null) {
                return Result.error("记录不存在：ID=" + id);
            }
            
            // 检查科目是否有关联的试题
            if ("kemu_types".equals(dict.getDicCode()) && dict.getCodeIndex() != null) {
                if (examQuestionService.hasQuestionsForSubject(dict.getCodeIndex())) {
                    return Result.error("科目「" + (dict.getIndexName() != null ? dict.getIndexName() : dict.getCodeIndex())
                            + "」已有关联的试题，无法删除");
                }
                // 检查是否有已发布或进行中的考试使用该科目
                if (examInfoService.hasReleasedOrOngoingExamUsingKemuTypes(dict.getCodeIndex())) {
                    return Result.error("科目「" + (dict.getIndexName() != null ? dict.getIndexName() : dict.getCodeIndex())
                            + "」已被「已发布/进行中」的考试使用，无法删除");
                }
            }
        }
        
        boolean success = dictionaryService.removeBatchByIds(Arrays.asList(ids));
        
        // 清除所有字典缓存
        if (success) {
            redisCacheService.clearAllDictionaryCache();
        }
        
        return Result.success(success);
    }

    /**
     * 根据类型字段查询所有
     * 
     * @param req
     * @return
     */
    @IgnoreAuth
    @PostMapping("/list")
    public Result list(@RequestBody DictionaryReq req) {
        try {
            String cacheKey = req.getDicCode();
            
            // 先从缓存获取
            Object cachedData = redisCacheService.getDictionaryCache(cacheKey);
            if (cachedData != null) {
                return Result.success(cachedData);
            }
            
            // 缓存未命中，查询数据库
            List<Dictionary> list = dictionaryService.lambdaQuery().eq(Dictionary::getDicCode, req.getDicCode()).list();
            
            // 存入缓存（24小时）
            redisCacheService.setDictionaryCache(cacheKey, list);
            
            return Result.success(list);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询字典失败: " + e.getMessage());
        }
    }

    /**
     * 加载所有
     * 
     * @return
     */
    @GetMapping("/loadAll")
    public Result loadAll() {
        return Result.success(dictionaryService.list());
    }

    /**
     * 查询字典名
     * 
     * @param dicCode
     * @param codeIndex
     * @return
     */
    @GetMapping("/getDictionaryName")
    public Result getDictionaryName(String dicCode, Integer codeIndex) {
        return Result.success(dictionaryService.getDictionaryName(dicCode, codeIndex));
    }

    /**
     * 测试字典缓存功能
     * 
     * @param dicCode 字典类型（如 kemu_types）
     * @return
     */
    @IgnoreAuth
    @GetMapping("/testCache")
    public Result testCache(@RequestParam String dicCode) {
        try {
            long startTime1 = System.currentTimeMillis();
            
            // 第一次查询（应该从数据库查询）
            DictionaryReq req1 = new DictionaryReq();
            req1.setDicCode(dicCode);
            Result result1 = list(req1);
            
            long endTime1 = System.currentTimeMillis();
            long time1 = endTime1 - startTime1;
            
            // 等待一小段时间
            Thread.sleep(100);
            
            long startTime2 = System.currentTimeMillis();
            
            // 第二次查询（应该从缓存获取）
            DictionaryReq req2 = new DictionaryReq();
            req2.setDicCode(dicCode);
            Result result2 = list(req2);
            
            long endTime2 = System.currentTimeMillis();
            long time2 = endTime2 - startTime2;
            
            // 检查 Redis 中是否有缓存
            Object cachedData = redisCacheService.getDictionaryCache(dicCode);
            boolean hasCached = cachedData != null;
            
            // 构建测试结果
            java.util.Map<String, Object> testResult = new java.util.HashMap<>();
            testResult.put("测试类型", "字典缓存测试");
            testResult.put("字典类型", dicCode);
            testResult.put("第一次查询耗时", time1 + "ms（从数据库）");
            testResult.put("第二次查询耗时", time2 + "ms（从缓存）");
            testResult.put("性能提升", time1 > 0 ? String.format("%.1f倍", (double)time1 / time2) : "N/A");
            testResult.put("缓存状态", hasCached ? "✅ 已缓存" : "❌ 未缓存");
            testResult.put("数据条数", result1.getData() instanceof List ? ((List<?>)result1.getData()).size() : 0);
            testResult.put("缓存Key", "dictionary:" + dicCode);
            testResult.put("缓存过期时间", "24小时");
            
            if (time2 < time1) {
                testResult.put("测试结果", "✅ 缓存生效！第二次查询明显更快");
            } else {
                testResult.put("测试结果", "⚠️ 缓存可能未生效，请检查 Redis 连接");
            }
            
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
