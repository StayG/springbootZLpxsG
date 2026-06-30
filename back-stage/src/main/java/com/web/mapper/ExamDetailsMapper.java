package com.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.web.domain.ExamDetails;
import com.web.domain.Req.ExamDetailsReq;
import com.web.domain.Resp.ExamDetailsResp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExamDetailsMapper extends BaseMapper<ExamDetails> {

    List<ExamDetailsResp> selectRespList(Page page, @Param("req") ExamDetailsReq req);

    ExamDetailsResp info(@Param("id") Integer id);

}
