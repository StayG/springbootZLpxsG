package com.web.domain.Resp;

import com.web.domain.ExamQuestion;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ExamQuestionResp extends ExamQuestion implements Serializable {
    private static final long serialVersionUID = 1L;


    @Schema(description = "主键 id")
    private Integer id;

    @Schema(description = "试题名")
    private String examQuestionName;

    @Schema(description = "科目类")
    private Integer kemuTypes;

    @Schema(description = "科目名")
    private String kemuTypesName;

    @Schema(description = "正确答案")
    private String examQuestionAnswer;

    @Schema(description = "答案解析")
    private String examQuestionAnalysis;

    @Schema(description = "试题类型值")
    private Integer examQuestionTypes;

    @Schema(description = "试题类型名")
    private String examQuestionTypesName;

    @Schema(description = "排序值")
    private Integer examQuestionSequence;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "修改时间")
    private Date updateTime;


}
