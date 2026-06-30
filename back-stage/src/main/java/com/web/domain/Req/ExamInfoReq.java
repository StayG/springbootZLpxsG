package com.web.domain.Req;

import com.web.utils.PageInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>
 * 考试信息查询请求类
 * </p>
 */
@Data
public class ExamInfoReq extends PageInfo {

    public PageInfo getPageInfo() {
        return this;
    }

    @Schema(description = "考试名称（模糊查询）")
    private String examName;

    @Schema(description = "科目ID")
    private Integer kemuTypes;

    @Schema(description = "考试状态：0未开始，1进行中，2已结束")
    private Integer status;

    // ===== 数据隔离相关字段 =====

    @Schema(description = "用户角色（用于数据隔离）")
    private String role;

    @Schema(description = "教师ID（用于数据隔离）")
    private Integer teacherId;

    @Schema(description = "学生ID（用于查询学生考试状态）")
    private Integer usersId;
}
