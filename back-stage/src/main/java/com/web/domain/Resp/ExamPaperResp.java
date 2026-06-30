package com.web.domain.Resp;

import com.web.domain.ExamPaper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
public class ExamPaperResp extends ExamPaper implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "科目名")
    private String kemuTypesName;

    @Schema(description = "试卷状态名")
    private String examPaperTypesName;

    @Schema(description = "组卷方式名称")
    private String zujuanTypesName;

}
