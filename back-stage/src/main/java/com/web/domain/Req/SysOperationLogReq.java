package com.web.domain.Req;

import com.web.utils.PageInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysOperationLogReq extends PageInfo {

    private String beginTime;
    private String endTime;
    private String adminName;
    private String operationModule;
    private String actionType;
    private String keyword;

    /** 教师端：仅后端写入，按操作人姓名限制可见范围 */
    private String scopeTeacherDisplayName;

    public PageInfo getPageInfo() {
        return this;
    }
}
