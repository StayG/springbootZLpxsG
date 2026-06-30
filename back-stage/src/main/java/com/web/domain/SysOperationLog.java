package com.web.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_operation_log")
@Schema(name = "SysOperationLog", description = "管理员与教师操作日志")
public class SysOperationLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "操作时间")
    private Date operationTime;

    @Schema(description = "操作人姓名")
    private String adminName;

    @TableField("operation_module")
    @Schema(description = "操作模块")
    private String operationModule;

    @Schema(description = "操作类型")
    private String actionType;

    @Schema(description = "操作内容")
    private String content;

    @Schema(description = "IP")
    private String ip;

    /** 1 成功，0 失败 */
    @Schema(description = "是否成功")
    private Integer success;
}
