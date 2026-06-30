package com.web.domain.Req;

import com.web.utils.PageInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
public class ManagersReq extends PageInfo {


    public PageInfo getPageInfo() {
       return this;
    }

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "密码")
    private String password;

}
