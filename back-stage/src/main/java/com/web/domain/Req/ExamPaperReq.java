package com.web.domain.Req;

import com.web.utils.PageInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
public class ExamPaperReq extends PageInfo {

    public PageInfo getPageInfo() {
        return this;
    }

    @Schema(description = "主键 id")
    private Integer id;

    @Schema(description = "试卷名")
    private String examPaperName;

    @Schema(description = "考试时间（分钟）")
    private String examPaperDate;

    @Schema(description = "试卷总分")
    private Integer examPaperMyscore;

    @Schema(description = "科目")
    private Integer kemuTypes;

    @Schema(description = "试卷类")
    private Integer examPaperTypes;

    @Schema(description = "组卷方式")
    private Integer zujuanTypes;

    @Schema(description = "逻辑删除")
    private Integer examPaperDelete;

    // ===== 数据隔离相关字段 =====
    @Schema(description = "用户角色（用于数据隔离）")
    private String role;

    @Schema(description = "教师ID（用于数据隔离）")
    private Integer teacherId;



}
