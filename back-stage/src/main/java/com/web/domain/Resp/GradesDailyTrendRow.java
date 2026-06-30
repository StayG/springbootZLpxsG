package com.web.domain.Resp;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class GradesDailyTrendRow implements Serializable {
    private static final long serialVersionUID = 1L;

    private String dayKey;
    private String dayLabel;
    private BigDecimal avgScore;
    private BigDecimal passRate;
}
