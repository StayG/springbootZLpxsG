package com.web.domain.Resp;

import com.web.domain.ExamWrongQuestion;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class ExamWrongQuestionResp extends ExamWrongQuestion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "试卷名")
    private String examPaperName;

    @Schema(description = "考试名称")
    private String examName;

    @Schema(description = "考试信息ID")
    private Integer examInfoId;

    @Schema(description = "试题名")
    private String examQuestionName;

    @Schema(description = "考试时长")
    private String examPaperDate;

    @Schema(description = "试卷总分")
    private String examPaperMyscore;

    @Schema(description = "用户姓名")
    private String nickname;

    @Schema(description = "试题类")
    private Integer examQuestionTypes;

    @Schema(description = "试题类型名")
    private String examQuestionTypesName;

    @Schema(description = "试题排序")
    private Integer examQuestionSequence;

    @Schema(description = "选项")
    private String examQuestionOptions;

    @Schema(description = "正确答案")
    private String examQuestionAnswer;

    @Schema(description = "答案解析")
    private String examQuestionAnalysis;

    @Schema(description = "用户答案")
    private String examDetailsMyanswer;

    @Schema(description = "交卷后显示答案：0不显示，1显示")
    private Integer showAnswerAfterSubmit;

    @Schema(description = "考试记录状态：0-考试中，1-已提交，2-强制交卷，3-已完成")
    private Integer examRecordStatus;

    @Schema(description = "当前是否允许查看标准答案/解析")
    private Boolean allowViewAnswer;

    @Schema(description = "该题在本次考试中的得分（来自答题详情）")
    private Integer examDetailsMyscore;

    @Schema(description = "该题的错误次数（重复错题统计）")
    private Integer wrongCount;

}
