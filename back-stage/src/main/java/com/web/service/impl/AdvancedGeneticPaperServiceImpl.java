package com.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.web.domain.*;
import com.web.mapper.ExamQuestionMapper;
import com.web.service.ExamPaperTopicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 完整版遗传算法智能组卷服务
 */
@Slf4j
@Service
public class AdvancedGeneticPaperServiceImpl {

    @Autowired
    private ExamQuestionMapper questionMapper;
    
    @Autowired
    private ExamPaperTopicService paperTopicService;
    
    private Random random = new Random();

    /**
     * 遗传算法主流程
     */
    @Transactional
    public List<ExamQuestion> generatePaper(GeneticAlgorithmConfig config) {
        
        long startTime = System.currentTimeMillis();
        log.info("🧬 开始遗传算法组卷 - 配置: {}", config);
        
        // 动态调整参数（针对小数据集）
        int questionPoolSize = 0;
        
        // 1. 获取题库（按科目和题型分类）
        Map<Integer, List<ExamQuestion>> questionPoolByType = getQuestionPoolByType(config.getSubjectId());
        
        // 计算题库总大小
        for (List<ExamQuestion> questions : questionPoolByType.values()) {
            questionPoolSize += questions.size();
        }
        log.info("📚 题库总大小: {}, 目标题数: {}", questionPoolSize, config.getTargetQuestionCount());
        
        // 验证题库是否充足
        validateQuestionPool(questionPoolByType, config);
        
        // 动态调整算法参数（小数据集使用更少的迭代）
        int adjustedPopulationSize = config.getPopulationSize();
        int adjustedMaxGenerations = config.getMaxGenerations();
        
        if (questionPoolSize <= 10) {
            // 极小题库：减少种群和迭代次数
            adjustedPopulationSize = Math.min(20, config.getPopulationSize());
            adjustedMaxGenerations = Math.min(30, config.getMaxGenerations());
            log.info("⚙️ 检测到小题库，调整参数 - 种群: {} -> {}, 迭代: {} -> {}", 
                config.getPopulationSize(), adjustedPopulationSize,
                config.getMaxGenerations(), adjustedMaxGenerations);
        }
        
        // 2. 创建题目ID到题目对象的映射
        Map<Integer, ExamQuestion> questionMap = new HashMap<>();
        for (List<ExamQuestion> questions : questionPoolByType.values()) {
            for (ExamQuestion q : questions) {
                questionMap.put(q.getId(), q);
            }
        }
        
        // 3. 初始化种群
        List<Chromosome> population = initializePopulation(questionPoolByType, config, adjustedPopulationSize);
        log.info("✅ 初始化种群完成 - 种群大小: {}", population.size());
        
        // 4. 计算初始适应度
        for (Chromosome chromosome : population) {
            double fitness = calculateFitness(chromosome, questionMap, config);
            chromosome.setFitness(fitness);
        }
        
        // 5. 记录最优个体
        Chromosome bestChromosome = Collections.max(population);
        log.info("🎯 初始最优适应度: {}", String.format("%.2f", bestChromosome.getFitness()));
        
        // 6. 进化循环
        log.info("🔄 准备开始进化循环，调整后参数: 种群{}, 迭代{}", adjustedPopulationSize, adjustedMaxGenerations);
        
        for (int generation = 0; generation < adjustedMaxGenerations; generation++) {
            
            long generationStartTime = System.currentTimeMillis();
            
            log.info("🔄 开始第{}代进化...", generation);
            
            try {
                // 6.1 创建新种群
                List<Chromosome> newPopulation = new ArrayList<>();
                
                // 6.2 精英保留策略
                Collections.sort(population, Collections.reverseOrder());
                for (int i = 0; i < config.getEliteSize() && i < population.size(); i++) {
                    newPopulation.add(population.get(i).copy());
                }
                
                log.info("精英保留完成，已保留{}个个体", newPopulation.size());
                
                // 6.3 生成新个体
                int attempts = 0;
                int maxAttempts = adjustedPopulationSize * 3; // 防止死循环
                
                log.info("开始生成新个体，目标种群大小: {}, 当前: {}", adjustedPopulationSize, newPopulation.size());
                
                while (newPopulation.size() < adjustedPopulationSize && attempts < maxAttempts) {
                    attempts++;
                    
                    // 超时检查（每代最多5秒）
                    if (System.currentTimeMillis() - generationStartTime > 5000) {
                        log.warn("⚠️ 第{}代生成超时（5秒），当前种群大小: {}, 已尝试: {}", 
                            generation, newPopulation.size(), attempts);
                        break;
                    }
                    
                    // 每10次尝试输出一次进度（临时调试）
                    if (attempts % 10 == 0) {
                        log.info("第{}代: 已尝试{}次，当前种群大小: {}/{}", 
                            generation, attempts, newPopulation.size(), adjustedPopulationSize);
                    }
                    
                    try {
                        // 选择父代
                        Chromosome parent1 = tournamentSelection(population, config.getTournamentSize());
                        Chromosome parent2 = tournamentSelection(population, config.getTournamentSize());
                        
                        // 交叉操作
                        Chromosome[] children;
                        if (random.nextDouble() < config.getCrossoverRate()) {
                            children = orderCrossover(parent1, parent2);
                        } else {
                            children = new Chromosome[] { parent1.copy(), parent2.copy() };
                        }
                        
                        // 变异操作
                        mutate(children[0], questionPoolByType, config.getMutationRate());
                        if (newPopulation.size() + 1 < adjustedPopulationSize) {
                            mutate(children[1], questionPoolByType, config.getMutationRate());
                        }
                        
                        // 计算适应度
                        children[0].setFitness(calculateFitness(children[0], questionMap, config));
                        newPopulation.add(children[0]);
                        
                        if (newPopulation.size() < adjustedPopulationSize) {
                            children[1].setFitness(calculateFitness(children[1], questionMap, config));
                            newPopulation.add(children[1]);
                        }
                    } catch (Exception e) {
                        log.warn("⚠️ 生成第{}代个体时出错: {}", generation, e.getMessage());
                    }
                }
                
                if (attempts >= maxAttempts) {
                    log.warn("⚠️ 第{}代生成个体超过最大尝试次数，当前种群大小: {}", generation, newPopulation.size());
                }
                
                // 6.4 更新种群
                population = newPopulation;
                
                // 6.5 更新最优解
                Chromosome currentBest = Collections.max(population);
                if (currentBest.getFitness() > bestChromosome.getFitness()) {
                    bestChromosome = currentBest.copy();
                }
                
                // 6.6 日志输出（每10代或最后一代）
                if (generation % 10 == 0 || generation == adjustedMaxGenerations - 1) {
                    double avgFitness = population.stream()
                        .mapToDouble(Chromosome::getFitness)
                        .average()
                        .orElse(0.0);
                    log.info("📊 第{}代: 最优适应度={}, 平均适应度={}, 耗时={}ms", 
                        generation, String.format("%.2f", bestChromosome.getFitness()), 
                        String.format("%.2f", avgFitness),
                        System.currentTimeMillis() - generationStartTime);
                }
                
                // 6.7 提前终止条件（适应度达到95分以上）
                if (bestChromosome.getFitness() >= 95.0) {
                    log.info("🎉 达到最优解，提前终止在第{}代 - 适应度: {}", generation, String.format("%.2f", bestChromosome.getFitness()));
                    break;
                }
                
                // 6.8 总体超时检查（最多20秒）
                if (System.currentTimeMillis() - startTime > 20000) {
                    log.warn("⚠️ 算法总体超时（20秒），在第{}代终止", generation);
                    break;
                }
                
            } catch (Exception e) {
                log.error("❌ 第{}代进化过程出错", generation, e);
                break;
            }
        }
        
        long endTime = System.currentTimeMillis();
        log.info("✅ 遗传算法组卷完成 - 耗时: {}ms, 最优适应度: {}", 
            endTime - startTime, String.format("%.2f", bestChromosome.getFitness()));
        
        // 7. 返回最优解对应的题目列表
        List<ExamQuestion> selectedQuestions = bestChromosome.getGenes().stream()
            .map(questionMap::get)
            .collect(Collectors.toList());
        
        // 8. 保存到试卷
        if (config.getPaperId() != null) {
            saveQuestionsToPaper(config.getPaperId(), selectedQuestions, config);
        }
        
        return selectedQuestions;
    }

