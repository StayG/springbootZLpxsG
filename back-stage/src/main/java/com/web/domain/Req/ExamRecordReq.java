package com.web.domain.Req;

import com.web.utils.PageInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExamRecordReq extends PageInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    public PageInfo getPageInfo() {
        return this;
    }

    @Schema(description = "主键 id")
    private Integer id;

    @Schema(description = "考试编号")
    private String examRecordUuidNumber;

    @Schema(description = "用户姓名")
    private String nickname;

    @Schema(description = "用户 id")
    private Integer usersId;

    @Schema(description = "分页筛选：考试安排名称关键词（子串匹配 exam_info.exam_name；字段名保持 examPaperName 兼容前端）")
    private String examPaperName;

    @Schema(description = "考试安排名称（模糊筛选，与试卷名任一匹配即可）")
    private String examName;

    @Schema(description = "试卷 id")
    private Integer examPaperId;

    @Schema(description = "考试安排 id（exam_info.id，分页筛选）")
    private Integer examInfoId;

    @Schema(description = "总分")
    private Integer totalScore;

    @Schema(description = "考试时间")
    private Date insertTime;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "教师ID（用于权限过滤）")
    private Integer teacherId;

    @Schema(description = "科目ID（用于教师学科隔离）")
    private Integer kemuTypes;

    @Schema(description = "是否只查询当前有效记录：1-是，0/空-否")
    private Integer onlyLatest;

    //
    private List<Long> ids;


}
