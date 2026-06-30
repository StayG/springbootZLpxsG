package com.web.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 试题表
 */
@Data
@TableName("exam_question")
@Schema(name = "ExamQuestion 对象", description = "试题表")
public class ExamQuestion implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @Schema(description = "试题名称")
    private String examQuestionName;

    @Schema(description = "科目")
    private Integer kemuTypes;

    @Schema(description = "选项")
    private String examQuestionOptions;

    @Schema(description = "正确答案")
    private String examQuestionAnswer;

    @Schema(description = "答案解析")
    private String examQuestionAnalysis;

    @Schema(description = "试题类型")
    private Integer examQuestionTypes;

    @Schema(description = "难度等级: 1-简单, 2-中等, 3-困难")
    private Integer difficultyLevel;

    @Schema(description = "知识点标签")
    private String knowledgePoint;

    /**
     * 填空题、简答题：学生端输入框上方公式快捷栏类型。
     * none | math | physics | chemistry | biology | geography
     */
    @Schema(description = "公式快捷栏类型: none,math,physics,chemistry,biology,geography")
    private String formulaType;

    @Schema(description = "试题排序，值越大越排前面")
    private Integer examQuestionSequence;

    @Schema(description = "创建时间")
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @Schema(description = "修改时间")
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
