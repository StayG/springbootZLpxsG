package com.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.web.domain.ExamQuestion;
import com.web.domain.Req.ExamQuestionReq;
import com.web.domain.Resp.ExamQuestionResp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface ExamQuestionMapper extends BaseMapper<ExamQuestion> {

    List<ExamQuestionResp> selectRespList(Page page, @Param("req") ExamQuestionReq req);
}
