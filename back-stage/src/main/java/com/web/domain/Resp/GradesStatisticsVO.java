package com.web.domain.Resp;

import com.web.utils.PageUtils;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 成绩与统计整页数据
 */
@Data
public class GradesStatisticsVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private SummaryBlock summary;
    private List<ScoreDistributionItem> scoreDistribution;
    private List<TrendPoint> avgScoreTrend;
    private List<TrendPoint> passRateTrend;
    private List<ExamCompareItem> examAvgCompare;
    private PageUtils examTable;

    @Data
    public static class SummaryBlock implements Serializable {
        private MetricCard participants;
        private MetricCard avgScore;
        private MetricCard maxScore;
        private MetricCard passRate;
        private MetricCard examCount;
    }

    @Data
    public static class MetricCard implements Serializable {
        private String value;
        /** 较上期 文案，如 ↑ 12.45% */
        private String trendText;
        private boolean trendUp;
    }

    @Data
    public static class ScoreDistributionItem implements Serializable {
        private String label;
        private String rangeKey;
        private long count;
        private BigDecimal percent;
    }

    @Data
    public static class TrendPoint implements Serializable {
        private String date;
        private BigDecimal value;
    }

    @Data
    public static class ExamCompareItem implements Serializable {
        private String name;
        private BigDecimal avgScore;
    }
}
