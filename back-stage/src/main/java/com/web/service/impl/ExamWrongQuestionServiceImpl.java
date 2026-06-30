package com.web.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.web.domain.ExamWrongQuestion;
import com.web.domain.Req.ExamWrongQuestionReq;
import com.web.domain.Resp.ExamWrongQuestionResp;
import com.web.mapper.ExamWrongQuestionMapper;
import com.web.service.DictionaryService;
import com.web.service.ExamWrongQuestionService;
import com.web.utils.PageUtils;
import com.web.utils.Query;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class ExamWrongQuestionServiceImpl extends ServiceImpl<ExamWrongQuestionMapper, ExamWrongQuestion>
        implements ExamWrongQuestionService {

    @Resource
    private ExamWrongQuestionMapper examWrongQuestionMapper;

    @Resource
    private DictionaryService dictionaryService;

    @Override
    public PageUtils selectPage(ExamWrongQuestionReq req) {
        Page<ExamWrongQuestionResp> page = new Query<ExamWrongQuestionResp>(req.getPageInfo()).getPage();
        List<ExamWrongQuestionResp> records = examWrongQuestionMapper.selectRespList(page, req);
        if (records != null && !records.isEmpty() && dictionaryService != null) {
            for (ExamWrongQuestionResp resp : records) {
                try {
                    if (resp != null && resp.getExamQuestionTypes() != null) {
                        String typeName = dictionaryService.getDictionaryName("exam_question_types",
                                resp.getExamQuestionTypes());
                        if (typeName != null) {
                            resp.setExamQuestionTypesName(typeName);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("填充试题类型名称失败：" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        page.setRecords(records);
        return new PageUtils(page);
    }

    @Override
    public ExamWrongQuestionResp info(Integer id) {
        ExamWrongQuestionResp resp = examWrongQuestionMapper.info(id);
        if (dictionaryService != null && resp != null) {
            try {
                if (resp.getExamQuestionTypes() != null) {
                    String typeName = dictionaryService.getDictionaryName("exam_question_types",
                            resp.getExamQuestionTypes());
                    if (typeName != null) {
                        resp.setExamQuestionTypesName(typeName);
                    }
                }
            } catch (Exception e) {
                System.err.println("填充试题类型名称失败：" + e.getMessage());
                e.printStackTrace();
            }
        }
        return resp;
    }

    @Override
    public List<ExamWrongQuestionResp> getHistoryByUserAndQuestion(String usersId, String examQuestionId) {
        return examWrongQuestionMapper.selectHistoryByUserAndQuestion(usersId, examQuestionId);
    }
}
