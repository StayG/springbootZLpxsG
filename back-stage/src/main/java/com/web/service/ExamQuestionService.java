package com.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.web.domain.ExamQuestion;
import com.web.domain.Req.ExamQuestionReq;
import com.web.domain.Resp.ExamQuestionResp;
import com.web.domain.dto.ExamQuestionExcelDto;
import com.web.utils.PageUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface ExamQuestionService extends IService<ExamQuestion> {

    PageUtils selectPage(ExamQuestionReq req);

    ExamQuestionResp info(Integer id);

    Map<String, Object> getCurrentQuestionCounts(String kemuTypes);

    /**
     * Excel 批量导入试题
     * 
     * @param file Excel 文件
     * @return 导入结果
     */
    Map<String, Object> importExcel(MultipartFile file);

    /**
     * Excel 批量导出试题
     * 
     * @param req 查询条件
     * @return Excel 文件字节数组
     */
    byte[] exportExcel(ExamQuestionReq req);

    /**
     * 检查科目是否有关联的试题
     * 
     * @param kemuTypes 科目代码
     * @return true-有试题，false-无试题
     */
    boolean hasQuestionsForSubject(Integer kemuTypes);

}
