package com.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.web.domain.SysOperationLog;
import com.web.domain.Req.SysOperationLogReq;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysOperationLogMapper extends BaseMapper<SysOperationLog> {

    List<SysOperationLog> selectPageList(Page<?> page, @Param("req") SysOperationLogReq req);

    List<String> selectDistinctAdminNames(@Param("req") SysOperationLogReq req);

    List<SysOperationLog> selectExportList(@Param("req") SysOperationLogReq req);
}
