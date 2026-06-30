package com.web.domain;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 染色体类 - 采用整数编码方案
 * 染色体 = [题目ID1, 题目ID2, ..., 题目IDn]
 */
@Data
public class Chromosome implements Comparable<Chromosome> {
    
    /**
     * 基因序列：题目ID列表
     */
    private List<Integer> genes;
    
    /**
     * 适应度分数
     */
    private double fitness;
    
    /**
     * 构造函数
     */
    public Chromosome() {
        this.genes = new ArrayList<>();
        this.fitness = 0.0;
    }
    
    /**
     * 构造函数
     */
    public Chromosome(List<Integer> genes) {
        this.genes = new ArrayList<>(genes);
        this.fitness = 0.0;
    }
    
    /**
     * 深拷贝
     */
    public Chromosome copy() {
        Chromosome newChromosome = new Chromosome(new ArrayList<>(this.genes));
        newChromosome.setFitness(this.fitness);
        return newChromosome;
    }
    
    /**
     * 获取基因数量
     */
    public int size() {
        return genes.size();
    }
    
    /**
     * 获取指定位置的基因
     */
    public Integer getGene(int index) {
        return genes.get(index);
    }
    
    /**
     * 设置指定位置的基因
     */
    public void setGene(int index, Integer questionId) {
        genes.set(index, questionId);
    }
    
    /**
     * 是否包含某个题目ID
     */
    public boolean contains(Integer questionId) {
        return genes.contains(questionId);
    }
    
    /**
     * 比较器：按适应度降序排列
     */
    @Override
    public int compareTo(Chromosome other) {
        return Double.compare(other.fitness, this.fitness);
    }
    
    @Override
    public String toString() {
        return String.format("Chromosome{genes=%s, fitness=%.2f}", genes, fitness);
    }
}
