package com.web.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.web.domain.Notices;
import com.web.domain.Req.NoticesReq;
import com.web.domain.Resp.NoticesResp;
import com.web.mapper.NoticesMapper;
import com.web.service.NoticesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.web.utils.PageUtils;
import com.web.utils.Query;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;


@Service
public class NoticesServiceImpl extends ServiceImpl<NoticesMapper, Notices> implements NoticesService {

    @Resource
    private NoticesMapper noticesMapper;


    @Override
    public PageUtils selectPage(NoticesReq req) {
        Page<NoticesResp> page = new Query<NoticesResp>(req.getPageInfo()).getPage();
        page.setRecords(noticesMapper.selectRespList(page, req));
        return new PageUtils(page);
    }

    @Override
    public NoticesResp info(Integer id) {
        return BeanUtil.copyProperties(noticesMapper.selectById(id), NoticesResp.class);
    }
}
