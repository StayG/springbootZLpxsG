package com.web.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.web.domain.Managers;
import com.web.domain.Req.ManagersReq;
import com.web.domain.Resp.ManagersResp;
import com.web.mapper.ManagersMapper;
import com.web.service.ManagersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.web.utils.PageUtils;
import com.web.utils.Query;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;


@Service
public class ManagersServiceImpl extends ServiceImpl<ManagersMapper, Managers> implements ManagersService {

    @Resource
    private ManagersMapper managersMapper;


    @Override
    public PageUtils selectPage(ManagersReq req) {
        Page<ManagersResp> page = new Query<ManagersResp>(req.getPageInfo()).getPage();
        page.setRecords(managersMapper.selectRespList(page, req));
        return new PageUtils(page);
    }

    @Override
    public ManagersResp info(Integer id) {
        return BeanUtil.copyProperties(managersMapper.selectById(id), ManagersResp.class);
    }
}
