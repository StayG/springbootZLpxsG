package com.web.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.web.domain.ExamRecord;
import com.web.domain.Req.ExamRecordReq;
import com.web.domain.Req.GradesStatisticsReq;
import com.web.domain.Resp.ExamDetailsResp;
import com.web.domain.Resp.ExamGradeTableRow;
import com.web.domain.Resp.ExamRecordResp;
import com.web.domain.Resp.GradesDailyTrendRow;
import com.web.domain.Resp.GradesScoreDistributionRow;
import com.web.domain.Resp.GradesStatisticsVO;
import com.web.domain.Resp.GradesSummaryRow;
import com.web.mapper.ExamRecordMapper;
import com.web.service.ExamRecordService;
import com.web.utils.PageUtils;
import com.web.utils.Query;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class ExamRecordServiceImpl extends ServiceImpl<ExamRecordMapper, ExamRecord> implements ExamRecordService {

    @Resource
    private ExamRecordMapper examRecordMapper;


    @Override
    public PageUtils selectPage(ExamRecordReq req) {
        Page<ExamRecordResp> page = new Query<ExamRecordResp>(req.getPageInfo()).getPage();
        page.setRecords(examRecordMapper.selectRespList(page, req));
        return new PageUtils(page);
    }

    @Override
    public ExamRecordResp info(Integer id) {
        return BeanUtil.copyProperties(examRecordMapper.selectById(id), ExamRecordResp.class);
    }

    @Override
    public List<ExamDetailsResp> getDetailsList(String uuid) {
        return examRecordMapper.getDetailsList(uuid);
    }

    @Override
    public void fillPublishedExamRank(ExamRecordResp resp, ExamRecord record) {
        if (resp == null || record == null || record.getExamInfoId() == null || record.getId() == null) {
            if (resp != null) {
                resp.setExamRank(null);
                resp.setExamRankTotal(null);
            }
            return;
        }
        if (examRecordMapper.countIfRecordEligibleForExamRank(record.getId()) <= 0) {
            resp.setExamRank(null);
            resp.setExamRankTotal(null);
            return;
        }
        int total = examRecordMapper.countPublishedExamRecordsForExam(record.getExamInfoId());
        if (total <= 0) {
            resp.setExamRank(null);
            resp.setExamRankTotal(null);
            return;
        }
        int better = examRecordMapper.countPublishedExamRecordsWithHigherScore(
                record.getExamInfoId(), record.getTotalScore());
        resp.setExamRank(better + 1);
        resp.setExamRankTotal(total);
    }

    @Override
    public GradesStatisticsVO gradesStatistics(GradesStatisticsReq req) {
        if (req.getPage() == null || req.getPage() < 1) {
            req.setPage(1);
        }
        if (req.getLimit() == null || req.getLimit() < 1) {
            req.setLimit(10);
        }

        LocalDate[] curRange = resolveCurrentExamScheduleRange(req);
        LocalDate[] prevRange = resolvePreviousRange(curRange);

        GradesStatisticsReq curReq = applyDateRange(copyReq(req), curRange[0], curRange[1]);
        GradesStatisticsReq prevReq = applyDateRange(copyReq(req), prevRange[0], prevRange[1]);

        GradesSummaryRow curSum = examRecordMapper.selectGradesSummary(curReq);
        GradesSummaryRow prevSum = examRecordMapper.selectGradesSummary(prevReq);

        GradesStatisticsVO vo = new GradesStatisticsVO();
        vo.setSummary(buildSummaryBlock(curSum, prevSum));

        GradesScoreDistributionRow dist = examRecordMapper.selectGradesScoreDistribution(curReq);
        vo.setScoreDistribution(buildDistribution(dist));

        LocalDate[] trendWindow = resolveTrendWindow(req);
        String ts = trendWindow[0].toString();
        String te = trendWindow[1].toString();
        List<GradesDailyTrendRow> trendRows = examRecordMapper.selectGradesDailyTrend(curReq, ts, te);
        vo.setAvgScoreTrend(fillTrendSeries(trendWindow[0], trendWindow[1], trendRows, false));
        vo.setPassRateTrend(fillTrendSeries(trendWindow[0], trendWindow[1], trendRows, true));

        List<ExamGradeTableRow> compareRows = examRecordMapper.selectExamGradeCompare(curReq);
        vo.setExamAvgCompare(mapCompare(compareRows));

        Page<ExamGradeTableRow> page = new Query<ExamGradeTableRow>(req.getPageInfo()).getPage();
        page.setRecords(examRecordMapper.selectExamGradeTable(page, curReq));
        vo.setExamTable(new PageUtils(page));
        return vo;
    }

    @Override
    public List<ExamGradeTableRow> gradesStatisticsExportTable(GradesStatisticsReq req) {
        LocalDate[] curRange = resolveCurrentExamScheduleRange(req);
        GradesStatisticsReq curReq = applyDateRange(copyReq(req), curRange[0], curRange[1]);
        return examRecordMapper.selectExamGradeTableList(curReq);
    }

    private static LocalDate[] resolveCurrentExamScheduleRange(GradesStatisticsReq req) {
        if (req.getDateStart() != null && !req.getDateStart().isBlank()
                && req.getDateEnd() != null && !req.getDateEnd().isBlank()) {
            return new LocalDate[]{LocalDate.parse(req.getDateStart()), LocalDate.parse(req.getDateEnd())};
        }
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(29);
        return new LocalDate[]{start, end};
    }

    private static LocalDate[] resolvePreviousRange(LocalDate[] cur) {
        long days = ChronoUnit.DAYS.between(cur[0], cur[1]) + 1;
        LocalDate prevEnd = cur[0].minusDays(1);
        LocalDate prevStart = prevEnd.minusDays(days - 1);
        return new LocalDate[]{prevStart, prevEnd};
    }

    private static GradesStatisticsReq copyReq(GradesStatisticsReq src) {
        GradesStatisticsReq r = new GradesStatisticsReq();
        BeanUtil.copyProperties(src, r);
        return r;
    }

    private static GradesStatisticsReq applyDateRange(GradesStatisticsReq r, LocalDate start, LocalDate end) {
        r.setDateStart(start.toString());
        r.setDateEnd(end.toString());
        return r;
    }

    private static LocalDate[] resolveTrendWindow(GradesStatisticsReq req) {
        String tr = req.getTrendRange();
        if (tr == null || tr.isBlank()) {
            tr = "7";
        }
        if ("custom".equals(tr)
                && req.getTrendDateStart() != null && !req.getTrendDateStart().isBlank()
                && req.getTrendDateEnd() != null && !req.getTrendDateEnd().isBlank()) {
            LocalDate a = LocalDate.parse(req.getTrendDateStart());
            LocalDate b = LocalDate.parse(req.getTrendDateEnd());
            if (a.isAfter(b)) {
                LocalDate t = a;
                a = b;
                b = t;
            }
            return new LocalDate[]{a, b};
        }
        int minusDays = "30".equals(tr) ? 29 : 6;
        LocalDate trendEnd = LocalDate.now();
        LocalDate trendStart = trendEnd.minusDays(minusDays);
        return new LocalDate[]{trendStart, trendEnd};
    }

    private static GradesStatisticsVO.SummaryBlock buildSummaryBlock(GradesSummaryRow cur, GradesSummaryRow prev) {
        GradesStatisticsVO.SummaryBlock block = new GradesStatisticsVO.SummaryBlock();
        long pCur = nzLong(cur != null ? cur.getParticipants() : null);
        long pPrev = nzLong(prev != null ? prev.getParticipants() : null);
        block.setParticipants(metricPercentDelta(pCur, pPrev));

        BigDecimal avgCur = cur != null ? cur.getAvgScore() : null;
        BigDecimal avgPrev = prev != null ? prev.getAvgScore() : null;
        block.setAvgScore(metricDecimalDeltaPercent(avgCur, avgPrev, 2));

        int maxCur = nzInt(cur != null ? cur.getMaxScore() : null);
        int maxPrev = nzInt(prev != null ? prev.getMaxScore() : null);
        block.setMaxScore(metricIntDelta(maxCur, maxPrev));

        long passCur = nzLong(cur != null ? cur.getPassCount() : null);
        long gradeCur = nzLong(cur != null ? cur.getGradedCount() : null);
        long passPrev = nzLong(prev != null ? prev.getPassCount() : null);
        long gradePrev = nzLong(prev != null ? prev.getGradedCount() : null);
        block.setPassRate(metricPassRateDelta(passCur, gradeCur, passPrev, gradePrev));

        long exCur = nzLong(cur != null ? cur.getExamCount() : null);
        long exPrev = nzLong(prev != null ? prev.getExamCount() : null);
        block.setExamCount(metricIntCountDelta(exCur, exPrev));

        return block;
    }

    private static long nzLong(Long v) {
        return v == null ? 0L : v;
    }

    private static int nzInt(Integer v) {
        return v == null ? 0 : v;
    }

    private static GradesStatisticsVO.MetricCard metricPercentDelta(long cur, long prev) {
        GradesStatisticsVO.MetricCard m = new GradesStatisticsVO.MetricCard();
        m.setValue(formatThousands(cur));
        if (prev <= 0) {
            m.setTrendUp(cur > 0);
            m.setTrendText(cur > 0 ? "较上期 ↑ —" : "较上期 —");
            return m;
        }
        double pct = (cur - prev) * 100.0 / prev;
        boolean up = pct >= 0;
        m.setTrendUp(up);
        String num = String.format(Locale.CHINA, "%.2f", Math.abs(pct));
        m.setTrendText((up ? "较上期 ↑ " : "较上期 ↓ ") + num + "%");
        return m;
    }

    private static String formatThousands(long n) {
        return String.format(Locale.CHINA, "%,d", n);
    }

    private static GradesStatisticsVO.MetricCard metricDecimalDeltaPercent(BigDecimal cur, BigDecimal prev, int scale) {
        GradesStatisticsVO.MetricCard m = new GradesStatisticsVO.MetricCard();
        BigDecimal c = cur != null ? cur.setScale(scale, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        m.setValue(c.toPlainString());
        if (prev == null || prev.compareTo(BigDecimal.ZERO) == 0) {
            m.setTrendUp(cur != null && cur.compareTo(BigDecimal.ZERO) > 0);
            m.setTrendText(m.isTrendUp() ? "较上期 ↑ —" : "较上期 —");
            return m;
        }
        BigDecimal diff = c.subtract(prev.setScale(scale, RoundingMode.HALF_UP));
        double pct = diff.doubleValue() * 100.0 / prev.doubleValue();
        boolean up = pct >= 0;
        m.setTrendUp(up);
        String num = String.format(Locale.CHINA, "%.2f", Math.abs(pct));
        m.setTrendText((up ? "较上期 ↑ " : "较上期 ↓ ") + num + "%");
        return m;
    }

    private static GradesStatisticsVO.MetricCard metricIntDelta(int cur, int prev) {
        GradesStatisticsVO.MetricCard m = new GradesStatisticsVO.MetricCard();
        m.setValue(String.valueOf(cur));
        int d = cur - prev;
        boolean up = d >= 0;
        m.setTrendUp(up);
        if (prev == 0 && cur == 0) {
            m.setTrendText("较上期 —");
            return m;
        }
        m.setTrendText((up ? "较上期 ↑ " : "较上期 ↓ ") + Math.abs(d));
        return m;
    }

    private static GradesStatisticsVO.MetricCard metricPassRateDelta(long passCur, long gradeCur, long passPrev, long gradePrev) {
        GradesStatisticsVO.MetricCard m = new GradesStatisticsVO.MetricCard();
        BigDecimal rateCur = gradeCur <= 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(passCur * 100.0 / gradeCur).setScale(2, RoundingMode.HALF_UP);
        m.setValue(rateCur.toPlainString() + "%");
        BigDecimal ratePrev = gradePrev <= 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(passPrev * 100.0 / gradePrev).setScale(4, RoundingMode.HALF_UP);
        if (gradePrev <= 0) {
            m.setTrendUp(rateCur.compareTo(BigDecimal.ZERO) > 0);
            m.setTrendText(m.isTrendUp() ? "较上期 ↑ —" : "较上期 —");
            return m;
        }
        BigDecimal diff = rateCur.subtract(ratePrev.setScale(2, RoundingMode.HALF_UP));
        boolean up = diff.compareTo(BigDecimal.ZERO) >= 0;
        m.setTrendUp(up);
        String num = diff.abs().setScale(2, RoundingMode.HALF_UP).toPlainString();
        m.setTrendText((up ? "较上期 ↑ " : "较上期 ↓ ") + num + "%");
        return m;
    }

    private static GradesStatisticsVO.MetricCard metricIntCountDelta(long cur, long prev) {
        GradesStatisticsVO.MetricCard m = new GradesStatisticsVO.MetricCard();
        m.setValue(String.valueOf(cur));
        long d = cur - prev;
        boolean up = d >= 0;
        m.setTrendUp(up);
        if (prev == 0 && cur == 0) {
            m.setTrendText("较上期 —");
            return m;
        }
        m.setTrendText((up ? "较上期 ↑ " : "较上期 ↓ ") + Math.abs(d));
        return m;
    }

    private static List<GradesStatisticsVO.ScoreDistributionItem> buildDistribution(GradesScoreDistributionRow row) {
        long b90 = row != null && row.getCnt90() != null ? row.getCnt90() : 0;
        long b80 = row != null && row.getCnt80() != null ? row.getCnt80() : 0;
        long b70 = row != null && row.getCnt70() != null ? row.getCnt70() : 0;
        long b60 = row != null && row.getCnt60() != null ? row.getCnt60() : 0;
        long b0 = row != null && row.getCnt0() != null ? row.getCnt0() : 0;
        long total = b90 + b80 + b70 + b60 + b0;
        List<GradesStatisticsVO.ScoreDistributionItem> list = new ArrayList<>();
        String[] labels = {"90-100", "80-89", "70-79", "60-69", "60分以下"};
        String[] keys = {"90-100", "80-89", "70-79", "60-69", "0-59"};
        long[] counts = {b90, b80, b70, b60, b0};
        for (int i = 0; i < labels.length; i++) {
            GradesStatisticsVO.ScoreDistributionItem it = new GradesStatisticsVO.ScoreDistributionItem();
            it.setLabel(labels[i]);
            it.setRangeKey(keys[i]);
            it.setCount(counts[i]);
            BigDecimal pct = total <= 0 ? BigDecimal.ZERO
                    : BigDecimal.valueOf(counts[i] * 100.0 / total).setScale(2, RoundingMode.HALF_UP);
            it.setPercent(pct);
            list.add(it);
        }
        return list;
    }

    private static List<GradesStatisticsVO.TrendPoint> fillTrendSeries(LocalDate start, LocalDate end,
                                                                         List<GradesDailyTrendRow> rows,
                                                                         boolean passRate) {
        Map<String, GradesDailyTrendRow> map = rows == null ? new HashMap<>()
                : rows.stream().collect(Collectors.toMap(GradesDailyTrendRow::getDayKey, r -> r, (a, b) -> a));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        List<GradesStatisticsVO.TrendPoint> list = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            String key = d.toString();
            GradesStatisticsVO.TrendPoint p = new GradesStatisticsVO.TrendPoint();
            p.setDate(d.format(fmt));
            GradesDailyTrendRow row = map.get(key);
            if (row != null) {
                p.setValue(passRate ? row.getPassRate() : row.getAvgScore());
            }
            list.add(p);
        }
        return list;
    }

    private static List<GradesStatisticsVO.ExamCompareItem> mapCompare(List<ExamGradeTableRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return new ArrayList<>();
        }
        List<GradesStatisticsVO.ExamCompareItem> list = new ArrayList<>();
        for (ExamGradeTableRow r : rows) {
            GradesStatisticsVO.ExamCompareItem it = new GradesStatisticsVO.ExamCompareItem();
            String name = r.getExamName();
            if (name != null && name.length() > 12) {
                name = name.substring(0, 12) + "…";
            }
            it.setName(name);
            it.setAvgScore(r.getAvgScore() != null
                    ? r.getAvgScore().setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);
            list.add(it);
        }
        return list;
    }
}
