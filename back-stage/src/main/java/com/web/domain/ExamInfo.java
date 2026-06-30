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

/**
 * <p>
 * 考试信息实体类
 * </p>
 */
@Data
@TableName("exam_info")
@Schema(description = "考试信息")
public class ExamInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Integer id;

    @Schema(description = "考试名称")
    private String examName;

    @Schema(description = "科目ID")
    private Integer kemuTypes;

    @Schema(description = "创建教师ID")
    private Integer teacherId;

    @Schema(description = "关联试卷ID")
    private Integer examPaperId;

    @Schema(description = "考试开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @Schema(description = "考试结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    @Schema(description = "考试时长（分钟）")
    private Integer duration;

    @Schema(description = "及格分数")
    private Integer passingScore;

    @Schema(description = "是否允许切屏：0不允许，1允许")
    private Integer allowScreenSwitch;

    @Schema(description = "最大切屏次数")
    private Integer maxScreenSwitch;

    @Schema(description = "是否允许复制粘贴：0不允许，1允许")
    private Integer allowCopyPaste;

    @Schema(description = "选项乱序：0不乱序，1乱序")
    private Integer optionShuffle;

    @Schema(description = "考试密码")
    private String examPassword;

    @Schema(description = "交卷后显示答案：0不显示，1显示")
    private Integer showAnswerAfterSubmit;

    @Schema(description = "允许重考：0不允许，1允许")
    private Integer allowRetake;

    @Schema(description = "最多允许重考次数")
    private Integer maxRetakeCount;

    @Schema(description = "考试状态：0未发布，1已发布，2进行中，3已结束")
    private Integer status;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    // ========== 非数据库字段（用于前端展示）==========

    @TableField(exist = false)
    @Schema(description = "科目名称（字典转换）")
    private String kemuValue;

    @TableField(exist = false)
    @Schema(description = "试卷名称")
    private String examPaperName;

    @TableField(exist = false)
    @Schema(description = "试卷总分")
    private Integer examPaperScore;

    @TableField(exist = false)
    @Schema(description = "教师姓名")
    private String teacherName;
}
