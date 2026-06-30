package com.web.domain.Req;

import com.web.utils.PageInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 成绩与统计查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GradesStatisticsReq extends PageInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    public PageInfo getPageInfo() {
        return this;
    }

    @Schema(description = "考试安排 id（全部不传）")
    private Integer examInfoId;

    @Schema(description = "考试开始日期起 yyyy-MM-dd")
    private String dateStart;

    @Schema(description = "考试开始日期止 yyyy-MM-dd")
    private String dateEnd;

    @Schema(description = "考试记录状态：与 exam_record.status 一致，空为全部")
    private Integer status;

    /**
     * 趋势图区间：7 | 30 | custom
     */
    @Schema(description = "平均分/及格率趋势：7 近7天 30 近30天 custom 自定义")
    private String trendRange;

    @Schema(description = "趋势自定义开始 yyyy-MM-dd")
    private String trendDateStart;

    @Schema(description = "趋势自定义结束 yyyy-MM-dd")
    private String trendDateEnd;

    @Schema(description = "教师数据隔离：科目")
    private Integer kemuTypes;

    @Schema(description = "教师数据隔离：教师 id")
    private Integer teacherId;
}
