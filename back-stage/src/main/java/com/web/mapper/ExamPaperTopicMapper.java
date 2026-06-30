package com.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.web.domain.ExamPaperTopic;
import com.web.domain.Req.ExamPaperTopicReq;
import com.web.domain.Resp.ExamPaperTopicResp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ExamPaperTopicMapper extends BaseMapper<ExamPaperTopic> {

    List<ExamPaperTopicResp> selectRespList(Page page, @Param("req") ExamPaperTopicReq req);

    Integer selectscore(Integer examPaperId);

    ExamPaperTopicResp info(Integer id);

    List<ExamPaperTopicResp> selectListResp(Page page, @Param("req") Map<String, Object> req);
}
