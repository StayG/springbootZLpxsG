package com.web.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.web.domain.ExamDetails;
import com.web.domain.Req.ExamDetailsReq;
import com.web.domain.Resp.ExamDetailsResp;
import com.web.mapper.ExamDetailsMapper;
import com.web.service.DictionaryService;
import com.web.service.ExamDetailsService;
import com.web.utils.PageUtils;
import com.web.utils.Query;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class ExamDetailsServiceImpl extends ServiceImpl<ExamDetailsMapper, ExamDetails> implements ExamDetailsService {

    @Resource
    private ExamDetailsMapper examDetailsMapper;
    @Resource
    private DictionaryService dictionaryService;

    @Override
    public PageUtils selectPage(ExamDetailsReq req) {
        Page<ExamDetailsResp> page = new Query<ExamDetailsResp>(req.getPageInfo()).getPage();
        List<ExamDetailsResp> records = examDetailsMapper.selectRespList(page, req);

        if (records != null && !records.isEmpty() && dictionaryService != null) {
            for (ExamDetailsResp resp : records) {
                try {
                    if (resp != null) {
                        if (resp.getExamQuestionTypes() != null) {
                            String examQuestionTypesName = dictionaryService.getDictionaryName("exam_question_types",
                                    resp.getExamQuestionTypes());
                            if (examQuestionTypesName != null) {
                                resp.setExamQuestionTypesName(examQuestionTypesName);
                            }
                        }
                        if (resp.getKemuTypes() != null) {
                            String kemuName = dictionaryService.getDictionaryName("kemu_types", resp.getKemuTypes());
                            if (kemuName != null) {
                                resp.setKemuTypesName(kemuName);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("?????????" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        page.setRecords(records);
        return new PageUtils(page);
    }

    @Override
    public ExamDetailsResp info(Integer id) {
        return examDetailsMapper.info(id);
    }
}
