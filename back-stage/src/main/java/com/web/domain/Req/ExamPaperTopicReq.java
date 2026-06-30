package com.web.domain.Req;

import com.web.utils.PageInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
public class ExamPaperTopicReq extends PageInfo {


    public PageInfo getPageInfo() {
       return this;
    }

    @Schema(description = "主键 id")
    private Integer id;

    @Schema(description = "试卷名")
    private String examPaperName;

    @Schema(description = "试题名")
    private String examQuestionName;

    @Schema(description = "科目")
    private Integer kemuTypes;

    @Schema(description = "试卷 id")
    private Integer examPaperId;

    @Schema(description = "考试时间")
    private Integer examPaperDate;

    @Schema(description = "选项")
    private String examQuestionOptions;

    @Schema(description = "用户 id")
    private Integer usersId;

}
