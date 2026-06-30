package com.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.web.domain.ExamRecord;
import com.web.domain.Req.ExamRecordReq;
import com.web.domain.Resp.ExamRecordResp;
import com.web.domain.Req.GradesStatisticsReq;
import com.web.domain.Resp.ExamDetailsResp;
import com.web.domain.Resp.ExamGradeTableRow;
import com.web.domain.Resp.GradesDailyTrendRow;
import com.web.domain.Resp.GradesScoreDistributionRow;
import com.web.domain.Resp.GradesSummaryRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExamRecordMapper extends BaseMapper<ExamRecord> {

    List<ExamRecordResp> selectRespList(Page<ExamRecordResp> page, @Param("req") ExamRecordReq req);

    GradesSummaryRow selectGradesSummary(@Param("req") GradesStatisticsReq req);

    GradesScoreDistributionRow selectGradesScoreDistribution(@Param("req") GradesStatisticsReq req);

    List<GradesDailyTrendRow> selectGradesDailyTrend(@Param("req") GradesStatisticsReq req,
                                                      @Param("trendStart") String trendStart,
                                                      @Param("trendEnd") String trendEnd);

    List<ExamGradeTableRow> selectExamGradeCompare(@Param("req") GradesStatisticsReq req);

    List<ExamGradeTableRow> selectExamGradeTable(Page<ExamGradeTableRow> page, @Param("req") GradesStatisticsReq req);

    /** 成绩与统计导出：与分页列表同一条件，不分页 */
    List<ExamGradeTableRow> selectExamGradeTableList(@Param("req") GradesStatisticsReq req);

    List<ExamDetailsResp> getDetailsList(String uuid);

    int countIfRecordEligibleForExamRank(@Param("recordId") Integer recordId);

    int countPublishedExamRecordsForExam(@Param("examInfoId") Integer examInfoId);

    int countPublishedExamRecordsWithHigherScore(@Param("examInfoId") Integer examInfoId,
                                                 @Param("totalScore") Integer totalScore);

}
