package com.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.web.domain.ExamInfo;
import com.web.domain.Req.ExamInfoReq;
import com.web.domain.Resp.StudentExamInfoResp;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 考试信息 Mapper 接口
 * </p>
 */
public interface ExamInfoMapper extends BaseMapper<ExamInfo> {

    /**
     * 分页查询考试信息（支持数据隔离）
     *
     * @param req 查询条件
     * @return 考试信息列表
     */
    List<ExamInfo> selectPageList(@Param("req") ExamInfoReq req);

    /**
     * 学生端分页查询考试信息
     *
     * @param req 查询条件
     * @return 学生端考试信息列表
     */
    List<StudentExamInfoResp> selectStudentPageList(@Param("req") ExamInfoReq req);
}
