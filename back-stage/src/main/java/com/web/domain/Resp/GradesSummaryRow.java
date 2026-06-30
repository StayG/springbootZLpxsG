package com.web.domain.Resp;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class GradesSummaryRow implements Serializable {
    private static final long serialVersionUID = 1L;

    /** COUNT(DISTINCT users_id) */
    private Long participants;
    private BigDecimal avgScore;
    private Integer maxScore;
    private Long passCount;
    /** COUNT(*) 参与统计的记录数 */
    private Long gradedCount;
    /** COUNT(DISTINCT exam_info.id) */
    private Long examCount;
}
