package com.web.domain.Resp;


import com.web.domain.ExamPaperTopic;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
public class ExamPaperTopicResp extends ExamPaperTopic implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "试卷名")
    private String examPaperName;

    @Schema(description = "试题名")
    private String examQuestionName;

    @Schema(description = "试题类")
    private Integer examQuestionTypes;

    @Schema(description = "试题类型名")
    private String examQuestionTypesName;

    @Schema(description = "考试时长")
    private String examPaperDate;

    @Schema(description = "试卷总分")
    private String examPaperMyscore;

    @Schema(description = "正确答案")
    private String examQuestionAnswer;

    @Schema(description = "答案解析")
    private String examQuestionAnalysis;

    @Schema(description = "科目")
    private Integer kemuTypes;

    @Schema(description = "科目名")
    private String kemuTypesName;

    @Schema(description = "选项")
    private String examQuestionOptions;

    @Schema(description = "计算的试卷总分")
    private Integer calculatedTotalScore;

    @Schema(description = "用户 id")
    private Integer usersId;

    @Schema(description = "用户姓名")
    private String nickname;

    @Schema(description = "试题排序")
    private Integer examPaperTopicSequence;

    @Schema(description = "公式快捷栏类型: none,math,physics,chemistry,biology,geography")
    private String formulaType;

    @Schema(description = "难度等级: 1-简单, 2-中等, 3-困难")
    private Integer difficultyLevel;

    @Schema(description = "知识点标签")
    private String knowledgePoint;

}
