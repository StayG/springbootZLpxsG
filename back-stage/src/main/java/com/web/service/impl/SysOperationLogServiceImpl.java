package com.web.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.web.domain.SysOperationLog;
import com.web.domain.Req.SysOperationLogReq;
import com.web.mapper.SysOperationLogMapper;
import com.web.service.SysOperationLogService;
import com.web.utils.PageUtils;
import com.web.utils.Query;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class SysOperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog>
        implements SysOperationLogService {

    @Resource
    private SysOperationLogMapper sysOperationLogMapper;

    @Override
    public PageUtils selectPage(SysOperationLogReq req) {
        Page<SysOperationLog> page = new Query<SysOperationLog>(req.getPageInfo()).getPage();
        page.setRecords(sysOperationLogMapper.selectPageList(page, req));
        return new PageUtils(page);
    }

    @Override
    public List<String> listDistinctAdminNames(SysOperationLogReq req) {
        return sysOperationLogMapper.selectDistinctAdminNames(req);
    }

    @Override
    public List<SysOperationLog> listForExport(SysOperationLogReq req) {
        return sysOperationLogMapper.selectExportList(req);
    }
}
