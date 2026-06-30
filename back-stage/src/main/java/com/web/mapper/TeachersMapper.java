package com.web.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.web.domain.Req.TeachersReq;
import com.web.domain.Resp.TeachersResp;
import com.web.domain.Teachers;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface TeachersMapper extends BaseMapper<Teachers> {

    List<TeachersResp> selectRespList(Page page, @Param("req") TeachersReq req);
}
