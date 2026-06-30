package com.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.web.domain.SysOperationLog;
import com.web.domain.Req.SysOperationLogReq;
import com.web.utils.PageUtils;

import java.util.List;

public interface SysOperationLogService extends IService<SysOperationLog> {

    PageUtils selectPage(SysOperationLogReq req);

    List<String> listDistinctAdminNames(SysOperationLogReq req);

    List<SysOperationLog> listForExport(SysOperationLogReq req);
}
