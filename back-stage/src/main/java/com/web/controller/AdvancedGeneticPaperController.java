package com.web.controller;

import com.web.domain.ExamQuestion;
import com.web.domain.GeneticAlgorithmConfig;
import com.web.service.impl.AdvancedGeneticPaperServiceImpl;
import com.web.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 完整版遗传算法智能组卷控制器
 */
@Slf4j
@RestController
@RequestMapping("/advancedGeneticPaper")
@Tag(name = "智能组卷(完整版)", description = "使用完整遗传算法智能生成试卷")
public class AdvancedGeneticPaperController {

    @Autowired
    private AdvancedGeneticPaperServiceImpl geneticService;

    /**
     * 完整版遗传算法智能组卷
     * 
     * 请求示例:
     * POST /advancedGeneticPaper/generate
     * {
     *   "paperId": 1,
     *   "subjectId": 1,
     *   "targetQuestionCount": 25,
     *   "targetTotalScore": 100,
     *   "questionTypeCount": {
     *     "1": 10,  // 单选题10道
     *     "2": 5,   // 多选题5道
     *     "3": 5,   // 判断题5道
     *     "4": 3,   // 填空题3道
     *     "5": 2    // 简答题2道
     *   },
     *   "questionTypeScore": {
     *     "1": 2,   // 单选题每题2分
     *     "2": 4,   // 多选题每题4分
     *     "3": 2,   // 判断题每题2分
     *     "4": 5,   // 填空题每题5分
     *     "5": 10   // 简答题每题10分
     *   },
     *   "difficultyDistribution": {
     *     "1": 10,  // 简单题10道
     *     "2": 10,  // 中等题10道
     *     "3": 5    // 困难题5道
     *   },
     *   "requiredKnowledgePoints": ["古诗词鉴赏", "阅读理解", "文言文"],  // 必选知识点标签列表
     *   "populationSize": 100,
     *   "maxGenerations": 200,
     *   "crossoverRate": 0.8,
     *   "mutationRate": 0.05
     * }
     */
    @PostMapping("/generate")
    @Operation(summary = "完整版遗传算法智能组卷", 
               description = "使用完整遗传算法（选择、交叉、变异）生成最优试卷")
    public Result generate(@RequestBody GeneticAlgorithmConfig config) {
        try {
            log.info("📨 收到智能组卷请求: paperId={}, subjectId={}, targetCount={}, targetScore={}", 
                config.getPaperId(), config.getSubjectId(), 
                config.getTargetQuestionCount(), config.getTargetTotalScore());
            
            // 参数验证
            validateConfig(config);
            
            // 设置默认值（如果前端未传）
            setDefaultValues(config);
            
            // 执行遗传算法
            List<ExamQuestion> selectedQuestions = geneticService.generatePaper(config);
            
            log.info("✅ 智能组卷成功: 生成{}道题目", selectedQuestions.size());
            
            return Result.success(Map.of(
                "message", "智能组卷成功",
                "questionCount", selectedQuestions.size(),
                "questions", selectedQuestions
            ));
            
        } catch (Exception e) {
            log.error("❌ 智能组卷失败", e);
            return Result.error("生成失败: " + e.getMessage());
        }
    }

