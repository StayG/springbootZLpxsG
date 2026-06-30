package com.web.service;

import com.web.domain.Req.TeachersReq;
import com.web.domain.Resp.TeachersResp;
import com.web.utils.PageUtils;
import com.web.domain.Teachers;
import com.baomidou.mybatisplus.extension.service.IService;


public interface TeachersService extends IService<Teachers> {

    PageUtils selectPage(TeachersReq req);

    TeachersResp info(Integer id);
}
