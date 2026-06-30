package com.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.web.domain.ExamWrongQuestion;
import com.web.domain.Req.ExamWrongQuestionReq;
import com.web.domain.Resp.ExamWrongQuestionResp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExamWrongQuestionMapper extends BaseMapper<ExamWrongQuestion> {

    List<ExamWrongQuestionResp> selectRespList(Page page, @Param("req") ExamWrongQuestionReq req);

    ExamWrongQuestionResp info(Integer id);

    /**
     * 查询某个学生某道题的所有错误记录（用于查看历史作答）
     */
    List<ExamWrongQuestionResp> selectHistoryByUserAndQuestion(@Param("usersId") String usersId, @Param("examQuestionId") String examQuestionId);
}
