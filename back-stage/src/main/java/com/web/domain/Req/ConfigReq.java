package com.web.domain.Req;

import com.web.utils.PageInfo;
import lombok.Data;


@Data
public class ConfigReq extends PageInfo {


    public PageInfo getPageInfo() {
        return this;
    }
}
