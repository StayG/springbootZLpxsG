package com.web.service.impl;

import com.web.mapper.CommonMapper;
import com.web.service.CommonService;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class CommonServiceImpl implements CommonService {

    @Resource
    private CommonMapper commonMapper;

    @Override
    public Long todayAppointmentCount(Integer merchantId, Integer status) {
        return commonMapper.todayAppointmentCount(merchantId, status);
    }
}
