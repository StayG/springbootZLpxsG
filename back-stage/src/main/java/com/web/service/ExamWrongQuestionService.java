package com.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.web.domain.ExamWrongQuestion;
import com.web.domain.Req.ExamWrongQuestionReq;
import com.web.domain.Resp.ExamWrongQuestionResp;
import com.web.utils.PageUtils;

import java.util.List;


public interface ExamWrongQuestionService extends IService<ExamWrongQuestion> {

    PageUtils selectPage(ExamWrongQuestionReq req);

    ExamWrongQuestionResp info(Integer id);

    /**
     * 查询某个学生某道题的所有错误记录（用于查看历史作答）
     */
    List<ExamWrongQuestionResp> getHistoryByUserAndQuestion(String usersId, String examQuestionId);
}
