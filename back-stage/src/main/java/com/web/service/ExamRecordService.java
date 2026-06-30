package com.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.web.domain.ExamRecord;
import com.web.domain.Req.ExamRecordReq;
import com.web.domain.Req.GradesStatisticsReq;
import com.web.domain.Resp.ExamRecordResp;
import com.web.domain.Resp.ExamDetailsResp;
import com.web.domain.Resp.ExamGradeTableRow;
import com.web.domain.Resp.GradesStatisticsVO;
import com.web.utils.PageUtils;

import java.util.List;


public interface ExamRecordService extends IService<ExamRecord> {

    PageUtils selectPage(ExamRecordReq req);

    /**
     * 成绩与统计（卡片、图表、列表）
     */
    GradesStatisticsVO gradesStatistics(GradesStatisticsReq req);

    /**
     * 成绩与统计：导出用考试成绩列表（与页面筛选一致，不分页）
     */
    List<ExamGradeTableRow> gradesStatisticsExportTable(GradesStatisticsReq req);

    ExamRecordResp info(Integer id);

    List<ExamDetailsResp> getDetailsList(String uuid);

    /**
     * 填充「同场考试、成绩已发布」后的名次与总人数（不参与时字段为 null）
     */
    void fillPublishedExamRank(ExamRecordResp resp, ExamRecord record);

}
