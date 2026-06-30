package com.web.domain.Resp;

import lombok.Data;

import java.io.Serializable;

@Data
public class GradesScoreDistributionRow implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 列名避免 b90_100 形式，否则 mapUnderscoreToCamelCase 会映射成 b90100 导致读不到 */
    private Long cnt90;
    private Long cnt80;
    private Long cnt70;
    private Long cnt60;
    private Long cnt0;
}
