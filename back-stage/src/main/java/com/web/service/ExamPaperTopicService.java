package com.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.web.domain.ExamPaperTopic;
import com.web.domain.Req.ExamPaperTopicReq;
import com.web.domain.Resp.ExamPaperTopicResp;
import com.web.utils.PageUtils;

import java.util.Map;


public interface ExamPaperTopicService extends IService<ExamPaperTopic> {

    PageUtils selectPage(ExamPaperTopicReq req);

    ExamPaperTopicResp info(Integer id);

    Integer selectscore(Integer examPaperId );

    //
    PageUtils queryPage(Map<String, Object> req);

    void updateScore( Integer examPaperId);
}
