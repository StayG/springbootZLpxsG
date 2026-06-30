package com.web.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 试卷
 * </p>
 */
@Data
@TableName("exam_paper")
@Schema(name = "ExamPaper 对象", description = "试卷")
public class ExamPaper implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "试卷名称")
    private String examPaperName;

    @Schema(description = "试卷总分数")
    private Integer examPaperMyscore;

    @Schema(description = "科目")
    private Integer kemuTypes;

    @Schema(description = "试卷状态")
    private Integer examPaperTypes;

    @Schema(description = "组卷方式")
    private Integer zujuanTypes;

    @Schema(description = "创建时间")
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @Schema(description = "修改时间")
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
