package com.web.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.web.domain.Notices;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.web.domain.Req.NoticesReq;
import com.web.domain.Resp.NoticesResp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface NoticesMapper extends BaseMapper<Notices> {

    List<NoticesResp> selectRespList(Page page, @Param("req") NoticesReq req);
}
