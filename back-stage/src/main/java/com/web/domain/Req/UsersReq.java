package com.web.domain.Req;

import com.web.utils.PageInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
public class UsersReq  extends PageInfo {


      public PageInfo getPageInfo() {
    return this;
  }

      @Schema(description = "用户名")
      private String userName;

      @Schema(description = "邮箱")
      private String email;

      @Schema(description = "电话")
      private String phone;

      @Schema(description = "密码")
      private String password;

      @Schema(description = "性别")
      private String gender;



}
