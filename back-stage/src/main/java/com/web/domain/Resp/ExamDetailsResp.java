package com.web.domain.Resp;

import com.web.domain.ExamDetails;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class ExamDetailsResp extends ExamDetails implements Serializable {
    private static final long serialVersionUID = 1L;



    @Schema(description = "试题名")
    private String examQuestionName;

    @Schema(description = "用户姓名")
    private String nickname;

    @Schema(description = "考生账号")
    private String userName;

    @Schema(description = "试题类")
    private Integer examQuestionTypes;

    @Schema(description = "试题类型名")
    private String examQuestionTypesName;

    @Schema(description = "科目")
    private Integer kemuTypes;

    @Schema(description = "科目名")
    private String kemuTypesName;

    @Schema(description = "选项名")
    private String examQuestionOptions;

    @Schema(description = "正确答案")
    private String examQuestionAnswer;

    @Schema(description = "答案解析")
    private String examQuestionAnalysis;

    @Schema(description = "试卷名")
    private String examPaperName;

    @Schema(description = "试题分数")
    private Integer examPaperTopicNumber;

    @Schema(description = "题目在试卷中的排序（用于保证答题详情按原题顺序显示）")
    private Integer examPaperTopicSequence;




}
