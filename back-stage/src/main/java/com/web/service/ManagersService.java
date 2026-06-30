package com.web.service;

import com.web.domain.Req.ManagersReq;
import com.web.domain.Resp.ManagersResp;
import com.web.utils.PageUtils;
import com.web.domain.Managers;
import com.baomidou.mybatisplus.extension.service.IService;


public interface ManagersService extends IService<Managers> {

    PageUtils selectPage(ManagersReq req);

    ManagersResp info(Integer id);
}
