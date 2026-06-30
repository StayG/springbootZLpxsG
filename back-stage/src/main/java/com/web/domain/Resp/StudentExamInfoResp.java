package com.web.domain.Resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 学生端考试列表响应对象
 */
@Data
@Schema(description = "学生端考试列表响应对象")
public class StudentExamInfoResp implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "考试ID")
    private Integer id;

    @Schema(description = "考试名称")
    private String examName;

    @Schema(description = "试卷ID")
    private Integer examPaperId;

    @Schema(description = "科目名称")
    private String kemuValue;

    @Schema(description = "教师姓名")
    private String teacherName;

    @Schema(description = "考试开始时间")
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @Schema(description = "考试结束时间")
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @Schema(description = "考试时长（分钟）")
    private Integer duration;

    @Schema(description = "试卷总分")
    private Integer examPaperScore;

    @Schema(description = "当前状态：0-未开始, 1-进行中, 2-已结束, 3-已完成")
    private Integer status;

    @Schema(description = "考试记录UUID（用于继续考试或查看成绩）")
    private String examRecordUuidNumber;

    @Schema(description = "当前有效考试记录的作答开始时间（insertTime，用于与答题页倒计时对齐）")
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date examRecordInsertTime;

    @Schema(description = "学生得分（仅在已完成时返回）")
    private Integer totalScore;

    @Schema(description = "考试记录状态：0-考试中，1-已提交，2-强制交卷，3-已完成")
    private Integer examRecordStatus;

    @Schema(description = "待批阅简答题数量（>0 表示待出分，学生端宜引导查看答卷）")
    private Integer pendingReviewCount;

    @Schema(description = "允许重考：0-不允许，1-允许")
    private Integer allowRetake;

    @Schema(description = "最多允许重考次数")
    private Integer maxRetakeCount;

    @Schema(description = "当前总作答次数（含历史作废记录）")
    private Integer attemptCount;

    @Schema(description = "剩余重考次数")
    private Integer remainingRetakeCount;

    @Schema(description = "当前是否可重考：false-不可重考，true-可重考")
    private Boolean canRetake;

    @Schema(description = "学生端动作类型：not_started/resume_exam/enter_exam/retake_exam/view_answer_sheet/view_result/ended")
    private String actionType;

    @Schema(description = "学生端动作文案")
    private String actionText;
}
