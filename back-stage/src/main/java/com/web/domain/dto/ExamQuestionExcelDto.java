package com.web.domain.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

/**
 * 试题 Excel 导入 DTO
 */
@Data
public class ExamQuestionExcelDto {

    @ExcelProperty(value = "试题题干", index = 0)
    @ColumnWidth(25)
    private String examQuestionName;

    @ExcelProperty(value = "选项（JSON 格式）", index = 1)
    @ColumnWidth(30)
    private String examQuestionOptions;

    @ExcelProperty(value = "正确答案", index = 2)
    @ColumnWidth(15)
    private String examQuestionAnswer;

    @ExcelProperty(value = "答案解析", index = 3)
    @ColumnWidth(25)
    private String examQuestionAnalysis;

    @ExcelProperty(value = "试题类型（1:单选 2:多选 3:判断 4:填空 5:简答）", index = 4)
    @ColumnWidth(25)
    private Integer examQuestionTypes;

    @ExcelProperty(value = "难度等级（1:简单 2:中等 3:困难）", index = 5)
    @ColumnWidth(15)
    private Integer difficultyLevel;

    @ExcelProperty(value = "知识点标签", index = 6)
    @ColumnWidth(20)
    private String knowledgePoint;

    @ExcelProperty(value = "科目类型（1:语文 2:数学 3:英语 4:物理 5:化学 6:生物 7:政治 8:历史 9:地理）", index = 7)
    @ColumnWidth(25)
    private Integer kemuTypes;

    @ExcelProperty(value = "试题排序", index = 8)
    @ColumnWidth(10)
    private Integer examQuestionSequence;
}
