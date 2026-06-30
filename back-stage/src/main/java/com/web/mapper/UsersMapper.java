package com.web.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.web.domain.Req.UsersReq;
import com.web.domain.Resp.UsersResp;
import com.web.domain.Users;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface UsersMapper extends BaseMapper<Users> {

    List<UsersResp> selectRespList(Page page, @Param("req") UsersReq req);
}
