package com.web.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 错题
 * </p>
 */
@Data
@TableName("exam_wrong_question")
@Schema(name = "ExamWrongQuestion 对象", description = "错题")
public class ExamWrongQuestion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "用户 id")
    private String usersId;

    @Schema(description = "试卷 id")
    private String examPaperId;

    @Schema(description = "试题 id")
    private String examQuestionId;

    @Schema(description = "关联考试记录 id")
    private Integer examRecordId;

    @Schema(description = "考生作答")
    private String examDetailsMyanswer;

    @Schema(description = "掌握状态：0未掌握 1已掌握")
    private Integer masteryStatus;

    @Schema(description = "记录时间")
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "insert_time", fill = FieldFill.INSERT)
    private Date InsertTime;

    @Schema(description = "创建时间")
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
}
