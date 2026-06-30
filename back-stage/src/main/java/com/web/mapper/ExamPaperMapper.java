package com.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.web.domain.ExamPaper;
import com.web.domain.Req.ExamPaperReq;
import com.web.domain.Resp.ExamPaperResp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface ExamPaperMapper extends BaseMapper<ExamPaper> {

    List<ExamPaperResp> selectRespList(Page page, @Param("req") ExamPaperReq req);
}
