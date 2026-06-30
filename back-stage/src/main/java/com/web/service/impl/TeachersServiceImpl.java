package com.web.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.web.domain.Req.TeachersReq;
import com.web.domain.Resp.TeachersResp;
import com.web.domain.Teachers;
import com.web.mapper.TeachersMapper;
import com.web.service.DictionaryService;
import com.web.service.TeachersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.web.utils.PageUtils;
import com.web.utils.Query;
import org.springframework.stereotype.Service;
import java.util.List;

import jakarta.annotation.Resource;


@Service
public class TeachersServiceImpl extends ServiceImpl<TeachersMapper, Teachers> implements TeachersService {

        @Resource
        private TeachersMapper teachersMapper;

        @Resource
        private DictionaryService dictionaryService;


        @Override
        public PageUtils selectPage(TeachersReq req) {
            if (req == null) {
                req = new TeachersReq();
            }
            // TeachersReq 继承自 PageInfo，可以直接传入 Query 构造函数
            Page<TeachersResp> page = new Query<TeachersResp>(req).getPage();
            List<TeachersResp> records = teachersMapper.selectRespList(page, req);
            if (records != null && !records.isEmpty() && dictionaryService != null) {
                for (TeachersResp resp : records) {
                    fillDictionaryNames(resp);
                }
            }
            page.setRecords(records);
            return new PageUtils(page);
        }

        @Override
        public TeachersResp info(Integer id) {
            try {
                Teachers teachers = teachersMapper.selectById(id);
                if (teachers == null) {
                    return null;
                }
                TeachersResp resp = BeanUtil.copyProperties(teachers, TeachersResp.class);
                if (dictionaryService != null && resp != null) {
                    fillDictionaryNames(resp);
                }
                return resp;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("获取教师信息失败: " + e.getMessage(), e);
            }
        }

        private void fillDictionaryNames(TeachersResp resp) {
            if (resp == null || dictionaryService == null) {
                return;
            }
            try {
                if (resp.getKemuTypes() != null) {
                    String kemuName = dictionaryService.getDictionaryName("kemu_types", resp.getKemuTypes());
                    if (kemuName != null) {
                        resp.setKemuTypesName(kemuName);
                    }
                }
            } catch (Exception e) {
                System.err.println("字典转换失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
}
