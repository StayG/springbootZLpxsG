package com.web.domain.Req;

import com.web.utils.PageInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ExamQuestionReq extends PageInfo implements Serializable {
    private static final long serialVersionUID = 1L;


    public PageInfo getPageInfo() {
        return this;
    }

    private List<Integer> ids;

    @Schema(description = "主键 id")
    private Integer id;

    @Schema(description = "试题名")
    private String examQuestionName;

    @Schema(description = "科目")
    private Integer kemuTypes;

    @Schema(description = "选项")
    private String examQuestionOptions;

    @Schema(description = "正确答案")
    private String examQuestionAnswer;

    @Schema(description = "答案解析")
    private String examQuestionAnalysis;

    @Schema(description = "试题类")
    private String examQuestionTypes;

    @Schema(description = "试题排序")
    private Integer examQuestionSequence;

    // ==================== 数据隔离相关字段 ====================
    @Schema(description = "当前用户角色（用于数据隔离）")
    private String role;

    @Schema(description = "当前教师ID（用于数据隔离）")
    private Integer teacherId;

    //
    // private String startTime;
    // private String endTime;
}
