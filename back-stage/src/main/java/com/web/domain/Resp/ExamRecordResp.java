package com.web.domain.Resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.web.domain.ExamRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExamRecordResp extends ExamRecord {

    @Schema(description = "用户姓名")
    private String nickname;

    @Schema(description = "试卷名")
    private String examPaperName;

    @Schema(description = "考试名称")
    private String examName;

    @Schema(description = "用户 id")
    private Integer usersId;

    @Schema(description = "试卷总分")
    private Integer examPaperMyscore;

    @Schema(description = "试题类")
    private String examQuestionTypes;

    @Schema(description = "试题 id")
    private Integer examQuestionsId;

    @Schema(description = "正确答案")
    private String examQuestionAnswer;

    @Schema(description = "试题名")
    private String examQuestionName;

    @Schema(description = "试题选项")
    private String examQuestionOptions;

    @Schema(description = "用户答案")
    private String examDetailsMyanswer;

    
    private List<ExamDetailsResp> examDetailsRespList ;

    @Schema(description = "是否有简答题")
    private Boolean hasSubjectiveQuestions;

    @Schema(description = "待批阅简答题数量")
    private Integer pendingReviewCount;

    @Schema(description = "已批阅简答题数量")
    private Integer reviewedSubjectiveCount;

    @Schema(description = "实际考试时长（分钟）")
    private Integer actualDuration;

    @Schema(description = "交卷后显示答案：0不显示，1显示")
    private Integer showAnswerAfterSubmit;

    @Schema(description = "当前是否允许查看标准答案/解析")
    private Boolean allowViewAnswer;

    @Schema(description = "考试安排开始时间（来自 exam_info，成绩详情页展示）")
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date examScheduleStart;

    @Schema(description = "考试安排结束时间（来自 exam_info）")
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date examScheduleEnd;

    @Schema(description = "考试科目展示名（exam_info.kemu_types 关联 dictionary）")
    private String kemuValue;

    @Schema(description = "试卷类型（来自 exam_paper.exam_paper_types，用于筛选）")
    private Integer examPaperTypes;

    @Schema(description = "本场考试（阅卷完成后）个人名次，未参与排名时为 null")
    private Integer examRank;

    @Schema(description = "本场考试（阅卷完成后）参与排名的人数，未参与排名时为 null")
    private Integer examRankTotal;

}
