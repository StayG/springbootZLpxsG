package com.web.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.web.domain.Config;
import com.web.domain.Req.ConfigReq;
import com.web.domain.Resp.ConfigResp;
import com.web.mapper.ConfigMapper;
import com.web.service.ConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.web.utils.PageUtils;
import com.web.utils.Query;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;


@Service
public class ConfigServiceImpl extends ServiceImpl<ConfigMapper, Config> implements ConfigService {

    @Resource
    private ConfigMapper configMapper;


    @Override
    public PageUtils selectPage(ConfigReq req) {
        Page<ConfigResp> page = new Query<ConfigResp>(req.getPageInfo()).getPage();
        page.setRecords(configMapper.selectRespList(page, req));
        return new PageUtils(page);
    }

    @Override
    public ConfigResp info(Integer id) {
        return BeanUtil.copyProperties(configMapper.selectById(id), ConfigResp.class);
    }
}