    /**
     * 获取题库（按题型分类）
     */
    private Map<Integer, List<ExamQuestion>> getQuestionPoolByType(Long subjectId) {
        QueryWrapper<ExamQuestion> wrapper = new QueryWrapper<>();
        wrapper.eq("kemu_types", subjectId);
        List<ExamQuestion> allQuestions = questionMapper.selectList(wrapper);
        
        // 按题型分组
        return allQuestions.stream()
            .collect(Collectors.groupingBy(ExamQuestion::getExamQuestionTypes));
    }

    /**
     * 验证题库是否充足
     */
    private void validateQuestionPool(
            Map<Integer, List<ExamQuestion>> questionPoolByType, 
            GeneticAlgorithmConfig config) {
        
        for (Map.Entry<Integer, Integer> entry : config.getQuestionTypeCount().entrySet()) {
            int questionType = entry.getKey();
            int requiredCount = entry.getValue();
            
            List<ExamQuestion> availableQuestions = questionPoolByType.get(questionType);
            int availableCount = availableQuestions != null ? availableQuestions.size() : 0;
            
            if (availableCount < requiredCount) {
                String typeName = getQuestionTypeName(questionType);
                throw new RuntimeException(String.format(
                    "题库不足：%s需要%d题，但只有%d题", 
                    typeName, requiredCount, availableCount));
            }
        }
    }