    /**
     * 快速智能组卷（使用默认算法参数）
     */
    @PostMapping("/quickGenerate")
    @Operation(summary = "快速智能组卷", description = "使用默认算法参数快速生成试卷")
    public Result quickGenerate(@RequestBody Map<String, Object> params) {
        try {
            log.info("📨 收到快速组卷请求，参数: {}", params);
            
            // 构建配置
            GeneticAlgorithmConfig config = new GeneticAlgorithmConfig();
            
            // 必需参数 - 安全类型转换
            Object paperIdObj = params.get("paperId");
            if (paperIdObj != null) {
                if (paperIdObj instanceof Integer) {
                    config.setPaperId((Integer) paperIdObj);
                } else {
                    config.setPaperId(Integer.valueOf(paperIdObj.toString()));
                }
            }
            
            Object subjectIdObj = params.get("subjectId");
            if (subjectIdObj != null) {
                if (subjectIdObj instanceof Long) {
                    config.setSubjectId((Long) subjectIdObj);
                } else if (subjectIdObj instanceof Integer) {
                    config.setSubjectId(((Integer) subjectIdObj).longValue());
                } else {
                    config.setSubjectId(Long.valueOf(subjectIdObj.toString()));
                }
            }
            
            Object questionCountObj = params.get("targetQuestionCount");
            if (questionCountObj != null) {
                if (questionCountObj instanceof Integer) {
                    config.setTargetQuestionCount((Integer) questionCountObj);
                } else {
                    config.setTargetQuestionCount(Integer.valueOf(questionCountObj.toString()));
                }
            }
            
            Object totalScoreObj = params.get("targetTotalScore");
            if (totalScoreObj != null) {
                if (totalScoreObj instanceof Integer) {
                    config.setTargetTotalScore((Integer) totalScoreObj);
                } else {
                    config.setTargetTotalScore(Integer.valueOf(totalScoreObj.toString()));
                }
            }
            
            // 题型配置
            @SuppressWarnings("unchecked")
            Map<String, Object> typeCountMap = (Map<String, Object>) params.get("questionTypeCount");
            if (typeCountMap != null) {
                config.setQuestionTypeCount(convertMapToIntegerMap(typeCountMap));
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> typeScoreMap = (Map<String, Object>) params.get("questionTypeScore");
            if (typeScoreMap != null) {
                config.setQuestionTypeScore(convertMapToIntegerMap(typeScoreMap));
            }
            
            // 难度配置（可选）
            if (params.containsKey("difficultyDistribution") && params.get("difficultyDistribution") != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> difficultyMap = (Map<String, Object>) params.get("difficultyDistribution");
                config.setDifficultyDistribution(convertMapToIntegerMap(difficultyMap));
            }
            
            // 知识点配置（可选）- 接收文本标签列表
            if (params.containsKey("requiredKnowledgePoints") && params.get("requiredKnowledgePoints") != null) {
                @SuppressWarnings("unchecked")
                List<Object> knowledgePointsList = (List<Object>) params.get("requiredKnowledgePoints");
                List<String> knowledgePoints = knowledgePointsList.stream()
                    .map(obj -> obj.toString().trim())
                    .filter(s -> !s.isEmpty())
                    .collect(java.util.stream.Collectors.toList());
                config.setRequiredKnowledgePoints(knowledgePoints);
            }
            
            log.info("📋 解析后的配置: paperId={}, subjectId={}, targetCount={}, targetScore={}", 
                config.getPaperId(), config.getSubjectId(), 
                config.getTargetQuestionCount(), config.getTargetTotalScore());
            
            // 使用默认算法参数
            setDefaultValues(config);
            
            // 参数验证
            validateConfig(config);
            
            // 执行遗传算法
            List<ExamQuestion> selectedQuestions = geneticService.generatePaper(config);
            
            return Result.success(Map.of(
                "message", "智能组卷成功",
                "questionCount", selectedQuestions.size()
            ));
            
        } catch (Exception e) {
            log.error("❌ 快速智能组卷失败", e);
            return Result.error("生成失败: " + e.getMessage());
        }
    }

    /**
     * 参数验证
     */
    private void validateConfig(GeneticAlgorithmConfig config) {
        if (config.getSubjectId() == null) {
            throw new IllegalArgumentException("科目ID不能为空");
        }
        
        if (config.getTargetQuestionCount() <= 0) {
            throw new IllegalArgumentException("目标题目数量必须大于0");
        }
        
        if (config.getTargetTotalScore() <= 0) {
            throw new IllegalArgumentException("目标总分必须大于0");
        }
        
        if (config.getQuestionTypeCount() == null || config.getQuestionTypeCount().isEmpty()) {
            throw new IllegalArgumentException("题型数量配置不能为空");
        }
        
        if (config.getQuestionTypeScore() == null || config.getQuestionTypeScore().isEmpty()) {
            throw new IllegalArgumentException("题型分值配置不能为空");
        }
        
        // 验证题型数量总和
        int totalCount = config.getQuestionTypeCount().values().stream()
            .mapToInt(Integer::intValue).sum();
        if (totalCount != config.getTargetQuestionCount()) {
            throw new IllegalArgumentException(
                String.format("题型数量总和(%d)与目标题目数量(%d)不一致", 
                    totalCount, config.getTargetQuestionCount())
            );
        }
        
        // 验证总分计算
        int calculatedScore = 0;
        for (Map.Entry<Integer, Integer> entry : config.getQuestionTypeCount().entrySet()) {
            int type = entry.getKey();
            int count = entry.getValue();
            Integer score = config.getQuestionTypeScore().get(type);
            if (score != null) {
                calculatedScore += count * score;
            }
        }
        
        if (Math.abs(calculatedScore - config.getTargetTotalScore()) > 1) {
            throw new IllegalArgumentException(
                String.format("计算总分(%d)与目标总分(%d)不一致", 
                    calculatedScore, config.getTargetTotalScore())
            );
        }
    }

    /**
     * 设置默认值
     */
    private void setDefaultValues(GeneticAlgorithmConfig config) {
        if (config.getPopulationSize() <= 0) {
            config.setPopulationSize(50);
        }
        
        if (config.getMaxGenerations() <= 0) {
            config.setMaxGenerations(50);
        }
        
        if (config.getCrossoverRate() <= 0) {
            config.setCrossoverRate(0.7);
        }
        
        if (config.getMutationRate() <= 0) {
            config.setMutationRate(0.05);
        }
        
        if (config.getEliteSize() <= 0) {
            config.setEliteSize(10);
        }
        
        if (config.getTournamentSize() <= 0) {
            config.setTournamentSize(5);
        }
    }

    /**
     * 转换Map的key从String到Integer
     */
    private Map<Integer, Integer> convertKeyToInteger(Map<String, Integer> map) {
        if (map == null) {
            return Map.of();
        }
        
        return map.entrySet().stream()
            .collect(java.util.stream.Collectors.toMap(
                entry -> Integer.parseInt(entry.getKey()),
                Map.Entry::getValue
            ));
    }
    
    /**
     * 转换Map<String, Object>到Map<Integer, Integer>
     */
    private Map<Integer, Integer> convertMapToIntegerMap(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return Map.of();
        }
        
        Map<Integer, Integer> result = new java.util.HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            try {
                Integer key = Integer.parseInt(entry.getKey());
                Integer value;
                
                Object valueObj = entry.getValue();
                if (valueObj instanceof Integer) {
                    value = (Integer) valueObj;
                } else if (valueObj instanceof Number) {
                    value = ((Number) valueObj).intValue();
                } else {
                    value = Integer.valueOf(valueObj.toString());
                }
                
                result.put(key, value);
            } catch (Exception e) {
                log.warn("⚠️ 转换Map项失败: key={}, value={}", entry.getKey(), entry.getValue());
            }
        }
        
        return result;
    }
}
