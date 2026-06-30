package com.web.domain.Resp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.web.domain.Managers;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * ClassName: ManagersResp
 * Description:
 *
 * @Author Mr.hu
 * @Create 2025-02-06 17:14
 */
@Data
public class ManagersResp extends Managers {
    private static final long serialVersionUID = 1L;

}