    /**
     * 初始化种群
     */
    private List<Chromosome> initializePopulation(
            Map<Integer, List<ExamQuestion>> questionPoolByType,
            GeneticAlgorithmConfig config,
            int populationSize) {
        
        List<Chromosome> population = new ArrayList<>();
        
        for (int i = 0; i < populationSize; i++) {
            Chromosome chromosome = createRandomChromosome(questionPoolByType, config);
            population.add(chromosome);
        }
        
        return population;
    }

    /**
     * 创建随机染色体（满足题型约束）
     */
    private Chromosome createRandomChromosome(
            Map<Integer, List<ExamQuestion>> questionPoolByType,
            GeneticAlgorithmConfig config) {
        
        List<Integer> genes = new ArrayList<>();
        
        // 按题型选择题目
        for (Map.Entry<Integer, Integer> entry : config.getQuestionTypeCount().entrySet()) {
            int questionType = entry.getKey();
            int requiredCount = entry.getValue();
            
            List<ExamQuestion> availableQuestions = questionPoolByType.get(questionType);
            if (availableQuestions == null || availableQuestions.isEmpty()) {
                continue;
            }
            
            // 随机选择该题型的题目
            List<ExamQuestion> shuffled = new ArrayList<>(availableQuestions);
            Collections.shuffle(shuffled, random);
            
            for (int i = 0; i < requiredCount && i < shuffled.size(); i++) {
                genes.add(shuffled.get(i).getId());
            }
        }
        
        return new Chromosome(genes);
    }

