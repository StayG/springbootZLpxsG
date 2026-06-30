package com.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.web.domain.ExamPaper;
import com.web.domain.Req.ExamPaperReq;
import com.web.domain.Resp.ExamPaperResp;
import com.web.utils.PageUtils;


public interface ExamPaperService extends IService<ExamPaper> {

    PageUtils selectPage(ExamPaperReq req);

    ExamPaperResp info(Integer id);
}
