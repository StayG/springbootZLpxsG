package com.web.service;

import com.web.domain.Req.NoticesReq;
import com.web.domain.Resp.NoticesResp;
import com.web.utils.PageUtils;
import com.web.domain.Notices;
import com.baomidou.mybatisplus.extension.service.IService;


public interface NoticesService extends IService<Notices> {

    PageUtils selectPage(NoticesReq req);

    NoticesResp info(Integer id);
}
