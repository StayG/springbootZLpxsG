package com.web.domain.Resp;


import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.web.domain.Users;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
public class UsersResp extends Users implements Serializable {

    private static final long serialVersionUID = 1L;


}
