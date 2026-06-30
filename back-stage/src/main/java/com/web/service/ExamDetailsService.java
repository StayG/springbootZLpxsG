package com.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.web.domain.ExamDetails;
import com.web.domain.Req.ExamDetailsReq;
import com.web.domain.Resp.ExamDetailsResp;
import com.web.utils.PageUtils;


public interface ExamDetailsService extends IService<ExamDetails> {

    PageUtils selectPage(ExamDetailsReq req);

    ExamDetailsResp info(Integer id);
}
