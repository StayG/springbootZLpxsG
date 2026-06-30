package com.web.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 答题详情
 * </p>
 */
@Data
@TableName("exam_details")
@Schema(name = "ExamDetails 对象", description = "考试记录")
public class ExamDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "试卷编号")
    private String examDetailsUuidNumber;

    @Schema(description = "用户 id")
    private Integer usersId;

    @Schema(description = "考试记录ID")
    private Integer examRecordId;

    @Schema(description = "试题 id")
    private Integer examQuestionId;

    @Schema(description = "题目分值")
    private Integer examPaperTopicNumber;

    @Schema(description = "题目在试卷中的排序（用于保证答题详情按原题顺序显示）")
    private Integer examPaperTopicSequence;

    @Schema(description = "考生答案")
    private String examDetailsMyanswer;

    @Schema(description = "试题得分")
    private Integer examDetailsMyscore;

    @Schema(description = "教师评分（主观题）")
    private Integer teacherScore;

    @Schema(description = "教师评语（主观题）")
    private String teacherComment;

    @Schema(description = "批阅状态：0-待批阅 1-已批阅（仅用于简答题）")
    private Integer reviewStatus;

    @Schema(description = "创建时间")
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @Schema(description = "更新时间")
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private Date updateTime;
}
