package com.web.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 考试记录表
 */
@Data
@TableName("exam_record")
@Schema(name = "ExamRecord 对象", description = "考试记录表")
public class ExamRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "考试编号")
    private String examRecordUuidNumber;

    @Schema(description = "考试用户 id")
    private Integer usersId;

    @Schema(description = "所属试卷 id")
    private Integer examPaperId;

    @Schema(description = "所属考试 id（关联exam_info表）")
    private Integer examInfoId;

    @Schema(description = "总分")
    private Integer totalScore;

    @Schema(description = "客观题自动判分总和")
    private Integer autoScore;

    @Schema(description = "主观题教师批改得分总和")
    private Integer teacherScore;

    @Schema(description = "及格状态：0-待判定，1-及格，2-不及格")
    private Integer passStatus;

    @Schema(description = "考试状态：0-考试中，1-已提交，2-强制交卷，3-已完成")
    private Integer status;

    @Schema(description = "是否为当前有效记录：1-当前有效，0-历史作废")
    private Integer isLatest;

    @Schema(description = "切屏次数")
    private Integer screenSwitchCount;

    @Schema(description = "每次切屏发生时间（JSON 字符串数组，格式 yyyy-MM-dd HH:mm:ss）")
    private String screenSwitchTimes;

    @Schema(description = "考试时间")
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "insert_time", fill = FieldFill.INSERT)
    private Date insertTime;

    @Schema(description = "交卷时间/强制结束时间")
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @Schema(description = "创建时间")
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
}
