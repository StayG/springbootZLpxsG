package com.web.domain;

import lombok.Data;
import java.util.Map;
import java.util.List;

/**
 * 遗传算法配置类
 */
@Data
public class GeneticAlgorithmConfig {
    
    // ========== 算法参数 ==========
    /**
     * 种群大小
     */
    private int populationSize = 50;
    
    /**
     * 最大迭代次数
     */
    private int maxGenerations = 50;
    
    /**
     * 交叉概率
     */
    private double crossoverRate = 0.7;
    
    /**
     * 变异概率
     */
    private double mutationRate = 0.05;
    
    /**
     * 精英个体数量
     */
    private int eliteSize = 10;
    
    /**
     * 锦标赛选择大小
     */
    private int tournamentSize = 5;
    
    // ========== 硬约束 ==========
    /**
     * 科目ID
     */
    private Long subjectId;
    
    /**
     * 试卷ID
     */
    private Integer paperId;
    
    /**
     * 目标总题目数量
     */
    private int targetQuestionCount;
    
    /**
     * 目标总分
     */
    private int targetTotalScore;
    
    /**
     * 题型数量分布
     * key: 题型类型 (1=单选, 2=多选, 3=判断, 4=填空, 5=简答)
     * value: 题目数量
     */
    private Map<Integer, Integer> questionTypeCount;
    
    /**
     * 题型分值
     * key: 题型类型
     * value: 单题分值
     */
    private Map<Integer, Integer> questionTypeScore;
    
    // ========== 软约束 ==========
    /**
     * 难度分布比例
     * key: 难度等级 (1=简单, 2=中等, 3=困难)
     * value: 数量（按照比例计算后的实际数量）
     */
    private Map<Integer, Integer> difficultyDistribution;
    
    /**
     * 必选知识点标签列表（文本）
     */
    private List<String> requiredKnowledgePoints;
}
