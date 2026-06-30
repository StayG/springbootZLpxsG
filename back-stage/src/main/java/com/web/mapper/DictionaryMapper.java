package com.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.web.domain.Dictionary;
import com.web.domain.Req.DictionaryReq;
import com.web.domain.Resp.DictionaryResp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface DictionaryMapper extends BaseMapper<Dictionary> {

    List<DictionaryResp> selectRespList(Page page, @Param("req") DictionaryReq req);
}
