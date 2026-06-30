package com.web.domain.Resp;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ExamGradeTableRow implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer examInfoId;
    private String examName;
    /** 考试起止，如 2026-05-03 21:00:00—2026-05-03 23:00:00 */
    private String examTimeDisplay;
    private Long participants;
    private BigDecimal avgScore;
    private Integer maxScore;
    private BigDecimal passRate;
}
