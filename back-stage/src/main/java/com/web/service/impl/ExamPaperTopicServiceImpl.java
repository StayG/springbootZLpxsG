package com.web.service.impl;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.web.domain.ExamPaper;
import com.web.domain.ExamPaperTopic;
import com.web.domain.Req.ExamPaperTopicReq;
import com.web.domain.Resp.ExamPaperTopicResp;
import com.web.mapper.ExamPaperTopicMapper;
import com.web.service.DictionaryService;
import com.web.service.ExamPaperService;
import com.web.service.ExamPaperTopicService;
import com.web.service.RedisCacheService;
import com.web.utils.PageUtils;
import com.web.utils.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class ExamPaperTopicServiceImpl extends ServiceImpl<ExamPaperTopicMapper, ExamPaperTopic> implements ExamPaperTopicService {

    @Resource
    private ExamPaperTopicMapper examPaperTopicMapper;

    @Resource
    private DictionaryService dictionaryService;

    @Resource
    private ExamPaperService examPaperService;

    @Resource
    private RedisCacheService redisCacheService;


    @Override
    public PageUtils selectPage(ExamPaperTopicReq req) {
        Page<ExamPaperTopicResp> page = new Query<ExamPaperTopicResp>(req.getPageInfo()).getPage();
        List<ExamPaperTopicResp> records = examPaperTopicMapper.selectRespList(page, req);
        //
        if (records != null && !records.isEmpty() && dictionaryService != null) {
            for (ExamPaperTopicResp resp : records) {
                fillDictionaryNames(resp);
            }
        }
        page.setRecords(records);
        return new PageUtils(page);
    }

    @Override
    public ExamPaperTopicResp info(Integer id) {
        ExamPaperTopicResp resp = examPaperTopicMapper.info(id);
        //
        if (dictionaryService != null && resp != null) {
            fillDictionaryNames(resp);
        }
        return resp;
    }

    @Override
    public Integer selectscore(Integer examPaperId) {

        return examPaperTopicMapper.selectscore(examPaperId);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> req) {
        Page<ExamPaperTopicResp> page =new Query<ExamPaperTopicResp>(req).getPage();
        List<ExamPaperTopicResp> records = baseMapper.selectListResp(page,req);
        //
        if (records != null && !records.isEmpty() && dictionaryService != null) {
            for (ExamPaperTopicResp resp : records) {
                fillDictionaryNames(resp);
            }
        }
        page.setRecords(records);
        return new PageUtils(page);
    }

    
    private void fillDictionaryNames(ExamPaperTopicResp resp) {
        if (resp == null || dictionaryService == null) {
            return;
        }
        try {
            // 填充试题类型名称
            if (resp.getExamQuestionTypes() != null) {
                String typeName = dictionaryService.getDictionaryName("exam_question_types", resp.getExamQuestionTypes());
                if (typeName != null) {
                    resp.setExamQuestionTypesName(typeName);
                }
            }
            // 填充科目名称
            if (resp.getKemuTypes() != null) {
                String kemuName = dictionaryService.getDictionaryName("kemu_types", resp.getKemuTypes());
                if (kemuName != null) {
                    resp.setKemuTypesName(kemuName);
                }
            }
        } catch (Exception e) {
            // 填充字典名称失败
            System.err.println("填充字典名称失败：" + e.getMessage());
            e.printStackTrace();
        }
    }


    
    @Override
    public void updateScore( Integer examPaperId){

        List<ExamPaperTopic> list = this.lambdaQuery().eq(ExamPaperTopic::getExamPaperId, examPaperId).list();
        if(list != null && list.size()>0){
            Integer Score = 0;
            for(ExamPaperTopic examPaperTopic : list){
                Integer examPaperTopicNumber = examPaperTopic.getExamPaperTopicNumber();
                Score = Score +examPaperTopicNumber;
            }

            ExamPaper examPaper = examPaperService.getById(examPaperId);
            examPaper.setExamPaperMyscore(Score);
            examPaperService.updateById(examPaper);
            
            // 🔥 关键修复：更新试卷分数后立即清除相关缓存
            redisCacheService.deletePaperInfoCache(examPaperId);
            redisCacheService.deletePaperTopicsCache(examPaperId);
            redisCacheService.clearPaperListCache();
            log.info("🗑️  清除试卷缓存（更新总分 ID={}, 新总分={}）", examPaperId, Score);
        }
    }

}
