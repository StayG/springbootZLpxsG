package com.web.domain.Req;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.web.utils.PageInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
public class ExamDetailsReq extends PageInfo {
    private static final long serialVersionUID = 1L;

    public PageInfo getPageInfo() {
        return this;
    }

    @Schema(description = "主键 id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "试卷编号")
    private String examDetailsUuidNumber;

    @Schema(description = "用户 id")
    private Integer usersId;

    @Schema(description = "试卷 id")
    private Integer examPaperId;

    @Schema(description = "试题 id")
    private Integer examQuestionId;

    @Schema(description = "试题名")
    private String examQuestionName;

    @Schema(description = "选项名")
    private String examQuestionOptions;

    @Schema(description = "考生答案")
    private String examDetailsMyanswer;

    @Schema(description = "答案解析")
    private String examQuestionAnalysis;

    @Schema(description = "用户姓名")
   private String nickname;

    @Schema(description = "考试记录ID")
    private Integer examRecordId;

    @Schema(description = "试题得分")
    private Integer examDetailsMyscore;

    @Schema(description = "创建时间")
    @JsonFormat(locale = "zh", timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private Date createTime;
}
