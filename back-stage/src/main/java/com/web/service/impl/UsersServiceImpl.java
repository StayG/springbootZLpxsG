package com.web.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.web.domain.Req.UsersReq;
import com.web.domain.Resp.UsersResp;
import com.web.domain.Users;
import com.web.mapper.UsersMapper;
import com.web.service.UsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.web.utils.PageUtils;
import com.web.utils.Query;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;


@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements UsersService {

        @Resource
        private UsersMapper usersMapper;


        @Override
        public PageUtils selectPage(UsersReq req) {
        Page<UsersResp> page = new Query<UsersResp>(req.getPageInfo()).getPage();
            page.setRecords(usersMapper.selectRespList(page,req));
            return new PageUtils(page);
        }

        @Override
        public UsersResp info(Integer id) {
            return BeanUtil.copyProperties(usersMapper.selectById(id),UsersResp.class);
        }
}
