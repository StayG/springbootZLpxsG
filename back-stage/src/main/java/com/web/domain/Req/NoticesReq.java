package com.web.domain.Req;

import com.web.utils.PageInfo;
import lombok.Data;

@Data
public class NoticesReq extends PageInfo {

    private String title;

    // 数据隔离字段
    private Integer teacherId; // 教师ID
    private Integer kemuId; // 科目ID
    private String role; // 当前登录角色

    public PageInfo getPageInfo() {
        return this;
    }

}
