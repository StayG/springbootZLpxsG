package com.web.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 试卷选题
 * </p>
 */
@Data
@TableName("exam_paper_topic")
@Schema(name = "ExamPaperTopic 对象", description = "试卷选题")
public class ExamPaperTopic implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "试卷 id'")
    private Integer examPaperId;

    @Schema(description = "试题 id")
    private Integer examQuestionId;

    @Schema(description = "试题分数")
    private Integer examPaperTopicNumber;

    @Schema(description = "试题排序，值越大排越前面")
    private Integer examPaperTopicSequence;

    @Schema(description = "创建时间")
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
}
