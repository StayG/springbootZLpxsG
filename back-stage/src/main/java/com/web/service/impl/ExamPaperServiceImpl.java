package com.web.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.web.domain.ExamPaper;
import com.web.domain.Req.ExamPaperReq;
import com.web.domain.Resp.ExamPaperResp;
import com.web.mapper.ExamPaperMapper;
import com.web.service.DictionaryService;
import com.web.service.ExamPaperService;
import com.web.utils.PageUtils;
import com.web.utils.Query;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class ExamPaperServiceImpl extends ServiceImpl<ExamPaperMapper, ExamPaper> implements ExamPaperService {

    @Resource
    private ExamPaperMapper examPaperMapper;

    @Resource
    private DictionaryService dictionaryService;

    @Override
    public PageUtils selectPage(ExamPaperReq req) {
        Page<ExamPaperResp> page = new Query<ExamPaperResp>(req.getPageInfo()).getPage();
        List<ExamPaperResp> records = examPaperMapper.selectRespList(page, req);

        if (records != null && !records.isEmpty() && dictionaryService != null) {
            for (ExamPaperResp resp : records) {
                fillDictionaryNames(resp);
            }
        }
        page.setRecords(records);
        return new PageUtils(page);
    }

    @Override
    public ExamPaperResp info(Integer id) {
        ExamPaper examPaper = examPaperMapper.selectById(id);
        if (examPaper == null) {
            return null;
        }
        ExamPaperResp resp = BeanUtil.copyProperties(examPaper, ExamPaperResp.class);
        if (dictionaryService != null && resp != null) {
            fillDictionaryNames(resp);
        }
        return resp;
    }

    private void fillDictionaryNames(ExamPaperResp resp) {
        if (resp == null || dictionaryService == null) {
            return;
        }
        try {
            if (resp.getKemuTypes() != null) {
                String kemuName = dictionaryService.getDictionaryName("kemu_types", resp.getKemuTypes());
                if (kemuName != null) {
                    resp.setKemuTypesName(kemuName);
                }
            }
            if (resp.getZujuanTypes() != null) {
                String zujuanName = dictionaryService.getDictionaryName("zujuan_types", resp.getZujuanTypes());
                if (zujuanName != null) {
                    resp.setZujuanTypesName(zujuanName);
                }
            }
            if (resp.getExamPaperTypes() != null) {
                String examPaperTypeName = dictionaryService.getDictionaryName("exam_paper_types",
                        resp.getExamPaperTypes());
                if (examPaperTypeName != null) {
                    resp.setExamPaperTypesName(examPaperTypeName);
                }
            }
        } catch (Exception e) {
            System.err.println("?????????" + e.getMessage());
            e.printStackTrace();
        }
    }
}
