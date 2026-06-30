package com.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.web.domain.Dictionary;
import com.web.domain.Req.DictionaryReq;
import com.web.domain.Resp.DictionaryResp;
import com.web.utils.PageUtils;

import jakarta.servlet.http.HttpServletRequest;

public interface DictionaryService extends IService<Dictionary> {

    PageUtils selectPage(DictionaryReq req);

    DictionaryResp info(Integer id);

    void dictionaryConvert(Object obj, HttpServletRequest request);

    String getDictionaryName(String dicCode, Integer codeIndex);

    /**
     * 检查科目名称是否已存在
     * 
     * @param indexName 科目名称
     * @param excludeId 排除的ID（编辑时使用）
     * @return true-已存在，false-不存在
     */
    boolean isSubjectNameExists(String indexName, Long excludeId);
}