    /**
     * 计算适应度函数
     */
    private double calculateFitness(
            Chromosome chromosome, 
            Map<Integer, ExamQuestion> questionMap,
            GeneticAlgorithmConfig config) {
        
        List<Integer> genes = chromosome.getGenes();
        if (genes.isEmpty()) {
            return 0.0;
        }
        
        // 获取染色体对应的题目列表
        List<ExamQuestion> questions = genes.stream()
            .map(questionMap::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        double fitness = 100.0;  // 基础分100分
        
        // ========== 硬约束检查 ==========
        
        // 1. 题目数量约束（严格）
        if (questions.size() != config.getTargetQuestionCount()) {
            fitness -= 1000;
            return Math.max(0, fitness);
        }
        
        // 2. 题目去重约束（严格）
        Set<Integer> uniqueIds = new HashSet<>(genes);
        if (uniqueIds.size() != genes.size()) {
            fitness -= 1000;
            return Math.max(0, fitness);
        }
        
        // 3. 题型数量约束（严格）
        Map<Integer, Long> actualTypeCount = questions.stream()
            .collect(Collectors.groupingBy(
                ExamQuestion::getExamQuestionTypes,
                Collectors.counting()
            ));
        
        for (Map.Entry<Integer, Integer> entry : config.getQuestionTypeCount().entrySet()) {
            int type = entry.getKey();
            int required = entry.getValue();
            long actual = actualTypeCount.getOrDefault(type, 0L);
            
            if (actual != required) {
                fitness -= 500;  // 题型不匹配严重扣分
                return Math.max(0, fitness);
            }
        }
        
        // ========== 软约束评分 ==========
        
        // 4. 总分接近度（权重35%）
        int actualScore = 0;
        for (ExamQuestion q : questions) {
            int questionType = q.getExamQuestionTypes();
            Integer scorePerQuestion = config.getQuestionTypeScore().get(questionType);
            if (scorePerQuestion != null) {
                actualScore += scorePerQuestion;
            }
        }
        
        if (config.getTargetTotalScore() > 0) {
            double scoreDiff = Math.abs(actualScore - config.getTargetTotalScore()) 
                             / (double) config.getTargetTotalScore();
            fitness -= scoreDiff * 35;
        }
        
        // 5. 难度分布接近度（权重30%）
        if (config.getDifficultyDistribution() != null && !config.getDifficultyDistribution().isEmpty()) {
            Map<Integer, Long> actualDifficulty = questions.stream()
                .filter(q -> q.getDifficultyLevel() != null)
                .collect(Collectors.groupingBy(
                    ExamQuestion::getDifficultyLevel,
                    Collectors.counting()
                ));
            
            double difficultyPenalty = 0;
            int totalDifficultyTarget = config.getDifficultyDistribution().values()
                .stream().mapToInt(Integer::intValue).sum();
            
            for (Map.Entry<Integer, Integer> entry : config.getDifficultyDistribution().entrySet()) {
                int level = entry.getKey();
                int target = entry.getValue();
                long actual = actualDifficulty.getOrDefault(level, 0L);
                difficultyPenalty += Math.abs(actual - target);
            }
            
            if (totalDifficultyTarget > 0) {
                fitness -= (difficultyPenalty / totalDifficultyTarget) * 30;
            }
        }
        
        // 6. 必选知识点覆盖（权重25%）
        if (config.getRequiredKnowledgePoints() != null && !config.getRequiredKnowledgePoints().isEmpty()) {
            // 获取试卷中所有题目的知识点标签
            Set<String> coveredKnowledgePoints = questions.stream()
                .filter(q -> q.getKnowledgePoint() != null && !q.getKnowledgePoint().trim().isEmpty())
                .map(q -> q.getKnowledgePoint().trim())
                .collect(Collectors.toSet());
            
            int requiredCount = config.getRequiredKnowledgePoints().size();
            long coveredCount = config.getRequiredKnowledgePoints().stream()
                .filter(coveredKnowledgePoints::contains)
                .count();
            
            double coverageRate = (double) coveredCount / requiredCount;
            fitness += coverageRate * 25;  // 覆盖率越高，得分越高
        } else {
            // 如果没有指定必选知识点，则评估知识点多样性
            Set<String> knowledgePoints = questions.stream()
                .filter(q -> q.getKnowledgePoint() != null && !q.getKnowledgePoint().trim().isEmpty())
                .map(q -> q.getKnowledgePoint().trim())
                .collect(Collectors.toSet());
            
            double diversity = Math.min(knowledgePoints.size() / 10.0, 1.0);
            fitness += diversity * 10;
        }
        
        // 7. 题型分值一致性（权重10%）
        double scoreConsistency = 10.0;
        for (ExamQuestion q : questions) {
            int type = q.getExamQuestionTypes();
            Integer expectedScore = config.getQuestionTypeScore().get(type);
            // 这里假设题目分值是统一设置的，所以不需要额外检查
        }
        fitness += scoreConsistency;
        
        return Math.max(0, fitness);
    }

    /**
     * 锦标赛选择
     */
    private Chromosome tournamentSelection(List<Chromosome> population, int tournamentSize) {
        Chromosome best = null;
        
        for (int i = 0; i < tournamentSize; i++) {
            Chromosome candidate = population.get(random.nextInt(population.size()));
            if (best == null || candidate.getFitness() > best.getFitness()) {
                best = candidate;
            }
        }
        
        return best.copy();
    }

    /**
     * 顺序交叉（Order Crossover, OX）
     */
    private Chromosome[] orderCrossover(Chromosome parent1, Chromosome parent2) {
        List<Integer> p1Genes = parent1.getGenes();
        List<Integer> p2Genes = parent2.getGenes();
        int length = p1Genes.size();
        
        if (length <= 2) {
            return new Chromosome[] { parent1.copy(), parent2.copy() };
        }
        
        // 随机选择交叉点
        int point1 = random.nextInt(length - 1);
        int point2 = point1 + 1 + random.nextInt(length - point1 - 1);
        
        // 创建子代
        List<Integer> child1Genes = new ArrayList<>(Collections.nCopies(length, null));
        List<Integer> child2Genes = new ArrayList<>(Collections.nCopies(length, null));
        
        // 复制交叉段
        for (int i = point1; i <= point2; i++) {
            child1Genes.set(i, p1Genes.get(i));
            child2Genes.set(i, p2Genes.get(i));
        }
        
        // 从另一个父代填充剩余位置
        fillRemainingGenes(child1Genes, p2Genes, point1, point2);
        fillRemainingGenes(child2Genes, p1Genes, point1, point2);
        
        return new Chromosome[] {
            new Chromosome(child1Genes),
            new Chromosome(child2Genes)
        };
    }

    /**
     * 填充剩余基因（顺序交叉的辅助方法）
     */
    private void fillRemainingGenes(List<Integer> child, List<Integer> parent, 
                                     int point1, int point2) {
        int length = child.size();
        int currentPos = (point2 + 1) % length;
        
        for (int i = 0; i < length; i++) {
            int parentPos = (point2 + 1 + i) % length;
            Integer gene = parent.get(parentPos);
            
            if (!child.contains(gene)) {
                // 防止无限循环：最多尝试length次
                int attempts = 0;
                while (child.get(currentPos) != null && attempts < length) {
                    currentPos = (currentPos + 1) % length;
                    attempts++;
                }
                
                // 如果找到空位，填充
                if (attempts < length && child.get(currentPos) == null) {
                    child.set(currentPos, gene);
                } else {
                    // 如果没找到空位（理论上不应该发生），记录警告
                    log.warn("⚠️ fillRemainingGenes: 无法找到空位填充基因 {}", gene);
                }
            }
        }
    }

    /**
     * 变异操作
     */
    private void mutate(Chromosome chromosome, 
                       Map<Integer, List<ExamQuestion>> questionPoolByType,
                       double mutationRate) {
        
        if (random.nextDouble() >= mutationRate) {
            return;
        }
        
        List<Integer> genes = chromosome.getGenes();
        if (genes.isEmpty()) {
            return;
        }
        
        int mutationType = random.nextInt(2);
        
        if (mutationType == 0) {
            // 替换变异：随机替换1个题目（保持题型不变）
            swapMutation(genes, questionPoolByType);
        } else {
            // 交换变异：随机交换2个题目位置
            if (genes.size() >= 2) {
                int index1 = random.nextInt(genes.size());
                int index2 = random.nextInt(genes.size());
                Collections.swap(genes, index1, index2);
            }
        }
    }

    /**
     * 替换变异（保持题型不变）
     */
    private void swapMutation(List<Integer> genes, 
                             Map<Integer, List<ExamQuestion>> questionPoolByType) {
        
        if (genes.isEmpty()) {
            return;
        }
        
        // 随机选择一个位置进行替换
        int replaceIndex = random.nextInt(genes.size());
        Integer oldQuestionId = genes.get(replaceIndex);
        
        // 找到该题目所属的题型
        Integer questionType = null;
        for (Map.Entry<Integer, List<ExamQuestion>> entry : questionPoolByType.entrySet()) {
            for (ExamQuestion q : entry.getValue()) {
                if (q.getId().equals(oldQuestionId)) {
                    questionType = entry.getKey();
                    break;
                }
            }
            if (questionType != null) break;
        }
        
        if (questionType == null) {
            return;
        }
        
        // 从同题型中选择一个不在染色体中的题目
        List<ExamQuestion> sameTypeQuestions = questionPoolByType.get(questionType);
        if (sameTypeQuestions == null || sameTypeQuestions.isEmpty()) {
            return;
        }
        
        for (int attempt = 0; attempt < 50; attempt++) {
            ExamQuestion candidate = sameTypeQuestions.get(random.nextInt(sameTypeQuestions.size()));
            if (!genes.contains(candidate.getId())) {
                genes.set(replaceIndex, candidate.getId());
                break;
            }
        }
    }

    /**
     * 保存题目到试卷
     */
    private void saveQuestionsToPaper(Integer paperId, 
                                     List<ExamQuestion> questions,
                                     GeneticAlgorithmConfig config) {
        
        int sequence = 1;
        
        for (ExamQuestion question : questions) {
            ExamPaperTopic topic = new ExamPaperTopic();
            topic.setExamPaperId(paperId);
            topic.setExamQuestionId(question.getId());
            
            // 根据题型设置分值
            Integer score = config.getQuestionTypeScore().get(question.getExamQuestionTypes());
            topic.setExamPaperTopicNumber(score != null ? score : 0);
            topic.setExamPaperTopicSequence(sequence++);
            
            paperTopicService.save(topic);
        }
        
        log.info("✅ 已保存{}道题目到试卷ID={}", questions.size(), paperId);
    }

    /**
     * 获取题型名称
     */
    private String getQuestionTypeName(int type) {
        switch (type) {
            case 1: return "单选题";
            case 2: return "多选题";
            case 3: return "判断题";
            case 4: return "填空题";
            case 5: return "简答题";
            default: return "未知题型";
        }
    }
}
